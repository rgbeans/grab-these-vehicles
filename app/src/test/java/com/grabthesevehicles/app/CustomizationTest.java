package com.grabthesevehicles.app;

import static org.junit.Assert.*;
import android.Manifest;
import android.app.AlertDialog;
import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlertDialog;

@RunWith(RobolectricTestRunner.class)
@Config(sdk={26,36})
public class CustomizationTest {
    private Application app;
    @Before public void setUp() {
        app=RuntimeEnvironment.getApplication();
        RequestScheduler.prefs(app).edit().clear().commit();
        if(android.os.Build.VERSION.SDK_INT>=33) Shadows.shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS);
    }
    @Test public void contactSectionCollapsesAndKeepsSelectionsAndExpansionAfterReopening() {
        try(ActivityController<MainActivity> controller=Robolectric.buildActivity(MainActivity.class).setup()) {
            View root=controller.get().getWindow().getDecorView();
            View expand=findDescription(root,"Expand contacts, 20 enabled");
            assertNotNull(expand); expand.performClick();
            Switch toggle=(Switch)findText(root,ContactMessages.NAMES[1]+"\n"+ContactMessages.DESCRIPTIONS[1]);
            View section=(View)toggle.getParent().getParent();
            assertEquals(View.VISIBLE,section.getVisibility());
            toggle.setChecked(false);
            findDescription(root,"Collapse contacts, 19 enabled").performClick();
            assertEquals(View.GONE,section.getVisibility());
        }
        try(ActivityController<MainActivity> controller=Robolectric.buildActivity(MainActivity.class).setup()) {
            View root=controller.get().getWindow().getDecorView();
            findDescription(root,"Expand contacts, 19 enabled").performClick();
            assertFalse(((Switch)findText(root,ContactMessages.NAMES[1]+"\n"+ContactMessages.DESCRIPTIONS[1])).isChecked());
        }
        assertTrue(RequestScheduler.prefs(app).getBoolean("contacts_expanded",false));
    }
    @Test public void gridSelectionChangesOneLauncherEntryAndKeepsTheMainActivityEnabled() {
        try(ActivityController<MainActivity> controller=Robolectric.buildActivity(MainActivity.class).setup()) {
            findText(controller.get().getWindow().getDecorView(),"Customize app icon").performClick();
            AlertDialog dialog=ShadowAlertDialog.getLatestAlertDialog();
            assertTrue(dialog.isShowing());
            for(int i=0;i<ContactMessages.IDS.length;i++) if(ContactIcons.resource(i)!=0)
                assertNotNull(findDescription(dialog.getWindow().getDecorView(),"Use "+ContactMessages.NAMES[i]+" app icon"+(i==0 ? ", selected" : "")));
            assertNull(findDescription(dialog.getWindow().getDecorView(),"Use Prix Luxury Real Estate app icon"));
            findDescription(dialog.getWindow().getDecorView(),"Use Warstock Cache & Carry app icon").performClick();
            assertFalse(dialog.isShowing());
            assertEquals(1,LauncherIcons.selected(app));
        }
        assertOneAlias(1);
        int mainState=app.getPackageManager().getComponentEnabledSetting(new ComponentName(app,MainActivity.class));
        assertTrue(mainState==PackageManager.COMPONENT_ENABLED_STATE_DEFAULT || mainState==PackageManager.COMPONENT_ENABLED_STATE_ENABLED);
        LauncherIcons.select(app,0);
        assertOneAlias(0);
        assertEquals(0,LauncherIcons.selected(app));
    }
    @Test public void missingIconSelectionLeavesTheCurrentLauncherUntouched() {
        LauncherIcons.select(app,2);
        try { LauncherIcons.select(app,4); fail("Prix has no verified icon"); }
        catch(IllegalArgumentException expected) {}
        assertEquals(2,LauncherIcons.selected(app));
        assertOneAlias(2);
    }
    private void assertOneAlias(int selected) {
        for(int i=0;i<ContactMessages.IDS.length;i++) if(ContactIcons.resource(i)!=0)
            assertEquals(i==selected ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                app.getPackageManager().getComponentEnabledSetting(LauncherIcons.component(app,i)));
        Intent launcher=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setPackage(app.getPackageName());
        assertEquals(1,app.getPackageManager().queryIntentActivities(launcher,0).size());
    }
    private static View findDescription(View root,String text) {
        if(root.getContentDescription()!=null && text.contentEquals(root.getContentDescription())) return root;
        if(root instanceof ViewGroup) {
            ViewGroup group=(ViewGroup)root;
            for(int i=0;i<group.getChildCount();i++) { View found=findDescription(group.getChildAt(i),text); if(found!=null)return found; }
        }
        return null;
    }
    private static View findText(View root,String text) {
        if(root instanceof android.widget.TextView && text.contentEquals(((android.widget.TextView)root).getText())) return root;
        if(root instanceof ViewGroup) {
            ViewGroup group=(ViewGroup)root;
            for(int i=0;i<group.getChildCount();i++) { View found=findText(group.getChildAt(i),text); if(found!=null)return found; }
        }
        return null;
    }
}
