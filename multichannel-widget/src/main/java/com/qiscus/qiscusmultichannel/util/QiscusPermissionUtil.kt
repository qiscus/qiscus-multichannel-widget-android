package com.qiscus.qiscusmultichannel.util

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.lang.reflect.InvocationTargetException
import java.util.*

/**
 * Created on : 23/08/19
 * Author     : Taufik Budi S
 * GitHub     : https://github.com/tfkbudi
 */
object QiscusPermissionsUtil {

    private val TAG = "QiscusPermissionsUtil"

    fun hasPermissions(context: Context, perms: Array<String>): Boolean {
        // Always return true for SDK < M, let the system deal with the permissions
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.w(TAG, "hasPermissions: API version < M, returning true by default")
            return true
        }

        for (perm in perms) {
            val hasPerm = ContextCompat.checkSelfPermission(
                context,
                perm
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPerm) {
                return false
            }
        }

        return true
    }

    fun requestPermissions(
        obj: Any, rationale: String,
        requestCode: Int, perms: Array<String>
    ) {
        requestPermissions(
            obj, rationale,
            android.R.string.ok,
            android.R.string.cancel,
            requestCode, perms
        )
    }

    fun requestPermissions(
        obj: Any, rationale: String,
        @StringRes positiveButton: Int,
        @StringRes negativeButton: Int,
        requestCode: Int, perms: Array<String>
    ) {

        checkCallingObjectSuitability(obj)
        val callbacks = obj as PermissionCallbacks

        var shouldShowRationale = false
        for (perm in perms) {
            shouldShowRationale =
                shouldShowRationale || shouldShowRequestPermissionRationale(obj, perm)
        }

        if (shouldShowRationale) {
            val activity = getActivity(obj) ?: return

            val dialog = AlertDialog.Builder(activity)
                .setMessage(rationale)
                .setPositiveButton(
                    positiveButton
                ) { dialog1, which -> executePermissionsRequest(obj, perms, requestCode) }
                .setNegativeButton(negativeButton) { dialog12, which ->
                    // act as if the permissions were denied
                    callbacks.onPermissionsDenied(requestCode, Arrays.asList(*perms))
                }.create()
            dialog.show()

        } else {
            executePermissionsRequest(obj, perms, requestCode)
        }
    }

    fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray, obj: Any
    ) {

        checkCallingObjectSuitability(obj)
        val callbacks = obj as PermissionCallbacks

        // Make a collection of granted and denied permissions from the request.
        val granted = ArrayList<String>()
        val denied = ArrayList<String>()
        for (i in permissions.indices) {
            val perm = permissions[i]
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                granted.add(perm)
            } else {
                denied.add(perm)
            }
        }

        // Report granted permissions, if any.
        if (granted.isNotEmpty()) {
            // Notify callbacks
            callbacks.onPermissionsGranted(requestCode, granted)
        }

        // Report denied permissions, if any.
        if (denied.isNotEmpty()) {
            callbacks.onPermissionsDenied(requestCode, denied)
        }

        // If 100% successful, call annotated methods
        if (granted.isNotEmpty() && denied.isEmpty()) {
            runAnnotatedMethods(obj, requestCode)
        }
    }

    fun checkDeniedPermissionsNeverAskAgain(
        obj: Any, rationale: String,
        @StringRes positiveButton: Int,
        @StringRes negativeButton: Int,
        deniedPerms: List<String>
    ): Boolean {
        var shouldShowRationale: Boolean
        for (perm in deniedPerms) {
            shouldShowRationale = shouldShowRequestPermissionRationale(obj, perm)
            if (!shouldShowRationale) {
                val activity = getActivity(obj) ?: return true

                val dialog = AlertDialog.Builder(activity)
                    .setMessage(rationale)
                    .setPositiveButton(positiveButton) { dialog1, which ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", activity.packageName, null)
                        intent.data = uri
                        activity.startActivity(intent)
                    }
                    .setNegativeButton(negativeButton, null)
                    .create()

                dialog.show()
                return true
            }
        }

        return false
    }

    @TargetApi(23)
    private fun shouldShowRequestPermissionRationale(obj: Any, perm: String): Boolean {
        return if (obj is Activity) {
            ActivityCompat.shouldShowRequestPermissionRationale(obj, perm)
        } else if (obj is Fragment) {
            obj.shouldShowRequestPermissionRationale(perm)
        } else (obj as? android.app.Fragment)?.shouldShowRequestPermissionRationale(perm)
            ?: false
    }

    @TargetApi(23)
    private fun executePermissionsRequest(obj: Any, perms: Array<String>, requestCode: Int) {
        checkCallingObjectSuitability(obj)

        if (obj is Activity) {
            ActivityCompat.requestPermissions(obj, perms, requestCode)
        } else if (obj is Fragment) {
            obj.requestPermissions(perms, requestCode)
        } else if (obj is android.app.Fragment) {
            obj.requestPermissions(perms, requestCode)
        }
    }

    @TargetApi(11)
    private fun getActivity(obj: Any): Activity? {
        return obj as? Activity ?: if (obj is Fragment) {
            obj.activity
        } else if (obj is android.app.Fragment) {
            obj.activity
        } else {
            null
        }
    }

    private fun runAnnotatedMethods(obj: Any, requestCode: Int) {
        val clazz = obj.javaClass
        for (method in clazz.declaredMethods) {
            if (method.isAnnotationPresent(AfterPermissionGranted::class.java)) {
                // Check for annotated methods with matching request code.
                val ann = method.getAnnotation(AfterPermissionGranted::class.java)
                if (ann.value == requestCode) {
                    // Method must be void so that we can invoke it
                    if (method.parameterTypes.isNotEmpty()) {
                        throw RuntimeException(
                            "Cannot execute non-void method " + method.name
                        )
                    }

                    try {
                        // Make method accessible if private
                        if (!method.isAccessible) {
                            method.isAccessible = true
                        }
                        method.invoke(obj)
                    } catch (e: IllegalAccessException) {
                        Log.e(TAG, "runDefaultMethod:IllegalAccessException", e)
                    } catch (e: InvocationTargetException) {
                        Log.e(TAG, "runDefaultMethod:InvocationTargetException", e)
                    }

                }
            }
        }
    }

    private fun checkCallingObjectSuitability(obj: Any) {
        // Make sure Object is an Activity or Fragment
        val isActivity = obj is Activity
        val isSupportFragment = obj is Fragment
        val isAppFragment = obj is android.app.Fragment
        val isMinSdkM = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

        if (!(isSupportFragment || isActivity || isAppFragment && isMinSdkM)) {
            if (isAppFragment) {
                throw IllegalArgumentException(
                    "Target SDK needs to be greater than 23 if caller is android.app.Fragment"
                )
            } else {
                throw IllegalArgumentException("Caller must be an Activity or a Fragment.")
            }
        }

        // Make sure Object implements callbacks
        if (obj !is PermissionCallbacks) {
            throw IllegalArgumentException("Caller must implement PermissionCallbacks.")
        }
    }

    interface PermissionCallbacks : ActivityCompat.OnRequestPermissionsResultCallback {

        fun onPermissionsGranted(requestCode: Int, perms: List<String>)

        fun onPermissionsDenied(requestCode: Int, perms: List<String>)

    }
}