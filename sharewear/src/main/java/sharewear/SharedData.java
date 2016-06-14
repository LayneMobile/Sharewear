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

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;

/**
 * This class allows to share data more easily between app and wear. Any class that extends from this, must be present
 * in the common module! This class requires a Shareable.Creator CREATOR field:
 * <pre>
 *     <code>
 *     public static final Shareable.Creator&lt;T&gt; CREATOR = new Shareable.Creator&lt;&gt;(){ ... };
 *     </code>
 * </pre>
 */
public abstract class SharedData extends Shareable {
    /**
     * Creates a SharedData item from a {@link DataItem}.
     *
     * @param apiClient
     *         the google api client
     * @param dataItem
     *         the data item
     * @param classLoader
     *         the class loader
     * @param <T>
     *         the type of SharedData to return
     *
     * @return the shared data item
     *
     * @throws IllegalArgumentException
     *         if the {@code dataItem} is null
     */
    public static <T extends SharedData> T fromDataItem(GoogleApiClient apiClient,
            DataItem dataItem,
            ClassLoader classLoader) {
        if (dataItem == null) {
            throw new IllegalArgumentException("the data item must not be null");
        }
        return fromDataMapItem(apiClient, DataMapItem.fromDataItem(dataItem), classLoader);
    }

    /**
     * Creates a SharedData item from {@link DataMapItem}.
     *
     * @param apiClient
     *         the google api client
     * @param dataMapItem
     *         the data map item
     * @param classLoader
     *         the class loader
     * @param <T>
     *         the type of SharedData to return
     *
     * @return the shared data item
     *
     * @throws IllegalArgumentException
     *         if the {@code dataMapItem is null} or if the {@code CREATOR} field cannot be found
     * @throws IllegalStateException
     *         if the {@code CREATOR} object cannot be retrieved
     */
    public static <T extends SharedData> T fromDataMapItem(GoogleApiClient apiClient,
            DataMapItem dataMapItem,
            ClassLoader classLoader) {
        if (dataMapItem == null) {
            throw new IllegalArgumentException("The dataMapItem must not be null");
        }

        final DataMap map = dataMapItem.getDataMap();
        return fromDataMap(apiClient, map, classLoader);
    }

    /**
     * Creates a SharedData item from {@link DataMap}.
     *
     * @param apiClient
     *         the api client
     * @param dataMap
     *         the data map
     * @param classLoader
     *         the class loader
     * @param <T>
     *         the type of SharedData to return
     *
     * @return the shared data item
     *
     * @throws IllegalArgumentException
     *         if the {@code dataMapItem is null} or if the {@code CREATOR} field cannot be found
     * @throws IllegalStateException
     *         if the {@code CREATOR} object cannot be retrieved
     */
    public static <T extends SharedData> T fromDataMap(GoogleApiClient apiClient, DataMap dataMap,
            ClassLoader classLoader) {
        if (dataMap == null) {
            throw new IllegalArgumentException("The dataMap must not be null");
        }

        SharedParcel sharedParcel = new SharedParcel(apiClient, dataMap);
        return sharedParcel.readShareable(classLoader);
    }

    /**
     * Gets the path needed to create a {@link PutDataRequest}.
     *
     * @return the path
     */
    public abstract String getPath();

    /**
     * Gets the uri for this data item, also specifying the nodeId which created / will create the data.
     *
     * @param nodeId
     *         the node id
     *
     * @return the uri for the data item
     */
    public Uri getUri(String nodeId) {
        return WearUtils.getUriForDataItem(nodeId, getPath());
    }

    /**
     * Gets the uri for this data item. This does not specify the node id that created / will create the data. So it can
     * be used to access all data items for this path.
     *
     * @return the uri for the data item
     */
    public Uri getUri() {
        return WearUtils.getUriForDataItem(getPath());
    }

    /**
     * Creates a {@link PutDataRequest} from this object using {@link #getPath()} as the path.
     *
     * @return the PutDataRequest to send
     */
    public final PutDataRequest asPutDataRequest(GoogleApiClient apiClient) {
        PutDataMapRequest request = PutDataMapRequest.create(getPath());
        SharedParcel sharedParcel = new SharedParcel(apiClient, request.getDataMap());
        sharedParcel.writeShareable(this, 0);
        return request.asPutDataRequest();
    }
}
