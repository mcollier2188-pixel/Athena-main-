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

test("Unauthenticated user: cannot read notes", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("notes").get());
});

test("Root user document: blocks direct unvalidated writes", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({ arbitraryBlob: "unvalidated" })
  );
});

test("Authenticated user: can create own note with valid schema", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("notes").doc("note1").set({
      id: "note1",
      userId: ALICE_UID,
      name: "Solent Bedrock Survey",
      kind: "note",
      text: "Carrier frequency 42.6 Hz locked.",
      tags: "echo, survey"
    })
  );
});

test("Schema validation: rejects unexpected extra fields on note", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("notes").doc("note_extra").set({
      id: "note_extra",
      userId: ALICE_UID,
      name: "Injected Note",
      kind: "note",
      injectedAdminFlag: true
    })
  );
});

test("Schema validation: rejects mismatched note document ID", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("notes").doc("doc_a").set({
      id: "doc_b",
      userId: ALICE_UID,
      name: "Mismatch ID",
      kind: "note"
    })
  );
});

test("Cross-user access: Alice cannot read Bob's note", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).collection("notes").doc("bob_note").set({
      id: "bob_note",
      userId: BOB_UID,
      name: "Secret Note",
      kind: "note"
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("notes").doc("bob_note").get());
});

test("Schema validation: rejects note missing required name", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("notes").doc("bad_note").set({
      id: "bad_note",
      userId: ALICE_UID,
      kind: "note"
    })
  );
});

test("Generations & Profile: enforces valid schema and current profile doc ID", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("generations").doc("gen1").set({
      id: "gen1",
      userId: ALICE_UID,
      type: "chat",
      prompt: "Explain 42.6 Hz carrier",
      modelUsed: "gemini-2.5-flash"
    })
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("profile").doc("current").set({
      userId: ALICE_UID,
      displayName: "Researcher",
      favoriteFrequency: 42.6
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("profile").doc("other_doc").set({
      userId: ALICE_UID
    })
  );
});
