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

package sharewear;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;

/**
 * A convenience class to extend when you only want to handle a subset of the google api / wear events. This implements
 * all methods in the {@link WearHandler} but does nothing and returns {@code false} for all applicable methods.
 * <p/>
 * Created by layne on 10/17/14.
 */
public class SimpleWearHandler implements WearHandler {
    private Context context;

    @Override
    public void onInit(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public boolean handleMessageEvent(Context context, GoogleApiClient apiClient, MessageEvent messageEvent) {
        return false;
    }

    @Override
    public boolean handleDataEvent(Context context, GoogleApiClient apiClient, SharedDataEvent dataEvent) {
        return false;
    }

    @Override
    public void onConnected(Bundle bundle) {}

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onPeerConnected(Node node) {}

    @Override
    public void onPeerDisconnected(Node node) {}

    @Override
    public void onConnectionFailed(ConnectionResult result) {}

    public Context getContext() {
        return context;
    }
}
