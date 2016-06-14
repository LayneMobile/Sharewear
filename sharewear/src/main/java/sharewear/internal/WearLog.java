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

package sharewear.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;

import sharewear.util.GooglePlayServicesHelper;
import sharewear.util.Logger;

public final class WearLog {
    @NonNull private static volatile Logger sLogger = Logger.NONE;

    private WearLog() {}

    public static void setLogger(@Nullable Logger logger) {
        if (logger == null) { logger = Logger.NONE; }
        sLogger = logger;
    }

    public static void v(final String tag, final String msg) {
        sLogger.v(tag, msg);
    }

    public static void v(final String tag, final String format, final Object... args) {
        sLogger.v(tag, String.format(format, args));
    }

    public static void v(final String tag, final String msg, final Throwable tr) {
        sLogger.v(tag, msg, tr);
    }

    public static void d(final String tag, final String msg) {
        sLogger.d(tag, msg);
    }

    public static void d(final String tag, final String format, final Object... args) {
        sLogger.d(tag, String.format(format, args));
    }

    public static void d(final String tag, final String msg, final Throwable tr) {
        sLogger.d(tag, msg, tr);
    }

    public static void i(final String tag, final String msg) {
        sLogger.i(tag, msg);
    }

    public static void i(final String tag, final String format, final Object... args) {
        sLogger.i(tag, String.format(format, args));
    }

    public static void i(final String tag, final String msg, final Throwable tr) {
        sLogger.i(tag, msg, tr);
    }

    public static void w(final String tag, final String msg) {
        sLogger.w(tag, msg);
    }

    public static void w(final String tag, final String format, final Object... args) {
        sLogger.w(tag, String.format(format, args));
    }

    public static void w(final String tag, final String msg, final Throwable tr) {
        sLogger.w(tag, msg, tr);
    }

    public static void e(final String tag, final String msg) {
        sLogger.e(tag, msg);
    }

    public static void e(final String tag, final String format, final Object... args) {
        sLogger.e(tag, String.format(format, args));
    }

    public static void e(final String tag, final String msg, final Throwable tr) {
        sLogger.e(tag, msg, tr);
    }

    public static void e(String tag, ConnectionResult connectionResult, String msg) {
        if (GooglePlayServicesHelper.hasResolution(connectionResult)) {
            e(tag, msg);
        } else {
            w(tag, msg);
        }
    }

    public static void e(String tag, ConnectionResult connectionResult, String msg, Object... args) {
        if (GooglePlayServicesHelper.hasResolution(connectionResult)
                && connectionResult.getErrorCode() != ConnectionResult.SERVICE_MISSING) {
            e(tag, msg, args);
        } else {
            w(tag, msg, args);
        }
    }

    public static void e(String tag, ConnectionResult connectionResult, String msg, Throwable tr) {
        if (GooglePlayServicesHelper.hasResolution(connectionResult)
                && connectionResult.getErrorCode() != ConnectionResult.SERVICE_MISSING) {
            e(tag, msg, tr);
        } else {
            w(tag, msg, tr);
        }
    }
}
