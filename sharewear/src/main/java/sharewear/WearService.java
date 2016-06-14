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

import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

import sharewear.cache.NodeCache;
import sharewear.internal.WearLog;

/**
 * This class represents the main connection between the app and wear. Android will start this service whenever any
 * communication is sent across nodes. In order to receive communication from a node, register a {@link WearHandler}
 * through {@link #registerHandler(WearHandler)} or {@link #registerHandlers(WearHandler...)} in order to handle events.
 * The order in which handlers are registered are important. If one handler handles an event, then no other handlers
 * will be notified of the event. This is only applicable for boolean returning handler methods such as {@link
 * WearHandler#handleDataEvent(android.content.Context, com.google.android.gms.common.api.GoogleApiClient,
 * sharewear.SharedDataEvent)} or {@link WearHandler#handleMessageEvent(android.content.Context,
 * com.google.android.gms.common.api.GoogleApiClient, MessageEvent)}.
 */
public class WearService extends WearableListenerService {
    private static final String TAG = WearService.class.getSimpleName();

    private static final WearHandlerManager sWearManager = new WearHandlerManager(false);

    /**
     * Registers a group of handlers for wear communication events.
     *
     * @param handlers
     *         the handlers to register
     */
    static void registerHandlers(WearHandler... handlers) {
        sWearManager.registerHandlers(handlers);
    }

    /**
     * Registers a handler for wear communication events.
     *
     * @param handler
     *         the handler to register
     */
    static void registerHandler(WearHandler handler) {
        sWearManager.registerHandler(handler);
    }

    /**
     * Unregisters a handler from receiver wear communication events.
     *
     * @param handler
     *         the handler to unregister
     */
    static void unregisterHandler(WearHandler handler) {
        sWearManager.unregisterHandler(handler);
    }

    static {
        // Register node cache to keep a cache of connected nodes
        registerHandler(NodeCache.getInstance());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sWearManager.init(this);
        sWearManager.start();
        WearLog.d(TAG, "creating WearService");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sWearManager.stop();
        WearLog.d(TAG, "destroying WearService");
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        super.onDataChanged(dataEvents);
        sWearManager.onDataChanged(dataEvents);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        sWearManager.onMessageReceived(messageEvent);
    }

    @Override
    public void onPeerConnected(Node peer) {
        super.onPeerConnected(peer);
        sWearManager.onPeerConnected(peer);
    }

    @Override
    public void onPeerDisconnected(Node peer) {
        super.onPeerDisconnected(peer);
        sWearManager.onPeerDisconnected(peer);
    }
}
