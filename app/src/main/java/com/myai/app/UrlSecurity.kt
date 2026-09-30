package com.myai.app

import android.net.Uri

object UrlSecurity {

    enum class Status {
        VERIFIED,
        UNKNOWN,
        INVALID
    }

    data class CheckResult(
        val status: Status,
        val host: String
    )

    // अभी केवल known official domains की शुरुआती सूची।
    // आगे जरूरत के अनुसार इसे बढ़ाया जाएगा।
    private val trustedDomains = setOf(
        "google.com",
        "google.co.in",
        "youtube.com",
        "youtu.be"
    )

    fun check(url: String): CheckResult {
        return try {
            val uri = Uri.parse(url)
            val scheme = uri.scheme?.lowercase()
            val host = uri.host?.lowercase()

            if (
                (scheme != "https" && scheme != "http") ||
                host.isNullOrBlank()
            ) {
                return CheckResult(
                    Status.INVALID,
                    host ?: ""
                )
            }

            val verified = trustedDomains.any { domain ->
                host == domain || host.endsWith(".$domain")
            }

            CheckResult(
                if (verified) Status.VERIFIED else Status.UNKNOWN,
                host
            )
        } catch (_: Exception) {
            CheckResult(Status.INVALID, "")
        }
    }
}
