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

package sharewear.events;

import android.support.annotation.Nullable;

import sharewear.SharedData;
import sharewear.SharedDataEvent;

public abstract class BaseWearDataEvent<T extends SharedData>
        extends BaseWearEvent
        implements WearDataEvent<T> {
    protected BaseWearDataEvent() {}

    @Override
    public boolean canHandleEvent(SharedDataEvent event) {
        return event != null && canHandlePath(event.getPath());
    }

    @Nullable
    @Override
    public T getData(SharedDataEvent event) {
        if (canHandleEvent(event)) {
            return event.getSharedData();
        }
        return null;
    }

    @Nullable
    @Override
    public T getData(SharedDataEvent event, ClassLoader classLoader) {
        if (canHandleEvent(event)) {
            return event.getSharedData(classLoader);
        }
        return null;
    }
}
