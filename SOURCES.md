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

Retrieved 2026-10-05. The catalog is restricted to messages originally delivered
to the GTA Online phone by text or email. Call dialogue is excluded, including
Paige's Terrorbyte pitch. Terrorbyte purchase promotion remains in Warstock's
actual email. Simeon's existing vehicle requests remain available.

| Contact | Delivery | Source / game label | Included message |
| --- | --- | --- | --- |
| Warstock Cache & Carry | Email | [Warstock](https://gta.fandom.com/wiki/Warstock_Cache_%26_Carry), WSTCKMAIL3 | Complete short purchase promotion. |
| Pavel | Text | [Kosatka](https://gta.fandom.com/wiki/Kosatka), HIF_BUYSUB_0 | Exact closing excerpt inviting purchase of the submarine. |
| Lester | Text | [American Labels](https://gist.github.com/aaronlink127/afc889be7d52146a76bab72ede0512c7), HPLESTER_TXT_R | Complete Doomsday Facility Planning Screen reminder. |
| Lester | Text | [Arcades](https://gta.fandom.com/wiki/Arcades), CH_TXT_2 | Exact final sentence of the arcade purchase reminder. |
| Lester | Text | [Arcade equipment setup](https://gta.fandom.com/wiki/Arcades/Setup%3A_Equipment), CH_TXT_5 | Complete Casino planning invitation after arcade setup; not a claim that all finale preparations are complete. |
| Prix Luxury Real Estate | Email | [Mansions](https://gta.fandom.com/wiki/Mansions) | Exact opening 25-word excerpt of its property promotion. |
| Tony | Text | [Nightclubs](https://gta.fandom.com/wiki/Nightclubs), FMBB_TXT_0 | Complete nightclub purchase reminder, including Txx. |
| KDJ | Text | [Auto Shops](https://gta.fandom.com/wiki/Auto_Shops) | Complete Auto Shop purchase invitation. |
| Agent 14 | Text | [Mobile Operations](https://gta.fandom.com/wiki/Mobile_Operations) | Exact first two sentences of the MOC purchase reminder. |
| Bryony | Text | [Arena Workshop](https://gta.fandom.com/wiki/Arena_Workshop) | Exact workshop purchase sentence sent after competing without a workshop. |
| Ron | Text | [English labels](https://gist.github.com/FrazzIe/d90f3c0025a7933f2a5b04faab92c645), SM_FLOW_TXT_1 | Complete hangar purchase reminder. |
| Maze Bank Foreclosures | Email | [Maze Bank Foreclosures](https://gta.fandom.com/wiki/Maze_Bank_Foreclosures), MBANKMAIL | Exact opening sentence of the property offer. |
| Miguel Madrazo | Text | [Cayo Perico Heist](https://gta.fandom.com/wiki/The_Cayo_Perico_Heist), HIF_INTRO_TXT | Exact Music Locker meeting invitation excerpt. |
| Maude | Text | [Bounty Target](https://gta.fandom.com/wiki/Bounty_Target), BONET_TEXT1 | Exact introductory two-sentence bounty-hunting offer excerpt. |

Descriptions identify text/email origins and short excerpts. Formatting control
codes are removed; the quoted spelling and punctuation are retained. Longer
dialogue is not rewritten or passed off as a complete original message.
Unverified intros, generic vehicle stock announcements, and call-only dialogue
are excluded. New contacts use letter avatars; Simeon's portrait remains.

Each contact has its own toggle, notification ID, message shuffle bag, and copy
PendingIntent. The same contact may send consecutively. Its replacement uses
onlyAlertOnce=false, allowing Android to play the existing channel sound again;
other contacts' notifications remain visible. Android notification volume and
Do Not Disturb still apply. Messages are simulated offline and do not track
property ownership or actual heist progress.

## Additional phone introductions (v1.5)

| Contact | Game label / source | Scope |
| --- | --- | --- |
| Mors Mutual Insurance | MORS_TXT_MSG, [English game labels](https://gist.github.com/aaronlink127/afc889be7d52146a76bab72ede0512c7) | Exact opening excerpt of the insurance introduction after a vehicle is destroyed. |
| Pegasus | TXT_VEH_BASEPEG, [Labels List](https://pdfcoffee.com/labels-list-pdf-free.html) | Complete availability text following a vehicle purchase; not a pre-purchase offer. |
| SecuroServ | GB_1M_TXT2, [Organizations](https://gta.fandom.com/wiki/Organizations) | Exact opening sentence and separate office-purchase excerpt; ellipsis marks truncation. |
| Franklin | FIX_FLOW_TXT8, [Record A Studios](https://gta.fandom.com/wiki/Record_A_Studios) | Exact closing studio invitation after the Dre contract; not a claim that the Dre finale is ready. |
| Lamar | ORGANI_TEXT, [LD Organics Product](https://gta.fandom.com/wiki/LD_Organics_Product) | Exact opening two sentences, sent after the first package is found. |
| Gerald | DSH_TXT_NEAR, [Stash Houses](https://gta.fandom.com/wiki/Stash_Houses) | Exact opening two sentences of the stash-house introduction. |
| Gerald | DEADDROPTXT0, [G’s Caches](https://gta.fandom.com/wiki/G%27s_Caches) | Exact opening two sentences; English game labels preserve “over”, correcting the wiki transcription typo. |
| Junk Energy | SKYDIVEMAIL_TXT, [Junk Energy Skydives](https://gta.fandom.com/wiki/Junk_Energy_Skydives) | Exact invitation sentence from the phone message. |

Dom Beasley’s GTA Online parachuting introduction is a call and is excluded.
Story Mode texts are not imported. Brucie’s call-only BST pitch is also excluded.
The added service and collectible introductions can occur after an initial
purchase or encounter; they are not all messages to players who own nothing.
All original contact indices stay unchanged so existing preferences and
notification IDs survive an update. New contacts are appended.
