package dev.flondy.checkyourlove.monetization

import android.util.Base64
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dev.flondy.checkyourlove.monetization.billing.PurchaseVerifier
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.security.KeyPairGenerator
import java.security.Signature

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class PurchaseVerifierTest {
    private val keyPair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
    private val publicKey = Base64.encodeToString(keyPair.public.encoded, Base64.NO_WRAP)

    private fun sign(data: String): String {
        val s = Signature.getInstance("SHA1withRSA")
        s.initSign(keyPair.private)
        s.update(data.toByteArray())
        return Base64.encodeToString(s.sign(), Base64.NO_WRAP)
    }

    @Test fun `valid signature is accepted, tampered data rejected`() {
        val verifier = PurchaseVerifier(publicKey)
        val json = """{"productId":"premium","purchaseState":0}"""
        val sig = sign(json)
        assertThat(verifier.isConfigured).isTrue()
        assertThat(verifier.verify(json, sig)).isTrue()
        assertThat(verifier.verify(json.replace("0", "1"), sig)).isFalse()
        assertThat(verifier.verify(json, "")).isFalse()
        assertThat(verifier.verify(json, "bm90IGEgc2lnbmF0dXJl")).isFalse()
    }

    @Test fun `without key verifier is not configured`() {
        val verifier = PurchaseVerifier("")
        assertThat(verifier.isConfigured).isFalse()
        assertThat(PurchaseVerifier("garbage").isConfigured).isFalse()
    }
}
