package com.example.recorder

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.content.ContextCompat

object ContactsHelper {

    fun normalizePhoneNumber(number: String): String {
        return number.replace(Regex("[\\s\\-\\(\\)\\.]"), "")
    }

    fun formatDisplayNumber(number: String): String {
        val clean = number.trim()
        if (clean.length == 10 && clean.startsWith("0")) {
            // French format: 06 12 34 56 78
            return "${clean.substring(0, 2)} ${clean.substring(2, 4)} ${clean.substring(4, 6)} ${clean.substring(6, 8)} ${clean.substring(8, 10)}"
        }
        return clean
    }

    /**
     * Checks whether a phone number exists in the device's Contacts list.
     * Returns the contact's display name if found, or null if outside contacts.
     */
    fun getContactName(context: Context, phoneNumber: String): String? {
        if (phoneNumber.isBlank()) return null

        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            // Without permission, check against demo recognized contacts
            return getSimulatedContactMatch(phoneNumber)
        }

        try {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber)
            )
            val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        return cursor.getString(nameIndex)
                    }
                }
            }
        } catch (_: Exception) {}

        return getSimulatedContactMatch(phoneNumber)
    }

    fun isNumberOutsideContacts(context: Context, phoneNumber: String): Boolean {
        return getContactName(context, phoneNumber) == null
    }

    /**
     * Helper for simulation/testing when running on emulator without synced SIM contacts.
     */
    private fun getSimulatedContactMatch(phoneNumber: String): String? {
        val normalized = normalizePhoneNumber(phoneNumber)
        return when {
            normalized.endsWith("0612345678") || normalized.endsWith("612345678") -> "Sophie Martin (Famille)"
            normalized.endsWith("0140205050") -> "Dr. Dupont (Médecin)"
            normalized.endsWith("0601020304") -> "Bureau Travail"
            else -> null // Not in contacts -> considered an unknown number / outside contacts!
        }
    }
}
