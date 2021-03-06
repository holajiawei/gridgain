/*
 * Copyright 2019 GridGain Systems, Inc. and Contributors.
 *
 * Licensed under the GridGain Community Edition License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gridgain.com/products/software/community-edition/gridgain-community-edition-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.ml.math.primitives.vector;

import java.util.Set;

/**
 * A named vector interface based on {@link Vector}. In addition to base vector functionality allows to set and get
 * elements using names as index.
 */
public interface NamedVector extends Vector {
    /**
     * Returns element with specified string index.
     *
     * @param idx Element string index.
     * @return Element value.
     */
    public double get(String idx);

    /**
     * Sets element with specified string index and value.
     *
     * @param idx Element string index.
     * @param val Element value.
     * @return This vector.
     */
    public NamedVector set(String idx, double val);

    /**
     * Returns list of string indexes used in this vector.
     *
     * @return List of string indexes used in this vector.
     */
    public Set<String> getKeys();
}
