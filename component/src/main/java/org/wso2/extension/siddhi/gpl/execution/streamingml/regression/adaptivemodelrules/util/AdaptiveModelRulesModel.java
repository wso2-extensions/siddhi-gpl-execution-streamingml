/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.extension.siddhi.gpl.execution.streamingml.regression.adaptivemodelrules.util;

import com.yahoo.labs.samoa.instances.Instance;
import moa.classifiers.core.driftdetection.ADWINChangeDetector;
import moa.classifiers.core.driftdetection.ChangeDetector;
import moa.classifiers.rules.AMRulesRegressor;
import moa.classifiers.rules.core.anomalydetection.AnomalinessRatioScore;
import moa.classifiers.rules.core.anomalydetection.AnomalyDetector;
import moa.classifiers.rules.core.anomalydetection.NoAnomalyDetection;
import moa.classifiers.rules.core.anomalydetection.OddsRatioScore;
import moa.classifiers.rules.core.changedetection.NoChangeDetection;
import moa.core.ObjectRepository;
import moa.options.ClassOption;
import moa.tasks.TaskMonitor;
import org.apache.log4j.Logger;
import org.wso2.extension.siddhi.gpl.execution.streamingml.regression.AbstractRegressor;
import org.wso2.extension.siddhi.gpl.execution.streamingml.regression.Regressor;
import org.wso2.extension.siddhi.gpl.execution.streamingml.util.MathUtil;
import org.wso2.siddhi.query.api.exception.SiddhiAppValidationException;

/**
 * AMRules Regressor wrapper class
 */
public class AdaptiveModelRulesModel extends AbstractRegressor implements Regressor {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(AdaptiveModelRulesModel.class);

    private AMRulesRegressor amRulesRegressor;

    private int noOfFeatures;
    private String modelName;
    private boolean initialized = false;
    private int noOfInstances = 0;
    private double squaredError = 0;
    private double meanSquaredError = 0;

    public AdaptiveModelRulesModel(String modelName) {
        this.modelName = modelName;
    }

    public AdaptiveModelRulesModel(AdaptiveModelRulesModel model) {
        this.modelName = model.modelName;
        this.streamHeader = model.streamHeader;
        this.noOfFeatures = model.noOfFeatures;
    }

    @Override
    public void getDescription(StringBuilder stringBuilder, int i) {
        logger.info("Adaptive Model Rules Model for learning regression rules with streaming data");
    }

    /**
     * Initialize the model with input stream definition.
     *
     * @param noOfAttributes number of attributes including features and target
     */
    public void init(int noOfAttributes) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Regressor model [%s] is being initialized.", this.modelName));
        }
        this.noOfFeatures = noOfAttributes - 1;
        generateHeader(noOfAttributes);
        amRulesRegressor = new AMRulesRegressor();
        amRulesRegressor.setModelContext(streamHeader);
        amRulesRegressor.prepareForUse();
        initialized = true;
    }

    /**
     * @param cepEvent event data
     */
    @Override
    public double trainOnEvent(double[] cepEvent) {
        Instance trainInstance = createMOAInstance(cepEvent);
        trainInstance.setClassValue(cepEvent[cepEvent.length - 1]);
        trainInstance.setDataset(streamHeader);

        double truth = cepEvent[cepEvent.length - 1];
        double prediction = MathUtil.roundOff(amRulesRegressor.getVotesForInstance(trainInstance)[0], 3);

        //training on the event instance
        amRulesRegressor.trainOnInstanceImpl(trainInstance);
        return calMeanSquaredError(truth, prediction);
    }

    @Override
    public Object[] getPrediction(double[] cepEvent) {
        Instance testInstance = createMOAInstance(cepEvent);
        double votes = MathUtil.roundOff(amRulesRegressor.getVotesForInstance(testInstance)[0], 3);
        return new Object[]{votes, meanSquaredError};
    }

    /**
     * @return
     */
    public boolean isInitialized() {
        return initialized;
    }

    public boolean isValidStreamHeader(int noOfFeatures) {
        boolean validStreamHeader = true;
        if (noOfFeatures != this.noOfFeatures) {
            validStreamHeader = false;
        }
        return validStreamHeader;
    }


    public void setConfigurations(double splitConfidence, double tieBreakThreshold, int gracePeriod,
                                  int changeDetector, int anomalyDetector) {
        amRulesRegressor.splitConfidenceOption.setValue(splitConfidence);
        amRulesRegressor.tieThresholdOption.setValue(tieBreakThreshold);
        amRulesRegressor.gracePeriodOption.setValue(gracePeriod);

        switch (changeDetector) {
            case 0:
                amRulesRegressor.changeDetector = new ClassOption("changeDetector", 'H',
                        "Change Detector.", ChangeDetector.class, NoChangeDetection.class.getName());
                break;
            case 1:
                amRulesRegressor.changeDetector = new ClassOption("changeDetector", 'H',
                        "Change Detector.", ChangeDetector.class, ADWINChangeDetector.class.getName());
                break;
            case 2:
                amRulesRegressor.changeDetector = new ClassOption("changeDetector", 'H',
                        "Change Detector.", ChangeDetector.class, "PageHinkleyDM -d 0.05 -l 35.0");
                break;
            default:
                throw new SiddhiAppValidationException(String.format("Input for Change Detector hyper-parameter "
                        + "needs to be either 0,1,2. But found %s. %n "
                        + "0:NoChangeDetection, 1:ADWINChangeDetector, 2:PageHinkleyDM", changeDetector));
        }

        switch (anomalyDetector) {
            case 0:
                amRulesRegressor.anomalyDetector = new ClassOption("anomalyDetector", 'A',
                        "Anomaly Detector.", AnomalyDetector.class, NoAnomalyDetection.class.getName());
                break;
            case 1:
                amRulesRegressor.anomalyDetector = new ClassOption("anomalyDetector", 'A',
                        "Anomaly Detector.", AnomalyDetector.class, AnomalinessRatioScore.class.getName());
                break;
            case 2:
                amRulesRegressor.anomalyDetector = new ClassOption("anomalyDetector", 'A',
                        "Anomaly Detector.", AnomalyDetector.class, OddsRatioScore.class.getName());
                break;
            default:
                throw new SiddhiAppValidationException(String.format("Input for Anomaly Detector hyper-parameter "
                        + "needs to be either 0,1,2. But found %s.%n"
                        + " 0:NoAnomalyDetection, 1:AnomalinessRatioScore, 2:OddsRatioScore", anomalyDetector));
        }
    }

    private double calMeanSquaredError(double truth, double prediction) {
        noOfInstances++;
        squaredError += Math.pow((truth - prediction), 2);
        return meanSquaredError = MathUtil.roundOff((squaredError / noOfInstances), 3);
    }

    public int getNoOfFeatures() {
        return noOfFeatures;
    }

    @Override
    protected void prepareForUseImpl(TaskMonitor taskMonitor, ObjectRepository objectRepository) {

    }
}
