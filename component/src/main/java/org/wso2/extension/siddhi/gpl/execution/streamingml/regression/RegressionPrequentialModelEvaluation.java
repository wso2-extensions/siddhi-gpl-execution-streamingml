package org.wso2.extension.siddhi.gpl.execution.streamingml.regression;

/**
 *
 */
public class RegressionPrequentialModelEvaluation {

    public static double regressionMeasure(double[] truth, double[] prediction) {
        if (truth.length != prediction.length) {
            throw new IllegalArgumentException(String.format("The vector sizes don't match: %d != %d.",
                    truth.length, prediction.length));
        }

        int n = truth.length;
        double rss = 0.0;
        for (int i = 0; i < n; i++) {
            rss += Math.sqrt(truth[i] - prediction[i]);
        }
        return rss / n;
    }
}
