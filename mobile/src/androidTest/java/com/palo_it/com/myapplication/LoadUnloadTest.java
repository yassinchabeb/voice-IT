package com.palo_it.com.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;
import com.palo_it.com.myapplication.activity.WakeUpWordActivity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class LoadUnloadTest extends ActivityInstrumentationTestCase2 {

    private Activity activity;

    public LoadUnloadTest() {
        super(WakeUpWordActivity.class);
    }

    @Before
    public void setup() throws Exception {
        setActivityInitialTouchMode(false);
        activity = getActivity();
    }

    @Test
    public void testLoadApp() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.findViewById(R.id.fab).callOnClick();
            }
        });
    }
}