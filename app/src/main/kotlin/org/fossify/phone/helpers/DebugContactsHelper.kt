package org.fossify.phone.helpers

import android.content.Context
import android.provider.ContactsContract
import org.fossify.commons.extensions.hasPermission
import org.fossify.commons.helpers.PERMISSION_WRITE_CONTACTS
import org.fossify.phone.models.CallLogItem

object DebugContactsHelper {
    val sampleContacts = listOf(
        "Alice Johnson" to "+1 555-0101",
        "Bob Smith" to "+1 555-0102",
        "Charlie Brown" to "+1 555-0103",
        "David Miller" to "+1 555-0104",
        "Emma Watson" to "+1 555-0105",
        "Frank Castle" to "+1 555-0106",
        "Grace Hopper" to "+1 555-0107",
        "Henry Cavill" to "+1 555-0108",
        "Ivy League" to "+1 555-0109",
        "Jack Ryan" to "+1 555-0110",
        "Kavita Sharma" to "+91 98765 43210",
        "Liam Neeson" to "+1 555-0112",
        "Mia Khalifa" to "+1 555-0113",
        "Noah Centineo" to "+1 555-0114",
        "Olivia Rodrigo" to "+1 555-0115",
        "Peter Parker" to "+1 555-0116",
        "Quinn Fabray" to "+1 555-0117",
        "Rahul Dravid" to "+91 98111 22334",
        "Sarah Connor" to "+1 555-0119",
        "Tony Stark" to "+1 555-0120",
        "Uma Thurman" to "+1 555-0121",
        "Victor Stone" to "+1 555-0122",
        "Wanda Maximoff" to "+1 555-0123",
        "Xavier Charles" to "+1 555-0124",
        "Yara Shahidi" to "+1 555-0125",
        "Zack Snyder" to "+1 555-0126"
    )

    fun insertSampleContactsIfEmpty(context: Context, onComplete: (() -> Unit)? = null) {
        onComplete?.invoke()
    }

    fun removeSampleContacts(context: Context, onComplete: (() -> Unit)? = null) {
        if (!context.hasPermission(PERMISSION_WRITE_CONTACTS)) {
            onComplete?.invoke()
            return
        }

        Thread {
            try {
                for ((name, _) in sampleContacts) {
                    val cursor = context.contentResolver.query(
                        ContactsContract.Data.CONTENT_URI,
                        arrayOf(ContactsContract.Data.RAW_CONTACT_ID),
                        "${ContactsContract.Data.MIMETYPE} = ? AND ${ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME} = ?",
                        arrayOf(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE, name),
                        null
                    )
                    cursor?.use {
                        while (it.moveToNext()) {
                            val rawContactId = it.getLong(0)
                            context.contentResolver.delete(
                                ContactsContract.RawContacts.CONTENT_URI.buildUpon()
                                    .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
                                    .build(),
                                "${ContactsContract.RawContacts._ID} = ?",
                                arrayOf(rawContactId.toString())
                            )
                        }
                    }
                }
                val prefs = context.getSharedPreferences("debug_prefs", Context.MODE_PRIVATE)
                prefs.edit().remove("debug_contacts_seeded_v1").apply()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                onComplete?.invoke()
            }
        }.start()
    }

    fun getSampleCallLogs(): List<CallLogItem> = emptyList()
}
