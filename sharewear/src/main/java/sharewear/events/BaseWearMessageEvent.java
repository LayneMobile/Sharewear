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
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;

import sharewear.cache.NodeCache;
import sharewear.services.WearMessageService;

public abstract class BaseWearMessageEvent<P extends WearMessageEvent.Params>
        extends BaseWearEvent
        implements WearMessageEvent<P> {
    @Override
    public boolean canHandleEvent(MessageEvent event) {
        return event != null && canHandlePath(event.getPath());
    }

    @Override
    public void send(Context context, P p) {
        WearMessageService.sendMessage(context, p.getPath(), p.getData());
    }

    @Override
    public void send(Context context, String nodeId, P p) {
        WearMessageService.sendMessage(context, nodeId, p.getPath(), p.getData());
    }

    @Override
    public PendingResult<MessageApi.SendMessageResult> send(GoogleApiClient apiClient, String nodeId, P p) {
        return Wearable.MessageApi.sendMessage(apiClient, nodeId, p.getPath(), p.getData());
    }

    @NonNull
    @Override
    public List<PendingResult<MessageApi.SendMessageResult>> send(GoogleApiClient apiClient, P p) {
        final List<String> nodeIds = NodeCache.getInstance().getConnectedNodeIds();
        final int size = nodeIds.size();
        final List<PendingResult<MessageApi.SendMessageResult>> results
                = new ArrayList<PendingResult<MessageApi.SendMessageResult>>(size);
        final String path = p.getPath();
        final byte[] data = p.getData();
        for (String nodeId : nodeIds) {
            PendingResult<MessageApi.SendMessageResult> result =
                    Wearable.MessageApi.sendMessage(apiClient, nodeId, path, data);
            results.add(result);
        }
        return results;
    }
}
