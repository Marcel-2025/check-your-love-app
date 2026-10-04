package dev.flondy.checkyourlove.monetization

import com.google.common.truth.Truth.assertThat
import dev.flondy.checkyourlove.monetization.billing.Ownership
import dev.flondy.checkyourlove.monetization.billing.PurchaseEvaluator
import dev.flondy.checkyourlove.monetization.billing.PurchaseEvaluator.Source
import dev.flondy.checkyourlove.monetization.billing.PurchaseEvent
import dev.flondy.checkyourlove.monetization.billing.PurchaseSnapshot
import dev.flondy.checkyourlove.monetization.billing.PurchaseSnapshot.State
import org.junit.Test

class PurchaseEvaluatorTest {
    private val product = "premium"
    private val valid: (String, String) -> Boolean = { _, sig -> sig == "ok" }

    private fun purchase(state: State, ack: Boolean = false, sig: String = "ok", products: List<String> = listOf(product), token: String = "t1") =
        PurchaseSnapshot(products, state, ack, token, "{}", sig)

    @Test fun `purchased and verified unlocks and needs acknowledgement`() {
        val r = PurchaseEvaluator.evaluate(listOf(purchase(State.PURCHASED)), product, valid)
        assertThat(r.ownership).isEqualTo(Ownership.OWNED)
        assertThat(r.tokensToAcknowledge).containsExactly("t1")
    }

    @Test fun `already acknowledged purchase is not acknowledged again`() {
        val r = PurchaseEvaluator.evaluate(listOf(purchase(State.PURCHASED, ack = true)), product, valid)
        assertThat(r.ownership).isEqualTo(Ownership.OWNED)
        assertThat(r.tokensToAcknowledge).isEmpty()
    }

    @Test fun `invalid signature never unlocks`() {
        val r = PurchaseEvaluator.evaluate(listOf(purchase(State.PURCHASED, sig = "forged")), product, valid)
        assertThat(r.ownership).isEqualTo(Ownership.NOT_OWNED)
        assertThat(r.tokensToAcknowledge).isEmpty()
    }

    @Test fun `pending does not unlock`() {
        val r = PurchaseEvaluator.evaluate(listOf(purchase(State.PENDING)), product, valid)
        assertThat(r.ownership).isEqualTo(Ownership.PENDING)
        assertThat(r.tokensToAcknowledge).isEmpty()
    }

    @Test fun `other products are ignored`() {
        val r = PurchaseEvaluator.evaluate(listOf(purchase(State.PURCHASED, products = listOf("other"))), product, valid)
        assertThat(r.ownership).isEqualTo(Ownership.NOT_OWNED)
    }

    @Test fun `refunded purchase disappears from query and revokes premium`() {
        val r = PurchaseEvaluator.evaluate(emptyList(), product, valid)
        assertThat(r.ownership).isEqualTo(Ownership.NOT_OWNED)
        assertThat(PurchaseEvaluator.eventFor(Source.QUERY, Ownership.OWNED, r.ownership)).isEqualTo(PurchaseEvent.REVOKED)
    }

    @Test fun `events for restore and purchase updates`() {
        assertThat(PurchaseEvaluator.eventFor(Source.RESTORE, Ownership.NOT_OWNED, Ownership.OWNED)).isEqualTo(PurchaseEvent.RESTORED)
        assertThat(PurchaseEvaluator.eventFor(Source.RESTORE, Ownership.UNKNOWN, Ownership.NOT_OWNED)).isEqualTo(PurchaseEvent.NOTHING_TO_RESTORE)
        assertThat(PurchaseEvaluator.eventFor(Source.PURCHASE_UPDATE, Ownership.NOT_OWNED, Ownership.OWNED)).isEqualTo(PurchaseEvent.SUCCESS)
        assertThat(PurchaseEvaluator.eventFor(Source.PURCHASE_UPDATE, Ownership.NOT_OWNED, Ownership.PENDING)).isEqualTo(PurchaseEvent.PENDING)
        assertThat(PurchaseEvaluator.eventFor(Source.QUERY, Ownership.PENDING, Ownership.OWNED)).isEqualTo(PurchaseEvent.SUCCESS)
        assertThat(PurchaseEvaluator.eventFor(Source.QUERY, Ownership.NOT_OWNED, Ownership.NOT_OWNED)).isNull()
        assertThat(PurchaseEvaluator.eventFor(Source.QUERY, Ownership.UNKNOWN, Ownership.OWNED)).isNull()
    }
}
