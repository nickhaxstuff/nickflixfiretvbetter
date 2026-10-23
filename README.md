# NickFlix Fire TV Stick Lite app

This project wraps your live NickFlix website in a fullscreen Android WebView app for your Fire TV Stick Lite running Fire OS.

**Preconfigured site:** https://nickflix.netlify.app/

The wrapper hosts the existing site inside Android WebView (not a separate browser app). It enables JavaScript, DOM storage, third-party cookies, autoplay where the provider permits it, and fullscreen custom views. It also adds visible keyboard focus and D-pad focus-navigation fallback for the host page. Arrow/Select events are intentionally not intercepted while an iframe or media control is focused, so the embedded player can receive its own remote input. Profiles, saved progress, browser storage, Firebase sync, QR sync, and the music player remain website features and depend on the live site and its existing configuration. This app does not add a new pairing backend or change website data.

## Build the APK (Windows PC; no Android Studio needed)
1. Create a GitHub repository (private is fine).
2. Upload the contents of this folder to the repository. Make sure `.github/workflows/build-apk.yml` is included.
3. Open the repository's **Actions** tab and enable workflows if GitHub asks.
4. Select **Build NickFlix Fire TV APK** → **Run workflow**. The site URL is prefilled as `https://nickflix.netlify.app/`; leave it as-is.
5. Wait for the workflow to finish successfully. Open that run and download the `nickflix-debug-apk` artifact.
6. Extract the downloaded artifact ZIP. It contains `app-debug.apk`.

This creates a debug-signed APK for personal use; it is not an Amazon Appstore release build.

## Install it on the Fire TV Stick Lite
1. On the Fire TV, open **Settings → My Fire TV → About** and confirm it says Fire OS.
2. Go to **Settings → My Fire TV → Developer Options**. If Developer Options is missing, select the device name under **About** and press Select seven times.
3. Enable **ADB Debugging** and **Install Unknown Apps** for whichever installer app you use (for example, Downloader). Amazon's official ADB guide: https://developer.amazon.com/docs/fire-tv/connecting-adb-to-device.html
4. Transfer the APK to the Stick using Downloader from a direct HTTPS download link, or install it from your Windows PC over ADB. GitHub Actions' artifact download is easiest on the PC; artifacts are not usually direct APK links suitable for Downloader.
5. Launch **NickFlix** from Apps and test remote navigation, playback, and saved state.

## Install from Windows over Wi-Fi (optional)
- On Fire TV, enable **ADB Debugging** and note its IP address under **Settings → My Fire TV → About → Network**.
- Install Android SDK Platform-Tools on Windows.
- Connect the PC and Stick to the same Wi-Fi. In a terminal run `adb connect FIRE_TV_IP:5555`, approve the prompt on the TV, then run `adb install app-debug.apk`.
- Replace `FIRE_TV_IP` with the actual IP address shown on your TV.

## Notes
- The app opens the live hosted site, so site updates should appear without reinstalling the app.
- The website's own Firebase configuration, sync permissions, media providers, and network availability still determine whether those features work.
- WebView storage is local to this app and separate from Silk browser storage. Existing website storage on another browser will not automatically transfer into the app.
- Video providers may behave differently in WebView than in Silk or their own apps. Test playback on the actual Stick. These changes improve WebView and remote compatibility, but cannot make a provider work if it blocks WebView, requires unsupported DRM, or does not support TV navigation.
