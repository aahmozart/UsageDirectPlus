package godau.fynn.usagedirectplus.browser

object BrowserSupport {
    const val CHROME_PACKAGE = "com.android.chrome"
    const val VANADIUM_PACKAGE = "app.vanadium.browser"

    private const val FIREFOX_PREFIX = "org.mozilla.firefox"
    private const val FENIX_PREFIX = "org.mozilla.fenix"

    fun isSupportedBrowser(applicationId: String?): Boolean {
        if (applicationId == null) return false
        return familyFor(applicationId) != null
    }

    fun familyFor(applicationId: String?): BrowserFamily? {
        if (applicationId == null) return null

        return when {
            applicationId == CHROME_PACKAGE -> BrowserFamily.CHROMIUM
            applicationId == VANADIUM_PACKAGE -> BrowserFamily.CHROMIUM
            applicationId.startsWith(FIREFOX_PREFIX) -> BrowserFamily.FIREFOX
            applicationId.startsWith(FENIX_PREFIX) -> BrowserFamily.FIREFOX
            else -> null
        }
    }
}

enum class BrowserFamily {
    CHROMIUM,
    FIREFOX
}
