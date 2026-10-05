package com.grabthesevehicles.app;

import static org.junit.Assert.*;
import android.app.Application;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=36)
public class LauncherFramingTest {
    @Test public void everySelectableLauncherHasAnAdaptiveImageFillingTheVisibleViewport() throws Exception {
        Application app=RuntimeEnvironment.getApplication();
        for(int i=0;i<ContactMessages.IDS.length;i++) if(ContactIcons.resource(i)!=0) {
            ActivityInfo info=app.getPackageManager().getActivityInfo(LauncherIcons.component(app,i),PackageManager.MATCH_DISABLED_COMPONENTS);
            Drawable drawable=app.getDrawable(info.icon);
            assertTrue(ContactMessages.NAMES[i],drawable instanceof AdaptiveIconDrawable);
            AdaptiveIconDrawable icon=(AdaptiveIconDrawable)drawable;
            icon.setBounds(0,0,72,72);
            assertTrue(icon.getBackground() instanceof BitmapDrawable);
            assertTrue(icon.getForeground() instanceof InsetDrawable);
            Drawable picture=((InsetDrawable)icon.getForeground()).getDrawable();
            assertTrue(picture instanceof BitmapDrawable);
            assertTrue(ContactMessages.NAMES[i],picture.getBounds().width()>=71);
            assertTrue(ContactMessages.NAMES[i],picture.getBounds().height()>=71);
            assertTrue(picture.getBounds().left<=1 && picture.getBounds().top<=1);
        }
    }
}
