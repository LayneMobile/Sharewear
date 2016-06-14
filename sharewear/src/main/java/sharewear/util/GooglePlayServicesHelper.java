/*
 * Copyright 2016 Layne Mobile, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sharewear.util;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import sharewear.WearUtils;
import sharewear.internal.WearLog;

public class GooglePlayServicesHelper {
    private static final String TAG = GooglePlayServicesHelper.class.getSimpleName();

    private static volatile PlayServicesStatus sPlayServicesStatus;

    // initialize default options
    @NonNull private static volatile ErrorOptions sErrorOptions = new ErrorOptions() {
        @Override
        public boolean shouldShowErrorDialog(Context context) {
            return true;
        }
    };

    public static void setErrorOptions(ErrorOptions errorOptions) {
        if (errorOptions != null) {
            sErrorOptions = errorOptions;
        }
    }

    /**
     * Determines whether or not we should attempt to use google play services.
     *
     * @param context
     *         the context
     *
     * @return true if the user has GooglePlayServices installed and the current version or if there if the user can
     * resolve any issues in order to install it correctly or false if not.
     */
    public static boolean canAttemptPlayServices(Context context) {
        return getLastPlayServicesStatus(context) != PlayServicesStatus.ERROR_NO_RESOLUTION;
    }

    /**
     * Determines whether or not we should attempt to use google play services.
     *
     * @param resultCode
     *         the {@link ConnectionResult#getErrorCode()}
     *
     * @return true if the user has GooglePlayServices installed and the current version or if there if the user can
     * resolve any issues in order to install it correctly or false if not.
     */
    public static boolean canAttemptPlayServices(int resultCode) {
        return getPlayServicesStatus(resultCode) != PlayServicesStatus.ERROR_NO_RESOLUTION;
    }

    /**
     * Gets the last status we have received from GooglePlayServices. If we have not yet gotten the status, a call is
     * made to {@link #getPlayServicesStatus(Context)} to get the current status.
     *
     * @param context
     *         the context
     *
     * @return the status of GooglePlayServices on this device
     */
    public static PlayServicesStatus getLastPlayServicesStatus(Context context) {
        if (sPlayServicesStatus == null) {
            sPlayServicesStatus = getPlayServicesStatus(context);
        }
        return sPlayServicesStatus;
    }

    /**
     * Calls GooglePlayServices to see if the device is setup properly.
     *
     * @param context
     *         the context
     *
     * @return the status of GooglePlayServices on this device
     */
    public static PlayServicesStatus getPlayServicesStatus(Context context) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        return getPlayServicesStatus(resultCode);
    }

    /**
     * Calls GooglePlayServices to see if the device is setup properly.
     *
     * @param resultCode
     *         the {@link ConnectionResult#getErrorCode()}
     *
     * @return the status of GooglePlayServices on this device
     */
    public static PlayServicesStatus getPlayServicesStatus(int resultCode) {
        WearLog.d(TAG, "play services status: %s", new ConnectionResult(resultCode, null));
        if (resultCode == ConnectionResult.SUCCESS) {
            sPlayServicesStatus = PlayServicesStatus.SUCCESS;
        } else if (!WearUtils.isAmazonDevice() && GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
            sPlayServicesStatus = PlayServicesStatus.ERROR_USER_RESOLUTION;
        } else {
            WearLog.i(TAG, "This device is not supported.");
            sPlayServicesStatus = PlayServicesStatus.ERROR_NO_RESOLUTION;
        }
        return sPlayServicesStatus;
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If it doesn't, display a notification that
     * allows users to download the APK from the Google Play Store or enable it in the device's system settings.
     *
     * @param context
     *         the context
     *
     * @return the {@link PlayServicesStatus} regarding the state of GooglePlayServices on this device
     */
    public static PlayServicesStatus checkPlayServices(Context context) {
        PlayServicesStatus status = getPlayServicesStatus(context);
        switch (status) {
            case ERROR_USER_RESOLUTION:
                int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
                GooglePlayServicesUtil.showErrorNotification(resultCode, context);
                break;
        }
        return status;
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If it doesn't, display a dialog that allows
     * users to download the APK from the Google Play Store or enable it in the device's system settings.
     *
     * @param activity
     *         the calling activity
     * @param requestCode
     *         the request code to use for a resolution activity
     *
     * @return the {@link PlayServicesStatus} regarding the state of GooglePlayServices on this device
     */
    public static PlayServicesStatus checkPlayServices(Activity activity, int requestCode) {
        PlayServicesStatus status = getPlayServicesStatus(activity);
        switch (status) {
            case ERROR_USER_RESOLUTION:
                int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
                showErrorDialog(resultCode, activity, requestCode);
                break;
        }
        return status;
    }

    public static boolean shouldShowErrorDialog(Context context) {
        return sErrorOptions.shouldShowErrorDialog(context);
    }

    public static boolean hasResolution(ConnectionResult connectionResult) {
        if (connectionResult != null && !connectionResult.isSuccess()) {
            // Since no activities are running, then we should send a notification in this case
            int errorCode = connectionResult.getErrorCode();
            final boolean isRecoverable = connectionResult.hasResolution() || canAttemptPlayServices(errorCode);
            WearLog.d(TAG, "connection error is recoverable? %s", String.valueOf(isRecoverable));
            return isRecoverable;
        }
        return false;
    }

    public static boolean showResolutionNotification(Context context, ConnectionResult connectionResult) {
        if (hasResolution(connectionResult)) {
            int errorCode = connectionResult.getErrorCode();
            GooglePlayServicesUtil.showErrorNotification(errorCode, context);
            return true;
        }
        return false;
    }

    public static void showErrorDialog(int errorCode, Activity activity, int requestCode) {
        final Context context = activity.getApplicationContext();
        if (sErrorOptions.shouldShowErrorDialog(context)) {
            WearLog.w(TAG, "showing google play services error dialog");
            GooglePlayServicesUtil.getErrorDialog(errorCode, activity, requestCode)
                    .show();
        }
    }

    public enum PlayServicesStatus {
        SUCCESS,
        ERROR_USER_RESOLUTION,
        ERROR_NO_RESOLUTION
    }

    public interface ErrorOptions {
        boolean shouldShowErrorDialog(Context context);
    }
}
