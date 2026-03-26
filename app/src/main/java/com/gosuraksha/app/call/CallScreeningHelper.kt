package com.gosuraksha.app.call

import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.telecom.TelecomManager

object CallScreeningHelper {
    private const val REQUEST_CALL_SCREENING_ROLE = 1001

    fun isCallScreeningEnabled(context: Context): Boolean {
        val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
        return roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
    }

    fun requestCallScreeningRole(activity: Activity) {
        val roleManager = activity.getSystemService(Context.ROLE_SERVICE) as RoleManager
        if (!roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
            activity.startActivityForResult(intent, REQUEST_CALL_SCREENING_ROLE)
        }
    }

    fun requestDefaultDialer(activity: Activity) {
        val telecomManager = activity.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        if (activity.packageName != telecomManager.defaultDialerPackage) {
            val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
            intent.putExtra(
                TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME,
                activity.packageName
            )
            activity.startActivity(intent)
        }
    }
}
