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

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Looper;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import sharewear.events.WearDataEvent;
import sharewear.internal.WearLog;
import sharewear.util.Callback;

/**
 * Contains common utility methods.
 */
public final class WearUtils {
    private static final String TAG = WearUtils.class.getSimpleName();
    /**
     * Whether or not this is an Amazon device.
     */
    private static final boolean IS_AMAZON_DEVICE = "Amazon".equalsIgnoreCase(Build.MANUFACTURER);

    private WearUtils() { throw new AssertionError("no instances"); }

    public static boolean isAmazonDevice() {
        return IS_AMAZON_DEVICE;
    }

    public static boolean readBoolean(Parcel in) {
        return in.readInt() != 0;
    }

    public static void writeBoolean(Parcel out, boolean val) {
        out.writeInt(val ? 1 : 0);
    }

    /**
     * Closes a {@code Closeable} object and ignores the exception.
     *
     * @param closeable
     *         the closeable
     *
     * @throws RuntimeException
     *         if a runtime exception occurs while throwing. Any other exception is ignored
     */
    public static void closeQuietly(Closeable closeable) {
        try {
            closeable.close();
        } catch (RuntimeException rethrown) {
            throw rethrown;
        } catch (Exception e) {
            // ignore
        }
    }

    /**
     * Asserts this thread IS the UI thread.
     */
    public static void assertUiThread() {
        if (!isUiThread()) {
            throw new IllegalStateException("Must be called on main thread");
        }
    }

    /**
     * Asserts this thread is NOT the UI thread.
     */
    public static void assertNotUiThread() {
        if (isUiThread()) {
            throw new IllegalStateException("Cannot be called on main thread");
        }
    }

    /**
     * Checks if the current thread is the main UI thread.
     *
     * @return true if the main thread, false if not
     */
    public static boolean isUiThread() {
        return Looper.getMainLooper().equals(Looper.myLooper());
    }

    /**
     * Creats a uri for a {@link PutDataRequest} with specified path segments. For example, to create a uri with
     * nodeId='12345' and  path='/trunk/branch', you can use:
     * <pre>
     *     <code>
     *     Uri uri = CommonUtils.getUriForDataItem("12345", "trunk", "branch");
     *     </code>
     * </pre>
     *
     * @param nodeId
     *         the node id
     * @param pathSegments
     *         the path segemnts
     *
     * @return the uri for creating a put data request
     */
    public static Uri getUriForDataItem(String nodeId, String... pathSegments) {
        Uri.Builder builder = new Uri.Builder()
                .scheme(PutDataRequest.WEAR_URI_SCHEME)
                .authority(nodeId);

        if (pathSegments != null) {
            for (String pathSegment : pathSegments) {
                builder.appendPath(pathSegment);
            }
        }

        return builder.build();
    }

    /**
     * Creates a uri for a {@link PutDataRequest} with a specified path.
     *
     * @param nodeId
     *         the node id
     * @param path
     *         the path for the data item
     *
     * @return the uri for a data item
     */
    public static Uri getUriForDataItem(String nodeId, String path) {
        return new Uri.Builder()
                .scheme(PutDataRequest.WEAR_URI_SCHEME)
                .authority(nodeId)
                .path(path)
                .build();
    }

    /**
     * Creates a uri for a {@link PutDataRequest} with a specified path. This uri does not specify a specific node, so
     * it will access all data sets.
     *
     * @param path
     *         the path for the data item
     *
     * @return the uri for a data item
     */
    public static Uri getUriForDataItem(String path) {
        return new Uri.Builder()
                .scheme(PutDataRequest.WEAR_URI_SCHEME)
                .path(path)
                .build();
    }

    /**
     * Gets the local node id synchronously. Must NOT be called on the UI thread.
     *
     * @param client
     *         the GoogleApiClient
     *
     * @return the local node id
     */
    public static String getLocalNodeId(GoogleApiClient client) {
        assertNotUiThread();
        NodeApi.GetLocalNodeResult nodeResult
                = Wearable.NodeApi.getLocalNode(client).await();
        return nodeResult.getNode().getId();
    }

