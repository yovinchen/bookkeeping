package com.yovinchen.bookkeeping.viewmodel

import android.app.Application
import android.content.Context
import android.os.Environment
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import com.yovinchen.bookkeeping.data.BookkeepingDatabase
import com.yovinchen.bookkeeping.model.BookkeepingRecord
import com.yovinchen.bookkeeping.model.Category
import com.yovinchen.bookkeeping.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val database = BookkeepingDatabase.getDatabase(application)
    private val dao = database.bookkeepingDao()
    private val memberDao = database.memberDao()
    private val _isAutoBackupEnabled = MutableStateFlow(false)
    val isAutoBackupEnabled: StateFlow<Boolean> = _isAutoBackupEnabled.asStateFlow()

    private val _selectedCategoryType = MutableStateFlow(TransactionType.EXPENSE)
    val selectedCategoryType: StateFlow<TransactionType> = _selectedCategoryType.asStateFlow()

    val categories: StateFlow<List<Category>> = _selectedCategoryType.flatMapLatest { type ->
            dao.getCategoriesByType(type)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setSelectedCategoryType(type: TransactionType) {
        _selectedCategoryType.value = type
    }

    fun addCategory(name: String, type: TransactionType, iconResId: Int?) {
        viewModelScope.launch {
            val category = Category(name = name, type = type, icon = iconResId)
            dao.insertCategory(category)
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            dao.deleteCategory(category)
        }
    }

    fun updateCategory(category: Category, newName: String, iconResId: Int?) {
        viewModelScope.launch {
            val updatedCategory = category.copy(name = newName, icon = iconResId)
            dao.updateCategory(updatedCategory)
            // 更新所有使用该类别的记录
            dao.updateRecordCategories(category.name, newName)
        }
    }

    suspend fun isCategoryInUse(categoryName: String): Boolean {
        return dao.isCategoryInUse(categoryName)
    }

    fun setAutoBackup(enabled: Boolean) {
        viewModelScope.launch {
            _isAutoBackupEnabled.value = enabled
            if (enabled) {
                schedulePeriodicBackup()
            }
        }
    }

    private fun schedulePeriodicBackup() {
        viewModelScope.launch(Dispatchers.IO) {
            while (isAutoBackupEnabled.value) {
                try {
                    // 创建自动备份
                    SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    val backupDir = File(
                        getApplication<Application>().getExternalFilesDir(null), "auto_backups"
                    )
                    if (!backupDir.exists()) {
                        backupDir.mkdirs()
                    }

                    // 导出CSV
                    exportToCSV(getApplication(), backupDir)

                    // 等待24小时
                    delay(TimeUnit.HOURS.toMillis(24))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun exportToCSV(context: Context, customDir: File? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val timestamp =
                    SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val fileName = "bookkeeping_backup_$timestamp.csv"
                val downloadsDir = customDir ?: Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
                )
                val file = File(downloadsDir, fileName)

                CSVWriter(FileWriter(file)).use { writer ->
                    // 写入头部
                    writer.writeNext(arrayOf("日期", "类型", "金额", "类别", "备注", "成员"))

                    // 获取所有记录和成员
                    val records = dao.getAllRecords().first()
                    val members = memberDao.getAllMembers().first()

                    // 写入数据行
                    records.forEach { record ->
                        val member = members.find { member -> member.id == record.memberId }
                        writer.writeNext(
                            arrayOf(
                                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
                                    record.date
                                ),
                                record.type.toString(),
                                record.amount.toString(),
                                record.category,
                                record.description,
                                member?.name ?: "自己"
                            )
                        )
                    }
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "CSV导出成功: ${file.absolutePath}", Toast.LENGTH_LONG)
                        .show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "CSV导出失败: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun exportToExcel(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val workbook = XSSFWorkbook()
                val sheet = workbook.createSheet("账目记录")

                // 创建标题行
                val headerRow = sheet.createRow(0)
                val headers = arrayOf("日期", "类型", "金额", "类别", "备注", "成员")
                headers.forEachIndexed { index, header ->
                    headerRow.createCell(index).setCellValue(header)
                }

                // 获取所有记录和成员
                val records = dao.getAllRecords().first()
                val members = memberDao.getAllMembers().first()

                records.forEachIndexed { index, record ->
                    val row = sheet.createRow(index + 1)
                    val member = members.find { member -> member.id == record.memberId }

                    row.createCell(0).setCellValue(
                        SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss", Locale.getDefault()
                        ).format(record.date)
                    )
                    row.createCell(1).setCellValue(record.type.toString())
                    row.createCell(2).setCellValue(record.amount)
                    row.createCell(3).setCellValue(record.category)
                    row.createCell(4).setCellValue(record.description)
                    row.createCell(5).setCellValue(member?.name ?: "自己")
                }

                val timestamp =
                    SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val fileName = "bookkeeping_backup_$timestamp.xlsx"
                val downloadsDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, fileName)

                workbook.write(file.outputStream())
                workbook.close()

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context, "Excel导出成功: ${file.absolutePath}", Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Excel导出失败: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun restoreData(context: Context, backupFile: File) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                when {
                    backupFile.name.endsWith(".csv", ignoreCase = true) -> {
                        restoreFromCSV(backupFile)
                    }

                    backupFile.name.endsWith(".xlsx", ignoreCase = true) -> {
                        restoreFromExcel(backupFile)
                    }

                    else -> {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "不支持的文件格式", Toast.LENGTH_LONG).show()
                        }
                        return@launch
                    }
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "数据恢复成功", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "数据恢复失败: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private suspend fun restoreFromCSV(file: File) {
        CSVReader(FileReader(file)).use { reader ->
            // 跳过标题行
            reader.readNext()

            // 读取数据行
            var currentLine = reader.readNext()
            while (currentLine != null) {
                val record = BookkeepingRecord(
                    type = TransactionType.valueOf(currentLine[1]),
                    amount = currentLine[2].toDouble(),
                    category = currentLine[3],
                    description = currentLine[4],
                    date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(
                        currentLine[0]
                    ) ?: Date(),
                    memberId = findMemberIdByName(currentLine[5])
                )
                dao.insertRecord(record)
                currentLine = reader.readNext()
            }
        }
    }

    private suspend fun restoreFromExcel(file: File) {
        val workbook = XSSFWorkbook(file)
        val sheet = workbook.getSheetAt(0)

        // 跳过标题行
        for (rowIndex in 1..sheet.lastRowNum) {
            val row = sheet.getRow(rowIndex)
            val record = BookkeepingRecord(
                type = TransactionType.valueOf(row.getCell(1).stringCellValue),
                amount = row.getCell(2).numericCellValue,
                category = row.getCell(3).stringCellValue,
                description = row.getCell(4).stringCellValue,
                date = SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss",
                    Locale.getDefault()
                ).parse(row.getCell(0).stringCellValue),
                memberId = findMemberIdByName(row.getCell(5).stringCellValue)
            )
            dao.insertRecord(record)
        }
        workbook.close()
    }

    private suspend fun findMemberIdByName(name: String): Int? {
        return memberDao.getAllMembers().first().find { member -> member.name == name }?.id
    }
}
