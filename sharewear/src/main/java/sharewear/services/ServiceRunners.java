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

package sharewear.services;

import android.content.Intent;
import android.os.Parcelable;
import android.util.SparseArray;

import java.util.concurrent.atomic.AtomicInteger;

import sharewear.internal.SharewearIntent;

public class ServiceRunners<T extends ServiceRunners.Runner> {
    private final AtomicInteger counter = new AtomicInteger();
    private final SparseArray<T> runners = new SparseArray<T>();

    private ServiceRunners() {}

    public static <T extends Runner> ServiceRunners<T> create() {
        return new ServiceRunners<T>();
    }

    public final void add(Intent intent, T runner) {
        if (runner instanceof IntentRunner) {
            intent.putExtra(SharewearIntent.EXTRA_RUNNER, (IntentRunner) runner);
        } else {
            int id = counter.getAndIncrement();
            intent.putExtra(SharewearIntent.EXTRA_RUNNER_ID, id);
            synchronized (runners) {
                runners.put(id, runner);
            }
        }
    }

    public final T get(Intent intent) {
        int id = intent.getIntExtra(SharewearIntent.EXTRA_RUNNER_ID, -1);
        if (id != -1) {
            synchronized (runners) {
                T runner = runners.get(id);
                runners.remove(id);
                return runner;
            }
        }
        return getIntentRunner(intent);
    }

    private static <T extends IntentRunner> T getIntentRunner(Intent intent) {
        return intent.getParcelableExtra(SharewearIntent.EXTRA_RUNNER);
    }

    /**
     * Marker interface for a Service Runner
     */
    public interface Runner { }

    /**
     * Marker interface for a Service Intent Runner
     */
    public interface IntentRunner extends Runner, Parcelable { }
}
