package com.king.app.updater

import android.content.Context
import com.king.app.updater.callback.UpdateCallback
import com.king.app.updater.http.OkHttpManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

val appUpdaterExecutor: AppUpdaterExecutor by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
    AppUpdaterExecutor.INSTANCE
}

sealed class UpdateStatus {
    data class Downloading(val isDownloading: Boolean) : UpdateStatus()
    data class Progress(val progress: Long, val total: Long, val isChanged: Boolean) :
        UpdateStatus()

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

    fun start(
        url: String,
        fileName: String = "update.apk",
        path: String = "",
        appContext: Context
    ): Boolean {
        return if (isDownloading) {
            true
        } else {
            this.fileName = fileName
            this.path = path
            startDownload(url, appContext)
            false
        }
    }

    fun getDownloadStatus(): Boolean {
        return isDownloading
    }

    private fun startDownload(url: String, appContext: Context) {
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
        CoroutineScope(Dispatchers.IO).launch {
            _updateStatus.emit(UpdateStatus.Downloading(isDownloading))
        }
    }

    override fun onProgress(progress: Long, total: Long, isChanged: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            _updateStatus.emit(UpdateStatus.Progress(progress, total, isChanged))
        }
    }

    override fun onFinish(file: File?) {
        CoroutineScope(Dispatchers.IO).launch {
            _updateStatus.emit(UpdateStatus.Finish(file))
        }
        this.isDownloading = false
    }

    override fun onError(error: java.lang.Exception?) {
        CoroutineScope(Dispatchers.IO).launch {
            _updateStatus.emit(UpdateStatus.Error(error))
        }
        this.isDownloading = false
    }

    override fun onCancel() {
        this.isDownloading = false
    }

}