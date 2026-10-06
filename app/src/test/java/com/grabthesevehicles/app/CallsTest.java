package com.grabthesevehicles.app;
import static org.junit.Assert.*;
import android.Manifest;
import android.app.*;
import android.content.Intent;
import android.os.SystemClock;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk={26,36})
public class CallsTest {
    private Application app;
    @Before public void setup() {
        app=RuntimeEnvironment.getApplication();
        RequestScheduler.prefs(app).edit().clear().commit();
        CallScheduler.prefs(app).edit().clear().commit();
        Shadows.shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS);
        Shadows.shadowOf(app).denyPermissions(Manifest.permission.READ_CONTACTS,Manifest.permission.WRITE_CONTACTS);
    }
    @Test public void callsStartOffAndRemainOffWhenNotificationTimerRuns() {
        RequestScheduler.setEnabled(app,true); CallScheduler.ensureScheduled(app);
        assertFalse(CallScheduler.enabled(app));
        assertFalse(CallScheduler.prefs(app).contains("next_elapsed"));
        assertEquals(1,Shadows.shadowOf(app.getSystemService(AlarmManager.class)).getScheduledAlarms().size());
    }
    @Test public void timersAndPauseControlsAreIndependent() {
        RequestScheduler.setEnabled(app,true);
        long notification=RequestScheduler.prefs(app).getLong("next_elapsed",0);
        CallScheduler.prefs(app).edit().putInt("min",90).putInt("max",90).commit();
        CallScheduler.setEnabled(app,true);
        assertEquals(5_400_000,CallScheduler.prefs(app).getLong("next_elapsed",0)-SystemClock.elapsedRealtime());
        assertEquals(notification,RequestScheduler.prefs(app).getLong("next_elapsed",0));
        assertEquals(2,Shadows.shadowOf(app.getSystemService(AlarmManager.class)).getScheduledAlarms().size());
        CallScheduler.setEnabled(app,false);
        assertTrue(RequestScheduler.enabled(app));
        assertEquals(notification,RequestScheduler.prefs(app).getLong("next_elapsed",0));
        assertEquals(1,Shadows.shadowOf(app.getSystemService(AlarmManager.class)).getScheduledAlarms().size());
    }
    @Test public void reopeningAndRecoveryPreserveBothDeadlines() {
        RequestScheduler.setEnabled(app,true); CallScheduler.setEnabled(app,true);
        long notification=RequestScheduler.prefs(app).getLong("next_elapsed",0),call=CallScheduler.prefs(app).getLong("next_elapsed",0);
        new RestoreReceiver().onReceive(app,new Intent(Intent.ACTION_TIME_CHANGED));
        assertEquals(notification,RequestScheduler.prefs(app).getLong("next_elapsed",0));
        assertEquals(call,CallScheduler.prefs(app).getLong("next_elapsed",0));
        assertNotNull(app.getSystemService(android.app.job.JobScheduler.class).getPendingJob(CallScheduler.RECOVERY_JOB));
    }
    @Test public void bundledCallersUseManagedPhoneAccountAndCallVolume() {
        assertEquals(24,CallClips.callers(app).size());
        assertFalse(CallClips.available(app,1)); // Warstock has no invented voice.
        CallTelecom.register(app);
        android.telecom.PhoneAccount account=app.getSystemService(android.telecom.TelecomManager.class).getPhoneAccount(CallTelecom.handle(app));
        assertTrue(account.hasCapabilities(android.telecom.PhoneAccount.CAPABILITY_CALL_PROVIDER));
        assertFalse(account.hasCapabilities(android.telecom.PhoneAccount.CAPABILITY_SELF_MANAGED));
        assertTrue(account.supportsUriScheme("sip")); assertFalse(account.supportsUriScheme("tel"));
        assertEquals(android.media.AudioAttributes.USAGE_VOICE_COMMUNICATION,CallPlaybackService.attributes().getUsage());
        assertEquals(android.media.AudioManager.STREAM_VOICE_CALL,CallPlaybackService.attributes().getVolumeControlStream());
        assertFalse(CallNotifications.ring(app,3)); // Setup is required; there is no custom UI fallback.
        assertFalse(CallNotifications.active(app));
        assertNotEquals(0,CallContacts.icon(20)); // Paige
        assertNotEquals(0,CallContacts.icon(26)); // Mechanic
    }
    @Test public void everyBundledCallHasVerifiedCallerAndSource() throws Exception {
        assertEquals(ContactMessages.IDS.length,20);
        for(int i=0;i<ContactMessages.IDS.length;i++) assertEquals(ContactMessages.IDS[i],CallContacts.IDS[i]);
        org.json.JSONArray catalog;
        try(java.io.InputStream input=app.getAssets().open("call_catalog.json")) {
            java.io.ByteArrayOutputStream bytes=new java.io.ByteArrayOutputStream();
            byte[] buffer=new byte[4096]; int n; while((n=input.read(buffer))!=-1) bytes.write(buffer,0,n);
            catalog=new org.json.JSONArray(bytes.toString("UTF-8"));
        }
        java.util.Set<Integer> documented=new java.util.HashSet<>();
        java.util.Set<String> callers=new java.util.HashSet<>();
        for(int i=0;i<catalog.length();i++) {
            org.json.JSONObject row=catalog.getJSONObject(i);
            String caller=row.getString("caller"),resource=row.getString("resource");
            assertTrue(resource.startsWith("call_"+caller+"_"));
            assertTrue(java.util.Arrays.asList(CallContacts.IDS).contains(caller));
            assertEquals("introductory_or_work_offer_phone_call",row.getString("kind"));
            assertTrue(row.getString("source").startsWith("https://www.youtube.com/watch?v="));
            assertFalse(row.getString("context").isEmpty());
            int id=R.raw.class.getField(resource).getInt(null); assertTrue(documented.add(id)); callers.add(caller);
            java.security.MessageDigest digest=java.security.MessageDigest.getInstance("SHA-256");
            try(java.io.InputStream input=app.getResources().openRawResource(id)) {
                byte[] buffer=new byte[4096]; int n; while((n=input.read(buffer))!=-1) digest.update(buffer,0,n);
            }
            StringBuilder hash=new StringBuilder(); for(byte b:digest.digest()) hash.append(String.format("%02x",b&255));
            assertEquals(row.getString("sha256"),hash.toString());
        }
        assertEquals(24,callers.size());
        for(java.lang.reflect.Field field:R.raw.class.getFields()) if(field.getName().startsWith("call_")) assertTrue(documented.contains(field.getInt(null)));
        for(String removed:new String[]{"call_franklin","call_gerald","call_lamar","call_lester_fleeca","call_lester_ljt","call_pavel","call_ron","call_simeon","call_tony"}) {
            try { R.raw.class.getField(removed); fail("Rejected recording still bundled: "+removed); }
            catch(NoSuchFieldException expected) {}
        }
    }
    @Test public void notificationContactTogglesDoNotDisableCallers() {
        RequestScheduler.prefs(app).edit().putBoolean("contact_lester",false).commit();
        assertTrue(CallClips.callers(app).contains(3));
        CallScheduler.prefs(app).edit().putBoolean("contact_lester",false).commit();
        assertFalse(CallClips.callers(app).contains(3));
    }
    private void ringing(String token) {
        CallScheduler.prefs(app).edit().putString("active_token",token).putString("active_phase","ringing")
            .putInt("active_boot",CallScheduler.bootCount(app)).putLong("active_until",SystemClock.elapsedRealtime()+45_000).commit();
    }
    @Test public void nativePhoneConnectionAnswersHoldsAndDisconnects() {
        ringing("native");
        NativeCallService service=Robolectric.buildService(NativeCallService.class).create().get();
        android.os.Bundle extras=new android.os.Bundle(); extras.putString("gta_token","native"); extras.putInt("gta_contact",20);
        android.telecom.Connection connection=service.onCreateIncomingConnection(CallTelecom.handle(app),new android.telecom.ConnectionRequest(CallTelecom.handle(app),null,extras));
        assertEquals(android.telecom.Connection.STATE_RINGING,connection.getState());
        connection.onAnswer(); assertEquals(android.telecom.Connection.STATE_ACTIVE,connection.getState());
        assertEquals("playing",CallNotifications.phase(app));
        connection.onHold(); assertEquals(android.telecom.Connection.STATE_HOLDING,connection.getState());
        connection.onUnhold(); assertEquals(android.telecom.Connection.STATE_ACTIVE,connection.getState());
        connection.onDisconnect(); assertEquals(android.telecom.Connection.STATE_DISCONNECTED,connection.getState());
        assertFalse(CallNotifications.active(app));
    }
    @Test public void nativeAccountCannotPlaceOutgoingOrAcceptStaleCalls() {
        NativeCallService service=Robolectric.buildService(NativeCallService.class).create().get();
        android.os.Bundle extras=new android.os.Bundle(); extras.putString("gta_token","stale"); extras.putInt("gta_contact",26);
        android.telecom.ConnectionRequest request=new android.telecom.ConnectionRequest(CallTelecom.handle(app),null,extras);
        assertEquals(android.telecom.Connection.STATE_DISCONNECTED,service.onCreateIncomingConnection(CallTelecom.handle(app),request).getState());
        assertEquals(android.telecom.Connection.STATE_DISCONNECTED,service.onCreateOutgoingConnection(CallTelecom.handle(app),request).getState());
    }
    @Test public void nativeUnansweredCallTimesOutWithoutExactAlarmAccess() {
        ringing("timeout");
        NativeCallService service=Robolectric.buildService(NativeCallService.class).create().get();
        android.os.Bundle extras=new android.os.Bundle(); extras.putString("gta_token","timeout"); extras.putInt("gta_contact",26);
        android.telecom.Connection connection=service.onCreateIncomingConnection(CallTelecom.handle(app),new android.telecom.ConnectionRequest(CallTelecom.handle(app),null,extras));
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idleFor(java.time.Duration.ofSeconds(45));
        assertEquals(android.telecom.Connection.STATE_DISCONNECTED,connection.getState());
        assertEquals(android.telecom.DisconnectCause.MISSED,connection.getDisconnectCause().getCode());
        assertFalse(CallNotifications.active(app));
    }
    @Test public void onlyCurrentCallCanAnswerOrHangUp() {
        ringing("current");
        assertFalse(CallNotifications.answer(app,"old"));
        CallNotifications.end(app,"old"); assertTrue(CallNotifications.active(app));
        assertTrue(CallNotifications.answer(app,"current"));
        assertEquals("playing",CallNotifications.phase(app));
        assertFalse(CallNotifications.answer(app,"current"));
        CallNotifications.end(app,"current"); assertFalse(CallNotifications.active(app));
    }
    @Test public void expiredAndPreviousBootCallsCannotAnswer() {
        ringing("expired");
        CallScheduler.prefs(app).edit().putLong("active_until",SystemClock.elapsedRealtime()).commit();
        assertFalse(CallNotifications.answer(app,"expired"));
        ringing("oldboot"); CallScheduler.prefs(app).edit().putInt("active_boot",-1).commit();
        assertFalse(CallNotifications.answer(app,"oldboot"));
    }
    @Test public void ringtoneIsReadOnlyAndPrivateRecordingsAreNotExposed() throws Exception {
        assertEquals("audio/mpeg",app.getContentResolver().getType(SoundProvider.RINGTONE_URI));
        try(java.io.InputStream input=app.getContentResolver().openInputStream(SoundProvider.RINGTONE_URI)) {
            int bytes=0,n; byte[] buffer=new byte[4096]; while((n=input.read(buffer))!=-1) bytes+=n;
            assertEquals(350040,bytes);
        }
        for(android.net.Uri uri:new android.net.Uri[]{SoundProvider.RINGTONE_URI.buildUpon().appendQueryParameter("path","files/call-lester.audio").build(),android.net.Uri.parse("content://"+SoundProvider.AUTHORITY+"/call-lester.audio")}) {
            try(java.io.InputStream input=app.getContentResolver().openInputStream(uri)) { fail("Only bundled sounds are public"); }
            catch(java.io.FileNotFoundException expected) {}
        }
    }
}
