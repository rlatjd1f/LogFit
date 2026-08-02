package com.logfit.app;

import static org.junit.Assert.*;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        assertEquals("com.logfit.app", appContext.getPackageName());
    }

    @Test
    public void packageTargetsAndroid16AndSupportsPictureInPicture() throws Exception {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        PackageManager packageManager = appContext.getPackageManager();
        PackageInfo packageInfo = packageManager.getPackageInfo(appContext.getPackageName(), 0);

        assertEquals(36, packageInfo.applicationInfo.targetSdkVersion);
        assertTrue(packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE));
        assertTrue(Build.VERSION.SDK_INT >= 24);
    }
}
