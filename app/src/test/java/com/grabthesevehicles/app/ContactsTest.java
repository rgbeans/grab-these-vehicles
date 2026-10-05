package com.grabthesevehicles.app;

import static org.junit.Assert.*;
import android.Manifest;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=36)
public class ContactsTest {
    private Application app;
    @Before public void setUp() {
        app = RuntimeEnvironment.getApplication();
        RequestScheduler.prefs(app).edit().clear().commit();
        Shadows.shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS);
    }
    @Test public void disabledContactsNeverDeliver() {
        for (int i=0;i<ContactMessages.IDS.length;i++) RequestScheduler.prefs(app).edit().putBoolean("contact_"+ContactMessages.IDS[i],i==3).commit();
        for (int i=0;i<40;i++) {
            assertNotNull(RequestScheduler.nextMessage(app));
            assertEquals("Lester",RequestScheduler.prefs(app).getString("last_sender",""));
        }
    }
    @Test public void allDisabledLeavesHistoryAndNotificationsUntouched() {
        for (String id:ContactMessages.IDS) RequestScheduler.prefs(app).edit().putBoolean("contact_"+id,false).commit();
        assertFalse(SimeonNotifications.deliver(app));
        assertFalse(RequestScheduler.prefs(app).contains("last_message"));
        assertEquals(0,app.getSystemService(NotificationManager.class).getActiveNotifications().length);
    }
    @Test public void singleEnabledContactCanSendRepeatedMessages() {
        for (int i=0;i<ContactMessages.IDS.length;i++) RequestScheduler.prefs(app).edit().putBoolean("contact_"+ContactMessages.IDS[i],i==1).commit();
        assertEquals(RequestScheduler.nextMessage(app),RequestScheduler.nextMessage(app));
    }
    @Test public void eachContactCyclesEveryMessageWithoutBoundaryRepeats() {
        for(int c=0;c<ContactMessages.IDS.length;c++) {
            String previous=null;
            for(int cycle=0;cycle<3;cycle++) {
                java.util.Set<String> seen=new java.util.HashSet<>();
                for(int i=0;i<ContactMessages.count(c);i++) {
                    String message=RequestScheduler.nextMessage(app,c);
                    if (ContactMessages.count(c)>1) assertNotEquals(previous,message);
                    assertTrue(seen.add(message)); previous=message;
                }
                assertEquals(ContactMessages.count(c),seen.size());
            }
        }
    }
    @Test public void notificationUsesSelectedSenderAndExactCopyText() {
        for(int c=0;c<ContactMessages.IDS.length;c++) {
            assertTrue(SimeonNotifications.deliver(app,c));
            Notification n=null;
            for (android.service.notification.StatusBarNotification item:app.getSystemService(NotificationManager.class).getActiveNotifications())
                if (item.getId()==100+c) n=item.getNotification();
            assertNotNull(n);
            assertEquals(ContactMessages.NAMES[c],n.extras.getString(Notification.EXTRA_TITLE));
            assertEquals(RequestScheduler.prefs(app).getString("last_message",""),
                Shadows.shadowOf(n.contentIntent).getSavedIntent().getStringExtra(CopyMessageActivity.MESSAGE));
            assertNotNull(n.getLargeIcon());
        }
    }
    @Test public void contactsKeepSeparateNotificationsAndReplacementAlertsAgain() {
        assertTrue(SimeonNotifications.deliver(app,0));
        assertTrue(SimeonNotifications.deliver(app,3));
        NotificationManager manager=app.getSystemService(NotificationManager.class);
        assertEquals(2,manager.getActiveNotifications().length);
        assertTrue(SimeonNotifications.deliver(app,3));
        assertEquals(2,manager.getActiveNotifications().length);
        for(android.service.notification.StatusBarNotification item:manager.getActiveNotifications()) {
            assertEquals(0,item.getNotification().flags & Notification.FLAG_ONLY_ALERT_ONCE);
            String expected=item.getId()==100 ? "Simeon" : "Lester";
            assertEquals(expected,item.getNotification().extras.getString(Notification.EXTRA_TITLE));
        }
    }
    @Test public void laterContactDoesNotChangeEarlierContactsCopyIntent() {
        SimeonNotifications.deliver(app,0);
        NotificationManager manager=app.getSystemService(NotificationManager.class);
        String message=RequestScheduler.prefs(app).getString("last_message","");
        SimeonNotifications.deliver(app,3);
        for(android.service.notification.StatusBarNotification item:manager.getActiveNotifications())
            if(item.getId()==100) assertEquals(message,Shadows.shadowOf(item.getNotification().contentIntent)
                .getSavedIntent().getStringExtra(CopyMessageActivity.MESSAGE));
    }
    @Test public void simeonUpgradeKeepsExistingBagAndIntervals() {
        RequestScheduler.prefs(app).edit().putString("bag","2,3").putInt("min",45).putInt("max",90).commit();
        assertEquals(VehicleMessages.message(2),RequestScheduler.nextMessage(app,0));
        assertEquals("3",RequestScheduler.prefs(app).getString("bag",""));
        assertEquals(45,RequestScheduler.prefs(app).getInt("min",0));
        assertEquals(90,RequestScheduler.prefs(app).getInt("max",0));
    }
}
