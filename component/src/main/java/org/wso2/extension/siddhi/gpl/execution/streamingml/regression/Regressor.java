package org.wso2.extension.siddhi.gpl.execution.streamingml.regression;

/**
 *
 */
public interface Regressor {

    double trainOnEvent(double[] cepEvent);

    Object[] getPrediction(double[] cepEvent);
}
