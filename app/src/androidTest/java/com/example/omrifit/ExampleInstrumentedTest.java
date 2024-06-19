package com.example.omrifit;


import static org.junit.Assert.assertEquals;

import android.app.Instrumentation;
import android.content.pm.InstrumentationInfo;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.android.uiautomator.core.UiDevice;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.ktor.client.engine.android.Android;

@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    private UiDevice mDevice;

    @Before
    public void setUp() {
        mDevice = UiDevice.getInstance();
    }

    @Test
    public void useAppContext() {
        // Context of the app under test.
        assertEquals("com.example.omrifit", InstrumentationRegistry.getInstrumentation().getTargetContext().getPackageName());
    }
}
