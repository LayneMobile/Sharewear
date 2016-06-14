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

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import sharewear.SharedData;
import sharewear.internal.SharewearIntent;
import sharewear.internal.WearLog;

public class WearDataService extends AbstractWearApiService {
    private static final String TAG = WearDataService.class.getSimpleName();

    public WearDataService() {
        super(TAG);
    }

    public static void put(Context context, SharedData data) {
        Intent intent = new Intent(context, WearDataService.class);
        intent.setAction(SharewearIntent.ACTION_PUT);
        intent.putExtra(SharewearIntent.EXTRA_SHARED_DATA, data);
        context.startService(intent);
    }

    public static void delete(Context context, Uri uri) {
        Intent intent = new Intent(context, WearDataService.class);
        intent.setAction(SharewearIntent.ACTION_DELETE);
        intent.putExtra(SharewearIntent.EXTRA_URI, uri);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntentConnected(@NonNull GoogleApiClient apiClient,
            @Nullable Intent intent) {
        final String action;
        if (intent != null && (action = intent.getAction()) != null) {
            if (SharewearIntent.ACTION_PUT.equals(action)) {
                final SharedData data
                        = intent.getParcelableExtra(SharewearIntent.EXTRA_SHARED_DATA);
                if (data != null) {
                    final PutDataRequest request = data.asPutDataRequest(apiClient);
                    DataApi.DataItemResult result
                            = Wearable.DataApi.putDataItem(apiClient, request)
                            .await();
                    Status status = result.getStatus();
                    WearLog.i(TAG, "%s putting data item: %s",
                            status.isSuccess() ? "SUCCESS" : "FAILED",
                            request.getUri());
                    if (!status.isSuccess()) {
                        WearLog.e(TAG, "error sending message: %s", status);
                    }
                }
            } else if (SharewearIntent.ACTION_DELETE.equals(action)) {
                final Uri uri = intent.getParcelableExtra(SharewearIntent.EXTRA_URI);
                if (uri != null) {
                    DataApi.DeleteDataItemsResult result
                            = Wearable.DataApi.deleteDataItems(apiClient, uri)
                            .await();
                    Status status = result.getStatus();
                    WearLog.i(TAG, "%s deleting data item: %s", status.isSuccess() ? "SUCCESS" : "FAILED", uri);
                    if (!status.isSuccess()) {
                        WearLog.e(TAG, "error sending message: %s", status);
                    }
                }
            }
        }
    }

    @Override
    protected void onHandleIntentFailedConnecting(@NonNull ConnectionResult connectionResult,
            @Nullable Intent intent) {
        // TODO: maybe save off data to be handled later?
    }
}
