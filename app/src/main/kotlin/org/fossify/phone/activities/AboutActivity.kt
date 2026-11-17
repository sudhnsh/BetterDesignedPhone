package org.fossify.phone.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.fossify.commons.activities.LicenseActivity
import org.fossify.commons.helpers.APP_LICENSES
import org.fossify.commons.helpers.LICENSE_AUTOFITTEXTVIEW
import org.fossify.commons.helpers.LICENSE_GLIDE
import org.fossify.commons.helpers.LICENSE_INDICATOR_FAST_SCROLL
import org.fossify.phone.compose.screens.AboutScreen
import org.fossify.phone.compose.theme.PhoneTheme

class AboutActivity : SimpleActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PhoneTheme(activity = this) {
                AboutScreen(
                    onBackClick = { finish() },
                    onLicensesClick = {
                        val licenses = LICENSE_GLIDE or LICENSE_INDICATOR_FAST_SCROLL or LICENSE_AUTOFITTEXTVIEW
                        Intent(this, LicenseActivity::class.java).apply {
                            putExtra(APP_LICENSES, licenses)
                            startActivity(this)
                        }
                    }
                )
            }
        }
    }
}
