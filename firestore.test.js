const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_101";
const BOB_UID = "bob_uid_202";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validUserDoc(uid, createdAt) {
  return {
    userId: uid,
    email: `${uid}@example.com`,
    displayName: "Collector Alice",
    photoUrl: "https://example.com/avatar.png",
    mlbbIgn: "ShadowBlade",
    mlbbServerId: "1042",
    collectorRank: "Mythic",
    prestigeScore: 4200,
    coins: 250,
    diamonds: 15,
    uniqueSkinsCount: 18,
    totalSkinsOwned: 25,
    misprintCount: 1,
    totalPacksOpened: 5,
    totalTradesCompleted: 1,
    dailyStreakCount: 2,
    createdAt,
    updatedAt: createdAt,
  };
}

function validPublicProfile(uid, now) {
  return {
    userId: uid,
    displayName: "Collector Alice",
    photoUrl: "https://example.com/avatar.png",
    mlbbIgn: "ShadowBlade",
    mlbbServerId: "1042",
    collectorRank: "Mythic",
    motto: "Swapping Gusion & Kagura skins!",
    prestigeScore: 4200,
    uniqueSkinsCount: 18,
    totalPacksOpened: 5,
    totalTradesCompleted: 1,
    misprintCount: 1,
    pinnedBadgeIds: ["badge_first_rip", "badge_bazaar_broker"],
    showcaseSkinNames: ["Cosmic Gleam", "Soryu Maiden"],
    showcaseHeroNames: ["Gusion", "Kagura"],
    showcaseRarityNames: ["LEGEND", "COLLECTOR"],
    showcaseImageUrls: ["https://mlbbdex.com/a.webp", "https://mlbbdex.com/b.webp"],
    updatedAt: now,
  };
}

function validTradeListing(sellerUid, now) {
  return {
    sellerUid,
    sellerName: "Collector Alice",
    sellerPhotoUrl: "https://example.com/avatar.png",
    sellerMlbbIgn: "ShadowBlade",
    catalogId: "gusion_cosmic",
    skinName: "Cosmic Gleam",
    heroName: "Gusion",
    role: "Assassin",
    rawTier: "Legend",
    rarityName: "LEGEND",
    imageUrl: "https://mlbbdex.com/a.webp",
    isMisprint: false,
    misprintAnomalyName: "",
    serialCode: "STD-1A2B",
    seekingNote: "Seeking Lunox Legend or Franco Legend",
    tradeCode: "MEGA-TRADE-V1:abc",
    status: "OPEN",
    acceptedByUid: "",
    acceptedByName: "",
    acceptedCatalogId: "",
    acceptedSkinName: "",
    acceptedHeroName: "",
    acceptedRole: "",
    acceptedRawTier: "",
    acceptedRarityName: "",
    acceptedImageUrl: "",
    acceptedIsMisprint: false,
    acceptedMisprintAnomalyName: "",
    acceptedSerialCode: "",
    sellerSettled: false,
    createdAt: now,
    updatedAt: now,
  };
}

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

test("Unauthenticated users cannot read or write any collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const now = new Date(Date.now() - 2000);

  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("public_profiles").get());
  await assertFails(unauthDb.collection("trade_listings").where("status", "==", "OPEN").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).set(validUserDoc(ALICE_UID, now)));
});

test("Owner can create/update private profile and write history; cross-user denied", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 10000);
  const later = new Date(Date.now() - 1000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set(validUserDoc(ALICE_UID, created))
  );
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      createdAt: later,
      updatedAt: later,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("history").doc("evt_1").set({
      userId: ALICE_UID,
      eventType: "PULL",
      title: "Pulled Cosmic Gleam",
      detail: "Gusion (Legend)",
      rarityName: "LEGEND",
      coinDelta: -50,
      diamondDelta: 0,
      createdAt: later,
    })
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("history").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("history").get()
  );
});

test("Public profiles allow signed-in reads without email PII and block cross-user writes", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 2000);

  await assertSucceeds(
    aliceDb.collection("public_profiles").doc(ALICE_UID).set(validPublicProfile(ALICE_UID, now))
  );
  await assertSucceeds(bobDb.collection("public_profiles").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("public_profiles").doc(ALICE_UID).set(validPublicProfile(ALICE_UID, now))
  );

  // Reject unexpected email field on public profile
  await assertFails(
    aliceDb.collection("public_profiles").doc(ALICE_UID).set({
      ...validPublicProfile(ALICE_UID, now),
      email: "leak@example.com",
    })
  );
});

test("P2P trade listings enforce real player-to-player ownership and atomic swap acceptance", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 8000);
  const later = new Date(Date.now() - 1000);

  // Alice posts real listing
  await assertSucceeds(
    aliceDb.collection("trade_listings").doc("list_1").set(validTradeListing(ALICE_UID, created))
  );

  // Filtered query succeeds; unfiltered query fails
  await assertSucceeds(
    bobDb.collection("trade_listings").where("status", "==", "OPEN").get()
  );
  await assertFails(bobDb.collection("trade_listings").get());

  // Seller cannot self-accept their own listing
  await assertFails(
    aliceDb.collection("trade_listings").doc("list_1").update({
      status: "COMPLETED",
      acceptedByUid: ALICE_UID,
      acceptedByName: "Collector Alice",
      acceptedCatalogId: "miya_modena",
      acceptedSkinName: "Modena Butterfly",
      acceptedHeroName: "Miya",
      acceptedRole: "Marksman",
      acceptedRawTier: "Legend",
      acceptedRarityName: "LEGEND",
      acceptedImageUrl: "https://mlbbdex.com/c.webp",
      acceptedIsMisprint: false,
      acceptedMisprintAnomalyName: "",
      acceptedSerialCode: "STD-9999",
      updatedAt: later,
    })
  );

  // Bob accepts Alice's open listing atomically
  await assertSucceeds(
    bobDb.collection("trade_listings").doc("list_1").update({
      status: "COMPLETED",
      acceptedByUid: BOB_UID,
      acceptedByName: "Bob Mythic",
      acceptedCatalogId: "miya_modena",
      acceptedSkinName: "Modena Butterfly",
      acceptedHeroName: "Miya",
      acceptedRole: "Marksman",
      acceptedRawTier: "Legend",
      acceptedRarityName: "LEGEND",
      acceptedImageUrl: "https://mlbbdex.com/c.webp",
      acceptedIsMisprint: false,
      acceptedMisprintAnomalyName: "",
      acceptedSerialCode: "STD-9999",
      updatedAt: later,
    })
  );

  // Alice settles her completed listing to claim Bob's offered card
  await assertSucceeds(
    aliceDb.collection("trade_listings").doc("list_1").update({
      sellerSettled: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Completed trade listing cannot be deleted
  await assertFails(aliceDb.collection("trade_listings").doc("list_1").delete());
});
