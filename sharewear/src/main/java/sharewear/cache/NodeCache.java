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

package sharewear.cache;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import sharewear.SimpleWearHandler;
import sharewear.WearUtils;
import sharewear.services.WearRunnerService;

// TODO: this class may not work anymore, since new methods were added to Node and we don't know how the underlying implementation stores the nodes
public final class NodeCache extends SimpleWearHandler implements WearRunnerService.Runner {
    private static final NodeCache INSTANCE = new NodeCache();

    private final CopyOnWriteArraySet<Node> connectedNodes = new CopyOnWriteArraySet<Node>();
    private volatile boolean initialized;
    private volatile Context context;
    private volatile OnNodesChangedListener listener;

    private NodeCache() {}

    public static NodeCache getInstance() {
        return INSTANCE;
    }

    public void init(@NonNull Context context) {
        if (!initialized) {
            sync(context);
        }
    }

    public void setOnNodesChangedListener(@Nullable OnNodesChangedListener listener) {
        this.listener = listener;
    }

    public boolean isInitialized() {
        return initialized;
    }

    @NonNull
    public Set<Node> getConnectedNodes() {
        return Collections.unmodifiableSet(connectedNodes);
    }

    @NonNull
    public List<String> getConnectedNodeIds() {
        List<String> connectedNodeIds = new ArrayList<String>(connectedNodes.size());
        for (Node node : connectedNodes) {
            connectedNodeIds.add(node.getId());
        }
        return connectedNodeIds;
    }

    public void sync(@NonNull Context context) {
        if (this.context == null) {
            this.context = context.getApplicationContext();
        }
        WearRunnerService.run(context, this);
    }

    @Override
    public void onPeerConnected(Node node) {
        WearNode wearNode = WearNode.create(node);
        if (wearNode != null) {
            synchronized (connectedNodes) {
                connectedNodes.add(wearNode);
            }
            publishChangedEvent();
        }
    }

    @Override
    public void onPeerDisconnected(Node node) {
        WearNode wearNode = WearNode.create(node);
        if (wearNode != null) {
            synchronized (connectedNodes) {
                connectedNodes.remove(wearNode);
            }
            publishChangedEvent();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Context context = this.context;
        if (!initialized && context != null) {
            sync(context);
        }
    }

    @Override
    public void run(@NonNull Context context, @NonNull GoogleApiClient apiClient) {
        // Sync nodes
        List<Node> connectedNodes = WearUtils.getConnectedNodes(apiClient);
        syncNodes(connectedNodes);
    }

    @Override
    public void onFailedConnecting(@NonNull Context context,
            @NonNull ConnectionResult connectionResult) {
        // Do nothing
    }

    private void syncNodes(@NonNull List<Node> currentNodes) {
        synchronized (connectedNodes) {
            Set<Node> oldNodes = new HashSet<Node>(connectedNodes);
            for (Node node : currentNodes) {
                WearNode wearNode = WearNode.create(node);
                if (wearNode != null) {
                    if (connectedNodes.contains(wearNode)) {
                        // remove old node and add with current one
                        connectedNodes.remove(wearNode);
                    }
                    connectedNodes.add(wearNode);
                    oldNodes.remove(wearNode);
                }
            }
            connectedNodes.removeAll(oldNodes);
        }
        initialized = true;
        publishChangedEvent();
    }

    private void publishChangedEvent() {
        OnNodesChangedListener onNodesChangedListener = listener;
        if (onNodesChangedListener != null) {
            onNodesChangedListener.onNodesChanged(this);
        }
    }

    public interface OnNodesChangedListener {
        void onNodesChanged(@NonNull NodeCache nodeCache);
    }
}
