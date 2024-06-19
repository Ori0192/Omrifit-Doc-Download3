package com.example.omrifit.photo_editor

import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.omrifit.R
import com.google.android.material.snackbar.Snackbar

/**
 * BaseActivity is an open class that extends AppCompatActivity.
 * It provides utility methods for handling common tasks such as permission requests,
 * showing loading dialogs, and displaying snackbars.
 */
open class BaseActivity : AppCompatActivity() {
    private var mProgressDialog: ProgressDialog? = null
    private var mPermission: String? = null

    // Launcher for permission requests
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        isPermissionGranted(it, mPermission)
    }

    /**
     * Requests a specific permission.
     * @param permission The permission to request.
     * @return True if the permission is already granted, false otherwise.
     */
    fun requestPermission(permission: String): Boolean {
        val isGranted = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        if (!isGranted) {
            mPermission = permission
            permissionLauncher.launch(permission)
        }
        return isGranted
    }

    /**
     * Called when a permission request result is received.
     * Can be overridden by subclasses to handle the result.
     * @param isGranted Whether the permission was granted.
     * @param permission The requested permission.
     */
    open fun isPermissionGranted(isGranted: Boolean, permission: String?) {}

    /**
     * Makes the activity fullscreen by hiding the title and status bar.
     */
    fun makeFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }

    /**
     * Shows a loading dialog with a specific message.
     * @param message The message to display in the loading dialog.
     */
    protected fun showLoading(message: String) {
        mProgressDialog = ProgressDialog(this).apply {
            setMessage(message)
            setProgressStyle(ProgressDialog.STYLE_SPINNER)
            setCancelable(false)
            show()
        }
    }

    /**
     * Hides the loading dialog if it is currently shown.
     */
    protected fun hideLoading() {
        mProgressDialog?.dismiss()
    }

    /**
     * Shows a snackbar with a specific message.
     * If the snackbar cannot be shown, a toast is displayed instead.
     * @param message The message to display in the snackbar or toast.
     */
    protected fun showSnackbar(message: String) {
        val view = findViewById<View>(R.id.content)
        if (view != null) {
            Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}
