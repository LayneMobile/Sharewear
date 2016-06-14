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

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

public class WearRunnerService extends AbstractWearApiService {
    private static final String TAG = WearRunnerService.class.getSimpleName();

    private static final ServiceRunners<Runner> sRunners = ServiceRunners.create();

    public WearRunnerService() {
        super(TAG);
    }

    public static void run(@NonNull Context context, @NonNull Runner runner) {
        context.startService(newIntent(context, runner));
    }

    public static Intent newIntent(@NonNull Context context, @NonNull Runner runner) {
        Intent intent = new Intent(context, WearRunnerService.class);
        sRunners.add(intent, runner);
        return intent;
    }

    @Override
    protected void onHandleIntentConnected(@NonNull GoogleApiClient apiClient,
            @Nullable Intent intent) {
        final Runner runner = sRunners.get(intent);
        if (runner != null) {
            runner.run(this, apiClient);
        }
    }

    @Override
    protected void onHandleIntentFailedConnecting(@NonNull ConnectionResult connectionResult,
            @Nullable Intent intent) {
        final Runner runner = sRunners.get(intent);
        if (runner != null) {
            runner.onFailedConnecting(this, connectionResult);
        }
    }

    public interface Runner extends ServiceRunners.Runner {
        void run(@NonNull Context context, @NonNull GoogleApiClient apiClient);

        void onFailedConnecting(@NonNull Context context, @NonNull ConnectionResult connectionResult);
    }

    public interface IntentRunner extends Runner, ServiceRunners.IntentRunner { }
}
