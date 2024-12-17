package com.yovinchen.bookkeeping.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.yovinchen.bookkeeping.getPreregisteredFilePickerLauncher
import java.io.File
import java.io.FileOutputStream

object FilePickerUtil {
    private var currentCallback: ((File) -> Unit)? = null

    fun startFilePicker(activity: ComponentActivity, onFileSelected: (File) -> Unit) {
        currentCallback = onFileSelected
        
        try {
            val mimeTypes = arrayOf(
                "text/csv",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/vnd.ms-excel"
            )
            activity.getPreregisteredFilePickerLauncher().launch(mimeTypes)
        } catch (e: Exception) {
            Toast.makeText(activity, "无法启动文件选择器：${e.message}", Toast.LENGTH_SHORT).show()
            currentCallback = null
        }
    }

    fun handleFileSelection(context: Context, uri: Uri?) {
        if (uri == null) {
            Toast.makeText(context, "未选择文件", Toast.LENGTH_SHORT).show()
            currentCallback = null
            return
        }

        try {
            val mimeType = context.contentResolver.getType(uri)
            if (!isValidFileType(uri.toString(), mimeType)) {
                Toast.makeText(context, "请选择CSV或Excel文件", Toast.LENGTH_SHORT).show()
                return
            }

            // 获取持久性权限
            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, takeFlags)

            // 将选中的文件复制到应用私有目录
            val tempFile = copyUriToTempFile(context, uri)
            if (tempFile != null) {
                currentCallback?.invoke(tempFile)
            } else {
                Toast.makeText(context, "文件处理失败，请重试", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "文件处理出错：${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            currentCallback = null
        }
    }

    private fun isValidFileType(fileName: String, mimeType: String?): Boolean {
        val fileExtension = fileName.lowercase()
        return fileExtension.endsWith(".csv") || 
               fileExtension.endsWith(".xlsx") ||
               fileExtension.endsWith(".xls") ||
               mimeType == "text/csv" ||
               mimeType == "application/vnd.ms-excel" ||
               mimeType == "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    }

    private fun copyUriToTempFile(context: Context, uri: Uri): File? {
        return try {
            val fileName = getFileName(context, uri) ?: "temp_backup_${System.currentTimeMillis()}"
            val tempFile = File(context.cacheDir, fileName)
            
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var fileName: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val displayNameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    fileName = cursor.getString(displayNameIndex)
                }
            }
        }
        return fileName
    }
}
