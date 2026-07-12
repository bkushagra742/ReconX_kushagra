package com.kushagra.reconx.utils

/**
 * Constants.kt
 * ============
 * Single source of truth for app metadata, developer/social links, and
 * other constants that would otherwise be scattered/hardcoded across
 * multiple screens (per the "store all URLs in one config file" requirement).
 */
object Constants {
    const val APP_NAME = "Kushagra ReconX"
    const val APP_VERSION_NAME = "2.0.0"
    const val APP_VERSION_CODE = 1
    const val DATABASE_VERSION = 1
    const val APP_LICENSE = "MIT License"
    const val APP_MOTTO = "Stay curious, stay ethical."
    const val COPYRIGHT = "\u00A9 2026 Kushagra Singh Bisht. All Rights Reserved."

    const val DEVELOPER_NAME = "Kushagra Singh Bisht"
    const val DEVELOPER_ROLE = "Cybersecurity Student · Android Developer · Full Stack Developer · OSINT Enthusiast"
    const val DEVELOPER_BIO = "Passionate about cybersecurity, Android development, web development, " +
        "automation, OSINT, and creating modern applications. This project was developed as a " +
        "personal educational cybersecurity toolkit for defensive security, digital investigations, " +
        "and learning purposes."

    const val INSTAGRAM_USERNAME = "@bkushagra742"
    const val INSTAGRAM_URL = "https://www.instagram.com/bkushagra742?igsh=MTh1a2ZlaWc5czBpNQ=="
    const val GITHUB_USERNAME = "bkushagra742"
    const val GITHUB_URL = "https://github.com/bkushagra742"
    const val LINKEDIN_USERNAME = "bkushagra742"
    const val LINKEDIN_URL = "https://www.linkedin.com/bkushagra742"

    // Public, keyless OSINT data sources used by the online lookup tools.
    // No API keys/secrets are embedded anywhere in the app.
    const val GEOIP_API_BASE = "http://ip-api.com/json/"      // plain HTTP is this API's documented endpoint
    const val WHOIS_SERVER_DEFAULT = "whois.iana.org"
    const val WHOIS_PORT = 43
    const val DNS_PORT = 53
    const val PUBLIC_DNS_RESOLVER = "1.1.1.1"                  // Cloudflare public resolver

    const val PREFS_NAME = "reconx_settings"
}
