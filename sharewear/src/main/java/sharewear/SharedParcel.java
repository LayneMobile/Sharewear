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

import android.graphics.Bitmap;
import android.os.Parcel;
import android.support.annotation.NonNull;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataMap;

import java.lang.reflect.Field;
import java.util.HashMap;

import sharewear.internal.WearLog;
import sharewear.util.ImageUtils;

public final class SharedParcel {
    private static final String TAG = SharedParcel.class.getSimpleName();
    private static final String KEY_PREFIX = "SharedParcel-";

    // Cache of previously looked up CREATOR.createFromSharedParcel() methods for
    // particular classes.  Keys are the names of the classes, values are
    // Method objects.
    private static final HashMap<ClassLoader, HashMap<String, Shareable.Creator>> sCreators
            = new HashMap<ClassLoader, HashMap<String, Shareable.Creator>>();

    private GoogleApiClient apiClient;
    private DataMap dataMap;
    private int position;
    private final boolean isParcel;

    private Parcel parcel;

    public SharedParcel(@NonNull Parcel parcel) {
        this.parcel = parcel;
        this.isParcel = true;
    }

    public SharedParcel(@NonNull GoogleApiClient apiClient, @NonNull DataMap dataMap) {
        this.apiClient = apiClient;
        this.dataMap = dataMap;
        this.isParcel = false;
    }

    /**
     * Whether or not the underlying data is a {@link Parcel}.
     *
     * @return True if the underlying data storage is a Parcel, false if it is a DataMap
     */
    public boolean isParcel() {
        return isParcel;
    }

    public Parcel getParcel() {
        return parcel;
    }

    public DataMap getDataMap() {
        return dataMap;
    }

    public GoogleApiClient getApiClient() {
        return apiClient;
    }

    public boolean readBoolean() {
        if (isParcel) {
            return WearUtils.readBoolean(parcel);
        } else {
            return dataMap.getBoolean(key());
        }
    }

    public void writeBoolean(boolean val) {
        if (isParcel) {
            WearUtils.writeBoolean(parcel, val);
        } else {
            dataMap.putBoolean(key(), val);
        }
    }

    public int readInt() {
        if (isParcel) {
            return parcel.readInt();
        } else {
            return dataMap.getInt(key());
        }
    }

    public void writeInt(int val) {
        if (isParcel) {
            parcel.writeInt(val);
        } else {
            dataMap.putInt(key(), val);
        }
    }

    public long readLong() {
        if (isParcel) {
            return parcel.readLong();
        } else {
            return dataMap.getLong(key());
        }
    }

    public void writeLong(long val) {
        if (isParcel) {
            parcel.writeLong(val);
        } else {
            dataMap.putLong(key(), val);
        }
    }

    public String readString() {
        if (isParcel) {
            return parcel.readString();
        } else {
            return dataMap.getString(key());
        }
    }

    public void writeString(String val) {
        if (isParcel) {
            parcel.writeString(val);
        } else {
            dataMap.putString(key(), val);
        }
    }

    public Bitmap readBitmap() {
        if (isParcel) {
            return parcel.readParcelable(Bitmap.class.getClassLoader());
        } else {
            Asset asset = dataMap.getAsset(key());
            if (asset != null) {
                return ImageUtils.fromAsset(apiClient, asset);
            }
            return null;
        }
    }

    public void writeBitmap(Bitmap val, int flags) {
        if (isParcel) {
            parcel.writeParcelable(val, flags);
        } else {
            byte[] bytes = ImageUtils.toPngBytes(val);
            Asset asset = Asset.createFromBytes(bytes);
            dataMap.putAsset(key(), asset);
        }
    }

    public <T extends Shareable> T readShareable(ClassLoader loader) {
        if (isParcel) {
            return parcel.readParcelable(loader);
        } else {
            DataMap shareableMap = dataMap.getDataMap(key());
            Shareable.Creator<T> creator = readShareableCreator(shareableMap, loader);
            if (creator != null) {
                int storedVersion = readShareableVersion(shareableMap);
                return creator.createFromDataMap(apiClient, shareableMap, storedVersion);
            }
            return null;
        }
    }

