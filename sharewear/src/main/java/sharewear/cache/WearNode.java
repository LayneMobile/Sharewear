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

package sharewear.cache;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.wearable.Node;

final class WearNode implements Node {
    private final Node actual;

    private WearNode(@NonNull Node actual) {
        this.actual = actual;
    }

    @Nullable
    static WearNode create(@Nullable Node node) {
        if (node != null) {
            return new WearNode(node);
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WearNode node = (WearNode) o;

        String id = getId();
        if (id != null ? !id.equals(node.getId()) : node.getId() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        String id = getId();
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String getId() {
        return actual.getId();
    }

    @Override
    public String getDisplayName() {
        return actual.getDisplayName();
    }

    @Override
    public boolean isNearby() {
        return actual.isNearby();
    }
}
