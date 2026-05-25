package com.pg.management.core.share

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object WhatsAppShare {

    /**
     * Open WhatsApp with [phone] pre-selected and [message] pre-typed.
     * The user just hits send. Falls back to the system share sheet if
     * WhatsApp isn't installed.
     */
    fun send(context: Context, phone: String?, message: String) {
        val cleaned = phone?.replace(Regex("[^0-9+]"), "").orEmpty()
        val withCountry = when {
            cleaned.startsWith("+") -> cleaned.removePrefix("+")
            cleaned.length == 10 -> "91$cleaned"   // assume India if no country code
            else -> cleaned
        }

        if (withCountry.isBlank()) {
            // Generic share sheet
            shareText(context, message)
            return
        }

        val url = "https://wa.me/$withCountry?text=${Uri.encode(message)}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            shareText(context, message)
        }
    }

    private fun shareText(context: Context, text: String) {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(Intent.createChooser(send, "Share via").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "No app available to share", Toast.LENGTH_SHORT).show()
        }
    }
}
