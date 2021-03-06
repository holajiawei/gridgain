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

/**
 * <!-- Package description. -->
 * Package that contains basic primitives build on top of {@link org.apache.ignite.ml.dataset.Dataset}. Primitives are
 * simple components that can be used in other algorithms based on the dataset infrastructure or for debugging.
 *
 * Primitives include partition {@code context} implementations, partition {@code data} implementations and extensions
 * of dataset. Partition {@code context} and {@code data} implementations can be used in other algorithm in case these
 * algorithm doesn't need to keep specific data and can work with standard primitive {@code data} or {@code context}.
 * Extensions of dataset provides basic most often used functions that can be used for debugging or data analysis.
 */
package org.apache.ignite.ml.dataset.primitive;