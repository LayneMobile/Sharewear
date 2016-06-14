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
import android.support.annotation.NonNull;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMapItem;

/**
 * Helper class that wraps a {@link DataEvent} in order to get data more easily.
 * <p/>
 * Created by layne on 10/16/14.
 */
public final class SharedDataEvent {
    private final GoogleApiClient mApiClient;
    private final DataEvent mDataEvent;

    private SharedDataEvent(@NonNull GoogleApiClient apiClient, @NonNull DataEvent dataEvent) {
        this.mApiClient = apiClient;
        this.mDataEvent = dataEvent;
    }

    /**
     * Creates a SharedDataEvent from a DataEvent.
     *
     * @param dataEvent
     *         the data event
     *
     * @return the shared data event
     *
     * @throws IllegalArgumentException
     *         if the {@code dataEvent} is null.
     */
    public static SharedDataEvent from(@NonNull GoogleApiClient apiClient,
            @NonNull DataEvent dataEvent) {
        if (dataEvent == null) {
            throw new IllegalArgumentException("The data event must not be null");
        }
        return new SharedDataEvent(apiClient, dataEvent);
    }

    public GoogleApiClient getApiClient() {
        return mApiClient;
    }

    public DataEvent getDataEvent() {
        return mDataEvent;
    }

    /**
     * Returns {@link DataEvent#getType()}.
     *
     * @return the data event type
     */
    public int getType() {
        return mDataEvent.getType();
    }

    /**
     * Returns {@link DataEvent#getDataItem()}.
     *
     * @return the data item
     */
    public DataItem getDataItem() {
        return mDataEvent.getDataItem();
    }

    /**
     * Returns {@link DataMapItem#fromDataItem(DataItem)}.
     *
     * @return the data map item
     */
    public DataMapItem getDataMapItem() {
        return DataMapItem.fromDataItem(getDataItem());
    }

    /**
     * Returns {@link DataItem#getUri()}.
     *
     * @return the uri of the data item
     */
    public Uri getUri() {
        return getDataItem().getUri();
    }

    /**
     * Returns the path from {@link #getUri()}.
     *
     * @return the path of the uri
     */
    public String getPath() {
        return getUri().getPath();
    }

    /**
     * Creates a {@link SharedData} item from the {@link com.google.android.gms.wearable.DataMap}.
     *
     * @param <T>
     *         the type of shared data
     *
     * @return the shared data item
     */
    public <T extends SharedData> T getSharedData() {
        return getSharedData(getClass().getClassLoader());
    }

    /**
     * Creates a {@link SharedData} item from the {@link com.google.android.gms.wearable.DataMap}.
     *
     * @param classLoader
     *         the class loader
     * @param <T>
     *         the type of shared data
     *
     * @return the shared data item
     */
    public <T extends SharedData> T getSharedData(ClassLoader classLoader) {
        return SharedData.fromDataItem(mApiClient, getDataItem(), classLoader);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SharedDataEvent)) return false;

        SharedDataEvent that = (SharedDataEvent) o;

        if (!mDataEvent.equals(that.mDataEvent)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return mDataEvent.hashCode();
    }

    @Override
    public String toString() {
        return "SharedDataEvent{" +
                "mDataEvent=" + mDataEvent +
                '}';
    }
}
