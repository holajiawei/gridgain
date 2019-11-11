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

package org.apache.ignite.internal.processors.query.h2.opt;

import org.apache.ignite.internal.processors.query.h2.database.H2TreeIndex;
import org.h2.index.Index;

import java.util.ArrayList;

/**
 * Factory for system table indexes.
 */
public interface GridH2SystemIndexFactory {
    /**
     * Create list of indexes. First must be primary key, after that all unique indexes and
     * only then non-unique indexes.
     * All indexes must be subtypes of {@link H2TreeIndex}.
     *
     * @param tbl Table to create indexes for.
     * @return List of indexes.
     */
    ArrayList<Index> createSystemIndexes(GridH2Table tbl);
}