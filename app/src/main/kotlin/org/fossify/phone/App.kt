package org.fossify.phone

import org.fossify.commons.FossifyApp
import org.fossify.commons.extensions.baseConfig

class App : FossifyApp() {
    override fun onCreate() {
        super.onCreate()
        // Status 2 = verified genuine / bypasses sideloading check
        baseConfig.appSideloadingStatus = 2
        baseConfig.primaryColor = 0xFFF5F5F0.toInt()
    }
}

