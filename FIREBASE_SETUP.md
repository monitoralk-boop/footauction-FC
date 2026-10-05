# Firebase Setup Guide for FootAuction FC

## Steps to Enable Online Multiplayer & Authentication

### 1. Create a Firebase Project
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Add Project" → Name it "FootAuction FC"
3. Follow the wizard (you can disable Google Analytics if you want)

### 2. Register Your Android App
1. In Firebase Console, click "Add app" → Android
2. Enter package name: `com.aistudio.footauction.kxmpzq`
3. Download the `google-services.json` file
4. Replace the placeholder file at `app/google-services.json` with your downloaded file

### 3. Enable Email/Password Authentication
1. In Firebase Console → Authentication → Sign-in method
2. Enable "Email/Password" provider
3. Click Save

### 4. Enable Cloud Firestore
1. In Firebase Console → Firestore Database
2. Click "Create database"
3. Choose "Start in test mode" (for development)
4. Select your preferred region
5. Click Enable

### 5. Firestore Security Rules (for production)
Replace the default rules with:
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /rooms/{roomCode} {
      allow read, write: if request.auth != null;
      match /events/{eventId} {
        allow read, write: if request.auth != null;
      }
    }
  }
}
```

### 6. Rebuild the APK
After replacing google-services.json:
```powershell
.\build-apk.ps1
```

## Troubleshooting
- **"Firebase not configured"**: Replace the placeholder google-services.json with your real one from Firebase Console
- **"CONFIGURATION_NOT_FOUND"**: Email/Password auth not enabled in Firebase Console
- **"Permission denied"**: Firestore rules need to be set to test mode or the rules above
