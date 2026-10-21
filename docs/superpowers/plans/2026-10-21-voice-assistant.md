# Voice (FunctionGemma 270M Mobile Actions) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a production-quality, privacy-first, fully offline Android voice assistant named Voice, powered by Google's FunctionGemma 270M Mobile Actions on LiteRT-LM, executing native device actions with biometric security, foreground microphone service lifecycle, and a Material 3 Jetpack Compose UI.

**Architecture:** An audio capture pipeline (AudioRecord 16 kHz Mono PCM) feeds a lightweight wake-word detector ("Hey Voice", "Okay Voice") and an offline STT engine (Vosk). Transcribed text is formatted into the official FunctionGemma Mobile Actions chat template and evaluated on-device via LiteRT-LM. Extracted function calls undergo strict schema validation and a configurable biometric risk policy before dispatch to modular Android native action executors, with state managed via Kotlin StateFlow and presented through a Material 3 Jetpack Compose UI.

**Tech Stack:** Kotlin 2.0+, Coroutines & Flow, Android Jetpack Compose (Material 3), Google LiteRT-LM (`com.google.ai.edge.litertlm`), Vosk Offline STT, Android Keystore (AES-256-GCM & EC Biometric Signatures), Android Telecom/Camera/DevicePolicyManager APIs, Gradle 8.13.

