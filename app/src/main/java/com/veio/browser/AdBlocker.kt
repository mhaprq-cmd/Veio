package com.veio.browser

import android.net.Uri

/**
 * Basic domain-based ad and tracker blocking for Veio.
 *
 * This class only identifies URLs belonging to known ad/tracking
 * domains. MainActivity must call shouldBlock() from its WebView
 * request handling to apply the filtering.
 */
object AdBlocker {

    private val blockedDomains = setOf(
        // Google advertising
        "doubleclick.net",
        "googlesyndication.com",
        "googleadservices.com",
        "adservice.google.com",
        "pagead2.googlesyndication.com",

        // Advertising networks
        "adnxs.com",
        "criteo.com",
        "criteo.net",
        "taboola.com",
        "outbrain.com",
        "pubmatic.com",
        "openx.net",
        "rubiconproject.com",
        "smartadserver.com",
        "adsrvr.org",
        "advertising.com",
        "adform.net",
        " lijit.com".trim(),
        "bidswitch.net",
        "casalemedia.com",
        "media.net",

        // Tracking and analytics
        "scorecardresearch.com",
        "quantserve.com",
        "hotjar.com",
        "mouseflow.com",
        "clarity.ms",
        "analytics.tiktok.com",
        "pixel.facebook.com",
        "connect.facebook.net"
    )

    /**
     * Returns true when the URL host matches a blocked domain
     * or one of its subdomains.
     */
    fun shouldBlock(url: String): Boolean {
        if (url.isBlank()) return false

        val host = try {
            Uri.parse(url).host
                ?.lowercase()
                ?.trimEnd('.')
                ?: return false
        } catch (_: Exception) {
            return false
        }

        return blockedDomains.any { domain ->
            host == domain || host.endsWith(".$domain")
        }
    }
}
