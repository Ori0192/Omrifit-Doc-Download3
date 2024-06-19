package com.example.omrifit.photo_editor

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import java.io.IOException
import java.lang.Exception
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * A helper class for creating and managing files on a device.
 *
 * Usage:
 * Call [FileSaveHelper.createFile] to create a file. If the file is created, you will receive its file path and URI.
 * After you are done with the file, call [FileSaveHelper.notifyThatFileIsNowPubliclyAvailable].
 *
 * To properly release resources, call [FileSaveHelper.addObserver] or create an object with [FileSaveHelper].
 */
class FileSaveHelper(private val mContentResolver: ContentResolver) : LifecycleObserver {

    private val executor: ExecutorService? = Executors.newSingleThreadExecutor()
    private val fileCreatedResult: MutableLiveData<FileMeta> = MutableLiveData()
    private var resultListener: OnFileCreateResult? = null

    private val observer = Observer<FileMeta> { fileMeta ->
        resultListener?.onFileCreateResult(
            fileMeta.isCreated,
            fileMeta.filePath,
            fileMeta.error,
            fileMeta.uri
        )
    }

    constructor(activity: AppCompatActivity) : this(activity.contentResolver) {
        addObserver(activity)
    }

    private fun addObserver(lifecycleOwner: LifecycleOwner) {
        fileCreatedResult.observe(lifecycleOwner, observer)
        lifecycleOwner.lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun release() {
        executor?.shutdownNow()
    }

    /**
     * Creates a file and inserts it into the MediaStore.
     *
     * @param fileNameToSave The name of the file to save.
     * @param listener The result listener.
     */
    fun createFile(fileNameToSave: String, listener: OnFileCreateResult?) {
        resultListener = listener
        executor!!.submit {
            var cursor: Cursor? = null
            try {
                val newImageDetails = ContentValues()
                val imageCollection = buildUriCollection(newImageDetails)
                val editedImageUri = getEditedImageUri(fileNameToSave, newImageDetails, imageCollection)
                cursor = mContentResolver.query(
                    editedImageUri,
                    arrayOf(MediaStore.Images.Media.DATA),
                    null,
                    null,
                    null
                )
                val columnIndex = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                cursor.moveToFirst()
                val filePath = cursor.getString(columnIndex)
                updateResult(true, filePath, null, editedImageUri, newImageDetails)
            } catch (ex: Exception) {
                ex.printStackTrace()
                updateResult(false, null, ex.message, null, null)
            } finally {
                cursor?.close()
            }
        }
    }

    @Throws(IOException::class)
    private fun getEditedImageUri(
        fileNameToSave: String,
        newImageDetails: ContentValues,
        imageCollection: Uri
    ): Uri {
        newImageDetails.put(MediaStore.Images.Media.DISPLAY_NAME, fileNameToSave)
        val editedImageUri = mContentResolver.insert(imageCollection, newImageDetails)
        val outputStream = mContentResolver.openOutputStream(editedImageUri!!)
        outputStream!!.close()
        return editedImageUri
    }

    @SuppressLint("InlinedApi")
    private fun buildUriCollection(newImageDetails: ContentValues): Uri {
        return if (isSdkHigherThan28()) {
            newImageDetails.put(MediaStore.Images.Media.IS_PENDING, 1)
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
    }

    @SuppressLint("InlinedApi")
    fun notifyThatFileIsNowPubliclyAvailable(contentResolver: ContentResolver) {
        if (isSdkHigherThan28()) {
            executor!!.submit {
                fileCreatedResult.value?.let {
                    it.imageDetails!!.clear()
                    it.imageDetails!!.put(MediaStore.Images.Media.IS_PENDING, 0)
                    contentResolver.update(it.uri!!, it.imageDetails, null, null)
                }
            }
        }
    }

    private class FileMeta(
        var isCreated: Boolean,
        var filePath: String?,
        var uri: Uri?,
        var error: String?,
        var imageDetails: ContentValues?
    )

    interface OnFileCreateResult {
        /**
         * Callback for file creation result.
         *
         * @param created Whether the file was created successfully.
         * @param filePath The file path on disk. Null in case of failure.
         * @param error The error message in case of failure.
         * @param uri The URI of the newly created file. Null in case of failure.
         */
        fun onFileCreateResult(created: Boolean, filePath: String?, error: String?, uri: Uri?)
    }

    private fun updateResult(
        result: Boolean,
        filePath: String?,
        error: String?,
        uri: Uri?,
        newImageDetails: ContentValues?
    ) {
        fileCreatedResult.postValue(FileMeta(result, filePath, uri, error, newImageDetails))
    }

    companion object {
        fun isSdkHigherThan28(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        }
    }
}
