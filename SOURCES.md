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
are excluded. Phone icons are documented below.

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

## Phone icons (v1.5.1)

Original 64×64 GTA phone/notification textures from the
[game texture catalog](https://github.com/SwitchNetwork/fivem-wiki/wiki/Advanced-Notifications),
cross-checked against the [RAGE notification texture list](https://wiki.rage.mp/wiki/Notification_Pictures)
and English phone picture labels. The complete per-contact asset names and
pinned source URLs are recorded in [contact-icons.json](play-release/contact-icons.json).
Assets are decoded and saved losslessly as PNG; no portraits or logos are generated.

Lester uses CHAR_LESTER (the game's silhouette), not a character publicity
portrait. Franklin and Lamar use their newer CHAR_FIXFRANKLIN / CHAR_FIXLAMAR
phone assets. Maude retains her original bounty-hunting-era CHAR_MAUDE icon.
Company assets include CHAR_MILSITE (Warstock), CHAR_BANK_MAZE (Maze Bank),
CHAR_MP_MORS_MUTUAL, CHAR_PEGASUS_DELIVERY and CHAR_GANGAPP (SecuroServ).
Junk Energy uses CHAR_JUNK_JUMP.

No phone-contact image was verified for Prix Luxury's email sender. It uses no
large notification avatar and no contact-row image rather than an unrelated
website logo or a fabricated portrait. The catalog contains 19 verified assets
for 20 message senders. Artwork belongs to Rockstar Games / Take-Two.

## Launcher icon picker and collapsed contacts (v1.6)

Contacts start collapsed and show an enabled count. Expansion and contact
selections persist independently. The icon picker contains all 19 verified
assets; Prix Luxury is omitted because it has no verified picture.

Launcher icons use Android [activity aliases](https://developer.android.com/guide/topics/manifest/activity-alias-element).
The target MainActivity always remains enabled. Android 13+ changes aliases
atomically using [setComponentEnabledSettings](https://developer.android.com/reference/android/content/pm/PackageManager#setComponentEnabledSettings(java.util.List));
older devices enable the replacement before disabling the previous aliases.
The chosen icon changes the home-screen entry. Android/OEM notification-header
branding may continue using the application icon and is not promised to change.
Per-sender notification pictures remain independent of the launcher choice.

## Launcher framing hotfix (v1.6.1)

Every selectable launcher alias now uses an adaptive mipmap icon instead of
a legacy bitmap. This prevents launchers from shrinking the picture inside
a synthetic white background. A full image foreground fills the masked
viewport while the same picture supplies the background bleed area.
The original notification pictures are unchanged. Alias names stay stable
so a saved selection survives an update.


## Voice recordings (v1.7.1)

All nine v1.7 recordings were removed. The replacement catalog uses genuine GTA Online introductory, business-offer, service-introduction, mission-invitation, and work-reminder telephone dialogue. No in-person character quotes, post-heist congratulations, or raid warnings are used.

Recordings are captured original game audio from the linked gameplay videos. Leading ringing and adjacent calls are trimmed; the voice itself is not synthesized, retimed, or impersonated. Audio is encoded as mono 32 kHz Vorbis. Some quiet gameplay ambience may remain. The 2019 reminder compilation includes historical and platform-specific call variants; inclusion here does not imply the same call still triggers in every current game build. Original game audio belongs to Rockstar Games / Take-Two.

The machine-readable [call catalog](app/src/main/assets/call_catalog.json) records caller IDs, resources, source segments, contexts, and SHA-256 hashes. Each recording is mapped to its actual caller. Corporate service calls use their company name; the Executive Assistants, Dom, and Raf are separate callers. Warstock, Prix Luxury, Maze Bank Foreclosures, Miguel, SecuroServ, and Junk Energy have no verified matching bundled call in this catalog. Miguel’s Music Locker invitation is a text; his in-person introduction was not substituted for a telephone call.

| Caller | Topic | Source segment (seconds) | Context reference |
| --- | --- | --- | --- |
| agent14 | Unsolicited Maze Bank Foreclosures bunker / underground operation pitch | [Video](https://www.youtube.com/watch?v=isj6RYg9phs) 3.79–20.85 | [Context](https://gta.fandom.com/wiki/Bunkers) |
| assistant_female | Female Executive Assistant pitches sourcing/reselling high-value vehicles and available garage space on Dynasty 8, ending with an invitation to the office. Full single call; ASR confirms greeting and complete ending. Caller is the player’s assistant, not SecuroServ. | [Video](https://www.youtube.com/watch?v=HsoJt4WZJz0) 1162.5–1179.6 | [Context](https://gta.fandom.com/wiki/Executive_Assistant) |
| assistant_male | Male Executive Assistant introduces himself, pitches the Dynasty 8 office garage/vehicle-cargo expansion, and invites the player to the office. Full single call; ASR confirms greeting and complete ending. The caller is the player’s assistant, not SecuroServ. | [Video](https://www.youtube.com/watch?v=HsoJt4WZJz0) 1111.97–1127.6 | [Context](https://gta.fandom.com/wiki/Executive_Assistant) |
| brucie | Brucie’s phone introduction to Bull Shark Testosterone delivery after the player orders from the in-game site. One complete service pitch; ASR confirms process/delivery and call-me offer. | [Video](https://www.youtube.com/watch?v=N4Ok08g4MrI) 0–17.1 | [Context](https://gta.fandom.com/wiki/Brucie_Kibbutz) |
| bryony | Unsolicited Arena War / Arena Workshop pitch | [Video](https://www.youtube.com/watch?v=HsoJt4WZJz0) 10.559–39.91 | [Context](https://gta.fandom.com/wiki/Arena_Workshop) |
| dom | Dom says he nailed a jump, invites the player to check it out, and says he will send the details. Full single skydiving/parachuting invite; ASR confirms greeting and ending with no adjacent call. Legacy contact/job dialogue. | [Video](https://www.youtube.com/watch?v=HsoJt4WZJz0) 1599.91–1605.41 | [Context](https://gta.fandom.com/wiki/Dom_Beasley) |
| englishdave | English Dave suggests booking an available DJ through the Nightclub computer. The clip starts with his greeting and contains one complete pitch; this is a low-popularity business reminder, not a Cayo mission call. | [Video](https://www.youtube.com/watch?v=HsoJt4WZJz0) 116.5–127.6 | [Context](https://gta.fandom.com/wiki/English_Dave) |
| franklin | Cut from the original whole bestaudio track, not a YouTube section download. It includes Franklin's complete pre-mission call from its opening greeting through the sign-off. Whisper garbles the first second, but the wiki transcript gives the opening as 'Ey, partner, what's up. Listen, I got some news for us.' The remaining pitch and golf-club invitation are audible and align with the transcript. | [Video](https://www.youtube.com/watch?v=v79XoNe1Tbg) 0–29.2 | [Context](https://gta.fandom.com/wiki/On_Course/Transcript) |
| gerald | Contact-job introduction and offer | [Video](https://www.youtube.com/watch?v=HsoJt4WZJz0) 1495.9–1506 | [Context](https://gta.fandom.com/wiki/Gerald) |
| gerald | Contact-job availability reminder | [Video](https://www.youtube.com/watch?v=HsoJt4WZJz0) 1515.6–1520.85 | [Context](https://gta.fandom.com/wiki/Gerald) |
| kdj | First unsolicited Moodymann phone call requesting retrieval of his house shoes from Davis Mega Mall | [Video](https://www.youtube.com/watch?v=IlIqRsggLdA) 25.519–50.65 | [Context](https://gta.fandom.com/wiki/DJ_Requests) |
| lamar | Agency purchase and partnership offer | [Video](https://www.youtube.com/watch?v=tHwVMyos4F0) 3–28.7 | [Context](https://gta.fandom.com/wiki/Agencies) |
| lamar | Invitation to Benny's for Lowriders work | [Video](https://www.youtube.com/watch?v=jZuWY7M_LZc) 11.65–24.65 | [Context](https://gta.fandom.com/wiki/Lamar%27s_Lowrider_Missions) |
| lester | Doomsday Heist job invitation and ex-government facility purchase pitch | [Video](https://www.youtube.com/watch?v=HsoJt4WZJz0) 635.6–658.6 | [Context](https://gta.fandom.com/wiki/Facilities) |
| martin | Martin calls the player, introduces himself and an influential development contact, then asks the player to meet at LSIA. One continuous mission-intro call; ASR confirms full invitation. | [Video](https://www.youtube.com/watch?v=e80jAA4fJsk) 8–37.3 | [Context](https://gta.fandom.com/wiki/Money_Fronts) |
| maude | Incoming Maude call proposing the player open and run another Bail Enforcement office. She explains the opportunity, asks the player to choose a property, and ends the call after the business pitch. | [Video](https://www.youtube.com/watch?v=M7cr7X0NNus) 6.8–62.8 | [Context](https://gta.fandom.com/wiki/Bottom_Dollar_Bounties) |
| mechanic | Personal-vehicle delivery service introduction | [Video](https://www.youtube.com/watch?v=HsoJt4WZJz0) 1213.4–1219.15 | [Context](https://gta.fandom.com/wiki/Mechanic) |
| merryweather | Merryweather private-army service introductory offer | [Video](https://www.youtube.com/watch?v=HsoJt4WZJz0) 1340.65–1347.15 | [Context](https://gta.fandom.com/wiki/Merryweather_Security) |
| mors | Mors Mutual full-coverage insurance service offer | [Video](https://www.youtube.com/watch?v=HsoJt4WZJz0) 1242.6–1256.65 | [Context](https://gta.fandom.com/wiki/Mors_Mutual_Insurance) |
| paige | Paige introduces the Terrorbyte score sideline, names Warstock, and pitches buying/storing the truck under the nightclub. Full single phone offer; ASR confirms greeting and complete offer, no adjacent call. Legacy call branch. | [Video](https://www.youtube.com/watch?v=HsoJt4WZJz0) 467.75–491.15 | [Context](https://gta.fandom.com/wiki/Terrorbyte) |
| paige | Alternate Paige call variant: identifies her work with Lester, pitches the Terrorbyte on Warstock as a nerve center for scores, and asks the player to buy it. ASR confirms a full single call, no adjacent call. Legacy call branch. | [Video](https://www.youtube.com/watch?v=HsoJt4WZJz0) 492.07–513.87 | [Context](https://gta.fandom.com/wiki/Terrorbyte) |
| paige | Alternate Paige pitch for a nightclub owner: put the Terrorbyte in the underground garage, buy it on Warstock, and take scores. ASR confirms complete pitch and ending, no neighboring caller; opening wording partly garbled by ASR, captions identify Paige. Legacy call branch. | [Video](https://www.youtube.com/watch?v=HsoJt4WZJz0) 515.03–532.31 | [Context](https://gta.fandom.com/wiki/Terrorbyte) |
| pavel | Pavel's incoming business pitch: Darnell Brothers Factory is for sale, it would make a covert-operations base, and Pavel asks the player to purchase it. Full line ends at about 34.1 s. | [Video](https://www.youtube.com/watch?v=yRKXraeqxuk) 2.9–34.6 | [Context](https://gta.fandom.com/wiki/Pavel) |
| pegasus | Pegasus specialty-vehicle purchase and delivery introduction | [Video](https://www.youtube.com/watch?v=HsoJt4WZJz0) 1304.5–1316.8 | [Context](https://gta.fandom.com/wiki/Pegasus_Lifestyle_Management) |
| raf | Raf proposes helping Mr. Faber’s luxury-property development clients, says no monetary investment is needed, and asks whether the player wants in. Full continuous work offer; ASR confirms. Raf is the caller; this is Prix Luxury work, not a company voice. | [Video](https://www.youtube.com/watch?v=ULoKgcw_HSk) 0.3–32.14 | [Context](https://gta.fandom.com/wiki/New_Listings) |
| ron | Unsolicited Maze Bank Foreclosures hangar / air freight business pitch | [Video](https://www.youtube.com/watch?v=udtAzqEMD_w) 2.53–37.78 | [Context](https://gta.fandom.com/wiki/Hangars) |
| simeon | Export car-list work reminder | [Video](https://www.youtube.com/watch?v=HsoJt4WZJz0) 1476.75–1482.2 | [Context](https://gta.fandom.com/wiki/Simeon%27s_Export_Requests) |
| simeon | Export car-list work reminder | [Video](https://www.youtube.com/watch?v=HsoJt4WZJz0) 1482.45–1488.55 | [Context](https://gta.fandom.com/wiki/Simeon%27s_Export_Requests) |
| simeon | Export car-list work reminder | [Video](https://www.youtube.com/watch?v=HsoJt4WZJz0) 1488.85–1495.35 | [Context](https://gta.fandom.com/wiki/Simeon%27s_Export_Requests) |
| simeon | Contact-job offer | [Video](https://www.youtube.com/watch?v=HsoJt4WZJz0) 1452.45–1457.75 | [Context](https://gta.fandom.com/wiki/Simeon_Yetarian) |
| tony | Nightclub purchase and business offer | [Video](https://www.youtube.com/watch?v=PIlfaM6gOaA) 0.85–37.65 | [Context](https://gta.fandom.com/wiki/Nightclubs) |

The bundled `gta_ringtone.mp3` is the GTA Online phone ringtone supplied by the user. Imports stay in private app storage and are not exposed through the public sound provider. No microphone permission or generated voices are used.


## Native phone calls (v1.7.2)

Calls use Android's managed [ConnectionService](https://developer.android.com/reference/android/telecom/ConnectionService) and the existing default Phone app, following the [Telecom framework](https://developer.android.com/develop/connectivity/telecom). Voice playback uses [AudioAttributes.USAGE_VOICE_COMMUNICATION](https://developer.android.com/reference/android/media/AudioAttributes), with Telecom managing audio mode and routes. Native portraits and the custom ringtone use separate on-device GTA caller contacts. Call addresses are fictional +1-202-555-0100 through +1-202-555-0129, from the [NANPA reserved entertainment range](https://nanpa.com/numbering/555-line-numbers); they are app identities, not GTA's original phone numbers. The GTA account rejects outgoing calls and is excluded from ordinary telephone outgoing-account choices.

Nine additional original 64-pixel game phone textures cover Paige, Mechanic, Dom, Brucie, English Dave, Martin, Merryweather, and both Executive Assistants. Pinned source URLs and texture names are in [call-icons.json](play-release/call-icons.json), using SwitchNetwork/fivem-wiki commit `5e14fa9b8bb3dd661d905b4a14c01c689a5b3b6a`. Raf has no verified portrait in that pinned catalog and retains the Phone app's placeholder.
