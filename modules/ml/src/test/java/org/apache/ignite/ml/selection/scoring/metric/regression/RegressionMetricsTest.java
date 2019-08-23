package org.apache.ignite.ml.selection.scoring.metric.regression;

import java.util.HashMap;
import java.util.Map;
import org.apache.ignite.ml.IgniteModel;
import org.apache.ignite.ml.math.primitives.vector.Vector;
import org.apache.ignite.ml.math.primitives.vector.VectorUtils;
import org.apache.ignite.ml.selection.scoring.evaluator.EvaluationResult;
import org.apache.ignite.ml.selection.scoring.evaluator.Evaluator;
import org.apache.ignite.ml.selection.scoring.metric.MetricName;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for regression metrics.
 */
public class RegressionMetricsTest {
    /** */
    @Test
    public void testCalculation() {
        Map<Vector, Double> linearset = new HashMap<Vector, Double>() {{
           put(VectorUtils.of(0.), 0.);
           put(VectorUtils.of(1.), 1.);
           put(VectorUtils.of(2.), 2.);
           put(VectorUtils.of(3.), 3.);
        }};

        IgniteModel<Vector, Double> linearModel = v -> v.get(0);
        IgniteModel<Vector, Double> squareModel = v -> Math.pow(v.get(0), 2);

        EvaluationResult linearRes = Evaluator.evaluateRegression(linearset, linearModel, Vector::labeled);
        assertEquals(0., linearRes.get(MetricName.MAE), 0.01);
        assertEquals(0., linearRes.get(MetricName.MSE), 0.01);
        assertEquals(0., linearRes.get(MetricName.R2), 0.01);
        assertEquals(0., linearRes.get(MetricName.RSS), 0.01);
        assertEquals(0., linearRes.get(MetricName.RMSE), 0.01);

        EvaluationResult squareRes = Evaluator.evaluateRegression(linearset, squareModel, Vector::labeled);
        assertEquals(2., squareRes.get(MetricName.MAE), 0.01);
        assertEquals(10., squareRes.get(MetricName.MSE), 0.01);
        assertEquals(8., squareRes.get(MetricName.R2), 0.01);
        assertEquals(40., squareRes.get(MetricName.RSS), 0.01);
        assertEquals(Math.sqrt(10), squareRes.get(MetricName.RMSE), 0.01);
    }
}
