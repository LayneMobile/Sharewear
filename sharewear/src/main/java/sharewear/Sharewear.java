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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import sharewear.cache.NodeCache;
import sharewear.internal.WearLog;
import sharewear.util.GooglePlayServicesHelper;
import sharewear.util.Logger;

public final class Sharewear {
    private static final String TAG = Sharewear.class.getSimpleName();

    private Sharewear() {}

    /**
     * Initializes the common module. Should be done in the Application onCreate() method.
     *
     * @param context
     *         the context
     */
    public static void initialize(@NonNull Context context) {
        Context appContext = context.getApplicationContext();

        // sync connected nodes
        NodeCache.getInstance()
                .init(appContext);

        // Check play services
        GooglePlayServicesHelper.checkPlayServices(context);

        // TODO: add other items as needed
    }

    /**
     * Sets the logger used by this library.
     *
     * @param logger
     *         the logger
     */
    public static void setLogger(@Nullable Logger logger) {
        WearLog.setLogger(logger);
    }

    public static void setGooglePlayServicesHelperErrorOptions(GooglePlayServicesHelper.ErrorOptions errorOptions) {
        GooglePlayServicesHelper.setErrorOptions(errorOptions);
    }

    /**
     * Registers a group of handlers for wear communication events.
     *
     * @param wearHandlers
     *         the handlers to register
     */
    public static void registerWearHandlers(WearHandler... wearHandlers) {
        WearService.registerHandlers(wearHandlers);
    }

    /**
     * Registers a handler for wear communication events.
     *
     * @param wearHandler
     *         the handler to register
     */
    public static void registerWearHandler(WearHandler wearHandler) {
        WearService.registerHandler(wearHandler);
    }

    /**
     * Unregisters a handler from receiver wear communication events.
     *
     * @param wearHandler
     *         the handler to unregister
     */
    public static void unregisterWearHandler(WearHandler wearHandler) {
        WearService.unregisterHandler(wearHandler);
    }
}
