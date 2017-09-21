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

import org.apache.log4j.Logger;
import org.wso2.extension.siddhi.gpl.execution.streamingml.regression.Regressor;
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
 * Build/update an Adaptive Model Rules Regressor Model for regression analysis.
 * {@link AdaptiveModelRulesUpdaterStreamProcessorExtension}
 */
@Extension(
        name = "updateAMRulesRegressor",
        namespace = "streamingml",
        description = "This extension performs the build/update of the AMRules Regressor model for evolving data " +
                "streams.",

        parameters = {
                @Parameter(name = "model.name",
                        description = "The name of the model to be built/updated.",
                        type = {DataType.STRING}),
                @Parameter(name = "split.confidence",
                        description = "This is a Hoeffding Bound parameter.  It defines the percentage of error that " +
                                "to be allowed in a split decision. When the value specified is closer to 0, it takes" +
                                " longer to output the decision.",
                        type = {DataType.DOUBLE},
                        optional = true,
                        defaultValue = "1.0E-7D"),
                @Parameter(name = "tie.break.threshold",
                        description = "This is a Hoeffding Bound parameter. It specifies the threshold below which a " +
                                "split must be forced to break ties.",
                        type = {DataType.DOUBLE},
                        optional = true,
                        defaultValue = "0.05D"),
                @Parameter(name = "grace.period",
                        description = "This is a Hoeffding Bound parameter. The number of instances a leaf should "
                                + "observe between split attempts.",
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "200"),
                @Parameter(name = "change.detector",
                        description = " The Concept Drift Detection methodology to be used. The possible values " +
                                "are as follows.\n " +
                                "`0`:NoChangeDetection\n" +
                                "`1`:ADWINChangeDetector \n " +
                                "`2`:PageHinkleyDM",
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "2:PageHinkleyDM"),
                @Parameter(name = "anomaly.detector",
                        description = "The Anomaly Detection methodology to be used. The possible values are as " +
                                "follows:" +
                                "`0`:NoAnomalyDetection\n" +
                                "`1`:AnomalinessRatioScore\n" +
                                "`2`:OddsRatioScore",
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "2:OddsRatioScore"),
                @Parameter(name = "model.features",
                        description = "The features of the model that should be attributes of the stream.",
                        type = {DataType.DOUBLE, DataType.FLOAT, DataType.LONG, DataType.INT})
        },
        returnAttributes = {
                @ReturnAttribute(name = "meanSquaredError",
                        description = "The current Mean Squared Error of the model",
                        type = {DataType.DOUBLE})
        },
        examples = {
                @Example(
                        syntax = "define stream StreamA (attribute_0 double, attribute_1 double, "
                                + "attribute_2 double, attribute_3 double, attribute_4 string );\n"
                                + "\n"
                                + "from StreamA#streamingml:updateAMRulesRegressor('model1',) \n"
                                + "select attribute_0, attribute_1, attribute_2, attribute_3, "
                                + "meanSquaredError insert into OutputStream;",
                        description = "In this query, an AMRulesRegressor model named 'model1' is built/updated using" +
                                " `attribute_0`, `attribute_1`, `attribute_2`, and `attribute_3` attributes as " +
                                "features, and `attribute_4` as the target_value. The accuracy of the evaluation is " +
                                "output to the OutputStream stream."
                ),
                @Example(
                        syntax = "define stream StreamA (attribute_0 double, attribute_1 double, "
                                + "attribute_2 double, attribute_3 double, attribute_4 string );\n"
                                + "\n"
                                + "from StreamA#streamingml:updateAMRulesRegressor('model1', 1.0E-7D, 0.05D, 200,"
                                + " 0, 0) \n"
                                + "select attribute_0, attribute_1, attribute_2, attribute_3, meanSquaredError "
                                + "insert into OutputStream;",
                        description = "In this query, an `AMRulesRegressor` model named `model1` is built/updated " +
                                "with a split confidence of 1.0E-7D, a tie break threshold of 0.05D, and a grace " +
                                "period of 200. The Concept Drift Detection and Anomaly Detection methodologies " +
                                "used are `NoChangeDetection` and `NoAnomalyDetection` respectively. `attribute_0`, " +
                                "`attribute_1`, `attribute_2`, and `attribute_3` are used as features, and " +
                                "`attribute_4` is used as the target value. The `meanSquaredError` is output to the" +
                                " `OutputStream` stream."
                )
        }
)
public class AdaptiveModelRulesUpdaterStreamProcessorExtension extends StreamProcessor {
    private static final Logger logger = Logger.getLogger(AdaptiveModelRulesUpdaterStreamProcessorExtension.class);

    private static final int minNoOfFeatures = 2;
    private static final int minNoOfParameters = 1;
    private static final int noOfHyperParameters = 5;

    private int noOfAttributes;
    private int noOfParameters;
    private String modelName;

    private List<VariableExpressionExecutor> featureVariableExpressionExecutors = new ArrayList<>();

