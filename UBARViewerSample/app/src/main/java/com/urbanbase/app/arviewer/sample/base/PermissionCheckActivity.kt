package com.urbanbase.app.arviewer.sample.base

import android.Manifest
import android.content.pm.PackageManager
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlin.system.exitProcess

/**
 * 앱 권한 체크를 처리 하기 위한 클래스
 * 해당 액티비티를 상속받아 동적 앱 권한 처리를 전역적으로 쉽게 처리 하기 위해 사용한다
 */
open class PermissionCheckActivity : AppCompatActivity() {
    val permissionList = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    var onPermissionGrantedListener: (() -> Unit)? = null

    fun checkPermission(onPermissionGrantedListener: (() -> Unit)?) {
        this.onPermissionGrantedListener = onPermissionGrantedListener
        check(
            permissionList,
            CAMERA_PERMISSION_CODE
        )
    }

    private fun check(permissions: Array<String>, requestCode: Int) {
        var isAllGranted = true
        permissions.forEach {
            isAllGranted = isAllGranted.and(
                (ContextCompat.checkSelfPermission(this, it)
                        == PackageManager.PERMISSION_GRANTED)
            )
        }

        if (isAllGranted) {
            onPermissionGrantedListener?.invoke()
        } else {
            ActivityCompat.requestPermissions(
                this, permissions,
                requestCode
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                onPermissionGrantedListener?.invoke()
            } else {
                Handler().postDelayed({
                    exitProcess(0)
                }, 3000)
            }
        }
    }

    companion object {

        private const val CAMERA_PERMISSION_CODE = 100
    }
}