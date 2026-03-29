package godau.fynn.usagedirectplus.browser

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class HostnameExtractorTest {

    @Test
    fun `extracts hostname from full https url`() {
        assertThat(HostnameExtractor.extractHostname("https://example.com/path")).isEqualTo("example.com")
    }

    @Test
    fun `extracts hostname from http url`() {
        assertThat(HostnameExtractor.extractHostname("http://example.org")).isEqualTo("example.org")
    }

    @Test
    fun `strips www prefix`() {
        assertThat(HostnameExtractor.extractHostname("www.google.com/maps/@16")).isEqualTo("google.com")
    }

    @Test
    fun `strips protocol and www together`() {
        assertThat(HostnameExtractor.extractHostname("https://www.example.org")).isEqualTo("example.org")
    }

    @Test
    fun `preserves subdomain other than www`() {
        assertThat(HostnameExtractor.extractHostname("mail.proton.me/u/0/inbox")).isEqualTo("mail.proton.me")
    }

    @Test
    fun `handles bare hostname with path`() {
        assertThat(HostnameExtractor.extractHostname("x.com/home")).isEqualTo("x.com")
    }

    @Test
    fun `handles bare hostname without path`() {
        assertThat(HostnameExtractor.extractHostname("synergy.vn")).isEqualTo("synergy.vn")
    }

    @Test
    fun `lowercases hostname`() {
        assertThat(HostnameExtractor.extractHostname("WWW.Google.COM/search")).isEqualTo("google.com")
    }

    @Test
    fun `returns null for blank input`() {
        assertThat(HostnameExtractor.extractHostname("")).isNull()
        assertThat(HostnameExtractor.extractHostname("   ")).isNull()
    }

    @Test
    fun `returns null when no dot in hostname`() {
        assertThat(HostnameExtractor.extractHostname("localhost")).isNull()
    }

    @Test
    fun `returns null for whitespace in hostname`() {
        assertThat(HostnameExtractor.extractHostname("not a url")).isNull()
    }

    @Test
    fun `trims surrounding whitespace`() {
        assertThat(HostnameExtractor.extractHostname("  example.com  ")).isEqualTo("example.com")
    }
}
