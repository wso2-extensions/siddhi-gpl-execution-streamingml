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
package org.wso2.extension.siddhi.gpl.execution.streamingml.regression.adaptivemodelrules;

import io.siddhi.annotation.Example;
import io.siddhi.annotation.Extension;
import io.siddhi.annotation.Parameter;
import io.siddhi.annotation.ReturnAttribute;
import io.siddhi.annotation.util.DataType;
import io.siddhi.core.config.SiddhiQueryContext;
import io.siddhi.core.event.ComplexEvent;
import io.siddhi.core.event.ComplexEventChunk;
import io.siddhi.core.event.stream.MetaStreamEvent;
import io.siddhi.core.event.stream.StreamEvent;
import io.siddhi.core.event.stream.StreamEventCloner;
import io.siddhi.core.event.stream.holder.StreamEventClonerHolder;
import io.siddhi.core.event.stream.populater.ComplexEventPopulater;
import io.siddhi.core.exception.SiddhiAppRuntimeException;
import io.siddhi.core.executor.ConstantExpressionExecutor;
import io.siddhi.core.executor.ExpressionExecutor;
import io.siddhi.core.executor.VariableExpressionExecutor;
import io.siddhi.core.query.processor.ProcessingMode;
import io.siddhi.core.query.processor.Processor;
import io.siddhi.core.query.processor.stream.StreamProcessor;
import io.siddhi.core.util.config.ConfigReader;
import io.siddhi.core.util.snapshot.state.State;
import io.siddhi.core.util.snapshot.state.StateFactory;
import io.siddhi.query.api.definition.AbstractDefinition;
import io.siddhi.query.api.definition.Attribute;
import io.siddhi.query.api.exception.SiddhiAppValidationException;
import org.wso2.extension.siddhi.gpl.execution.streamingml.regression.RegressorModelHolder;
import org.wso2.extension.siddhi.gpl.execution.streamingml.regression.adaptivemodelrules.util.AdaptiveModelRulesModel;
import org.wso2.extension.siddhi.gpl.execution.streamingml.util.CoreUtils;

import java.util.ArrayList;
import java.util.List;

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
public class AdaptiveModelRulesRegressorStreamProcessorExtension extends StreamProcessor<State> {
    private static final int minNoOfParameters = 1;

    private String modelName;
    private int noOfFeatures;
    private List<VariableExpressionExecutor> featureVariableExpressionExecutors = new ArrayList<>();
    private double[] cepEvent;
    //set attributes for Output Stream
    List<Attribute> attributes = new ArrayList<>();

    @Override
    protected StateFactory<State> init(MetaStreamEvent metaStreamEvent, AbstractDefinition inputDefinition,
                                       ExpressionExecutor[] attributeExpressionExecutors, ConfigReader configReader,
                                       StreamEventClonerHolder streamEventClonerHolder,
                                       boolean outputExpectsExpiredEvents, boolean findToBeExecuted,
                                       SiddhiQueryContext siddhiQueryContext) {
        String modelPrefix;
        noOfFeatures = inputDefinition.getAttributeList().size();

        if (attributeExpressionLength > minNoOfParameters) {
            if (attributeExpressionExecutors[0] instanceof ConstantExpressionExecutor) {
                if (attributeExpressionExecutors[0].getReturnType() == Attribute.Type.STRING) {
                    modelPrefix = (String) ((ConstantExpressionExecutor)
                            attributeExpressionExecutors[0]).getValue();
                    // model name = user given name + siddhi app name
                    modelName = siddhiQueryContext.getSiddhiAppContext().getName() + "." + modelPrefix;
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

        attributes.add(new Attribute("prediction", Attribute.Type.DOUBLE));
        attributes.add(new Attribute("meanSquaredError", Attribute.Type.DOUBLE));
        return null;
    }

    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor,
                           StreamEventCloner streamEventCloner, ComplexEventPopulater complexEventPopulater,
                           State state) {
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
    public List<Attribute> getReturnAttributes() {
        return attributes;
    }

    @Override
    public ProcessingMode getProcessingMode() {
        return ProcessingMode.BATCH;
    }
}
