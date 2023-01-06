/*
 *    Copyright 2018-2021 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.prebid.mobile.api.rendering.customrenderer;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialControllerListener;
import org.prebid.mobile.rendering.bidding.listeners.DisplayViewListener;

// TODO add the comments also as Yurii put in the class diagram?
public interface PrebidMobilePluginCustomRenderer {

    String getName();
    String getVersion();
    @Nullable
    String getToken();

    // TODO what is the good reason to check here if the it's supported for a specific ad unit config

    View createBannerAdView(
            @NonNull Context context,
            @NonNull DisplayViewListener displayViewListener,
            @NonNull AdUnitConfiguration adUnitConfiguration,
            @NonNull BidResponse bidResponse
    );

    PrebidMobileInterstitialControllerInterface createInterstitialController(
            @NonNull Context context,
            @NonNull InterstitialControllerListener interstitialControllerListener,
            @NonNull AdUnitConfiguration adUnitConfiguration,
            @NonNull BidResponse bidResponse
    );
}
