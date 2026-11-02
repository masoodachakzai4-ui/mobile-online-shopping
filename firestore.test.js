const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const ADMIN_UID = "admin_789";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

// 1. Unauthenticated rejection tests
test("Unauthenticated user cannot read products, orders, or users", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("products").where("isActive", "==", true).get());
  await assertFails(unauthDb.collection("orders").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
});

// 2. User Profile PII isolation & Shadow Update test
test("User Profile: owner can read/write own profile, cross-user PII read and shadow fields fail", async () => {
  const now = new Date();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice Khan",
      email: "alice@example.com",
      phone: "03001234567",
      shippingAddress: "House 12, Street 5, F-7",
      city: "Islamabad",
      preferredPayment: "JazzCash",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Cross-user PII read must fail
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());

  // Shadow field injection must fail
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      displayName: "Alice Hacked",
      isAdmin: true,
      updatedAt: new Date(),
    })
  );
});

// 3. Products catalog & query alignment tests
test("Products: active products can be listed with isActive==true filter; unfiltered list fails", async () => {
  const now = new Date();
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx.firestore().collection("products").doc("prod_1").set({
      title: "Pro Wireless Headphones",
      description: "Active noise cancelling flagship headphones",
      category: "Electronics",
      price: 18500,
      originalPrice: 22000,
      stock: 15,
      imageUrl: "https://images.unsplash.com/photo-1505740420928-5e560c06d30e",
      featured: true,
      ratingAvg: 4.8,
      reviewCount: 12,
      brand: "SonicPulse",
      sellerId: ADMIN_UID,
      isActive: true,
      createdAt: now,
      updatedAt: now,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("products").where("isActive", "==", true).get()
  );
  await assertFails(aliceDb.collection("products").get());
});

// 4. Orders security & terminal state locking tests
test("Orders: owner can create Placed order and query own orders; cross-user read and terminal mutation fail", async () => {
  const now = new Date();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("orders").doc("order_101").set({
      userId: ALICE_UID,
      customerName: "Alice Khan",
      customerPhone: "03001234567",
      shippingAddress: "House 12, Street 5, F-7",
      city: "Islamabad",
      paymentMethod: "JazzCash",
      paymentAccountRef: "03001234567",
      paymentStatus: "Paid",
      orderStatus: "Placed",
      trackingNumber: "BZP-100234",
      itemsSummary: "1x Pro Wireless Headphones",
      itemCount: 1,
      subtotal: 18500,
      deliveryFee: 250,
      discountAmount: 0,
      totalAmount: 18750,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Owner aligned query succeeds
  await assertSucceeds(
    aliceDb.collection("orders").where("userId", "==", ALICE_UID).get()
  );

  // Cross-user read fails
  await assertFails(bobDb.collection("orders").doc("order_101").get());

  // Seed a Delivered order and verify non-admin owner cannot mutate it
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx.firestore().collection("orders").doc("order_delivered").set({
      userId: ALICE_UID,
      customerName: "Alice Khan",
      customerPhone: "03001234567",
      shippingAddress: "House 12, Street 5, F-7",
      city: "Islamabad",
      paymentMethod: "Cash on Delivery",
      paymentAccountRef: "",
      paymentStatus: "Paid",
      orderStatus: "Delivered",
      trackingNumber: "BZP-999999",
      itemsSummary: "1x Running Shoes",
      itemCount: 1,
      subtotal: 9500,
      deliveryFee: 250,
      discountAmount: 0,
      totalAmount: 9750,
      createdAt: now,
      updatedAt: now,
    });
  });

  await assertFails(
    aliceDb.collection("orders").doc("order_delivered").update({
      orderStatus: "Cancelled",
      updatedAt: new Date(),
    })
  );
});

// 5. Reviews parent existence check & public query alignment
test("Reviews: cannot create review for non-existent product; valid review succeeds", async () => {
  const now = new Date();
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx.firestore().collection("products").doc("prod_valid").set({
      title: "Smart Watch Ultra",
      description: "AMOLED GPS fitness smartwatch",
      category: "Electronics",
      price: 14500,
      originalPrice: 17000,
      stock: 20,
      imageUrl: "",
      featured: true,
      ratingAvg: 4.9,
      reviewCount: 5,
      brand: "ChronoTech",
      sellerId: ADMIN_UID,
      isActive: true,
      createdAt: now,
      updatedAt: now,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();

  // Orphaned review on non-existent product must fail
  await assertFails(
    aliceDb.collection("reviews").doc("rev_orphan").set({
      productId: "non_existent_product",
      userId: ALICE_UID,
      userName: "Alice Khan",
      rating: 5,
      comment: "Great product!",
      verifiedPurchase: true,
      isPublic: true,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Valid review on existing product succeeds
  await assertSucceeds(
    aliceDb.collection("reviews").doc("rev_valid").set({
      productId: "prod_valid",
      userId: ALICE_UID,
      userName: "Alice Khan",
      rating: 5,
      comment: "Amazing battery life and build quality!",
      verifiedPurchase: true,
      isPublic: true,
      createdAt: now,
      updatedAt: now,
    })
  );
});
