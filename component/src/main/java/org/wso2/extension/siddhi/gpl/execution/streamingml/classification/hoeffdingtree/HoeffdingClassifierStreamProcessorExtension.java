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
import org.wso2.extension.siddhi.gpl.execution.streamingml.classification.hoeffdingtree.util.AdaptiveHoeffdingModelsHolder;
import org.wso2.extension.siddhi.gpl.execution.streamingml.classification.hoeffdingtree.util.AdaptiveHoeffdingTreeModel;
import org.wso2.extension.siddhi.gpl.execution.streamingml.util.CoreUtils;

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
        description = "This extension performs classification using the Hoeffding Adaptive Tree algorithm for evolving "
                + "data streams that use `ADWIN` to replace branches with new ones.",
        parameters = {
                @Parameter(name = "model.name",
                        description = "The name of the model to be used for prediction.",
                        type = {DataType.STRING})
        },
        returnAttributes = {
                @ReturnAttribute(name = "prediction",
                        description = "The predicted class label.",
                        type = {DataType.STRING}),
                @ReturnAttribute(name = "confidenceLevel",
                        description = "The probability of the prediction.",
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
                                + "prediction, predictionConfidence insert into OutputStream;",
                        description = "This query uses a Hoeffding Tree model named `model1` to predict the label " +
                                "of the feature vector represented by `attribute_0`, `attribute_1`, " +
                                "`attribute_2`, and attribute_3 attributes. The predicted label (`String/Bool`) " +
                                "along with the prediction confidence and the feature vector are output to the " +
                                "`OutputStream` stream. The expected definition of the `OutputStream` is as follows:" +
                                "(attribute_0 double, attribute_1 double, attribute_2\n" +
                                " double, attribute_3 double, prediction string, \n" +
                                "confidenceLevel double)."
                )
        }
)
public class HoeffdingClassifierStreamProcessorExtension extends
        StreamProcessor<HoeffdingClassifierStreamProcessorExtension.ExtensionState> {
    private static final int MINIMUM_NUMBER_OF_FEATURES = 2;
    private static final int MINIMUM_NUMBER_OF_PARAMETERS = 1;

    private String modelName;
    private int noOfFeatures;
    private List<VariableExpressionExecutor> featureVariableExpressionExecutors = new ArrayList<>();
    private double[] cepEvent;
    //set attributes for Output Stream
    private List<Attribute> attributes = new ArrayList<Attribute>();

    @Override
    protected StateFactory<ExtensionState> init(MetaStreamEvent metaStreamEvent, AbstractDefinition inputDefinition,
                                                ExpressionExecutor[] attributeExpressionExecutors,
                                                ConfigReader configReader,
                                                StreamEventClonerHolder streamEventClonerHolder,
                                                boolean outputExpectsExpiredEvents, boolean findToBeExecuted,
                                                SiddhiQueryContext siddhiQueryContext) {
        String siddhiAppName = siddhiQueryContext.getSiddhiAppContext().getName();
        String modelPrefix;
        noOfFeatures = inputDefinition.getAttributeList().size();
        if (attributeExpressionExecutors.length >= (MINIMUM_NUMBER_OF_FEATURES + MINIMUM_NUMBER_OF_PARAMETERS)) {
            if (noOfFeatures < MINIMUM_NUMBER_OF_FEATURES) {
                throw new SiddhiAppValidationException(String.format("Invalid number of feature attributes for "
                                + "streamingml:hoeffdingTreeClassifier. This Stream Processor requires at least %s "
                                + "feature attributes, but found %s feature attributes",
                        MINIMUM_NUMBER_OF_FEATURES, noOfFeatures));
            }
            if (noOfFeatures != (attributeExpressionLength - MINIMUM_NUMBER_OF_PARAMETERS)) {
                throw new SiddhiAppValidationException(String.format("Invalid number of feature attributes for "
                                + "streamingml:hoeffdingTreeClassifier. This Stream Processor is defined with %s "
                                + "features, but found %s feature attributes",
                        noOfFeatures, (attributeExpressionLength - MINIMUM_NUMBER_OF_PARAMETERS)));
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
                            + " but found %s parameters", (MINIMUM_NUMBER_OF_PARAMETERS + MINIMUM_NUMBER_OF_FEATURES),
                    MINIMUM_NUMBER_OF_FEATURES, attributeExpressionExecutors.length));
        }
        attributes.add(new Attribute("prediction", Attribute.Type.STRING));
        attributes.add(new Attribute("confidenceLevel", Attribute.Type.DOUBLE));
        return () -> new ExtensionState();
    }

    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor,
                           StreamEventCloner streamEventCloner, ComplexEventPopulater complexEventPopulater,
                           ExtensionState state) {
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
        AdaptiveHoeffdingModelsHolder.getInstance().deleteHoeffdingModel(modelName);
    }



    @Override
    public List<Attribute> getReturnAttributes() {
        return attributes;
    }

    @Override
    public ProcessingMode getProcessingMode() {
        return ProcessingMode.BATCH;
    }

    static class ExtensionState extends State {

        @Override
        public boolean canDestroy() {
            return false;
        }

        @Override
        public Map<String, Object> snapshot() {
            Map<String, Object> currentState = new HashMap<>();
            currentState.put("AdaptiveHoeffdingModelsMap", AdaptiveHoeffdingModelsHolder.
                    getInstance().getClonedHoeffdingModelMap());
            return currentState;
        }

        @Override
        public void restore(Map<String, Object> state) {
            AdaptiveHoeffdingModelsHolder.getInstance().
                    setHoeffdingModelMap((Map<String, AdaptiveHoeffdingTreeModel>) state.
                            get("AdaptiveHoeffdingModelsMap"));
        }
    }
}
