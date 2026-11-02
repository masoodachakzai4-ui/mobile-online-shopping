# Security Specification (`security_spec.md`)

## 1. Data Invariants
1. **Strict Authentication Gate**: No collection allows unauthenticated reads or writes (`request.auth != null`).
2. **PII Isolation**: `/users/{userId}` and `/orders/{orderId}` contain PII (email, phone, shippingAddress) and are strictly readable only by the document owner (`request.auth.uid == userId` / `resource.data.userId == request.auth.uid`) or verified `isAdmin()`.
3. **Subcollection Ownership**: `/users/{userId}/cart/{cartItemId}` and `/users/{userId}/wishlist/{productId}` require both path ownership (`request.auth.uid == userId`) and payload ownership (`data.userId == request.auth.uid`).
4. **Catalog & Review Visibility**: `/products/{productId}` `list` queries require `resource.data.isActive == true` (or seller/admin ownership). `/reviews/{reviewId}` `list` queries require `resource.data.isPublic == true` (or author/admin ownership) and `create` verifies `exists(/databases/$(database)/documents/products/$(incoming().productId))`.
5. **Terminal State Locking**: Orders in terminal states (`Delivered` or `Cancelled`) cannot be mutated by standard users.

## 2. The "Dirty Dozen" Adversarial Payloads
1. **Unauthenticated Catalog Scraping**: `unauthDb.collection("products").where("isActive", "==", true).get()` -> REJECTED.
2. **Cross-User Profile PII Read**: Bob reads `/users/alice_123` -> REJECTED.
3. **Cross-User Order PII Read**: Bob reads `/orders/alice_order` -> REJECTED.
4. **Unfiltered Order List Query**: Alice calls `db.collection("orders").get()` without `.where("userId", "==", aliceUid)` -> REJECTED.
5. **Shadow Field Injection on Profile**: Alice writes `isVerified: true` or `isAdmin: true` to `/users/alice_123` -> REJECTED via `hasOnly`.
6. **Identity Spoofing on Order Creation**: Alice creates an order with `userId: "bob_456"` -> REJECTED.
7. **State Shortcutting on Order Creation**: Alice creates an order directly in `orderStatus: "Delivered"` -> REJECTED (must start at `'Placed'`).
8. **Terminal State Mutation**: Alice attempts to update an order whose `orderStatus` is already `'Delivered'` -> REJECTED.
9. **Orphaned Review Creation**: Alice creates a review for a non-existent `productId: "ghost_prod_999"` -> REJECTED via `exists(...)`.
10. **Value Poisoning on Cart Quantity**: Alice updates cart `quantity` to `-5` or `"ten"` -> REJECTED via `isValidCartItem`.
11. **Immortal Field Tampering**: Alice updates `createdAt` or `sellerId` on an existing product -> REJECTED.
12. **Admin Email Spoofing**: Attacker signs in with unverified `masoodachakzai4@gmail.com` (`email_verified: false`) -> REJECTED.
