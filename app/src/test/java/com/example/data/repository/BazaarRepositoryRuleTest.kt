package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.CartItem
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class BazaarRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createAndObserveProduct_authenticatedSeller_succeeds() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repo = BazaarRepository(firestore)
        val customId = "prod_${UUID.randomUUID().toString().replace("-", "").take(12)}"

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.createProduct(
                title = "Flagship Earbuds Pro",
                description = "Active noise cancelling wireless earbuds with 36h battery.",
                category = "Electronics",
                price = 9500.0,
                originalPrice = 12000.0,
                stock = 25L,
                imageUrl = "https://images.unsplash.com/photo-1590658268037-6bf12165a8df",
                featured = true,
                brand = "SonicPulse",
                customId = customId
            )
        }
        assertTrue("Expected product creation to succeed: ${createResult.exceptionOrNull()}", createResult.isSuccess)

        val emittedProducts = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeActiveProducts().first { list -> list.any { it.id == customId } }
        }
        assertTrue(emittedProducts.any { it.id == customId })
    }

    @Test
    fun placeOrderAndCrossUserAccess_enforcesOwnerIsolation() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = BazaarRepository(firestore)

        val orderIdResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.placeOrder(
                customerName = "Alice Khan",
                customerPhone = "03001234567",
                shippingAddress = "House 14, Street 8, DHA Phase 5",
                city = "Lahore",
                paymentMethod = "JazzCash",
                paymentAccountRef = "03001234567",
                cartItems = listOf(
                    CartItem(
                        id = "",
                        userId = aliceUid,
                        productId = "prod_1",
                        productTitle = "Flagship Earbuds Pro",
                        productImage = "",
                        category = "Electronics",
                        unitPrice = 9500.0,
                        quantity = 2L
                    )
                )
            )
        }
        assertTrue("Expected placeOrder to succeed: ${orderIdResult.exceptionOrNull()}", orderIdResult.isSuccess)
        val orderId = orderIdResult.getOrThrow()

        // Switch to Bob and verify cross-user order read fails with PERMISSION_DENIED
        signInTestUser(BOB_EMAIL)
        val bobRepo = BazaarRepository(firestore)
        val crossReadResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getOrderById(orderId)
        }
        assertTrue("Expected cross-user order read to fail", crossReadResult.isFailure)
        val fsEx = crossReadResult.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull("Expected FirebaseFirestoreException", fsEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx?.code)
    }

    @Test
    fun observeActiveProducts_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repo = BazaarRepository(firestore)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repo.observeActiveProducts().first()
            }
            fail("Expected unauthenticated observeActiveProducts to throw exception")
        } catch (e: Exception) {
            val fsEx = e.unwrapFirestoreException()
            assertNotNull("Expected nested FirebaseFirestoreException, got $e", fsEx)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx?.code)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice@bazaarpk.test"
        const val BOB_EMAIL = "bob@bazaarpk.test"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}
