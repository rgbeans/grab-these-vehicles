# Google Play submission packet — version 1.3

Status: prepared locally; not uploaded or published.

The release packet contains a signed Android App Bundle, English listing copy,
release notes, an offline privacy-policy draft, and native renders of the app
views, launcher icon and feature graphic. Check the screenshots against your
phone before submitting them. They are rendered from the real Android view
hierarchy rather than captured on a physical device.

## Release identity

- Package: `com.grabthesevehicles.app`
- Version name: `1.3`
- Version code: `4`
- Target SDK: Android 16 / API 36
- Minimum SDK: Android 8 / API 26
- No runtime libraries, ads, account system, billing, or internet permission.

## Still required from the publisher

1. Access to a verified Google Play developer account, with the account's
   required agreements and verification completed by its owner.
2. A public developer contact email and the intended target audience.
3. Review and document the legal basis for distributing the Rockstar portrait,
   game text and audio in the app and screenshots, such as permission or an
   applicable copyright exception. The publisher states that the use is
   nonprofit and transformative, and that the audio is approximately 1.5
   seconds. This is the publisher's fair-use position, not a verified legal
   determination. Short duration and nonprofit use alone do not establish
   fair use, and the portrait and text need their own analysis. Google Play
   may request evidence supporting the use of third-party content.
4. Add the publisher's contact details to `privacy-policy.html`, then host it at
   a public, working HTTPS URL and enter that URL in Play Console. The draft is
   not yet a hosted privacy policy.
5. Complete Play Console's app-access, ads, target-audience, content-rating,
   Data safety, and other applicable app-content declarations.
6. Configure Play App Signing. The local APK and bundle are signed with the
   existing dedicated personal key. A Google-generated app-signing key would
   differ from the currently installed APK's key. Import the existing signing
   key if Play-delivered builds must update that installation in place, or
   plan a one-time reinstall. Keep the private release signing key and any private source bundle
   containing it private. This public repository does not contain that key.
7. Start an internal test and check actual scheduled delivery, background
   recovery, clipboard actions, settings navigation and sound on devices.
8. For personal developer accounts created after November 13, 2023, complete
   a closed test with at least 12 opted-in testers continuously for 14 days,
   then apply for production access. This requirement is conditional on the
   account type and creation date.
9. Submit the app for Google's review; only an approved rollout makes it live.

## Data safety draft, based on this build

- User data collected or shared by the app: No.
- Accounts: None; no account-deletion flow is applicable.
- Ads: No.
- Permissions: notifications, reboot restoration, and optional exact alarms.
- Local storage: intervals, enabled state, upcoming alarm deadlines, latest
  request, and shuffled message rotation.
- Clipboard: only a bundled message chosen by the user is written to Android's
  clipboard. The app never reads the existing clipboard or transmits its contents.
- The public provider exposes one read-only bundled audio clip only.
- No network code, webviews, tracking SDKs, or cloud data processing.

The publisher remains responsible for submitting accurate declarations. The
intended audience and content rating cannot be inferred solely from GTA V's rating.

## Official references checked October 4, 2026

- Target API: https://support.google.com/googleplay/android-developer/answer/11926878
- App setup: https://support.google.com/googleplay/android-developer/answer/9859152
- Testing: https://support.google.com/googleplay/android-developer/answer/14151465
- Intellectual property: https://support.google.com/googleplay/android-developer/answer/9888072
- Data safety and privacy policy: https://support.google.com/googleplay/android-developer/answer/10787469
- Fair use (United States): https://www.copyright.gov/fair-use/

## Publication status

Google Play publication is pending. The signed version 1.3 APK is available
in the repository download folder. This repository contains no release signing
key. Complete the publisher requirements above before submitting to Play.
