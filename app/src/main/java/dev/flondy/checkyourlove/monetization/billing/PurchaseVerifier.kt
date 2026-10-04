package dev.flondy.checkyourlove.monetization.billing

import android.util.Base64
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec

/**
 * Lokale Signaturprüfung von Käufen mit dem ÖFFENTLICHEN Lizenzschlüssel aus der Play Console.
 *
 * Für einen einmaligen, rein kosmetischen Kauf ohne eigenen Server ist das die übliche
 * Absicherung: Freischaltungen basieren auf von Google signierten Kaufdaten statt auf einem
 * frei veränderbaren lokalen Schalter. Eine serverseitige Prüfung (Play Developer API) wäre
 * nur mit eigenem Backend möglich und ist für diese App bewusst nicht vorgesehen.
 */
class PurchaseVerifier(base64PublicKey: String) {
    private val publicKey: PublicKey? = base64PublicKey.trim().takeIf { it.isNotEmpty() }?.let { key ->
        runCatching {
            val decoded = Base64.decode(key, Base64.DEFAULT)
            KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(decoded))
        }.getOrNull()
    }

    /** true, wenn ein (gültiger) Schlüssel hinterlegt ist. */
    val isConfigured: Boolean get() = publicKey != null

    /**
     * Prüft die Signatur. Ohne konfigurierten Schlüssel wird den direkt vom Play-Store-Dienst
     * gelieferten Daten vertraut (Debug/Tests); Release-Builds sollten den Schlüssel setzen.
     */
    fun verify(signedData: String, signature: String): Boolean {
        val key = publicKey ?: return true
        if (signedData.isEmpty() || signature.isEmpty()) return false
        return runCatching {
            val sig = Signature.getInstance("SHA1withRSA")
            sig.initVerify(key)
            sig.update(signedData.toByteArray(Charsets.UTF_8))
            sig.verify(Base64.decode(signature, Base64.DEFAULT))
        }.getOrDefault(false)
    }
}
