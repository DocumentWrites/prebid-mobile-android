package com.openx.apollo.networking.modelcontrollers;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;

import com.openx.apollo.errors.AdException;
import com.openx.apollo.listeners.AdIdFetchListener;
import com.openx.apollo.models.AdConfiguration;
import com.openx.apollo.networking.BaseNetworkTask;
import com.openx.apollo.networking.ResponseHandler;
import com.openx.apollo.networking.parameters.AdRequestInput;
import com.openx.apollo.networking.parameters.AppInfoParameterBuilder;
import com.openx.apollo.networking.parameters.BasicParameterBuilder;
import com.openx.apollo.networking.parameters.DeviceInfoParameterBuilder;
import com.openx.apollo.networking.parameters.GeoLocationParameterBuilder;
import com.openx.apollo.networking.parameters.NetworkParameterBuilder;
import com.openx.apollo.networking.parameters.ParameterBuilder;
import com.openx.apollo.networking.parameters.UserConsentParameterBuilder;
import com.openx.apollo.networking.parameters.UserParameters;
import com.openx.apollo.networking.urlBuilder.PathBuilderBase;
import com.openx.apollo.networking.urlBuilder.URLBuilder;
import com.openx.apollo.networking.urlBuilder.URLComponents;
import com.openx.apollo.sdk.ManagersResolver;
import com.openx.apollo.sdk.deviceData.managers.ConnectionInfoManager;
import com.openx.apollo.sdk.deviceData.managers.DeviceInfoManager;
import com.openx.apollo.utils.helpers.AdIdManager;
import com.openx.apollo.utils.helpers.AppInfoManager;
import com.openx.apollo.utils.helpers.ExternalViewerUtils;
import com.openx.apollo.utils.logger.OXLog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public abstract class Requester {
    private static final String TAG = Requester.class.getSimpleName();

    protected String mRequestName;
    protected WeakReference<Context> mContextReference;
    protected AdConfiguration mAdConfiguration;
    protected URLBuilder mUrlBuilder;
    protected ResponseHandler mAdResponseCallBack;
    protected AsyncTask mNetworkTask;

    Requester(Context context, AdConfiguration config, AdRequestInput adRequestInput, ResponseHandler responseHandler) {
        mRequestName = "";
        mContextReference = new WeakReference<>(context);
        mAdConfiguration = config;

        /*
            IMPORTANT
            Order of builders in the array matters because of decorator pattern
            Later builder parameters has more priority
        */
        ArrayList<ParameterBuilder> parameterBuilderArray = new ArrayList<>(getParameterBuilders());
        mUrlBuilder = new URLBuilder(getPathBuilder(), parameterBuilderArray, adRequestInput);
        mAdResponseCallBack = responseHandler;
    }

    public abstract void startAdRequest();

    public void destroy() {
        if (mNetworkTask != null) {
            mNetworkTask.cancel(true);
        }
    }

    protected List<ParameterBuilder> getParameterBuilders() {
        Context context = mContextReference.get();
        Resources resources = null;
        if (context != null) {
            resources = context.getResources();
        }
        boolean browserActivityAvailable = ExternalViewerUtils.isBrowserActivityCallable(context);

        ArrayList<ParameterBuilder> parameterBuilderArray = new ArrayList<>();
        parameterBuilderArray.add(new BasicParameterBuilder(mAdConfiguration, resources, browserActivityAvailable));
        parameterBuilderArray.add(new GeoLocationParameterBuilder());
        parameterBuilderArray.add(new AppInfoParameterBuilder());
        parameterBuilderArray.add(new DeviceInfoParameterBuilder(mAdConfiguration));
        parameterBuilderArray.add(new NetworkParameterBuilder());
        parameterBuilderArray.add(new UserConsentParameterBuilder());
        return parameterBuilderArray;
    }

    /*
     * Attempts to get the advertisement ID
     *
     * Ad request continues AFTER the attempt to get the advertisement ID. This is because the SDK
     * must attempt to grab and honor the latest LMT value for each ad request
     */
    protected void getAdId() {
        final Context context = mContextReference.get();
        if (context == null) {
            sendAdException(
                "Context is null",
                "Context is null. Can't continue with ad request"
            );
            return;
        }

        AdIdManager.initAdId(context, new AdIdInitListener(this));
    }

    protected abstract PathBuilderBase getPathBuilder();

    private void sendAdException(String logMsg, String exceptionMsg) {
        OXLog.warn(TAG, logMsg);
        AdException adException = new AdException(AdException.INIT_ERROR, exceptionMsg);
        mAdResponseCallBack.onErrorWithException(adException, 0);
    }

    protected void makeAdRequest() {
        // Check if app has internet permissions
        DeviceInfoManager deviceManager = ManagersResolver.getInstance().getDeviceManager();
        if (deviceManager == null || !deviceManager.isPermissionGranted("android.permission.INTERNET")) {
            sendAdException(
                "Either OpenX DeviceManager is not initialized or android.permission.INTERNET is not specified. Please check",
                "Internet permission not granted"
            );
            return;
        }

        // Check if device is connected to the internet
        ConnectionInfoManager connectionInfoManager = ManagersResolver.getInstance().getNetworkManager();
        if (connectionInfoManager == null || connectionInfoManager.getConnectionType() == UserParameters.OXMConnectionType.OFFLINE) {
            sendAdException(
                "Either OpenX networkManager is not initialized or Device is offline. Please check the internet connection",
                "No internet connection detected"
            );
            return;
        }

        // Send ad request
        URLComponents jsonUrlComponents = buildUrlComponent();
        sendAdRequest(jsonUrlComponents);
    }

    protected URLComponents buildUrlComponent() {
        return mUrlBuilder.buildUrl();
    }

    protected void sendAdRequest(URLComponents jsonUrlComponents) {
        BaseNetworkTask.GetUrlParams params = new BaseNetworkTask.GetUrlParams();
        params.url = jsonUrlComponents.getBaseUrl();
        params.queryParams = jsonUrlComponents.getQueryArgString();
        params.requestType = "POST";
        params.userAgent = AppInfoManager.getUserAgent();
        params.name = mRequestName;

        BaseNetworkTask networkTask = new BaseNetworkTask(mAdResponseCallBack);
        mNetworkTask = networkTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
    }

    protected static class AdIdInitListener implements AdIdFetchListener {

        private WeakReference<Requester> mWeakRequester;

        public AdIdInitListener(Requester requester) {
            mWeakRequester = new WeakReference<>(requester);
        }

        @Override
        public void adIdFetchCompletion() {
            OXLog.info(TAG, "adIdFetchCompletion");
            makeAdRequest();
        }

        @Override
        public void adIdFetchFailure() {
            OXLog.warn(TAG, "adIdFetchFailure");
            makeAdRequest();
        }

        private void makeAdRequest() {
            Requester requester = mWeakRequester.get();
            if (requester == null) {
                OXLog.warn(TAG, "Requester is null");
                return;
            }

            requester.makeAdRequest();
        }
    }
}
