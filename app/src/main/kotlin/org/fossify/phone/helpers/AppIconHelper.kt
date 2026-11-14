package org.fossify.phone.helpers

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

object AppIconHelper {
    const val ICON_DARK = "dark"
    const val ICON_LIGHT = "light"

    fun setAppIcon(context: Context, icon: String) {
        val pm = context.packageManager
        val pkg = context.packageName
        val darkAlias = ComponentName(pkg, "$pkg.activities.SplashActivity.Dark")
        val lightAlias = ComponentName(pkg, "$pkg.activities.SplashActivity.Light")

        val enableDark = icon == ICON_DARK

        pm.setComponentEnabledSetting(
            darkAlias,
            if (enableDark) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
        pm.setComponentEnabledSetting(
            lightAlias,
            if (!enableDark) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    fun getCurrentIcon(context: Context): String {
        val pm = context.packageManager
        val pkg = context.packageName
        val lightAlias = ComponentName(pkg, "$pkg.activities.SplashActivity.Light")
        return try {
            val state = pm.getComponentEnabledSetting(lightAlias)
            if (state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) ICON_LIGHT else ICON_DARK
        } catch (_: Exception) {
            ICON_DARK
        }
    }
}
