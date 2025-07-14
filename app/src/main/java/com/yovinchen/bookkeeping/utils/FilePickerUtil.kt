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

/**
 * 文件选择器工具类
 * 用于处理文件选择、权限获取和文件处理的工具类
 * 
 * 主要功能：
 * 1. 启动系统文件选择器
 * 2. 处理选择结果
 * 3. 将选择的文件复制到应用缓存目录
 * 4. 文件类型验证
 */
object FilePickerUtil {
    /**
     * 当前活跃的文件选择回调
     * 用于在文件选择完成后调用
     */
    private var currentCallback: ((File) -> Unit)? = null

    /**
     * 启动文件选择器
     * 
     * @param activity 当前活动，用于启动文件选择器
     * @param onFileSelected 文件选择完成后的回调函数，参数为选中的文件
     */
    fun startFilePicker(activity: ComponentActivity, onFileSelected: (File) -> Unit) {
        currentCallback = onFileSelected
        
        try {
            // 设置可选择的文件类型，限制为CSV和Excel文件
            val mimeTypes = arrayOf(
                "text/csv",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/vnd.ms-excel"
            )
            // 使用预注册的文件选择器启动文件选择流程
            activity.getPreregisteredFilePickerLauncher().launch(mimeTypes)
        } catch (e: Exception) {
            // 文件选择器启动失败时显示错误提示
            Toast.makeText(activity, "无法启动文件选择器：${e.message}", Toast.LENGTH_SHORT).show()
            currentCallback = null
        }
    }

    /**
     * 处理文件选择结果
     * 
     * @param context 上下文对象，用于访问ContentResolver
     * @param uri 选中文件的URI，如果用户取消选择则为null
     */
    fun handleFileSelection(context: Context, uri: Uri?) {
        if (uri == null) {
            // 用户未选择文件时显示提示
            Toast.makeText(context, "未选择文件", Toast.LENGTH_SHORT).show()
            currentCallback = null
            return
        }

        try {
            // 获取文件MIME类型
            val mimeType = context.contentResolver.getType(uri)
            // 验证文件类型是否合法
            if (!isValidFileType(uri.toString(), mimeType)) {
                Toast.makeText(context, "请选择CSV或Excel文件", Toast.LENGTH_SHORT).show()
                return
            }

            // 获取持久性权限，确保应用在重启后仍能访问该文件
            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, takeFlags)

            // 将选中的文件复制到应用私有目录
            val tempFile = copyUriToTempFile(context, uri)
            if (tempFile != null) {
                // 调用回调函数，传递临时文件
                currentCallback?.invoke(tempFile)
            } else {
                Toast.makeText(context, "文件处理失败，请重试", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "文件处理出错：${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            // 清除回调引用，避免内存泄漏
            currentCallback = null
        }
    }

    /**
     * 验证文件类型是否合法
     * 
     * @param fileName 文件名，用于检查文件扩展名
     * @param mimeType 文件MIME类型
     * @return 如果文件类型合法则返回true，否则返回false
     */
    private fun isValidFileType(fileName: String, mimeType: String?): Boolean {
        val fileExtension = fileName.lowercase()
        return fileExtension.endsWith(".csv") || 
               fileExtension.endsWith(".xlsx") ||
               fileExtension.endsWith(".xls") ||
               mimeType == "text/csv" ||
               mimeType == "application/vnd.ms-excel" ||
               mimeType == "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    }

    /**
     * 将URI指向的文件复制到应用缓存目录
     * 
     * @param context 上下文对象，用于访问ContentResolver和缓存目录
     * @param uri 要复制的文件URI
     * @return 复制后的临时文件，如果复制失败则返回null
     */
    private fun copyUriToTempFile(context: Context, uri: Uri): File? {
        return try {
            // 获取文件名，如果无法获取则使用时间戳作为文件名
            val fileName = getFileName(context, uri) ?: "temp_backup_${System.currentTimeMillis()}"
            val tempFile = File(context.cacheDir, fileName)
            
            // 从URI读取内容并写入临时文件
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

    /**
     * 从URI中获取文件名
     * 
     * @param context 上下文对象，用于访问ContentResolver
     * @param uri 文件URI
     * @return 文件名，如果无法获取则返回null
     */
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
