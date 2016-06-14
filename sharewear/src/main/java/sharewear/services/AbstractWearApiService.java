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

package sharewear.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import java.util.concurrent.TimeUnit;

import sharewear.internal.WearLog;
import sharewear.util.GooglePlayServicesHelper;

public abstract class AbstractWearApiService extends IntentService {
    private final String TAG = getClass().getSimpleName();

    private GoogleApiClient mApiClient;

    protected AbstractWearApiService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mApiClient.connect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mApiClient.disconnect();
    }

    @Override
    protected final void onHandleIntent(Intent intent) {
        try {
            ConnectionResult connectionResult;
            if (mApiClient.isConnected()
                    || (connectionResult = mApiClient.blockingConnect(10, TimeUnit.SECONDS)).isSuccess()) {
                onHandleIntentConnected(mApiClient, intent);
            } else {
                WearLog.e(TAG, connectionResult, "unable to connect to google play services: %s", connectionResult);
                boolean shown = GooglePlayServicesHelper.showResolutionNotification(this, connectionResult);
                WearLog.d(TAG, "%s resolution notification", shown ? "unable to show" : "success showing");
                onHandleIntentFailedConnecting(connectionResult, intent);
            }
        } catch (Exception e) {
            WearLog.e(TAG, "received an exception handling intent", e);
        }
    }

    protected abstract void onHandleIntentConnected(@NonNull GoogleApiClient apiClient,
            @Nullable Intent intent);

    protected abstract void onHandleIntentFailedConnecting(
            @NonNull ConnectionResult connectionResult, @Nullable Intent intent);
}
