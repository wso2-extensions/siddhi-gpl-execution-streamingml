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
package org.wso2.extension.siddhi.gpl.execution.streamingml.regression.adaptivemodelrules;

import org.wso2.extension.siddhi.gpl.execution.streamingml.regression.RegressorModelHolder;
import org.wso2.extension.siddhi.gpl.execution.streamingml.regression.adaptivemodelrules.util.AdaptiveModelRulesModel;
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
 * Perform regression analysis using an Adaptive Model Rules Regressor model.
 * built via @{@link AdaptiveModelRulesRegressorStreamProcessorExtension}
 */
@Extension(
        name = "AMRulesRegressor",
        namespace = "streamingml",
        description = "This extension performs regression tasks using the `AMRulesRegressor` algorithm.",
        parameters = {
                @Parameter(name = "model.name",
                        description = "The name of the model to be used for prediction.",
                        type = {DataType.STRING}),
                @Parameter(name = "model.feature",
                        description = "The feature vector for the regression analysis.",
                        type = {DataType.INT, DataType.FLOAT, DataType.FLOAT, DataType.DOUBLE}),
        },
        returnAttributes = {
                @ReturnAttribute(name = "prediction",
                        description = "The predicted value.",
                        type = {DataType.DOUBLE}),
                @ReturnAttribute(name = "meanSquaredError",
                        description = "The `MeanSquaredError` of the predicting model.",
                        type = {DataType.DOUBLE})
        },
        examples = {
                @Example(
                        syntax = "define stream StreamA (attribute_0 double, attribute_1 double, "
                                + "attribute_2 double, attribute_3 double);\n" +
                                "\n"
                                + "from StreamA#streamingml:AMRulesRegressor('model1', "
                                + " attribute_0, attribute_1, attribute_2, attribute_3) \n"
                                + "select attribute_0, attribute_1, attribute_2, attribute_3, "
                                + "prediction, meanSquaredError insert into OutputStream;",
                        description = "This query uses an `AMRules` model named `model1` that is used to predict " +
                                "the value for the feature vector represented by `attribute_0`, `attribute_1`, " +
                                "`attribute_2`, and `attribute_3`. The predicted value along with the " +
                                "`MeanSquaredError` and the feature vector are output to a stream named " +
                                "`OutputStream`. The resulting definition of the `OutputStream` stream is as " +
                                "follows:\n"
                                + "(attribute_0 double, attribute_1 double, attribute_2"
                                + " double, attribute_3 double, prediction double, "
                                + "meanSquaredError double)."
                )
        }
)
public class AdaptiveModelRulesRegressorStreamProcessorExtension extends StreamProcessor {
    private static final int minNoOfParameters = 1;

    private String modelName;
    private int noOfFeatures;
    private List<VariableExpressionExecutor> featureVariableExpressionExecutors = new ArrayList<>();
    private double[] cepEvent;

    @Override
    protected List<Attribute> init(AbstractDefinition abstractDefinition, ExpressionExecutor[] expressionExecutors,
                                   ConfigReader configReader, SiddhiAppContext siddhiAppContext) {
        String modelPrefix;
        noOfFeatures = inputDefinition.getAttributeList().size();

        if (attributeExpressionLength > minNoOfParameters) {
            if (attributeExpressionExecutors[0] instanceof ConstantExpressionExecutor) {
                if (attributeExpressionExecutors[0].getReturnType() == Attribute.Type.STRING) {
                    modelPrefix = (String) ((ConstantExpressionExecutor)
                            attributeExpressionExecutors[0]).getValue();
                    // model name = user given name + siddhi app name
                    modelName = siddhiAppContext.getName() + "." + modelPrefix;
                } else {
                    throw new SiddhiAppValidationException(String.format("Invalid parameter type found for the "
                                    + "model.name argument, required %s, but found %s.",
                            Attribute.Type.STRING, attributeExpressionExecutors[0].getReturnType().toString()));
                }
            } else {
                throw new SiddhiAppValidationException("Parameter model.name must be a constant but found "
                        + attributeExpressionExecutors[0].getClass().getCanonicalName());
            }
        } else {
            throw new SiddhiAppValidationException(String.format("streamingML:AMRulesRegressor needs exactly "
                            + "model.name and %s feature atttributes, but found %s.",
                    noOfFeatures, attributeExpressionLength));
        }
        AdaptiveModelRulesModel model = RegressorModelHolder.getInstance().getAMRulesRegressorModel(modelName);
        if (model == null || !model.isInitialized()) {
            throw new SiddhiAppValidationException(String.format("Model [%s] needs to initialized "
                    + "prior to be used with streamingml:AMRulesRegressor. "
                    + "Perform streamingml:updateAMRulesRegressor process first.", modelName));
        }
        if (!model.isValidStreamHeader(noOfFeatures)) {
            throw new SiddhiAppValidationException(String.format("Invalid number of parameters for "
                            + "streamingml:AMRulesRegressor. Model [%s] expects %s features, but "
                            + "the input specifies %s features.",
                    this.modelName, model.getNoOfFeatures(), noOfFeatures));
        }
        if (attributeExpressionLength != ((model.getNoOfFeatures()) + minNoOfParameters)) {
            throw new SiddhiAppValidationException(String.format("Invalid number of parameters for "
                            + "streamingml:AMRulesRegressor. This Stream Processor requires  %s "
                            + "parameters, namely, model.name and %s feature_attributes, "
                            + "but found %s parameters", (minNoOfParameters + (model.getNoOfFeatures())),
                    model.getNoOfFeatures(), (attributeExpressionExecutors.length - minNoOfParameters)));
        }
        featureVariableExpressionExecutors = CoreUtils.extractAndValidateFeatures(inputDefinition,
                attributeExpressionExecutors, (attributeExpressionLength - noOfFeatures), noOfFeatures);

        cepEvent = new double[noOfFeatures + 1];

        //set attributes for Output Stream
        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("prediction", Attribute.Type.DOUBLE));
        attributes.add(new Attribute("meanSquaredError", Attribute.Type.DOUBLE));
        return attributes;

    }

    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor processor,
                           StreamEventCloner streamEventCloner, ComplexEventPopulater complexEventPopulater) {
        synchronized (this) {
            while (streamEventChunk.hasNext()) {
                ComplexEvent complexEvent = streamEventChunk.next();
                // Set feature_attributes
                for (int i = 0; i < noOfFeatures; i++) {
                    try {
                        cepEvent[i] = ((Number) featureVariableExpressionExecutors.get(i)
                                .execute(complexEvent)).doubleValue();
                    } catch (ClassCastException e) {
                        throw new SiddhiAppRuntimeException(String.format("Incompatible attribute feature type"
                                + " at position %s. Not of any numeric type. Please refer the stream definition "
                                + "of Model[%s]", (i + 1), modelName));
                    }
                }
                AdaptiveModelRulesModel model = RegressorModelHolder.getInstance()
                        .getAMRulesRegressorModel(modelName);
                Object[] outputData = model.getPrediction(cepEvent);
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
        return new HashMap<>();
    }

    @Override
    public void restoreState(Map<String, Object> map) {

    }
}
