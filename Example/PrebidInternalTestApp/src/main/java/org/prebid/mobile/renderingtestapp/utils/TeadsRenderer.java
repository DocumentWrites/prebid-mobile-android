package org.prebid.mobile.renderingtestapp.utils;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static org.prebid.mobile.api.exceptions.AdException.THIRD_PARTY;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.api.rendering.customrenderer.AdRenderer;
import org.prebid.mobile.api.rendering.customrenderer.InterstitialControllerInterface;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialControllerListener;
import org.prebid.mobile.rendering.bidding.listeners.DisplayViewListener;

import tv.teads.sdk.AdOpportunityTrackerView;
import tv.teads.sdk.AdPlacementSettings;
import tv.teads.sdk.AdRatio;
import tv.teads.sdk.AdRequestSettings;
import tv.teads.sdk.InReadAd;
import tv.teads.sdk.InReadAdModelListener;
import tv.teads.sdk.InReadAdPlacement;
import tv.teads.sdk.TeadsSDK;
import tv.teads.sdk.renderer.InReadAdView;

public class TeadsRenderer implements AdRenderer {

    private FrameLayout frameLayout;

    @Override
    public View getBannerAdView( @NonNull Context context,
                                 DisplayViewListener listener,
                                 @NonNull AdUnitConfiguration adUnitConfiguration,
                                 @NonNull BidResponse response) {
        frameLayout = new FrameLayout(context);

        /* todo expected behaviors
          1 - pass rich to third party sdk to load and return an ad view
          2 - sync displayViewListener to the third party sdk ad life cycle listener
         */

        // todo test ad view returning
        InReadAdView inReadAdView = new InReadAdView(context);
        inReadAdView.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        getAdd(inReadAdView, listener);

        return inReadAdView;
    }

    @Override
    public InterstitialControllerInterface getInterstitialController(Context context, InterstitialControllerListener listener) {
        return new InterstitialControllerInterface() {
            @Override
            public void loadAd(AdUnitConfiguration adUnitConfiguration, BidResponse bidResponse) {
                Toast.makeText(context, "load ad", Toast.LENGTH_LONG).show();
                // todo I think here it is not a matter of returning a view because another view is put over all others
                listener.onInterstitialReadyForDisplay();
            }

            @Override
            public void show() {
                Toast.makeText(context, "show ad", Toast.LENGTH_LONG).show();
                listener.onInterstitialDisplayed();
            }

            @Override
            public void destroy() {

            }
        };
    }


    // todo temp
    private void getAdd(InReadAdView inReadAdView, DisplayViewListener displayViewListener) {
        InReadAdPlacement inReadAdPlacement;
        AdPlacementSettings adPlacementSettings = new AdPlacementSettings.Builder().enableDebug().build();
        AdRequestSettings adRequestSettings = new AdRequestSettings.Builder().pageSlotUrl("http://teads.com").build();

        inReadAdPlacement = TeadsSDK.INSTANCE.createInReadPlacement(inReadAdView.getContext(), 84242, adPlacementSettings);

        inReadAdPlacement.requestAd(adRequestSettings, new InReadAdModelListener() {
            @Override
            public void onAdReceived(@NonNull InReadAd inReadAd, @NonNull AdRatio adRatio) {
                inReadAdView.bind(inReadAd);
                displayViewListener.onAdLoaded();
            }

            @Override
            public void adOpportunityTrackerView(@NonNull AdOpportunityTrackerView adOpportunityTrackerView) {

            }

            @Override
            public void onFailToReceiveAd(@NonNull String s) {
                displayViewListener.onAdFailed(new AdException(THIRD_PARTY, s));
            }

            @Override
            public void onAdRatioUpdate(@NonNull AdRatio adRatio) {}

            @Override
            public void onAdImpression() {
                displayViewListener.onAdDisplayed();
            }

            @Override
            public void onAdClicked() {
                displayViewListener.onAdClicked();
            }

            @Override
            public void onAdError(int i, @NonNull String s) {
                displayViewListener.onAdFailed(new AdException(THIRD_PARTY, s));
            }

            @Override
            public void onAdClosed() {
                displayViewListener.onAdClosed();
            }

            @Override
            public void onAdExpandedToFullscreen() {}

            @Override
            public void onAdCollapsedFromFullscreen() {}
        });
    }

}
