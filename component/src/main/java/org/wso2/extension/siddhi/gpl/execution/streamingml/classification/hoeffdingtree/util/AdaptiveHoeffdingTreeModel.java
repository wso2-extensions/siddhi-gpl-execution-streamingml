/*
 * Copyright (C) 2017 WSO2 Inc. (http://wso2.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.wso2.extension.siddhi.gpl.execution.streamingml.classification.hoeffdingtree.util;

import com.yahoo.labs.samoa.instances.Attribute;
import com.yahoo.labs.samoa.instances.DenseInstance;
import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.Instances;
import com.yahoo.labs.samoa.instances.InstancesHeader;
import moa.classifiers.trees.HoeffdingAdaptiveTree;
import moa.core.ObjectRepository;
import moa.options.AbstractOptionHandler;
import moa.streams.InstanceStream;
import moa.tasks.TaskMonitor;
import org.apache.log4j.Logger;
import org.wso2.extension.siddhi.gpl.execution.streamingml.classification.ClassifierPrequentialModelEvaluation;
import org.wso2.extension.siddhi.gpl.execution.streamingml.util.CoreUtils;
import org.wso2.extension.siddhi.gpl.execution.streamingml.util.MathUtil;
import org.wso2.siddhi.core.exception.SiddhiAppRuntimeException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents the Hoeffding Adaptive Tree Model
 */
public class AdaptiveHoeffdingTreeModel extends AbstractOptionHandler {
    private static final long SERIAL_VERSION_UID = 1L;
    private static final Logger LOGGER = Logger.getLogger(AdaptiveHoeffdingTreeModel.class);

    private String modelName;
    private InstancesHeader streamHeader;
    private int noOfFeatures;
    private int noOfClasses;
    private HoeffdingAdaptiveTree hoeffdingAdaptiveTree;
    private List<String> classes = new ArrayList<String>();

    @Override
    public void getDescription(StringBuilder stringBuilder, int i) {
        LOGGER.info("Hoeffding Adaptive Tree for evolving data streams that uses ADWIN to replace "
                + "branches for new ones.");
    }

    public AdaptiveHoeffdingTreeModel(String modelName) {
        this.modelName = modelName;
    }

    public AdaptiveHoeffdingTreeModel(AdaptiveHoeffdingTreeModel model) {
        this.modelName = model.modelName;
        this.streamHeader = model.streamHeader;
        this.noOfFeatures = model.noOfFeatures;
        this.noOfClasses = model.noOfClasses;
        this.hoeffdingAdaptiveTree = model.hoeffdingAdaptiveTree;
        this.classes = model.classes;
    }

