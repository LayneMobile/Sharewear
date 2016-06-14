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

package sharewear.events;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;

import java.util.List;

public interface WearMessageEvent<P extends WearMessageEvent.Params> extends WearEvent {
    boolean canHandleEvent(MessageEvent event);

    void send(Context context, String nodeId, P p);

    void send(Context context, P p);

    PendingResult<MessageApi.SendMessageResult> send(GoogleApiClient apiClient, String nodeId, P p);

    @NonNull List<PendingResult<MessageApi.SendMessageResult>> send(GoogleApiClient apiClient, P p);

    P parse(MessageEvent event);

    interface Params {
        String getPath();

        byte[] getData();
    }
}
