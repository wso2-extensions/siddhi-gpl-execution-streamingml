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
import org.wso2.extension.siddhi.gpl.execution.streamingml.regression.RegressionPrequentialModelEvaluation;
import org.wso2.extension.siddhi.gpl.execution.streamingml.regression.Regressor;
import org.wso2.siddhi.query.api.exception.SiddhiAppValidationException;

/**
 *
 */
public class AdaptiveModelRulesModel extends AbstractRegressor implements Regressor {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(AdaptiveModelRulesModel.class);

    private AMRulesRegressor amRulesRegressor;

    private int noOfFeatures;
    private String modelName;
    private boolean initialized = false;

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
        //training on the event instance
        trainInstance.setDataset(streamHeader);
        double[] prediction = amRulesRegressor.getVotesForInstance(trainInstance);
        amRulesRegressor.trainOnInstanceImpl(trainInstance);
        return RegressionPrequentialModelEvaluation.regressionMeasure(cepEvent, prediction);
    }

    @Override
    public Object[] getPrediction(double[] cepEvent) {
        Instance testInstance = createMOAInstance(cepEvent);
        double votes = amRulesRegressor.getVotesForInstance(testInstance)[0];
        double weightedError = amRulesRegressor.newErrorWeightedVote().getWeightedError();
        return new Object[]{votes, weightedError};
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

    public int getNoOfFeatures() {
        return noOfFeatures;
    }

    @Override
    protected void prepareForUseImpl(TaskMonitor taskMonitor, ObjectRepository objectRepository) {

    }


}
