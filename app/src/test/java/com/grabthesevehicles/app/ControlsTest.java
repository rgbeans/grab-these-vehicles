package com.grabthesevehicles.app;

import static org.junit.Assert.*;
import android.Manifest;
import android.app.Application;
import android.content.ClipboardManager;
import android.content.Intent;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=36)
public class ControlsTest {
    private Application app;
    @Before public void setUp() {
        app=RuntimeEnvironment.getApplication();
        RequestScheduler.prefs(app).edit().clear().commit();
        Shadows.shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS);
    }

    @Test public void cardCopiesTheExactMessageVisibleEvenIfANewerRequestArrives() {
        SimeonNotifications.deliver(app,0);
        String shown=RequestScheduler.prefs(app).getString("last_message","");
        try(ActivityController<MainActivity> activity=Robolectric.buildActivity(MainActivity.class).setup()) {
            View card=(View)findText(activity.get().getWindow().getDecorView(),shown).getParent();
            assertTrue(card.isClickable());
            SimeonNotifications.deliver(app,0);
            assertNotEquals(shown,RequestScheduler.prefs(app).getString("last_message",""));
            card.performClick();
            assertEquals(shown,app.getSystemService(ClipboardManager.class).getPrimaryClip().getItemAt(0).getText().toString());
        }
    }

    @Test public void previewIsNotPresentedAsACopyableLastMessage() {
        try(ActivityController<MainActivity> activity=Robolectric.buildActivity(MainActivity.class).setup()) {
            View card=(View)findText(activity.get().getWindow().getDecorView(),VehicleMessages.preview()).getParent();
            assertFalse(card.isClickable());
            assertFalse(card.isFocusable());
        }
    }

    @Test public void soundSettingsOpensTheActiveNotificationChannel() {
        try(ActivityController<MainActivity> activity=Robolectric.buildActivity(MainActivity.class).setup()) {
            findText(activity.get().getWindow().getDecorView(),"Sound & notification settings").performClick();
            Intent settings=Shadows.shadowOf(activity.get()).getNextStartedActivity();
            assertEquals(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS,settings.getAction());
            assertEquals(app.getPackageName(),settings.getStringExtra(Settings.EXTRA_APP_PACKAGE));
            assertEquals(SimeonNotifications.CHANNEL,settings.getStringExtra(Settings.EXTRA_CHANNEL_ID));
        }
    }

    private static TextView findText(View root,String text) {
        if(root instanceof TextView && text.contentEquals(((TextView)root).getText())) return (TextView)root;
        if(root instanceof ViewGroup) {
            ViewGroup group=(ViewGroup)root;
            for(int i=0;i<group.getChildCount();i++) {
                TextView found=findText(group.getChildAt(i),text);
                if(found!=null) return found;
            }
        }
        return null;
    }
}
