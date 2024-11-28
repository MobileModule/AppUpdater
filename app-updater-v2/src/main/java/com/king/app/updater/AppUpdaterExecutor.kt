package com.king.app.updater

import android.content.Context
import com.king.app.updater.callback.UpdateCallback
import com.king.app.updater.http.OkHttpManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

val appUpdaterExecutor: AppUpdaterExecutor by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
    AppUpdaterExecutor.INSTANCE
}

sealed class UpdateStatus {
    data class Downloading(val isDownloading: Boolean) : UpdateStatus()
    data class Progress(val progress: Long, val total: Long, val isChanged: Boolean) : UpdateStatus()
    data class Finish(val file: File?) : UpdateStatus()
    data class Error(val exception: Exception?) : UpdateStatus()
}

class AppUpdaterExecutor : UpdateCallback {
    companion object {
        val INSTANCE: AppUpdaterExecutor by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            AppUpdaterExecutor()
        }
    }

    private val _updateStatus = MutableStateFlow<UpdateStatus?>(null)
    val updateStatus: StateFlow<UpdateStatus?> = _updateStatus

    private var path = ""
    private var fileName = "ecotopia.apk"
    private var downloadUrl = ""

    private var mAppUpdater: AppUpdater? = null

    fun start(url: String,fileName:String="update.apk",path:String="",appContext: Context): Boolean {
        return if (isDownloading) {
            true
        } else {
            this.fileName = fileName
            this.path = path
            startDownload(url,appContext)
            false
        }
    }

    fun getDownloadStatus():Boolean{
        return isDownloading
    }

    private fun startDownload(url: String,appContext: Context) {
        this.downloadUrl = url
        mAppUpdater = AppUpdater.Builder(appContext)
            .setFilename(fileName)
            .setInstallApk(true)
            .setUrl(downloadUrl)
            .setReDownload(true)
//            .setPath(path)
            .build()
            .setHttpManager(OkHttpManager.getInstance())
            .setUpdateCallback(this)
        mAppUpdater?.start()
    }

    private var isDownloading = false

    override fun onStart(url: String?) {
        this.isDownloading = true
    }

    override fun onDownloading(isDownloading: Boolean) {
        this.isDownloading = isDownloading
        _updateStatus.value = UpdateStatus.Downloading(isDownloading)
    }

    override fun onProgress(progress: Long, total: Long, isChanged: Boolean) {
        _updateStatus.value = UpdateStatus.Progress(progress, total, isChanged)
    }

    override fun onFinish(file: File?) {
        _updateStatus.value = UpdateStatus.Finish(file)
        this.isDownloading = false
    }

    override fun onError(error: java.lang.Exception?) {
        _updateStatus.value = UpdateStatus.Error(error)
        this.isDownloading = false
    }

    override fun onCancel() {
        this.isDownloading = false
    }

}