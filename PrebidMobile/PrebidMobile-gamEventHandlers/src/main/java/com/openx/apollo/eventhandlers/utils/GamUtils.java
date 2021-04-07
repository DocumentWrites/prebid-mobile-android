package com.openx.apollo.eventhandlers.utils;

import android.os.Bundle;

import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.formats.NativeCustomTemplateAd;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.openx.apollo.bidding.data.FetchDemandResult;
import com.openx.apollo.bidding.data.NativeFetchDemandResult;
import com.openx.apollo.bidding.data.bid.BidResponse;
import com.openx.apollo.bidding.listeners.NativeAdCallback;
import com.openx.apollo.utils.logger.OXLog;
import com.openx.apollo.utils.ntv.NativeUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class GamUtils {
    private static final String TAG = GamUtils.class.getSimpleName();
    static final HashSet<String> RESERVED_KEYS;

    private static final String KEY_IS_APOLLO_CREATIVE = "isApolloCreative";
    private static final String KEY_IS_PREBID_CREATIVE = "isPrebid";

    static {
        RESERVED_KEYS = new HashSet<>();
    }

    public static void prepare(PublisherAdRequest.Builder adRequestBuilder, NativeFetchDemandResult result) {
        PublisherAdRequest publisherAdRequest = adRequestBuilder.build();
        removeUsedCustomTargetingForGam(publisherAdRequest);

        Map<String, String> targeting = result.getKeyWordsMap();

        if (targeting == null || targeting.isEmpty()) {
            OXLog.error(TAG, "prepare: Failed. Result contains invalid keywords");
            return;
        }

        for (Map.Entry<String, String> entry : targeting.entrySet()) {
            String key = entry.getKey();
            adRequestBuilder.addCustomTargeting(key, entry.getValue());
            addReservedKeys(key);
        }
    }

    public static void findNativeAd(NativeCustomTemplateAd nativeCustomTemplateAd, NativeAdCallback callback) {
        if (nativeCustomTemplateAd == null || callback == null) {
            OXLog.error(TAG, "findNativeAd: Failed. Passed nativeTemplateAd or callback is invalid");
            return;
        }

        final CharSequence cacheIdText = nativeCustomTemplateAd.getText(BidResponse.KEY_CACHE_ID);
        final String cacheId = cacheIdText != null ? cacheIdText.toString() : null;
        final NativeFetchDemandResult fetchDemandResult = createSuccessDemandResult(cacheId);

        NativeUtils.findNativeAd(fetchDemandResult, callback);
    }

    public static void findNativeAd(UnifiedNativeAd unifiedNativeAd, NativeAdCallback callback) {
        if (unifiedNativeAd == null || callback == null) {
            OXLog.error(TAG, "findNativeAd: Failed. Passed nativeTemplateAd or callback is invalid");
            return;
        }

        final String cacheId = unifiedNativeAd.getCallToAction();
        final NativeFetchDemandResult fetchDemandResult = createSuccessDemandResult(cacheId);

        NativeUtils.findNativeAd(fetchDemandResult, callback);
    }

    public static boolean didApolloWin(UnifiedNativeAd unifiedNativeAd) {
        if (unifiedNativeAd == null) {
            return false;
        }

        final String body = unifiedNativeAd.getBody();
        return KEY_IS_APOLLO_CREATIVE.equals(body);
    }

    public static boolean didApolloWin(NativeCustomTemplateAd ad) {
        if (ad == null) {
            return false;
        }

        CharSequence isApolloValue = ad.getText(KEY_IS_APOLLO_CREATIVE);
        CharSequence isPrebidValue = ad.getText(KEY_IS_PREBID_CREATIVE);

        return (isApolloValue != null && "1".contentEquals(isApolloValue))
               || (isPrebidValue != null && "1".contentEquals(isPrebidValue));
    }

    private static void removeUsedCustomTargetingForGam(PublisherAdRequest adRequestObj) {
        Bundle bundle = adRequestObj.getCustomTargeting();
        if (bundle != null && RESERVED_KEYS != null) {
            for (String key : RESERVED_KEYS) {
                bundle.remove(key);
            }
        }
    }

    private static NativeFetchDemandResult createSuccessDemandResult(String cacheId) {
        final NativeFetchDemandResult nativeFetchDemandResult = new NativeFetchDemandResult(FetchDemandResult.SUCCESS);
        final Map<String, String> keywordMap = new HashMap<>();
        keywordMap.put(BidResponse.KEY_CACHE_ID, cacheId);

        nativeFetchDemandResult.setKeyWordsMap(keywordMap);
        return nativeFetchDemandResult;
    }

    private static void addReservedKeys(String key) {
        synchronized (RESERVED_KEYS) {
            RESERVED_KEYS.add(key);
        }
    }
}
