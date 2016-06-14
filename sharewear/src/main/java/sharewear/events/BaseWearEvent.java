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

public abstract class BaseWearEvent implements WearEvent {
    @Override
    public boolean canHandlePath(String path) {
        return path != null && path.startsWith(getPathPrefix());
    }

    protected String makePath(String... segments) {
        if (segments == null) {
            return "null";
        }

        StringBuilder path = new StringBuilder();
        boolean first = true;
        for (String segment : segments) {
            if (!first || !segment.startsWith("/")) {
                path.append("/");
            }
            first = false;
            path.append(segment);
        }
        return path.toString();
    }
}
