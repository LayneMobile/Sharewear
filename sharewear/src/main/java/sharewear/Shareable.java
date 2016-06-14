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

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;

/**
 * This class allows to share data more easily between app and wear. Any class that extends from this, must be present
 * in the common module! This class requires a Shareable.Creator CREATOR field:
 * <pre>
 *     <code>
 *     public static final Shareable.Creator&lt;T&gt; CREATOR = new Shareable.Creator&lt;T&gt;(){ ... };
 *     </code>
 * </pre>
 */
public abstract class Shareable implements Parcelable {
    /**
     * Returns the version of the shared data object.
     *
     * @return the shared data version
     */
    public abstract int getVersion();

    /**
     * Flatten this object into a {@link DataMap}.
     *
     * @param dataMap
     *         The DataMap in which the object should be written.
     */
    public final void writeToDataMap(GoogleApiClient apiClient, DataMap dataMap) {
        SharedParcel shared = new SharedParcel(apiClient, dataMap);
        writeToSharedParcel(shared, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void writeToParcel(Parcel dest, int flags) {
        SharedParcel shared = new SharedParcel(dest);
        writeToSharedParcel(shared, flags);
    }

    /**
     * Flatten this object into a {@link sharewear.SharedParcel}. The SharedParcel object could represent either a
     * Parcel or DataMap.
     *
     * @param dest
     *         The SharedParcel in which the object should be written.
     * @param flags
     *         Additional flags about how the object should be written. May be 0 or {@link
     *         #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    public abstract void writeToSharedParcel(SharedParcel dest, int flags);

    /**
     * Interface that must be implemented and provided as a public CREATOR field that generates instances of your
     * Shareable object from a DataMap.
     */
    public static abstract class Creator<T> implements Parcelable.Creator<T> {
        /**
         * Returns the version of the shared data object.
         *
         * @return the shared data version
         */
        public abstract int getVersion();

        /**
         * Create a new instance of the Shareable class, instantiating it from the given DataMap whose data had
         * previously been written by {@link Shareable#writeToDataMap(GoogleApiClient, DataMap)}.
         *
         * @param apiClient
         *         The api client
         * @param source
         *         The DataMap to read the object's data from.
         * @param version
         *         The version of the shareable object to be created
         *
         * @return Returns a new instance of the Shareable class.
         */
        public final T createFromDataMap(GoogleApiClient apiClient, DataMap source, int version) {
            SharedParcel in = new SharedParcel(apiClient, source);
            return createFromSharedParcel(in, version);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public final T createFromParcel(Parcel source) {
            SharedParcel in = new SharedParcel(source);
            return createFromSharedParcel(in, getVersion());
        }

        /**
         * Creates a new instance of the Shareable object, instantiating it from the given SharedParcel. The
         * SharedParcel could represent either a Parcel or a DataMap.
         *
         * @param source
         *         The SharedParcel to read the object's data from
         * @param version
         *         The version of the shareable object to be created
         *
         * @return Returns a new instance of the Shareable class.
         */
        public abstract T createFromSharedParcel(SharedParcel source, int version);
    }
}
