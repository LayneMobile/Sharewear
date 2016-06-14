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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Wearable;

import java.util.Arrays;
import java.util.List;

import sharewear.WearUtils;
import sharewear.internal.SharewearIntent;
import sharewear.internal.WearLog;

public class WearMessageService extends AbstractWearApiService {
    private static final String TAG = WearMessageService.class.getSimpleName();

    public WearMessageService() {
        super(TAG);
    }

    public static void sendMessage(Context context, String path, byte[] data) {
        sendMessage(context, null, path, data);
    }

    public static void sendMessage(Context context, String nodeId, String path, byte[] data) {
        if (context != null) {
            Intent intent = new Intent(context, WearMessageService.class);
            intent.putExtra(SharewearIntent.EXTRA_NODE_ID, nodeId);
            intent.putExtra(SharewearIntent.EXTRA_PATH, path);
            intent.putExtra(SharewearIntent.EXTRA_DATA, data);
            context.startService(intent);
        }
    }

    @Override
    protected void onHandleIntentConnected(@NonNull GoogleApiClient apiClient, @Nullable Intent intent) {
        if (intent == null) { return; }
        String nodeId = intent.getStringExtra(SharewearIntent.EXTRA_NODE_ID);
        String path = intent.getStringExtra(SharewearIntent.EXTRA_PATH);
        byte[] data = intent.getByteArrayExtra(SharewearIntent.EXTRA_DATA);
        List<String> nodeIds = nodeId == null
                ? WearUtils.getConnectedNodeIds(apiClient)
                : Arrays.asList(nodeId);
        // TODO: get result
        // TODO: how do we notify of failure?
        for (String id : nodeIds) {
            MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(apiClient, id, path, data)
                    .await();
            Status status = result.getStatus();
            WearLog.i(TAG, "%s sending message: %s",
                    status.isSuccess() ? "SUCCESS" : "FAILED",
                    path);
            if (!status.isSuccess()) {
                WearLog.e(TAG, "error sending message: %s", status);
            }
        }
    }

    @Override
    protected void onHandleIntentFailedConnecting(@NonNull ConnectionResult connectionResult, @Nullable Intent intent) {
        // TODO: how do we notify of failure?
    }
}
