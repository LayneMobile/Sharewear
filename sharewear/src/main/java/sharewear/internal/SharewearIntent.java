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

public final class SharewearIntent {
    private static final String BASE = "sharewear.intent.";

    private static final String ACTION = BASE + "action.";
    public static final String ACTION_PUT = ACTION + "PUT";
    public static final String ACTION_DELETE = ACTION + "DELETE";

    private static final String EXTRA = BASE + "extra.";
    public static final String EXTRA_SHARED_DATA = EXTRA + "SharedData";
    public static final String EXTRA_URI = EXTRA + "Uri";
    public static final String EXTRA_RUNNER = EXTRA + "Runner";
    public static final String EXTRA_RUNNER_ID = EXTRA + "RunnerId";
    public static final String EXTRA_NODE_ID = EXTRA + "NodeId";
    public static final String EXTRA_PATH = EXTRA + "Path";
    public static final String EXTRA_DATA = EXTRA + "Data";

    private SharewearIntent() {}
}