    private double[] cepEvent;

    @Override
    protected List<Attribute> init(AbstractDefinition abstractDefinition, ExpressionExecutor[] expressionExecutors,
                                   ConfigReader configReader, SiddhiAppContext siddhiAppContext) {
        String modelPrefix;
        noOfAttributes = inputDefinition.getAttributeList().size();
        noOfParameters = attributeExpressionLength - noOfAttributes;

        if (attributeExpressionLength >= minNoOfParameters + minNoOfFeatures) {
            if (attributeExpressionExecutors[0] instanceof ConstantExpressionExecutor) {
                ConstantExpressionExecutor modelNameExecutor =
                        (ConstantExpressionExecutor) attributeExpressionExecutors[0];
                if (modelNameExecutor.getReturnType() == Attribute.Type.STRING) {
                    modelPrefix = (String) modelNameExecutor.getValue();
                    // model name = user given name + siddhi app name
                    modelName = siddhiAppContext.getName() + "." + modelPrefix;
                } else {
                    throw new SiddhiAppValidationException(
                            "Invalid parameter type found for the model.name argument, "
                                    + "required " + Attribute.Type.STRING + " but found "
                                    + modelNameExecutor.getReturnType().toString());
                }
            } else {
                throw new SiddhiAppValidationException("Model.name must be a Constant but found "
                        + attributeExpressionExecutors[0].getClass().getCanonicalName());
            }

            if (noOfAttributes > minNoOfFeatures) {
                featureVariableExpressionExecutors = CoreUtils
                        .extractAndValidateFeatures(inputDefinition, attributeExpressionExecutors,
                                (attributeExpressionLength - noOfAttributes), noOfAttributes);
            } else {
                throw new SiddhiAppValidationException(
                        "Number of features must be greater than 2 but" + " found "
                                + noOfAttributes);
            }
            AdaptiveModelRulesModel model
                    = RegressorModelHolder.getInstance().getAMRulesRegressorModel(modelName);
            if (model == null) {
                model = RegressorModelHolder.getInstance().createAMRulesRegressorModel(modelName);
            }
            if (!model.isInitialized()) {
                if (logger.isDebugEnabled()) {
                    logger.debug(String.format("Model [%s] has not been initialized.", modelName));
                }
                model.init(noOfAttributes);
            }
            cepEvent = new double[noOfAttributes];
            if (noOfParameters > minNoOfParameters) {
                //configuation with hyper-parameters
                if (noOfParameters == (minNoOfParameters + noOfHyperParameters)) {
                    //configuring AMRules Regressor model with hyper-parameters
                    if (logger.isDebugEnabled()) {
                        logger.debug("AMRules Regressor model is configured with hyper-parameters");
                    }
                    configureModelWithHyperParameters(modelName);
                } else {
                    throw new SiddhiAppValidationException(String.format("Number of hyper-parameters needed for model"
                                    + " manual configuration is %s but found %s",
                            noOfHyperParameters, (noOfParameters - minNoOfParameters)));
                }

            }
        } else {
            throw new SiddhiAppValidationException(String.format("Invalid number of attributes for "
                            + "streamingml:updateAMRulesRegressor. This Stream Processor requires at least %s ,"
                            + "parameters namely, model.name and %s features but found %s parameters and %s features",
                    minNoOfParameters, minNoOfFeatures, (attributeExpressionLength - noOfAttributes), noOfAttributes));
        }
        //set attributes for OutputStream
        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("meanSquaredError", Attribute.Type.DOUBLE));
        return attributes;
    }

    private void configureModelWithHyperParameters(String modelName) {
        //default configurations for AMRules Regressor Model
        double splitConfidence = 1.0E-7D;
        double tieBreakThreshold = 0.05D;
        int gracePeriod = 200;
        int changeDetector = 2;
        int anomalyDetector = 2;

        int parameterPosition = minNoOfParameters;

        List<String> hyperParameters = new ArrayList<>();
        hyperParameters.add("Split Confidence");
        hyperParameters.add("Tie Break Threshold");
        hyperParameters.add("Grace Period");
        hyperParameters.add("Change Detector");
        hyperParameters.add("Anomaly Detector");

        for (int i = parameterPosition; i < noOfParameters; i++) {
            if (attributeExpressionExecutors[i] instanceof ConstantExpressionExecutor) {
                switch (i) {
                    case 1:
                        Attribute.Type splitConfidenceType = attributeExpressionExecutors[1].getReturnType();
                        if (CoreUtils.isNumeric(splitConfidenceType)) {
                            splitConfidence = ((Number) ((ConstantExpressionExecutor)
                                    attributeExpressionExecutors[1]).getValue()).doubleValue();
                            parameterPosition++;
                        } else {
                            throw new SiddhiAppValidationException(String.format("Split Confidence must be an %s."
                                            + " But found %s at position %s",
                                    Attribute.Type.DOUBLE, attributeExpressionExecutors[1].getReturnType(), (i + 1)));
                        }
                        break;
                    case 2:
                        Attribute.Type tieBreakThresholdType = attributeExpressionExecutors[2].getReturnType();
                        if (CoreUtils.isNumeric(tieBreakThresholdType)) {
                            tieBreakThreshold = ((Number) ((ConstantExpressionExecutor)
                                    attributeExpressionExecutors[2]).getValue()).doubleValue();
                            parameterPosition++;
                        } else {
                            throw new SiddhiAppValidationException(String.format("Tie Break Threshold must be an %s."
                                            + " But found %s at position %s",
                                    Attribute.Type.DOUBLE, attributeExpressionExecutors[2].getReturnType(), (i + 1)));
                        }
                        break;
                    case 3:
                        if (attributeExpressionExecutors[3].getReturnType() == Attribute.Type.INT) {
                            gracePeriod = (Integer) ((ConstantExpressionExecutor)
                                    attributeExpressionExecutors[3]).getValue();
                            parameterPosition++;
                        } else {
                            throw new SiddhiAppValidationException(String.format("Grace Period must be a %s. "
                                            + "But found %s at position %s.",
                                    Attribute.Type.DOUBLE, attributeExpressionExecutors[3].getReturnType(), (i + 1)));
                        }
                        break;
                    case 4:
                        if (attributeExpressionExecutors[4].getReturnType() == Attribute.Type.INT) {
                            changeDetector = (Integer) ((ConstantExpressionExecutor)
                                    attributeExpressionExecutors[4]).getValue();
                            parameterPosition++;
                        } else {
                            throw new SiddhiAppValidationException(String.format("Change Detector must be a %s. "
                                            + "But found %s at position %s.",
                                    Attribute.Type.INT, attributeExpressionExecutors[4].getReturnType(), (i + 1)));
                        }
                        break;
                    case 5:
                        if (attributeExpressionExecutors[5].getReturnType() == Attribute.Type.INT) {
                            anomalyDetector = (Integer) ((ConstantExpressionExecutor)
                                    attributeExpressionExecutors[5]).getValue();
                            parameterPosition++;
                        } else {
                            throw new SiddhiAppValidationException(String.format("Anomaly Detector must be a %s. "
                                            + "But found %s at position %s.",
                                    Attribute.Type.INT, attributeExpressionExecutors[5].getReturnType(), (i + 1)));
                        }
                        break;
                    default:
                }
            } else {
                throw new SiddhiAppValidationException(String.format("%s must be (ConstantExpressionExecutor) "
                                + "but found %s in position %s.", hyperParameters.get(i - minNoOfParameters),
                        attributeExpressionExecutors[i].getClass().getCanonicalName(), (i + 1)));
            }
        }
        if (parameterPosition == (noOfHyperParameters + minNoOfParameters)) {
            AdaptiveModelRulesModel model = RegressorModelHolder.getInstance()
                    .getAMRulesRegressorModel(modelName);
            model.setConfigurations(splitConfidence, tieBreakThreshold, gracePeriod, changeDetector, anomalyDetector);
        } else {
            throw new SiddhiAppValidationException("Number of hyper-parameters needed for model "
                    + "manual configuration is " + noOfHyperParameters + " but found "
                    + (parameterPosition - minNoOfParameters));
        }
    }

    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor processor,
                           StreamEventCloner streamEventCloner, ComplexEventPopulater complexEventPopulater) {
        synchronized (this) {
            while (streamEventChunk.hasNext()) {
                ComplexEvent complexEvent = streamEventChunk.next();
                for (int i = 0; i < noOfAttributes; i++) {
                    try {
                        cepEvent[i] = ((Number) featureVariableExpressionExecutors.get(i)
                                .execute(complexEvent)).doubleValue();
                    } catch (ClassCastException e) {
                        throw new SiddhiAppRuntimeException(String.format("Incompatible attribute feature type"
                                + " at position %s. Not of numeric type. Please refer the stream definition "
                                + "of Model[%s]", (i + 1), modelName));
                    }
                }
                AdaptiveModelRulesModel model = RegressorModelHolder.getInstance().getAMRulesRegressorModel(modelName);
                double meanSquaredError = model.trainOnEvent(cepEvent);
                complexEventPopulater.populateComplexEvent(complexEvent, new Object[]{meanSquaredError});
            }
            nextProcessor.process(streamEventChunk);
        }
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        RegressorModelHolder.getInstance().deleteRegressorModel(modelName);
    }

    @Override
    public Map<String, Object> currentState() {
        Map<String, Object> currentState = new HashMap<>();
        currentState.put("RegressorModel", RegressorModelHolder.getInstance().getClonedPerceptronModel(modelName));
        return currentState;
    }

    @Override
    public void restoreState(Map<String, Object> state) {
        RegressorModelHolder.getInstance().addRegressorModel(modelName, (Regressor)
                state.get("RegressorModel"));
    }
}