    /**
     * Initialize the model with input stream definition.
     *
     * @param noOfAttributes number of feature attributes
     * @param noOfClasses    number of classes
     */
    public void init(int noOfAttributes, int noOfClasses) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("Model [%s] is being initialized.", this.modelName));
        }
        this.noOfFeatures = noOfAttributes;
        this.noOfClasses = noOfClasses;
        this.streamHeader = createMOAInstanceHeader(this.noOfFeatures);
        this.hoeffdingAdaptiveTree = new HoeffdingAdaptiveTree();
        this.hoeffdingAdaptiveTree.setModelContext(streamHeader);
        this.hoeffdingAdaptiveTree.prepareForUse();
    }

    /**
     * Configure Hoeffding Adaptive Tree Model with hyper-parameters.
     *
     * @param gracePeriod            number of instances a leaf should observe between split attempts.
     * @param splittingCriteria      Split criterion to use. 0:InfoGainSplitCriterion, 1:GiniSplitCriterion".
     * @param allowableSplitError    Allowable error in split decision, values closer to 0 will take.
     * @param breakTieThreshold      Threshold below which a split will be forced to break ties.
     * @param binarySplitOption      Allow binary splits.
     * @param disablePrePruning      Disable pre-pruning.
     * @param leafpredictionStrategy Leaf prediction to use.
     */
    public void setConfigurations(int gracePeriod, int splittingCriteria,
                                  double allowableSplitError, double breakTieThreshold,
                                  boolean binarySplitOption, boolean disablePrePruning,
                                  int leafpredictionStrategy) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("Model [%s] is being configured with hyper-parameters.", this.modelName));
        }
        hoeffdingAdaptiveTree.gracePeriodOption.setValue(gracePeriod);
        if (splittingCriteria == 0) {
            hoeffdingAdaptiveTree.splitCriterionOption
                    .setValueViaCLIString("InfoGainSplitCriterion");
        } else {
            hoeffdingAdaptiveTree.splitCriterionOption
                    .setValueViaCLIString("GiniSplitCriterion");
        }
        hoeffdingAdaptiveTree.splitConfidenceOption.setValue(allowableSplitError);
        hoeffdingAdaptiveTree.tieThresholdOption.setValue(breakTieThreshold);
        hoeffdingAdaptiveTree.binarySplitsOption.setValue(binarySplitOption);
        hoeffdingAdaptiveTree.noPrePruneOption.setValue(disablePrePruning);
        hoeffdingAdaptiveTree.leafpredictionOption.setChosenIndex(leafpredictionStrategy);
    }

    /**
     * Train the model on event instance
     * @param cepEvent   event data
     * @param classLabel class  label of the cepEvent
     */
    public void trainOnEvent(double[] cepEvent, String classLabel) {
        cepEvent[noOfFeatures - 1] = addClass(classLabel);
        Instance trainInstance = createMOAInstance(cepEvent);
        trainInstance.setClassValue(cepEvent[noOfFeatures - 1]);
        //training on the event instance
        hoeffdingAdaptiveTree.trainOnInstanceImpl(trainInstance);
    }

    /**
     * Calculate prequential accuracy of the model
     * @param modelEvaluation Prequential Model Evaluator.
     * @param cepEvent        event data
     * @param classValue      class label of the cepEvent
     * @return Prequential accuracy
     */
    public double evaluationTrainOnEvent(ClassifierPrequentialModelEvaluation modelEvaluation,
                                         double[] cepEvent, String classValue) {
        int classIndex = cepEvent.length - 1;
        //create instance with only the feature attributes
        double[] test = Arrays.copyOfRange(cepEvent, 0, classIndex);
        Instance testInstance = createMOAInstance(test);
        double[] votes = hoeffdingAdaptiveTree.getVotesForInstance(testInstance);
        cepEvent[classIndex] = getClasses().indexOf(classValue);
        Instance trainInstance = createMOAInstance(cepEvent);
        hoeffdingAdaptiveTree.trainOnInstanceImpl(trainInstance);
        modelEvaluation.addResult(trainInstance, votes);
        return MathUtil.roundOff(modelEvaluation.getFractionCorrectlyClassified(), 3);
    }

    /**
     * Predict the class label for event with fearure attributes
     * @param cepEvent Event data.
     * @return predicted class index, probability of the prediction.
     */
    public Object[] getPrediction(double[] cepEvent) {
        Instance testInstance = createMOAInstance(cepEvent);
        double[] votes = hoeffdingAdaptiveTree.getVotesForInstance(testInstance);
        int classIndex = CoreUtils.argMaxIndex(votes);
        double confidenceLevel = getPredictionConfidence(votes);
        return new Object[]{classIndex, confidenceLevel};
    }

    /**
     * Convert CEP event into MOA instance
     * @param cepEvent Event Data
     * @return represents a single Event
     */
    private Instance createMOAInstance(double[] cepEvent) {
        Instance instance = new DenseInstance(1.0D, cepEvent);
        //set schema header for the instance
        instance.setDataset(streamHeader);
        return instance;
    }

    /**
     * Create MOAInstance schema
     * @param numberOfAttributes
     * @return Schema for MOAInstances
     */
    private InstancesHeader createMOAInstanceHeader(int numberOfAttributes) {
        List<Attribute> attributes = new ArrayList<Attribute>();
        for (int i = 0; i < numberOfAttributes - 1; i++) {
            attributes.add(new Attribute("numeric" + (i + 1)));
        }
        // Add class value
        List<String> classLabels = new ArrayList<String>();
        for (int i = 0; i < this.noOfClasses; i++) {
            classLabels.add("class" + (i + 1));
        }
        attributes.add(new Attribute("class", classLabels));
        InstancesHeader streamHeader = new InstancesHeader(new Instances(getCLICreationString(InstanceStream.class),
                attributes, 0));
        streamHeader.setClassIndex(noOfFeatures - 1);
        return streamHeader;
    }

    /**
     * Add a Class label
     * @param label
     * @return
     */
    private int addClass(String label) {
        // Set class value
        if (classes.contains(label)) {
            return classes.indexOf(label);
        } else {
            if (classes.size() < noOfClasses) {
                classes.add(label);
                return classes.indexOf(label);
            } else {
                throw new SiddhiAppRuntimeException(String.format("Number of classes %s is expected from the model "
                        + "%s but found %s", noOfClasses, modelName, classes.size()));
            }
        }
    }

    /**
     * Calculate prediction confidence
     * @param votes-Vote prediction for the class labels
     * @return Prediction confidence to the 3rd decimal point
     */
    private double getPredictionConfidence(double[] votes) {
        return MathUtil.roundOff((CoreUtils.argMax(votes) / MathUtil.sum(votes)), 3);
    }

    public String getModelName() {
        return modelName;
    }

    public InstancesHeader getStreamHeader() {
        return this.streamHeader;
    }

    public List<String> getClasses() {
        return this.classes;
    }

    public int getNoOfFeatures() {
        return this.noOfFeatures;
    }

    @Override
    protected void prepareForUseImpl(TaskMonitor taskMonitor, ObjectRepository objectRepository) {
    }
}
