package com.openx.apollo;

import android.app.Activity;
import android.content.Context;

import com.openx.apollo.views.interstitial.InterstitialManager;
import com.openx.apollo.views.webview.OpenXWebViewBase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class HTMLCreativeViewTest {

    private Activity mTestActivity;
    private Context mMockContext;

    @Before
    public void setUp() throws Exception {
        mTestActivity = Robolectric.buildActivity(Activity.class).create().get();
        mMockContext = mTestActivity.getApplicationContext();
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testSetMediaUrl() throws Exception {

        OpenXWebViewBase mockHTMLCreativeView = new OpenXWebViewBase(mMockContext, mock(InterstitialManager.class));
        //mockHTMLCreativeView.start();
        assertNotNull(mockHTMLCreativeView);
    }

   
}