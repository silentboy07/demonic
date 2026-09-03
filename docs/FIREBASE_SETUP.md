# Firebase Setup Guide — DEMONIC

Follow these steps to configure your own Firebase backend for DEMONIC.

---

## 1. Create a Firebase Project
1. Navigate to the [Firebase Console](https://console.firebase.google.com).
2. Click **Create a project** and name it (e.g., `demonic-app`).
3. You can disable or enable Google Analytics according to your preference.

---

## 2. Register Android Application
1. Click the **Android** icon to add an app.
2. Enter the exact package name:
   ```
   com.nddfeon.demonic
   ```
3. Enter App nickname (e.g., `DEMONIC`).
4. Add the **Debug signing certificate SHA-1**:
   ```
   18:2E:65:37:36:B6:81:1E:58:A1:A4:B5:E2:B5:87:CE:03:90:DF:6C
   ```
   *(Also add SHA-256 for optimal Google Sign-In reliability:)*
   ```
   18:4A:35:C4:1A:81:6C:F5:68:35:10:92:FA:45:75:1C:F0:AE:59:0F:29:25:98:4F:0F:A3:4C:7C:CD:B3:91:7C
   ```
5. Click **Register app**.
6. Download the `google-services.json` file.
7. Place it inside the `app/` folder of the project, replacing the existing template `app/google-services.json`.

---

## 3. Enable Authentication (Google Sign-In)
1. In the Firebase console sidebar, go to **Build** > **Authentication**.
2. Click **Get Started**.
3. Under the **Sign-in method** tab, select **Google**.
4. Toggle **Enable**.
5. Select your project support email and click **Save**.

---

## 4. Enable Realtime Database
1. In the sidebar, go to **Build** > **Realtime Database**.
2. Click **Create Database**.
3. Choose your nearest database location (e.g., United States or Singapore).
4. Start in **test mode** or set the security rules as follows:

```json
{
  "rules": {
    "rooms": {
      ".read": true,
      ".write": true
    }
  }
}
```
5. Click **Publish**.
