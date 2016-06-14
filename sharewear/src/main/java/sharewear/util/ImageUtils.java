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

package sharewear.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import sharewear.WearUtils;
import sharewear.internal.WearLog;

/**
 * Class with static functions to aid in common Image/UI tasks.
 */
public final class ImageUtils {
    private static final String TAG = ImageUtils.class.getSimpleName();

    private ImageUtils() {}

    /**
     * Converts a byte array to a {@link Bitmap}.
     *
     * @param bytes
     *         the byte array to convert
     *
     * @return the Bitmap or null if it could not be converted
     */
    public static Bitmap toBitmap(byte[] bytes) {
        Bitmap bitmap = null;
        if (bytes != null && bytes.length > 0) {
            ByteArrayInputStream is = new ByteArrayInputStream(bytes);
            bitmap = BitmapFactory.decodeStream(is);
            WearUtils.closeQuietly(is);
        }
        return bitmap;
    }

    /**
     * Converts a {@link Bitmap} into a byte array.
     *
     * @param bitmap
     *         the bitmap to convert
     *
     * @return the byte array or null if it could not be converted
     */
    public static byte[] toPngBytes(Bitmap bitmap) {
        return toBytes(bitmap, Bitmap.CompressFormat.PNG, 100);
    }

    /**
     * Converts a {@link Bitmap} into a byte array using a specified format and quality.
     *
     * @param bitmap
     *         the bitmap to convert
     * @param format
     *         the conversion format
     * @param quality
     *         the quality of the output
     *
     * @return the byte array or null if it could not be converted
     */
    public static byte[] toBytes(Bitmap bitmap, Bitmap.CompressFormat format, int quality) {
        byte[] bytes = null;
        if (bitmap != null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bitmap.compress(format, quality, out);
            bytes = out.toByteArray();
            WearUtils.closeQuietly(out);
        }
        return bytes;
    }

    public static Bitmap fromAsset(@NonNull GoogleApiClient apiClient, @NonNull Asset asset) {
        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(apiClient, asset)
                .await()
                .getInputStream();

        if (assetInputStream == null) {
            WearLog.w(TAG, "Requested an unknown Asset.");
            return null;
        }

        return BitmapFactory.decodeStream(assetInputStream);
    }
}
