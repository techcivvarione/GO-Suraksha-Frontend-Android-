package com.gosuraksha.app.core

import android.content.Context

class OnboardingManager(
    context: Context
) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isFirstLaunch(): Boolean = prefs.getBoolean(KEY_IS_FIRST_LAUNCH, true)

    fun setFirstLaunchCompleted() {
        prefs.edit().putBoolean(KEY_IS_FIRST_LAUNCH, false).apply()
    }

    private companion object {
        private const val PREFS_NAME = "onboarding_manager"
        private const val KEY_IS_FIRST_LAUNCH = "is_first_launch"
    }
}
