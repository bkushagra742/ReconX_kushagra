package com.kushagra.reconx.scanner

import com.kushagra.reconx.models.CertificateInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

/**
 * TlsInspector.kt
 * ================
 * Certificate and TLS-configuration inspection: negotiated TLS version,
 * negotiated cipher suite, subject/issuer, and expiry. This opens a single
 * standard TLS handshake to the host on port 443 (exactly what a browser
 * does when visiting the site) and reads back the handshake session info
 * -- it does not attempt to force weak protocol/cipher downgrades or any
 * other active probing.
 */
object TlsInspector {

    suspend fun inspect(host: String, port: Int = 443, timeoutMs: Int = 10000): Result<CertificateInfo> =
        withContext(Dispatchers.IO) {
            runCatching {
                val factory = SSLSocketFactory.getDefault() as SSLSocketFactory
                (factory.createSocket() as SSLSocket).use { socket ->
                    socket.connect(java.net.InetSocketAddress(host, port), timeoutMs)
                    socket.soTimeout = timeoutMs
                    socket.startHandshake()

                    val session = socket.session
                    val cert = session.peerCertificates.firstOrNull() as? X509Certificate
                        ?: error("No peer certificate presented")

                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    val now = System.currentTimeMillis()
                    val daysLeft = TimeUnit.MILLISECONDS.toDays(cert.notAfter.time - now)

                    CertificateInfo(
                        subject = cert.subjectX500Principal.name,
                        issuer = cert.issuerX500Principal.name,
                        validFrom = sdf.format(cert.notBefore),
                        validTo = sdf.format(cert.notAfter),
                        daysUntilExpiry = daysLeft,
                        tlsVersion = session.protocol,
                        cipherSuite = session.cipherSuite,
                        isExpired = now > cert.notAfter.time,
                        isSelfSigned = cert.subjectX500Principal == cert.issuerX500Principal,
                    )
                }
            }
        }

    /** Enabled protocol/cipher lists supported by this device's TLS stack (client-side capability, informational). */
    fun supportedProtocolsAndCiphers(): Pair<List<String>, List<String>> {
        val factory = SSLSocketFactory.getDefault() as SSLSocketFactory
        val socket = factory.createSocket() as SSLSocket
        val protocols = socket.supportedProtocols.toList()
        val ciphers = socket.supportedCipherSuites.toList()
        socket.close()
        return protocols to ciphers
    }
}
