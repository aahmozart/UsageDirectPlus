package godau.fynn.usagedirectplus.browser

import java.util.Locale

object HostnameExtractor {

    fun extractHostname(url: String): String? {
        var cleaned = url.trim().lowercase(Locale.US)
        if (cleaned.isBlank()) return null

        cleaned = cleaned.removePrefix("https://").removePrefix("http://")
        cleaned = cleaned.removePrefix("www.")

        val slashIndex = cleaned.indexOf('/')
        val host = if (slashIndex >= 0) cleaned.substring(0, slashIndex) else cleaned

        if (host.isBlank() || '.' !in host || ' ' in host) return null
        return host
    }
}
