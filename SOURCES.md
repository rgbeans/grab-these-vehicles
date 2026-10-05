# Message and image sources

Researched on 2026-10-04. This app uses the English vehicle-request prefix
`Grab these vehicles:` and the complete five-vehicle lists, rather than
inventing Simeon dialogue.

- The GTA V game-label extract verifies `CELL_CLTEST1` and the English
  manufacturer names (`IMPEX_CAR_*B`):
  https://gist.github.com/FrazzIe/d90f3c0025a7933f2a5b04faab92c645
- Eight post-August-2020 lists, including their SMS order:
  https://gta.fandom.com/wiki/Simeon%27s_Export_Requests
- Eight original lists, in the original text order:
  https://gta.fandom.com/es/wiki/Veh%C3%ADculos_para_exportar_de_Simeon
- Cross-check of original and updated list memberships:
  https://www.gtabase.com/gta-online/jobs/simeon-s-export-requests
- Seven original messages are also reproduced here. This transcription
  omits the Fusilade/Gresley/Buccaneer/Daemon/Bagger list and misspells
  Landstalker, so those were checked against the lists and game labels above:
  https://dev.to/brodan/grab-these-vehicles-send-real-simeon-texts-using-twilio-functions-3igo
- Simeon portrait used in the launcher icon, app and notifications:
  https://gta5wiki.com/characters/supporting/simeon-yetarian/
  https://gta5wiki.com/images/Simeon-GTA-V.webp

The updated Banshee/Coquette/Sentinel/Dubsta/Infernus list and the
Infernus/Coquette/Banshee/Dubsta/Sentinel list contain the same vehicles in
different SMS orders, so both are retained. There are 16 distinct message
texts across the two eras, not 16 distinct sets of vehicles.

GTA V, Simeon, vehicle names and game imagery belong to Rockstar Games /
Take-Two Interactive. This is an unaffiliated fan app. Publishing this repository does not grant
a license to third-party game text, imagery, or audio.

## Version 1.1 notification sound

Requested reference: https://youtu.be/9O7iD9VmZdI
(`GTA V / Sound notification`, approximately two seconds).

The video page and metadata were accessible, but its audio media endpoint
returned an unavailable-page response. The bundled GTA notification sound
was obtained from this public downloadable copy instead:
https://www.myinstants.com/en/instant/gta-v-notification-96319/
https://www.myinstants.com/media/sounds/gta-v-notification.mp3

The downloaded MP3 was decoded to 1.497688 seconds of audio and converted
to Ogg Vorbis without pitch or speed changes. A sample-for-sample match to
the linked YouTube upload could not be verified.

Android implementation references:

- https://developer.android.com/develop/background-work/services/alarms
- https://developer.android.com/about/versions/14/changes/schedule-exact-alarms
- https://developer.android.com/develop/ui/views/notifications/notification-permission
- https://developer.android.com/develop/ui/views/touch-and-input/copy-paste
- https://developer.android.com/reference/android/app/NotificationChannel
- https://developer.android.com/guide/topics/providers/content-provider-creating
- https://developer.android.com/reference/android/content/ContentProvider


## Version 1.3 reliability and publication references

- Elapsed-time alarms and idle behavior:
  https://developer.android.com/develop/background-work/services/alarms
- Persisted periodic recovery job:
  https://developer.android.com/reference/android/app/job/JobInfo.Builder
- Unused-app restrictions:
  https://developer.android.com/topic/performance/app-hibernation
- Android 16 build requirements:
  https://developer.android.com/about/versions/16/setup-sdk
- Android notification channel settings:
  https://developer.android.com/reference/android/provider/Settings#ACTION_CHANNEL_NOTIFICATION_SETTINGS
- Robolectric JDK module configuration:
  https://robolectric.org/getting-started/
- Google Play policies and publisher checklist:
  see `play-release/submission.md`.

## v1.4 contact messages

Retrieved 2026-10-05. The added contacts only use purchase reminders and heist-ready notices. Simeon's existing vehicle lists remain available. Messages are simulated offline, with no live game/account connection.

| Contact | Origin | Included text |
| --- | --- | --- |
| Warstock | [GTA Wiki: Warstock Cache & Carry](https://gta.fandom.com/wiki/Warstock_Cache_%26_Carry), `WSTCKMAIL3` | Vehicle-service purchase promotion; formatting controls removed. |
| Paige Harris | [GTA Wiki: Terrorbyte](https://gta.fandom.com/wiki/Terrorbyte) | Exact short sentence from her Terrorbyte purchase reminder call. Presented as a notification, rather than claiming it was originally SMS. |
| Lester | [GTA V American Labels](https://gist.github.com/aaronlink127/afc889be7d52146a76bab72ede0512c7), `HPLESTER_TXT_R` | Exact Facility Planning Screen reminder for continuing the Doomsday Heist. |
| Prix Luxury Real Estate | [GTA Wiki: Mansions](https://gta.fandom.com/wiki/Mansions) | Opening 25-word excerpt from its mansion promotion email, with an ellipsis. Identified as an excerpt in the contact list. |

Generic vehicle-stock announcements and invented dialogue are excluded. New contacts use letter avatars; Simeon's existing portrait is retained.

Each contact has a stable notification ID and separate copy PendingIntent. Reposting the same ID replaces that contact's old message, with `onlyAlertOnce=false`, so Android can alert again according to the existing channel sound and the user's sound/DND settings. Other contacts' notifications stay visible.