    public void writeShareable(Shareable val, int parcelableFlags) {
        if (isParcel) {
            parcel.writeParcelable(val, parcelableFlags);
        } else {
            // Store in a new data map so that we only ever take up one key for the shareable object
            DataMap shareableMap = null;
            if (val != null) {
                shareableMap = new DataMap();

                // write class name
                String name = val.getClass().getName();
                writeShareableName(name, shareableMap);

                // write data version
                int version = val.getVersion();
                writeShareableVersion(version, shareableMap);

                // write shareable data
                val.writeToDataMap(apiClient, shareableMap);
            }
            dataMap.putDataMap(key(), shareableMap);
        }
    }

    public int dataPosition() {
        if (isParcel) {
            return parcel.dataPosition();
        } else {
            return position;
        }
    }

    public void setDataPosition(int position) {
        if (isParcel) {
            parcel.setDataPosition(position);
        } else {
            this.position = position;
        }
    }

    private String readShareableName(DataMap dataMap) {
        return dataMap.getString(shareableNameKey(), null);
    }

    private void writeShareableName(String name, DataMap dataMap) {
        dataMap.putString(shareableNameKey(), name);
    }

    private int readShareableVersion(DataMap dataMap) {
        return dataMap.getInt(shareableVersionKey(), -1);
    }

    private void writeShareableVersion(int version, DataMap dataMap) {
        dataMap.putInt(shareableVersionKey(), version);
    }

    private String shareableNameKey() {
        return KEY_PREFIX + "Name";
    }

    private String shareableVersionKey() {
        return KEY_PREFIX + "Version";
    }

    private String key() {
        return KEY_PREFIX + position++;
    }

    /**
     * Reads the CREATOR field from a Shareable object.
     *
     * @param loader
     *         the class loader
     * @param <T>
     *         the type of Shareable
     *
     * @return the CREATOR field
     */
    @SuppressWarnings("unchecked")
    private <T extends Shareable> Shareable.Creator<T> readShareableCreator(DataMap dataMap,
            ClassLoader loader) {
        String name;
        if (dataMap == null || (name = readShareableName(dataMap)) == null) {
            return null;
        }

        Shareable.Creator<T> creator;
        synchronized (sCreators) {
            HashMap<String, Shareable.Creator> map = sCreators.get(loader);
            if (map == null) {
                map = new HashMap<String, Shareable.Creator>();
                sCreators.put(loader, map);
            }
            creator = map.get(name);
            if (creator == null) {
                try {
                    Class c = loader == null
                            ? Class.forName(name)
                            : Class.forName(name, true, loader);
                    Field f = c.getField("CREATOR");
                    creator = (Shareable.Creator) f.get(null);
                } catch (IllegalAccessException e) {
                    WearLog.e(TAG, "Illegal access when unmarshalling: " + name, e);
                    throw new BadShareableException("IllegalAccessException when unmarshalling: " + name);
                } catch (ClassNotFoundException e) {
                    WearLog.e(TAG, "Class not found when unmarshalling: " + name, e);
                    // Because data items are persisted across app versions, don't throw here
                    // The Shareable object could have changed names, so just return null
                    return null;
                } catch (ClassCastException e) {
                    throw new BadShareableException("Shareable protocol requires a "
                            + "Shareable.Creator object called "
                            + " CREATOR on class " + name);
                } catch (NoSuchFieldException e) {
                    throw new BadShareableException("Shareable protocol requires a "
                            + "Shareable.Creator object called "
                            + " CREATOR on class " + name);
                } catch (NullPointerException e) {
                    throw new BadShareableException("Shareable protocol requires "
                            + "the CREATOR object to be static on class " + name);
                }
                if (creator == null) {
                    throw new BadShareableException("Shareable protocol requires a "
                            + "Shareable.Creator object called "
                            + " CREATOR on class " + name);
                }

                map.put(name, creator);
            }
        }

        return creator;
    }
}
