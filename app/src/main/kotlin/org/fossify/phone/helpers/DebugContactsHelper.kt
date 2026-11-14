package org.fossify.phone.helpers

import android.content.ContentProviderOperation
import android.content.Context
import android.provider.CallLog.Calls
import android.provider.ContactsContract
import org.fossify.commons.extensions.hasPermission
import org.fossify.commons.helpers.PERMISSION_WRITE_CONTACTS
import org.fossify.phone.extensions.getDayCode
import org.fossify.phone.models.CallLogItem
import org.fossify.phone.models.RecentCall

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
        if (!debugMode) {
            onComplete?.invoke()
            return
        }

        val prefs = context.getSharedPreferences("debug_prefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("debug_contacts_seeded_v1", false)) {
            onComplete?.invoke()
            return
        }

        if (!context.hasPermission(PERMISSION_WRITE_CONTACTS)) {
            onComplete?.invoke()
            return
        }

        Thread {
            try {
                for ((name, number) in sampleContacts) {
                    val ops = ArrayList<ContentProviderOperation>()
                    val rawContactInsertIndex = 0

                    ops.add(
                        ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                            .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                            .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                            .build()
                    )

                    ops.add(
                        ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                            .build()
                    )

                    ops.add(
                        ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
                            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                            .build()
                    )

                    context.contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
                }

                prefs.edit().putBoolean("debug_contacts_seeded_v1", true).apply()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                onComplete?.invoke()
            }
        }.start()
    }

    fun getSampleCallLogs(): List<CallLogItem> {
        val now = System.currentTimeMillis()
        val dayMs = 86400000L
        val today = now
        val yesterday = now - dayMs
        val twoDaysAgo = now - 2 * dayMs

        return listOf(
            CallLogItem.Date(today, today.getDayCode()),
            RecentCall(
                id = 1001,
                phoneNumber = "+1 555-0101",
                name = "Alice Johnson",
                photoUri = "",
                startTS = now - 1800000L,
                duration = 145,
                type = Calls.INCOMING_TYPE,
                simID = 1,
                simColor = 0xFF2196F3.toInt(),
                specificNumber = "",
                specificType = "Mobile",
                isUnknownNumber = false
            ),
            RecentCall(
                id = 1002,
                phoneNumber = "+1 555-0102",
                name = "Bob Smith",
                photoUri = "",
                startTS = now - 7200000L,
                duration = 0,
                type = Calls.MISSED_TYPE,
                simID = 1,
                simColor = 0xFF2196F3.toInt(),
                specificNumber = "",
                specificType = "Mobile",
                isUnknownNumber = false
            ),
            RecentCall(
                id = 1003,
                phoneNumber = "+91 98765 43210",
                name = "Kavita Sharma",
                photoUri = "",
                startTS = now - 14400000L,
                duration = 320,
                type = Calls.OUTGOING_TYPE,
                simID = 2,
                simColor = 0xFF4CAF50.toInt(),
                specificNumber = "",
                specificType = "Work",
                isUnknownNumber = false
            ),
            CallLogItem.Date(yesterday, yesterday.getDayCode()),
            RecentCall(
                id = 1004,
                phoneNumber = "+1 555-0120",
                name = "Tony Stark",
                photoUri = "",
                startTS = yesterday + 3600000L,
                duration = 84,
                type = Calls.OUTGOING_TYPE,
                simID = 1,
                simColor = 0xFF2196F3.toInt(),
                specificNumber = "",
                specificType = "Mobile",
                isUnknownNumber = false
            ),
            RecentCall(
                id = 1005,
                phoneNumber = "+1 555-0105",
                name = "Emma Watson",
                photoUri = "",
                startTS = yesterday - 7200000L,
                duration = 210,
                type = Calls.INCOMING_TYPE,
                simID = 1,
                simColor = 0xFF2196F3.toInt(),
                specificNumber = "",
                specificType = "Home",
                isUnknownNumber = false
            ),
            CallLogItem.Date(twoDaysAgo, twoDaysAgo.getDayCode()),
            RecentCall(
                id = 1006,
                phoneNumber = "+91 98111 22334",
                name = "Rahul Dravid",
                photoUri = "",
                startTS = twoDaysAgo,
                duration = 612,
                type = Calls.INCOMING_TYPE,
                simID = 2,
                simColor = 0xFF4CAF50.toInt(),
                specificNumber = "",
                specificType = "Mobile",
                isUnknownNumber = false
            )
        )
    }
}
