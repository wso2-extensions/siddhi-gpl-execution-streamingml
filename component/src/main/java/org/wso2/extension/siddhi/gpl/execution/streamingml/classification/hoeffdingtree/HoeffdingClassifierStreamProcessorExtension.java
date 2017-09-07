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
package org.wso2.extension.siddhi.gpl.execution.streamingml.classification.hoeffdingtree;

import org.wso2.extension.siddhi.gpl.execution.streamingml.classification.hoeffdingtree.util.AdaptiveHoeffdingModelsHolder;
import org.wso2.extension.siddhi.gpl.execution.streamingml.classification.hoeffdingtree.util.AdaptiveHoeffdingTreeModel;
import org.wso2.extension.siddhi.gpl.execution.streamingml.util.CoreUtils;
import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.ReturnAttribute;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.populater.ComplexEventPopulater;
import org.wso2.siddhi.core.exception.SiddhiAppRuntimeException;
import org.wso2.siddhi.core.executor.ConstantExpressionExecutor;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.stream.StreamProcessor;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.SiddhiAppValidationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Predict using a Hoeffding Adaptive Tree model.
 * built via @{@link HoeffdingClassifierStreamProcessorExtension}
 */
@Extension(
        name = "hoeffdingTreeClassifier",
        namespace = "streamingml",
        description = "Performs classification with Hoeffding Adaptive Tree for evolving "
                + "data streams that uses ADWIN to replace branches for new ones.",
        parameters = {
                @Parameter(name = "model.name",
                        description = "The name of the model to be used for prediction.",
                        type = {DataType.STRING})
        },
        returnAttributes = {
                @ReturnAttribute(name = "prediction",
                        description = "Predicted class label",
                        type = {DataType.STRING}),
                @ReturnAttribute(name = "confidenceLevel",
                        description = "Probability of the prediction",
                        type = {DataType.DOUBLE})
        },
        examples = {
                @Example(
                        syntax = "define stream StreamA (attribute_0 double, attribute_1 double, "
                                + "attribute_2 double, attribute_3 double);\n" +
                                "\n"
                                + "from StreamA#streamingml:hoeffdingTreeClassifier('model1', "
                                + " attribute_0, attribute_1, attribute_2, attribute_3) \n"
                                + "select attribute_0, attribute_1, attribute_2, attribute_3, "
                                + "prediction, predictionConfidence insert into outputStream;",
                        description = "A Hoeffding Tree model with the name 'model1' will be used "
                                + "to predict the label of the feature vector represented "
                                + "by attribute_0, attribute_1, attribute_2, attribute_3. "
                                + "Predicted label (String/Bool) along with the Prediction Confidence "
                                + "and the feature vector will be emitted to the outputStream. "
                                + "The outputStream will have following definition; "
                                + "(attribute_0 double, attribute_1 double, attribute_2"
                                + " double, attribute_3 double, prediction string, "
                                + "confidenceLevel double)."
                )
        }
)
public class HoeffdingClassifierStreamProcessorExtension extends StreamProcessor {
    private static final int minNoOfFeatures = 2;
    private static final int minNoOfParameters = 1;

    private String modelName;
    private int noOfFeatures;
    private List<VariableExpressionExecutor> featureVariableExpressionExecutors = new ArrayList<>();
    private double[] cepEvent;

