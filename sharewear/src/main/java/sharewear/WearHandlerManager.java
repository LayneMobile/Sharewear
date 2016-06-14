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
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import sharewear.internal.WearLog;
import sharewear.util.GooglePlayServicesHelper;

/**
 * Class designed to manage connections to wear and also handlers that can handle events.
 * <p/>
 * Created by layne on 10/16/14.
 */
public final class WearHandlerManager
        implements NodeApi.NodeListener, MessageApi.MessageListener, DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = WearHandlerManager.class.getSimpleName();

    private final CopyOnWriteArraySet<WearHandler> mHandlers = new CopyOnWriteArraySet<WearHandler>();
    private final boolean mAddApiListeners;
    private volatile Context mContext;
    private volatile GoogleApiClient mApiClient;
    private volatile boolean mIsWearService;
    private final AtomicBoolean mConnected = new AtomicBoolean();

    /**
     * Creates a {@code WearHandlerManager}.
     *
     * @param addApiListeners
     *         true if {@code NodeApi}, {@code MessageApi}, and {@code DataApi} listeners should be added and events
     *         sent to the registered wear handlers.
     */
    public WearHandlerManager(boolean addApiListeners) {
        this.mAddApiListeners = addApiListeners;
    }

    /**
     * Registers a group of handlers for wear communication events.
     *
     * @param handlers
     *         the handlers to register
     */
    public synchronized void registerHandlers(WearHandler... handlers) {
        Collection<WearHandler> list = Arrays.asList(handlers);
        mHandlers.addAll(list);
        initHandlers(list);
    }

    /**
     * Registers a handler for wear communication events.
     *
     * @param handler
     *         the handler to register
     */
    public synchronized void registerHandler(WearHandler handler) {
        mHandlers.add(handler);
        initHandlers(Arrays.asList(handler));
    }

    /**
     * Unregisters a handler from receiver wear communication events.
     *
     * @param handler
     *         the handler to unregister
     */
    public synchronized void unregisterHandler(WearHandler handler) {
        mHandlers.remove(handler);
    }

    /**
     * Initializes the {@code GoogleApiClient}.
     *
     * @param context
     *         the context
     */
    public synchronized void init(Context context) {
        if (mApiClient == null) {
            mIsWearService = context instanceof WearService;
            mContext = context.getApplicationContext();
            mApiClient = new GoogleApiClient.Builder(mContext)
                    .addApi(Wearable.API)
                    .build();
            initHandlers(mHandlers);
        }
    }

    /**
     * Starts listening for wear events and notifies wear handlers. Also creates the {@code GoogleApiClient} if not
     * already created and connects to it.
     *
     * @throws IllegalStateException
     *         if {@link #init(Context)} has not been called and the {@code GoogleApiClient} is null
     */
    public void start() {
        if (mApiClient == null) {
            throw new IllegalStateException("Must call init(context) before calling start()");
        }
        mApiClient.registerConnectionCallbacks(this);
        mApiClient.registerConnectionFailedListener(this);
        if (mAddApiListeners) {
            Wearable.NodeApi.addListener(mApiClient, this);
            Wearable.MessageApi.addListener(mApiClient, this);
            Wearable.DataApi.addListener(mApiClient, this);
        }
        mApiClient.connect();
    }

    /**
     * Stops listening to wear events and disconnectes from the associated {@code GoogleApiClient}.
     *
     * @throws IllegalStateException
     *         if {@link #init(Context)} has not been called and the {@code GoogleApiClient} is null
     */
    public void stop() {
        if (mApiClient == null) {
            throw new IllegalStateException("Must call init(context) before calling stop()!");
        }
        if (mAddApiListeners) {
            Wearable.NodeApi.removeListener(mApiClient, this);
            Wearable.MessageApi.removeListener(mApiClient, this);
            Wearable.DataApi.removeListener(mApiClient, this);
        }
        mApiClient.unregisterConnectionCallbacks(this);
        mApiClient.unregisterConnectionFailedListener(this);
        if (mApiClient.isConnected() || mApiClient.isConnecting()) {
            mApiClient.disconnect();
        }
        mConnected.set(false);
    }

    /**
     * Gets the api client. Will be null until {@link #init(Context)} is called.
     *
     * @return the api client
     */
    @Nullable
    public GoogleApiClient getApiClient() {
        return mApiClient;
    }

    public boolean isConnected() {
        return mConnected.get();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        dataEvents.release();
        WearLog.i(TAG, "onDataChanged: %s", events);

        boolean connected = isConnected();
        if (!connected) {
            connected = mApiClient.blockingConnect(10, TimeUnit.SECONDS)
                    .isSuccess();
        }
        WearLog.i(TAG, "onDataChanged: connected? %s", String.valueOf(connected));

        NEXT_EVENT:
        for (DataEvent dataEvent : events) {
            WearLog.d(TAG, "dataEvent: %s", dataEvent);
            SharedDataEvent sharedDataEvent = SharedDataEvent.from(mApiClient, dataEvent);
            for (WearHandler handler : mHandlers) {
                try {
                    if (handler.handleDataEvent(mContext, mApiClient, sharedDataEvent)) {
                        WearLog.d(TAG, "handler: %s handled event: %s", handler.getClass(), dataEvent);
                        continue NEXT_EVENT;
                    }
                } catch (Exception e) {
                    WearLog.e(TAG, "error handling data event", e);
                }
            }
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        WearLog.i(TAG, "onMessageReceived: %s", messageEvent == null ? null : messageEvent.getPath());

        boolean connected = isConnected();
        if (!connected) {
            connected = mApiClient.blockingConnect(10, TimeUnit.SECONDS)
                    .isSuccess();
        }
        WearLog.i(TAG, "onMessageReceived: connected? %s", String.valueOf(connected));

        for (WearHandler handler : mHandlers) {
            try {
                if (handler.handleMessageEvent(mContext, mApiClient, messageEvent)) {
                    WearLog.d(TAG, "handler: %s handled message: %s", handler.getClass(),
                            messageEvent);
                    break;
                }
            } catch (Exception e) {
                WearLog.e(TAG, "error handling message event", e);
            }
        }
    }

    @Override
    public void onPeerConnected(Node peer) {
        for (WearHandler handler : mHandlers) {
            handler.onPeerConnected(peer);
        }
    }

    @Override
    public void onPeerDisconnected(Node peer) {
        for (WearHandler handler : mHandlers) {
            handler.onPeerDisconnected(peer);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        mConnected.set(true);
        for (WearHandler handler : mHandlers) {
            handler.onConnected(bundle);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mConnected.set(false);
        for (WearHandler handler : mHandlers) {
            handler.onConnectionSuspended(i);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        mConnected.set(false);
        WearLog.e(TAG, result, "onConnectionFailed: %s", result);
        if (mIsWearService) {
            boolean shown = GooglePlayServicesHelper.showResolutionNotification(mContext, result);
            WearLog.d(TAG, "%s resolution notification", shown ? "unable to show" : "success showing");
        }
        for (WearHandler handler : mHandlers) {
            handler.onConnectionFailed(result);
        }
    }

    private void initHandlers(Collection<WearHandler> wearHandlers) {
        if (mApiClient != null) {
            for (WearHandler wearHandler : wearHandlers) {
                wearHandler.onInit(mContext);
            }
        }
    }
}
