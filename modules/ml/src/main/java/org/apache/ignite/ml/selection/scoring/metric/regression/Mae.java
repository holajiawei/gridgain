/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.ml.selection.scoring.metric.regression;

import org.apache.ignite.ml.selection.scoring.evaluator.aggregator.RegressionMetricStatsAggregator;
import org.apache.ignite.ml.selection.scoring.evaluator.context.EmptyContext;
import org.apache.ignite.ml.selection.scoring.metric.Metric;
import org.apache.ignite.ml.selection.scoring.metric.MetricName;

/**
 * Class for mean absolute error metric.
 */
public class Mae implements Metric<Double, EmptyContext, RegressionMetricStatsAggregator> {
    /** Serial version uid. */
    private static final long serialVersionUID = -5154991220994862827L;

    /** Value. */
    private double value = Double.NaN;

    /** {@inheritDoc} */
    @Override public RegressionMetricStatsAggregator makeAggregator() {
        return new RegressionMetricStatsAggregator();
    }

    /** {@inheritDoc} */
    @Override
    public Mae initBy(RegressionMetricStatsAggregator aggr) {
        value = aggr.getMAE();
        return this;
    }

    /** {@inheritDoc} */
    @Override public double value() {
        return value;
    }

    /** {@inheritDoc} */
    @Override public MetricName name() {
        return MetricName.MAE;
    }
}