    /**
     * Gets the connected nodes synchronously. Must NOT be called on the UI thread.
     *
     * @param apiClient
     *         the GoogleApiClient
     *
     * @return the list of nodes or an empty list if none are available
     */
    public static List<Node> getConnectedNodes(GoogleApiClient apiClient) {
        assertNotUiThread();
        NodeApi.GetConnectedNodesResult nodesResult = Wearable.NodeApi.getConnectedNodes(apiClient)
                .await();
        if (nodesResult.getStatus().isSuccess()) {
            return nodesResult.getNodes();
        }
        return Collections.emptyList();
    }

    /**
     * Gets the connected node ids synchronously. Must NOT be called on tne UI thread.
     *
     * @param apiClient
     *         the GoogleApiClient
     *
     * @return the list of node ids or an empty list if none are available
     */
    public static List<String> getConnectedNodeIds(GoogleApiClient apiClient) {
        List<Node> nodes = getConnectedNodes(apiClient);
        List<String> results = new ArrayList<String>(nodes.size());
        for (Node node : nodes) {
            results.add(node.getId());
        }
        return results;
    }

    public static void getConnectedNodeIds(@NonNull GoogleApiClient client,
            @NonNull final Callback<List<String>> callback) {
        if (!isUiThread()) {
            List<String> nodeIds = getConnectedNodeIds(client);
            callback.onResult(nodeIds);
            return;
        }

        Wearable.NodeApi.getConnectedNodes(client)
                .setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult result) {
                        List<Node> nodes = result.getNodes();
                        List<String> results = new ArrayList<String>(nodes.size());
                        for (Node node : nodes) {
                            results.add(node.getId());
                        }
                        callback.onResult(results);
                    }
                });
    }

    /**
     * Gets the node id from this event.
     *
     * @param event
     *         the event
     *
     * @return the node id from which this event was sent
     */
    @Nullable
    public static String getNodeId(SharedDataEvent event) {
        if (event != null) {
            return getNodeId(event.getUri());
        }
        return null;
    }

    /**
     * Gets the node id from this uri.
     *
     * @param uri
     *         the uri
     *
     * @return the node id
     */
    @Nullable
    public static String getNodeId(Uri uri) {
        if (uri != null) {
            return uri.getAuthority();
        }
        return null;
    }

    @Nullable
    public static <T extends SharedData> T getData(@NonNull GoogleApiClient apiClient,
            @NonNull String path) {
        assertNotUiThread();
        Uri uri = WearUtils.getUriForDataItem(path);
        WearLog.d(TAG, "getData uri: %s", uri);
        DataItemBuffer buffer = Wearable.DataApi.getDataItems(apiClient, uri)
                .await();
        List<T> results = getDataFromDataItemBuffer(apiClient, buffer);
        if (results.size() > 0) {
            return results.get(0);
        }
        return null;
    }

    @Nullable
    public static <T extends SharedData> T getData(@NonNull GoogleApiClient apiClient,
            @NonNull String nodeId,
            @NonNull String path) {
        assertNotUiThread();
        Uri uri = WearUtils.getUriForDataItem(nodeId, path);
        WearLog.d(TAG, "getData uri: %s", uri);
        DataApi.DataItemResult result = Wearable.DataApi.getDataItem(apiClient, uri)
                .await();
        return getDataFromResult(apiClient, result);
    }

    @Nullable
    public static <T extends SharedData> List<T> getAllData(@NonNull GoogleApiClient apiClient,
            @NonNull WearDataEvent<T> dataEvent) {
        assertNotUiThread();
        DataItemBuffer dataItems = Wearable.DataApi.getDataItems(apiClient)
                .await();
        try {
            final List<T> returnItems = new ArrayList<T>();
            if (dataItems.getStatus().isSuccess()) {
                int count = dataItems.getCount();
                for (int i = 0; i < count; i++) {
                    DataItem dataItem = dataItems.get(i);
                    Uri uri = dataItem.getUri();
                    String path = uri.getPath();
                    WearLog.d(TAG, "getAllData uri: %s", uri);
                    WearLog.d(TAG, "getAllData path: %s", path);
                    if (dataEvent.canHandlePath(path)) {
                        WearLog.d(TAG, "getAllData dataEvent: %s can handle path: %s", dataEvent,
                                path);
                        T sharedData = SharedData.fromDataItem(apiClient, dataItem,
                                SharedData.class.getClassLoader());
                        if (sharedData != null) {
                            returnItems.add(sharedData);
                        }
                    }
                }
            }
            return returnItems;
        } finally {
            dataItems.release();
        }
    }

    public static <T extends SharedData> void getDataAsync(@NonNull final GoogleApiClient apiClient,
            final @NonNull String path, @NonNull final Callback<T> callback) {
        executeDataTask(callback, new Callable<T>() {
            @Override
            public T call() throws Exception {
                return getData(apiClient, path);
            }
        });
    }

    public static <T extends SharedData> void getDataAsync(@NonNull final GoogleApiClient apiClient,
            @NonNull final String nodeId, @NonNull final String path,
            @NonNull final Callback<T> callback) {
        executeDataTask(callback, new Callable<T>() {
            @Override
            public T call() throws Exception {
                return getData(apiClient, nodeId, path);
            }
        });
    }

    public static <T extends SharedData> void getAllDataAsync(
            @NonNull final GoogleApiClient apiClient,
            @NonNull final WearDataEvent<T> dataEvent,
            @NonNull final Callback<List<T>> callback) {
        executeDataTask(callback, new Callable<List<T>>() {
            @Override
            public List<T> call() throws Exception {
                return getAllData(apiClient, dataEvent);
            }
        });
    }

    public static <T extends SharedData> T getDataFromResult(@NonNull GoogleApiClient apiClient,
            @NonNull DataApi.DataItemResult dataItemResult) {
        T sharedData = null;
        WearLog.d(TAG, "sharedData result success? %s, dataItem: %s",
                dataItemResult.getStatus().isSuccess(),
                dataItemResult.getDataItem());
        if (dataItemResult.getStatus().isSuccess() && dataItemResult.getDataItem() != null) {
            sharedData = SharedData.fromDataItem(apiClient, dataItemResult.getDataItem(),
                    SharedData.class.getClassLoader());
        }
        WearLog.d(TAG, "sharedData: %s", sharedData);
        return sharedData;
    }

    public static <T extends SharedData> List<T> getDataFromDataItemBuffer(
            final @NonNull GoogleApiClient apiClient,
            @NonNull DataItemBuffer dataItemBuffer) {
        try {
            if (dataItemBuffer.getStatus().isSuccess()) {
                final List<DataItem> dataItems = FreezableUtils.freezeIterable(dataItemBuffer);
                final List<T> returnItems = new ArrayList<T>(dataItems.size());
                for (DataItem dataItem : dataItems) {
                    T sharedData = SharedData.fromDataItem(apiClient, dataItem,
                            SharedData.class.getClassLoader());
                    if (sharedData != null) {
                        returnItems.add(sharedData);
                    }
                }
                return returnItems;
            }
            return Collections.emptyList();
        } finally {
            dataItemBuffer.release();
        }
    }

    private static <T> void executeDataTask(Callback<T> callback, Callable<T> callable) {
        DataTask<T> task = new DataTask<T>(callable, callback);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            task.execute();
        }
    }

    private static final class DataTask<T> extends AsyncTask<Object, Object, T> {
        private final Callable<T> callable;
        private final Callback<T> callback;

        protected DataTask(Callable<T> callable, Callback<T> callback) {
            this.callable = callable;
            this.callback = callback;
        }

        @Override
        protected T doInBackground(Object... params) {
            try {
                return callable.call();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(T t) {
            callback.onResult(t);
        }
    }
}
