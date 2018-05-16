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
package org.wso2.extension.siddhi.gpl.execution.streamingml.util;

import org.wso2.extension.siddhi.gpl.execution.streamingml.classification.hoeffdingtree.util.AdaptiveHoeffdingTreeModel;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.SiddhiAppValidationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Common utils for Streaming Machine Learning tasks.
 */
public class CoreUtils {
    private static final List<Attribute.Type> numericTypes = Arrays.asList(Attribute.Type.INT,
            Attribute.Type.DOUBLE, Attribute.Type.LONG, Attribute.Type.FLOAT);
    private static final List<Attribute.Type> labelTypes = Arrays.asList(Attribute.Type.STRING, Attribute.Type.BOOL);

    /**
     * Get index of the Maximum from double array
     * @param doubleArray - array of doubles
     * @return index of the maximum
     */
    public static int argMaxIndex(double[] doubleArray) {
        double maximum = 0.0D;
        int maxIndex = 0;
        for (int i = 0; i < doubleArray.length; ++i) {
            if (i == 0 || doubleArray[i] > maximum) {
                maxIndex = i;
                maximum = doubleArray[i];
            }
        }
        return maxIndex;
    }

    /**
     * Get maximum value from the double array
     * @param doubleArray - array of doubles
     * @return maximum value of the double array
     */
    public static double argMax(double[] doubleArray) {
        double maximum = 0.0D;
        for (int i = 0; i < doubleArray.length; ++i) {
            if (i == 0 || doubleArray[i] > maximum) {
                maximum = doubleArray[i];
            }
        }
        return maximum;
    }

    /**
     * Validate and extract feature attribute executors
     * @param inputDefinition the incoming stream definition
     * @param attributeExpressionExecutors the executors of each function parameters
     * @param startIndex starting index of the feature attributes
     * @param noOfFeatures number of feature attributes
     * @return list of executors of feature attribute parameters
     */
    public static List<VariableExpressionExecutor> extractAndValidateFeatures(
            AbstractDefinition inputDefinition, ExpressionExecutor[]
            attributeExpressionExecutors,
            int startIndex, int noOfFeatures) {

        List<VariableExpressionExecutor> featureVariableExpressionExecutors = new ArrayList<>();

        // feature values start
        for (int i = startIndex; i < (startIndex + noOfFeatures); i++) {
            if (attributeExpressionExecutors[i] instanceof VariableExpressionExecutor) {
                featureVariableExpressionExecutors.add((VariableExpressionExecutor)
                        attributeExpressionExecutors[i]);
                // other attributes should be numeric type.
                String attributeName = ((VariableExpressionExecutor)
                        attributeExpressionExecutors[i]).getAttribute().getName();
                Attribute.Type featureAttributeType = inputDefinition.
                        getAttributeType(attributeName);

                //feature attributes not numerical type
                if (!isNumeric(featureAttributeType)) {
                    throw new SiddhiAppValidationException("model.features in " + (i + 1) + "th parameter is not "
                            + "a numerical type attribute. Found " + attributeExpressionExecutors[i].getReturnType()
                            + ". Check the input stream definition.");
                }
            } else {
                throw new SiddhiAppValidationException((i + 1) + "th parameter is not " +
                        "an attribute (VariableExpressionExecutor) present in the stream definition. Found a "
                        + attributeExpressionExecutors[i].getClass().getCanonicalName());
            }
        }
        return featureVariableExpressionExecutors;
    }

    public static boolean isNumeric(Attribute.Type attributeType) {
        return numericTypes.contains(attributeType);
    }

    public static boolean isLabelType(Attribute.Type attributeType) {
        return labelTypes.contains(attributeType);
    }

    /**
     * Validate and extract class label attribute executor
     * @param inputDefinition the incoming stream definition
     * @param attributeExpressionExecutors the executors of each function parameters
     * @param classIndex index of the class label
     * @return executor of class label parameter
     */
    public static VariableExpressionExecutor extractAndValidateClassLabel
    (AbstractDefinition inputDefinition, ExpressionExecutor[] attributeExpressionExecutors, int classIndex) {
        VariableExpressionExecutor classLabelVariableExecutor;

        if (attributeExpressionExecutors[classIndex] instanceof VariableExpressionExecutor) {
            // other attributes should be numeric type.
            String attributeName = ((VariableExpressionExecutor)
                    attributeExpressionExecutors[classIndex]).getAttribute().getName();
            Attribute.Type classLabelAttributeType = inputDefinition.
                    getAttributeType(attributeName);

            //class label should be String or Bool
            if (isLabelType(classLabelAttributeType)) {
                classLabelVariableExecutor = (VariableExpressionExecutor) attributeExpressionExecutors[classIndex];
            } else {
                throw new SiddhiAppValidationException(String.format("[label attribute] in %s th index of " +
                                "classifierUpdate should be either a %s or a %s but found %s",
                        classIndex, Attribute.Type.BOOL, Attribute
                                .Type.STRING, classLabelAttributeType));
            }

        } else {
            throw new SiddhiAppValidationException((classIndex) + "th parameter is not " +
                    "an attribute (VariableExpressionExecutor) present in the stream definition. Found a "
                    + attributeExpressionExecutors[classIndex].getClass().getCanonicalName());
        }
        return classLabelVariableExecutor;
    }

    /**
     * Check whether the model is initialized
     * @param model Instance of AdaptiveHoeffdingTree model
     * @param noOfFeatures Number of feature attributes used to train the model
     * @return true/false
     */
    public static boolean isInitialized(AdaptiveHoeffdingTreeModel model, int noOfFeatures) {
        boolean initialized;
        if (model.getStreamHeader() != null) {
            initialized = true;
            // validate the model
            if (noOfFeatures != model.getNoOfFeatures()) {
                throw new SiddhiAppValidationException(String.format("Model [%s] expects %s " +
                                "features, but the streamingml:updateHoeffdingTree " +
                                "specifies %s features.", model.getModelName(), model.getNoOfFeatures(),
                        noOfFeatures));
            }
        } else {
            initialized = false;
        }
        return initialized;
    }
}
