package com.openx.internal_test_app.utils.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.mopub.common.MoPub
import com.mopub.common.SdkConfiguration
import com.mopub.nativeads.*
import com.openx.apollo.bidding.display.MoPubNativeAdUnit
import com.openx.apollo.models.ntv.NativeAdConfiguration
import com.openx.internal_test_app.R

class MoPubNativeFeedAdapter(context: Context,
                             private val configId: String,
                             private val adUnitId: String,
                             private val nativeAdConfiguration: NativeAdConfiguration?) : BaseFeedAdapter(context) {
    private var mopubNative: MoPubNative? = null
    private var mopubNativeAdUnit: MoPubNativeAdUnit? = null

    override fun destroy() {
        mopubNative?.destroy()
        mopubNativeAdUnit?.destroy()
    }

    override fun initAndLoadAdView(parent: ViewGroup?, container: FrameLayout): View? {
        val context = container.context
        mopubNative = MoPubNative(context, adUnitId, object : MoPubNative.MoPubNativeNetworkListener {
            override fun onNativeLoad(nativeAd: NativeAd?) {
                val adapterHelper = AdapterHelper(context, 0, 3)
                val view = adapterHelper.getAdView(null, parent, nativeAd)
                container.addView(view)
            }

            override fun onNativeFail(errorCode: NativeErrorCode?) {}
        })
        val viewBinder = ViewBinder.Builder(R.layout.lyt_native_ad)
                .titleId(R.id.tvNativeTitle)
                .textId(R.id.tvNativeBody)
                .sponsoredTextId(R.id.tvNativeBrand)
                .mainImageId(R.id.ivNativeMain)
                .iconImageId(R.id.ivNativeIcon)
                .callToActionId(R.id.btnNativeAction)
                .build()
        mopubNative?.registerAdRenderer(ApolloNativeAdRenderer(viewBinder))
        mopubNative?.registerAdRenderer(MoPubStaticNativeAdRenderer(viewBinder))
        mopubNativeAdUnit = MoPubNativeAdUnit(context, configId, nativeAdConfiguration)
        MoPub.initializeSdk(context, SdkConfiguration.Builder(adUnitId).build()) {
            val keywordsContainer = HashMap<String, String>()
            mopubNativeAdUnit?.fetchDemand(keywordsContainer, mopubNative!!) {
                val requestParameters = RequestParameters.Builder()
                        .keywords(convertMapToMoPubKeywords(keywordsContainer))
                        .build()
                mopubNative?.makeRequest(requestParameters)
            }
        }

        return null
    }

    private fun convertMapToMoPubKeywords(keywordMap: Map<String, String>): String? {
        val result = StringBuilder()
        for (key in keywordMap.keys) {
            result.append(key).append(":").append(keywordMap[key]).append(",")
        }
        if (result.isNotEmpty()) {
            result.delete(result.length - 1, result.length)
        }
        return result.toString()
    }
}