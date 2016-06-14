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
import android.support.annotation.NonNull;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.NodeApi;

/**
 * Represents an object that can handle wear message and data events from the app.
 */
public interface WearHandler
        extends NodeApi.NodeListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    /**
     * Called when the {@link WearHandlerManager} is initialized.
     *
     * @param context
     *         the context
     */
    void onInit(@NonNull Context context);

    /**
     * Called when a message is received. The message should be handled in this method if it matches an event this
     * handler can handle.
     *
     * @param context
     *         the context
     * @param apiClient
     *         the google apiClient
     * @param messageEvent
     *         the message event
     *
     * @return true if handled, false if not
     */
    boolean handleMessageEvent(Context context, GoogleApiClient apiClient,
            MessageEvent messageEvent);

    /**
     * Called when a data item is received. The data should be handled in this method if it matches a type of data this
     * handler can handle.
     *
     * @param context
     *         the context
     * @param apiClient
     *         the google apiClient
     * @param dataEvent
     *         the data event
     *
     * @return true if handled, false if not
     */
    boolean handleDataEvent(Context context, GoogleApiClient apiClient, SharedDataEvent dataEvent);
}