**Spec:** [User Master Development Prompt - Voice](file:///c:/Users/rc821/OneDrive/Desktop/Mobile%20Voice%20Agent/README-1.md)

## Global Constraints

- Strictly offline in the `offline` build variant: NO `android.permission.INTERNET`, no cloud APIs, no external analytics, no telemetry.
- Intent model: FunctionGemma 270M Mobile Actions (`litert-community/functiongemma-270m-ft-mobile-actions`) executed locally via LiteRT-LM (`.litertlm`).
- Never use regex or generic cloud LLMs to replace FunctionGemma action routing.
- Microphone foreground service requires `FOREGROUND_SERVICE_MICROPHONE`, user-visible ongoing notification with immediate Stop action, and `START_NOT_STICKY`.
- Biometric authentication (`BiometricPrompt` with `BIOMETRIC_STRONG` Android Keystore signing) is mandatory for sensitive commands (messages, notification replies, contacts/calls when locked).
- All model output must pass strict schema validation against the 13 supported mobile action functions.
- Android 14+ / 15+ platform rules strictly respected: no background Activity launches; user-tapped notifications for approvals; explicit runtime permissions.

---

### Task 1: Project Build Configuration, Build Flavors, and Dependency Setup

**Files:**
- Modify: `VoiceOffline/app/build.gradle.kts`
- Modify: `VoiceOffline/build.gradle.kts`
- Modify: `VoiceOffline/settings.gradle.kts`

**Interfaces:**
- Produces: Gradle configuration supporting Jetpack Compose, Material 3, LiteRT-LM, Vosk, Coroutines, AndroidX Biometrics, and two build flavors (`offline` and `play`).

- [ ] **Step 1: Update app/build.gradle.kts with Compose, build flavors, and dependencies**
Add Jetpack Compose build features, `offline` and `play` product flavors, Coroutines, Room, LiteRT-LM, and test dependencies.

- [ ] **Step 2: Run Gradle sync/test to verify dependency resolution**
Run: `$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'; .\gradlew.bat testOfflineDebugUnitTest`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit build configuration**
Commit build and flavor changes.

---

### Task 2: Function Calling Schema, Prompt Builder, and Parser for FunctionGemma 270M

**Files:**
- Create: `VoiceOffline/app/src/main/java/dev/voice/offline/ai/functioncalling/ActionFunctionDeclaration.kt`
- Create: `VoiceOffline/app/src/main/java/dev/voice/offline/ai/functioncalling/FunctionCallingPromptBuilder.kt`
- Create: `VoiceOffline/app/src/main/java/dev/voice/offline/ai/functioncalling/FunctionCallParser.kt`
- Create: `VoiceOffline/app/src/main/java/dev/voice/offline/ai/functioncalling/FunctionSchemaValidator.kt`
- Create: `VoiceOffline/app/src/main/java/dev/voice/offline/ai/functioncalling/StructuredAction.kt`
- Create: `VoiceOffline/app/src/test/java/dev/voice/offline/ai/functioncalling/FunctionCallingTest.kt`

**Interfaces:**
- Produces:
  - `StructuredAction`: Typed sealed class for all 13 supported mobile actions: `MakeCall`, `AnswerCall`, `RejectCall`, `OpenCamera`, `LockScreen`, `TakeNotes`, `OpenApp`, `SetAlarm`, `TurnFlashlightOn`, `TurnFlashlightOff`, `TextMsg`, `ReadNotifications`, `ReplyToNotification`.
  - `FunctionCallingPromptBuilder.buildPrompt(transcript: String): String`: Formats user text with tool declarations into FunctionGemma mobile actions chat template.
  - `FunctionCallParser.parse(modelOutput: String): RawFunctionCall?`: Parses `<start_function_call>call:func{k:<escape>v<escape>}<end_function_call>`.
  - `FunctionSchemaValidator.validate(raw: RawFunctionCall): ValidationResult<StructuredAction>`: Validates arguments against schema.

- [ ] **Step 1: Write unit tests for PromptBuilder, FunctionCallParser, and FunctionSchemaValidator**
Test formatting, token generation, parsing valid function calls, handling unescaped/escaped arguments, and rejecting unknown functions or malicious prompt injections.

- [ ] **Step 2: Run tests to verify failure**
Run: `$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'; .\gradlew.bat testOfflineDebugUnitTest --tests "dev.voice.offline.ai.functioncalling.FunctionCallingTest"`
Expected: Compilation failure or missing class.

- [ ] **Step 3: Implement StructuredAction, ActionFunctionDeclaration, PromptBuilder, Parser, and Validator**
Implement the official FunctionGemma 270M Mobile Actions format with `<start_function_call>`, `<escape>`, and strict schema validation.

- [ ] **Step 4: Run tests to verify they pass**
Run: `$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'; .\gradlew.bat testOfflineDebugUnitTest --tests "dev.voice.offline.ai.functioncalling.FunctionCallingTest"`
Expected: PASS

- [ ] **Step 5: Commit Function Calling implementation**
Commit schema, parser, and tests.

---

### Task 3: LiteRT-LM Inference Engine, Model Manager, and Adapter

**Files:**
- Create: `VoiceOffline/app/src/main/java/dev/voice/offline/ai/functiongemma/LiteRtInferenceAdapter.kt`
- Create: `VoiceOffline/app/src/main/java/dev/voice/offline/ai/functiongemma/FunctionGemmaEngine.kt`
- Create: `VoiceOffline/app/src/main/java/dev/voice/offline/ai/functiongemma/MobileActionsModelManager.kt`
- Create: `VoiceOffline/app/src/main/java/dev/voice/offline/benchmark/ModelBenchmark.kt`
- Create: `VoiceOffline/app/src/test/java/dev/voice/offline/ai/functiongemma/FunctionGemmaEngineTest.kt`

**Interfaces:**
- Produces:
  - `LiteRtInferenceAdapter`: Interface and implementation wrapping `Engine` / `Conversation` with session warm-up, timeout cancellation (e.g. 5 seconds max), memory protection, and mock/fallback adapter for environments without NPU/GPU drivers.
  - `FunctionGemmaEngine`: Dispatches prompts to LiteRT-LM, parses raw output tokens, validates structured actions.
  - `MobileActionsModelManager`: Manages `.litertlm` artifact integrity (SHA-256), version compatibility, asset installation into local app storage, warm-up, and memory release on trim.
  - `ModelBenchmark`: Benchmark suite measuring Time-To-First-Token (TTFT), complete inference latency, schema validation latency, and memory delta.

- [ ] **Step 1: Write unit tests for FunctionGemmaEngine and ModelManager**
Verify prompt-to-action routing, timeout cancellation, and model file integrity checks.

- [ ] **Step 2: Run tests to verify failure**
Run: `$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'; .\gradlew.bat testOfflineDebugUnitTest --tests "dev.voice.offline.ai.functiongemma.FunctionGemmaEngineTest"`

- [ ] **Step 3: Implement LiteRtInferenceAdapter, FunctionGemmaEngine, MobileActionsModelManager, and ModelBenchmark**
Implement memory-efficient model lifecycle, coroutine cancellation, token limits, and benchmark harness.

- [ ] **Step 4: Run tests to verify they pass**
Run: `$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'; .\gradlew.bat testOfflineDebugUnitTest --tests "dev.voice.offline.ai.functiongemma.FunctionGemmaEngineTest"`
Expected: PASS

- [ ] **Step 5: Commit LiteRT-LM FunctionGemma engine**
Commit engine and benchmark components.

---

### Task 4: Modular Native Action Executors

**Files:**
- Create: `VoiceOffline/app/src/main/java/dev/voice/offline/actions/ActionExecutorRegistry.kt`
- Create: `VoiceOffline/app/src/main/java/dev/voice/offline/actions/ExecutionResult.kt`
- Create: `VoiceOffline/app/src/main/java/dev/voice/offline/actions/calls/CallActionExecutor.kt`
- Create: `VoiceOffline/app/src/main/java/dev/voice/offline/actions/messaging/MessagingActionExecutor.kt`
- Create: `VoiceOffline/app/src/main/java/dev/voice/offline/actions/notifications/NotificationActionExecutor.kt`
- Create: `VoiceOffline/app/src/main/java/dev/voice/offline/actions/camera/CameraActionExecutor.kt`
- Create: `VoiceOffline/app/src/main/java/dev/voice/offline/actions/screen/ScreenActionExecutor.kt`
- Create: `VoiceOffline/app/src/main/java/dev/voice/offline/actions/flashlight/FlashlightActionExecutor.kt`
- Create: `VoiceOffline/app/src/main/java/dev/voice/offline/actions/alarms/AlarmActionExecutor.kt`
- Create: `VoiceOffline/app/src/main/java/dev/voice/offline/actions/notes/NoteActionExecutor.kt`
- Create: `VoiceOffline/app/src/main/java/dev/voice/offline/actions/apps/AppActionExecutor.kt`
- Create: `VoiceOffline/app/src/test/java/dev/voice/offline/actions/ActionExecutorTest.kt`

**Interfaces:**
- Consumes: `StructuredAction`
- Produces:
  - `ExecutionResult`: `Success(message)`, `RequiresConfirmation(message)`, `RequiresAuthentication(message)`, `RequiresPermission(permission)`, `Ambiguous(candidates)`, `Unsupported(message)`, `Failed(error)`.
  - `ActionExecutorRegistry.execute(action: StructuredAction): ExecutionResult`

- [ ] **Step 1: Write unit tests for ActionExecutorRegistry and argument validation**
Verify contact disambiguation handling, camera intent formatting, flashlight safety, alarm parameter checks, encrypted note storage, and permission requirement detection.

- [ ] **Step 2: Run tests to verify failure**
Run: `$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'; .\gradlew.bat testOfflineDebugUnitTest --tests "dev.voice.offline.actions.ActionExecutorTest"`

- [ ] **Step 3: Implement modular executors and registry**
Implement one clean executor per action family with Android API boundary checks.

- [ ] **Step 4: Run tests to verify they pass**
Run: `$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'; .\gradlew.bat testOfflineDebugUnitTest --tests "dev.voice.offline.actions.ActionExecutorTest"`
Expected: PASS

- [ ] **Step 5: Commit modular action executors**
Commit actions and tests.

---

### Task 5: Biometric Security Policy, KeystoreManager, and Authorization Queue

**Files:**
- Create: `VoiceOffline/app/src/main/java/dev/voice/offline/security/SensitiveActionPolicy.kt`
- Create: `VoiceOffline/app/src/main/java/dev/voice/offline/security/PendingCommandStore.kt`
- Create: `VoiceOffline/app/src/main/java/dev/voice/offline/security/BiometricAuthorizationManager.kt`
- Create: `VoiceOffline/app/src/main/java/dev/voice/offline/security/KeystoreManager.kt`
- Modify: `VoiceOffline/app/src/main/java/dev/voice/offline/security/AuthorizationActivity.kt`
- Create: `VoiceOffline/app/src/test/java/dev/voice/offline/security/SecurityPolicyTest.kt`

**Interfaces:**
- Produces:
  - `SensitiveActionPolicy`: Categorizes actions into `LOW`, `MEDIUM`, `HIGH` risk with configurable overrides.
  - `PendingCommandStore`: Generates cryptographically secure single-use tokens with a 30-second TTL and automatic purge. Prevents replays and tampering.
  - `KeystoreManager`: Checks StrongBox / TEE hardware security levels, handles key generation with `AUTH_BIOMETRIC_STRONG` and `setInvalidatedByBiometricEnrollment(true)`.
  - `BiometricAuthorizationManager`: Orchestrates BiometricPrompt with hardware-bound signature authentication.

- [ ] **Step 1: Write unit tests for SensitiveActionPolicy, PendingCommandStore, and KeystoreManager**
Test risk classification, command expiry, replay prevention (token cannot be used twice), and biometric enrollment invalidation handling.

- [ ] **Step 2: Run tests to verify failure**
Run: `$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'; .\gradlew.bat testOfflineDebugUnitTest --tests "dev.voice.offline.security.SecurityPolicyTest"`

- [ ] **Step 3: Implement security classes and enhance AuthorizationActivity**
Implement robust biometric verification and secure UI.

- [ ] **Step 4: Run tests to verify they pass**
Run: `$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'; .\gradlew.bat testOfflineDebugUnitTest --tests "dev.voice.offline.security.SecurityPolicyTest"`
Expected: PASS

- [ ] **Step 5: Commit biometric security module**
Commit security components and tests.

---

### Task 6: Audio Pipeline, Wake-Word Detector, and Offline STT Integration

**Files:**
- Create: `VoiceOffline/app/src/main/java/dev/voice/offline/ai/wakeword/WakeWordDetector.kt`
- Create: `VoiceOffline/app/src/main/java/dev/voice/offline/ai/wakeword/VoskWakeWordDetector.kt`
- Create: `VoiceOffline/app/src/main/java/dev/voice/offline/ai/stt/OfflineSpeechRecognizer.kt`
- Create: `VoiceOffline/app/src/main/java/dev/voice/offline/ai/stt/VoskSpeechRecognizer.kt`
- Create: `VoiceOffline/app/src/main/java/dev/voice/offline/core/audio/AudioRecordManager.kt`
- Create: `VoiceOffline/app/src/main/java/dev/voice/offline/core/audio/AudioRingBuffer.kt`
- Create: `VoiceOffline/app/src/test/java/dev/voice/offline/core/audio/AudioPipelineTest.kt`

**Interfaces:**
- Produces:
  - `AudioRecordManager`: Captures 16 kHz Mono PCM audio in a dedicated coroutine with bounded ring buffer and recovery from microphone contention.
  - `WakeWordDetector`: Detects "Hey Voice" / "Okay Voice" with cooldown after activation, sensitivity configuration, and false positive reduction.
  - `OfflineSpeechRecognizer`: Recognizes user commands with VAD, silence timeout (3s), and max recording duration (7s).

- [ ] **Step 1: Write unit tests for AudioRingBuffer, WakeWordDetector cooldown, and silence detection**
Test bounded buffer overflow protection, audio chunk offering, and state transitions.

- [ ] **Step 2: Run tests to verify failure**
Run: `$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'; .\gradlew.bat testOfflineDebugUnitTest --tests "dev.voice.offline.core.audio.AudioPipelineTest"`

- [ ] **Step 3: Implement AudioRecordManager, AudioRingBuffer, WakeWordDetector, and OfflineSpeechRecognizer**
Implement decoupled audio pipeline with coroutine channels.

- [ ] **Step 4: Run tests to verify they pass**
Run: `$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'; .\gradlew.bat testOfflineDebugUnitTest --tests "dev.voice.offline.core.audio.AudioPipelineTest"`
Expected: PASS

- [ ] **Step 5: Commit audio pipeline module**
Commit audio components and tests.

---

### Task 7: Foreground Service and End-to-End Assistant State Machine

**Files:**
- Create: `VoiceOffline/app/src/main/java/dev/voice/offline/service/AssistantState.kt`
- Modify: `VoiceOffline/app/src/main/java/dev/voice/offline/VoiceAssistantService.kt`
- Create: `VoiceOffline/app/src/test/java/dev/voice/offline/service/AssistantStateMachineTest.kt`

**Interfaces:**
- Produces:
  - `AssistantState`: `STOPPED`, `INITIALIZING`, `LISTENING`, `WAKE_DETECTED`, `RECORDING_COMMAND`, `TRANSCRIBING`, `INFERENCING`, `AUTHORIZING`, `EXECUTING`, `CONFIRMING`, `ERROR`.
  - `VoiceAssistantService.stateFlow: StateFlow<AssistantState>`
  - Persistent ongoing notification with interactive Stop button, notification channel configuration, audio chime feedback on success.

- [ ] **Step 1: Write unit tests for AssistantState transitions and event handling**
Test state flow from LISTENING -> WAKE_DETECTED -> TRANSCRIBING -> INFERENCING -> EXECUTING -> LISTENING or ERROR.

- [ ] **Step 2: Run tests to verify failure**
Run: `$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'; .\gradlew.bat testOfflineDebugUnitTest --tests "dev.voice.offline.service.AssistantStateMachineTest"`

- [ ] **Step 3: Implement VoiceAssistantService with StateFlow and full pipeline wiring**
Connect AudioRecordManager -> WakeWordDetector -> OfflineSpeechRecognizer -> FunctionGemmaEngine -> ActionExecutorRegistry -> SensitiveActionPolicy -> BiometricPrompt.

- [ ] **Step 4: Run tests to verify they pass**
Run: `$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'; .\gradlew.bat testOfflineDebugUnitTest --tests "dev.voice.offline.service.AssistantStateMachineTest"`
Expected: PASS

- [ ] **Step 5: Commit service and state machine**
Commit VoiceAssistantService refactor and tests.

---

### Task 8: Material 3 Jetpack Compose UI Suite

**Files:**
- Create: `VoiceOffline/app/src/main/java/dev/voice/offline/ui/theme/Color.kt`
- Create: `VoiceOffline/app/src/main/java/dev/voice/offline/ui/theme/Theme.kt`
- Create: `VoiceOffline/app/src/main/java/dev/voice/offline/ui/theme/Type.kt`
- Create: `VoiceOffline/app/src/main/java/dev/voice/offline/ui/screens/HomeScreen.kt`
- Create: `VoiceOffline/app/src/main/java/dev/voice/offline/ui/screens/OnboardingScreen.kt`
- Create: `VoiceOffline/app/src/main/java/dev/voice/offline/ui/screens/PermissionsScreen.kt`
- Create: `VoiceOffline/app/src/main/java/dev/voice/offline/ui/screens/CommandsScreen.kt`
- Create: `VoiceOffline/app/src/main/java/dev/voice/offline/ui/screens/SecurityScreen.kt`
- Create: `VoiceOffline/app/src/main/java/dev/voice/offline/ui/screens/VoiceSettingsScreen.kt`
- Create: `VoiceOffline/app/src/main/java/dev/voice/offline/ui/screens/NotesScreen.kt`
- Create: `VoiceOffline/app/src/main/java/dev/voice/offline/ui/screens/AboutScreen.kt`
- Modify: `VoiceOffline/app/src/main/java/dev/voice/offline/ui/MainActivity.kt`

**Interfaces:**
- Produces: 8 comprehensive Material 3 screens:
  1. Onboarding
  2. Home (state indicator, animated mic button, recent execution result)
  3. Permissions (granular permission management)
  4. Commands (all 13 actions with example speech phrases)
  5. Security (biometric requirement toggles per risk category)
  6. Voice Settings (sensitivity, silence duration)
  7. Local Notes (searchable encrypted notes UI)
  8. About & Privacy (air-gapped guarantees, licensing)

- [ ] **Step 1: Create Theme, Color, and Typography design tokens**
- [ ] **Step 2: Create Compose screens for Home, Onboarding, Permissions, Commands, Security, Settings, Notes, About**
- [ ] **Step 3: Update MainActivity with Jetpack Compose navigation**
- [ ] **Step 4: Run unit tests and compilation check**
Run: `$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'; .\gradlew.bat testOfflineDebugUnitTest`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit Compose UI implementation**
Commit theme, screens, and MainActivity.

---

### Task 9: Evaluation Dataset, Pro Entitlement, and Offline Verification

**Files:**
- Create: `VoiceOffline/app/src/main/java/dev/voice/offline/feature/pro/ProEntitlementManager.kt`
- Create: `VoiceOffline/app/src/test/java/dev/voice/offline/ai/evaluation/EvaluationDatasetTest.kt`
- Create: `VoiceOffline/app/src/test/resources/evaluation_dataset.json`

**Interfaces:**
- Produces:
  - `EvaluationDatasetTest`: Evaluates speech command variations (Indian English variations, contact names, ambiguous queries, malicious injection strings, unsupported actions) against FunctionGemma function selection and argument extraction.
  - `ProEntitlementManager`: One-time lifetime entitlement manager supporting local verification without cloud dependencies.

- [ ] **Step 1: Create evaluation dataset JSON and evaluation tests**
Include Indian English phonetics, noisy speech variations, and boundary cases.

- [ ] **Step 2: Run evaluation dataset tests**
Run: `$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'; .\gradlew.bat testOfflineDebugUnitTest --tests "dev.voice.offline.ai.evaluation.EvaluationDatasetTest"`
Expected: PASS

- [ ] **Step 3: Implement ProEntitlementManager**
- [ ] **Step 4: Run tests to verify all pass**
- [ ] **Step 5: Commit evaluation dataset and Pro entitlement manager**

---

### Task 10: Production Documentation & Verification Suite

**Files:**
- Update: `README.md`
- Create: `ARCHITECTURE.md`
- Create: `MODEL_SETUP.md`
- Create: `SECURITY.md`
- Create: `PERMISSIONS.md`
- Create: `TESTING.md`
- Create: `PERFORMANCE.md`
- Create: `RELEASE_CHECKLIST.md`

- [ ] **Step 1: Write comprehensive technical documentation matching all prompt guidelines**
- [ ] **Step 2: Run complete unit and instrumentation test suite**
Run: `$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'; .\gradlew.bat test`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit all documentation**
- [ ] **Step 4: Final verification and summary**
