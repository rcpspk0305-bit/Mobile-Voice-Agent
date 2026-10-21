# Permissions & Background Execution Audit — Voice

## 1. Overview of Permissions

Voice strictly requests only permissions required to execute the user's explicit voice commands. All permissions are audited, explained to the user before request, and handled with graceful degradation.

---

## 2. Permissions Matrix

| Permission | Category | Purpose | Fallback if Denied |
| :--- | :--- | :--- | :--- |
| `android.permission.RECORD_AUDIO` | **CORE (Mandatory)** | Continuous 16 kHz Mono PCM audio recording for local wake word and speech recognition. | Service cannot start; user prompted with explanation dialog. |
| `android.permission.FOREGROUND_SERVICE` | **CORE (Mandatory)** | Keeps assistant listening active in foreground with user visibility. | Cannot run continuous listening in background. |
| `android.permission.FOREGROUND_SERVICE_MICROPHONE` | **CORE (Android 14+)** | Specifies microphone foreground service type as required by Android 14 (API 34)+. | Service start throws SecurityException. |
| `android.permission.POST_NOTIFICATIONS` | **CORE (Android 13+)** | Shows ongoing listening status notification and biometric authorization prompts. | Biometric approval prompts cannot be delivered interactively. |
| `android.permission.READ_CONTACTS` | **OPTIONAL** | Resolves contact names for "Call [Name]" and "Message [Name]". | Calls and messages must specify raw numeric phone numbers. |
| `android.permission.CALL_PHONE` | **OPTIONAL** | Initiates direct voice calls when user commands "Call Mom". | Dispatches ACTION_DIAL intent to dialer without placing call automatically. |
| `android.permission.ANSWER_PHONE_CALLS` | **OPTIONAL** | Answers incoming phone calls when user commands "Answer call". | Command reports unsupported / permission denied. |
| `android.permission.CAMERA` | **OPTIONAL** | Opens camera and controls LED torch for "Turn on flashlight". | Flashlight and camera commands fail with descriptive error. |
| `android.permission.SCHEDULE_EXACT_ALARM` | **OPTIONAL** | Sets alarms for exact requested times without power manager delay. | Alarms use inexact scheduling. |
| `android.permission.USE_BIOMETRIC` | **SECURITY** | Authenticates high-risk actions via fingerprint or face unlock. | High-risk actions fail closed. |
| `BIND_DEVICE_ADMIN` | **OPTIONAL (Special)** | Locks screen securely upon user command "Lock screen". | Screen lock command reports permission required. |
| `BIND_NOTIFICATION_LISTENER_SERVICE` | **OPTIONAL (Special)** | Reads notifications and sends inline replies via RemoteInput. | Notification actions unavailable. |

---

## 3. Background Execution & Android 14/15/16 Compliance

### 3.1 Foreground Service Microphone Rules (API 34+)
Android 14 introduced strict policies preventing background services from starting microphone capture without foreground visibility:
- **Rule 1**: The service is declared in `AndroidManifest.xml` with:
  ```xml
  <service
      android:name=".VoiceAssistantService"
      android:exported="false"
      android:foregroundServiceType="microphone" />
  ```
- **Rule 2**: `VoiceAssistantService` is started only from a visible, focused `Activity` (e.g. `MainActivity`), never from `BOOT_COMPLETED` broadcast receiver or background alarms.
- **Rule 3**: `startForeground()` is called within 5 seconds of service start, attaching an ongoing `PRIORITY_LOW` notification with an interactive **Stop** action.

### 3.2 Background Activity Launch Restrictions
Android restricts background services from launching activities directly (e.g. starting a biometric prompt while the phone is in a pocket or on home screen):
- **Compliance Pattern**:
  1. High-risk command received $\rightarrow$ Stored in `PendingCommandStore` (30s TTL).
  2. Service posts high-priority notification with `PendingIntent` targeting `AuthorizationActivity`.
  3. The **user taps the notification** to launch `AuthorizationActivity` in the foreground.
  4. Once authenticated, the action is dispatched and the activity closes gracefully.

---

## 4. Permissions Disclosure Flow

All permissions are exposed through the dedicated **Permissions Screen** (`PermissionsView`) in the app:
- Real-time status indicators (Granted / Missing).
- Human-readable explanation of why each permission is needed.
- One-tap buttons to trigger standard runtime permission dialogs or deep-link to device settings.
- Zero dark patterns: Denied optional permissions never lock out core functionality.