    @Override
    protected List<Attribute> init(AbstractDefinition abstractDefinition,
                                   ExpressionExecutor[] expressionExecutors,
                                   ConfigReader configReader, SiddhiAppContext siddhiAppContext) {
        String siddhiAppName = siddhiAppContext.getName();
        String modelPrefix;
        noOfFeatures = inputDefinition.getAttributeList().size();

        if (attributeExpressionExecutors.length >= (minNoOfFeatures + minNoOfParameters)) {
            if (noOfFeatures < minNoOfFeatures) {
                throw new SiddhiAppValidationException(String.format("Invalid number of feature attributes for "
                                + "streamingml:hoeffdingTreeClassifier. This Stream Processor requires at least %s "
                                + "feature attributes, but found %s feature attributes",
                        minNoOfFeatures, noOfFeatures));
            }
            if (noOfFeatures != (attributeExpressionLength - minNoOfParameters)) {
                throw new SiddhiAppValidationException(String.format("Invalid number of feature attributes for "
                                + "streamingml:hoeffdingTreeClassifier. This Stream Processor is defined with %s "
                                + "features, but found %s feature attributes",
                        noOfFeatures, (attributeExpressionLength - minNoOfParameters)));
            }
            if (attributeExpressionExecutors[0] instanceof ConstantExpressionExecutor) {
                if (attributeExpressionExecutors[0].getReturnType() == Attribute.Type.STRING) {
                    modelPrefix = (String) ((ConstantExpressionExecutor)
                            attributeExpressionExecutors[0])
                            .getValue();
                    // model name = user given name + siddhi app name
                    modelName = siddhiAppName + "." + modelPrefix;
                } else {
                    throw new SiddhiAppValidationException(
                            "Invalid parameter type found for the model.name argument, "
                                    + "required " + Attribute.Type.STRING
                                    + " but found " + attributeExpressionExecutors[0].
                                    getReturnType().toString());
                }
            } else {
                throw new SiddhiAppValidationException("Parameter model.name must be a constant but found "
                        + attributeExpressionExecutors[0].getClass().getCanonicalName());
            }

            featureVariableExpressionExecutors = CoreUtils
                    .extractAndValidateFeatures(inputDefinition, attributeExpressionExecutors,
                            (attributeExpressionLength - noOfFeatures), noOfFeatures);

            AdaptiveHoeffdingTreeModel model
                    = AdaptiveHoeffdingModelsHolder.getInstance().getHoeffdingModel(modelName);

            if (!CoreUtils.isInitialized(model, (noOfFeatures + 1))) {
                throw new SiddhiAppValidationException(String.format("Model [%s] needs to initialized "
                        + "prior to be used with streamingml:hoeffdingTreeClassifier. "
                        + "Perform streamingml:updateHoeffdingTree process first.", modelName));
            }
        } else {
            throw new SiddhiAppValidationException(String.format("Invalid number of parameters for "
                            + "streamingml:hoeffdingTreeClassifier. This Stream Processor requires "
                            + "at least %s parameters, namely, model.name and at least %s feature_attributes,"
                            + " but found %s parameters",
                    (minNoOfParameters + minNoOfFeatures), minNoOfFeatures, attributeExpressionExecutors.length));
        }
        //set attributes for Output Stream
        List<Attribute> attributes = new ArrayList<Attribute>();
        attributes.add(new Attribute("prediction", Attribute.Type.STRING));
        attributes.add(new Attribute("confidenceLevel", Attribute.Type.DOUBLE));
        return attributes;
    }

    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor
            processor, StreamEventCloner streamEventCloner,
                           ComplexEventPopulater complexEventPopulater) {
        synchronized (this) {
            while (streamEventChunk.hasNext()) {
                ComplexEvent complexEvent = streamEventChunk.next();
                cepEvent = new double[noOfFeatures];
                // Set feature_attributes
                for (int i = 0; i < noOfFeatures; i++) {
                    try {
                        cepEvent[i] = ((Number) featureVariableExpressionExecutors.get(i)
                                .execute(complexEvent)).doubleValue();
                    } catch (ClassCastException e) {
                        throw new SiddhiAppRuntimeException(String.format("Incompatible attribute feature type"
                                + " at position %s. Not of any numeric type. Please refer the stream definition "
                                + "for Model[%s]", (i + 1), modelName));
                    }
                }
                AdaptiveHoeffdingTreeModel model = AdaptiveHoeffdingModelsHolder.getInstance()
                        .getHoeffdingModel(modelName);
                Object[] outputData = model.getPrediction(cepEvent);
                int indexPredict = (int) outputData[0];
                outputData[0] = model.getClasses().get(indexPredict);
                complexEventPopulater.populateComplexEvent(complexEvent, outputData);
            }
            nextProcessor.process(streamEventChunk);
        }
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public Map<String, Object> currentState() {
        Map<String, Object> currentState = new HashMap<>();
        currentState.put("AdaptiveHoeffdingModelsMap", AdaptiveHoeffdingModelsHolder.
                getInstance().getClonedHoeffdingModelMap());
        return currentState;
    }

    @Override
    public void restoreState(Map<String, Object> state) {
        AdaptiveHoeffdingModelsHolder.getInstance().
                setHoeffdingModelMap((Map<String, AdaptiveHoeffdingTreeModel>) state.get
                        ("AdaptiveHoeffdingModelsMap"));
    }

}
