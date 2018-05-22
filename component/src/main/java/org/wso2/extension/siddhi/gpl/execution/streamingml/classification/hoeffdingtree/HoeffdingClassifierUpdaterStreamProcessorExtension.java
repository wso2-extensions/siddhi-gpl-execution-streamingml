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

import org.apache.log4j.Logger;
import org.wso2.extension.siddhi.gpl.execution.streamingml.classification.ClassifierPrequentialModelEvaluation;
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
 * Build/update using a Hoeffding Adaptive Tree Model.
 * {@link HoeffdingClassifierUpdaterStreamProcessorExtension}
 */
@Extension(
        name = "updateHoeffdingTree",
        namespace = "streamingml",
        description = "This extension performs the build/update of Hoeffding Adaptive Tree for evolving "
                + "data streams that use `ADWIN` to replace branches for new ones.",
        parameters = {
                @Parameter(name = "model.name",
                        description = "The name of the model to be built/updated.",
                        type = {DataType.STRING}),
                @Parameter(name = "no.of.classes",
                        description = "The number of class labels in the datastream.",
                        type = {DataType.INT}),
                @Parameter(name = "grace.period",
                        description = "The number of instances a leaf should observe between split attempts. A " +
                                "minimum and a maximum value should be specified. e.g., `min:0, max:2147483647`.",
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "200"),
                @Parameter(name = "split.criterion",
                        description = "The split criterion to be used. Possible values are as follows:\n" +
                                "`0`:InfoGainSplitCriterion\n" +
                                "`1`:GiniSplitCriterion",
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "0:InfoGainSplitCriterion"),
                @Parameter(name = "split.confidence",
                        description = "The amount of error that should be allowed in a split decision. When the" +
                                " value specified is closer to 0, it takes longer to output the decision.",
                        type = {DataType.DOUBLE},
                        optional = true,
                        defaultValue = "1e-7"),
                @Parameter(name = "tie.break.threshold",
                        description = "The threshold at which a split must be forced to break ties. A minimum value " +
                                "and a maximum value must be specified. e.g., `min:0.0D, max:1.0D`",
                        type = {DataType.DOUBLE},
                        optional = true,
                        defaultValue = "0.05D"),
                @Parameter(name = "binary.split",
                        description = "If this parameter is set to `true`, onlybinary splits are allowed.",
                        type = {DataType.BOOL},
                        optional = true,
                        defaultValue = "false"),
                @Parameter(name = "pre.prune",
                        description = "If this parameter is set to `true`, pre-pruning is allowed.",
                        type = {DataType.BOOL},
                        optional = true,
                        defaultValue = "false"),
                @Parameter(name = "leaf.prediction.strategy",
                        description = "This specifies the leaf prediction strategy to be used. Possible values are " +
                                "as follows:\n" +
                                "`0`:Majority class \n" +
                                "`1`:Naive Bayes\n" +
                                "`2`:Naive Bayes Adaptive.",
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "2:Naive Bayes Adaptive"),
                @Parameter(name = "model.features",
                        description = "The features of the model that should be attributes of the stream.",
                        type = {DataType.DOUBLE, DataType.INT})
        },
        returnAttributes = {
                @ReturnAttribute(name = "accuracy",
                        description = "The accuracy evaluation of the model(Prequnetial Evaluation)",
                        type = {DataType.DOUBLE})
        },
        examples = {
                @Example(
                        syntax = "define stream StreamA (attribute_0 double, attribute_1 double, "
                                + "attribute_2 double, attribute_3 double, attribute_4 string );\n"
                                + "\n"
                                + "from StreamA#streamingml:updateHoeffdingTree('model1', 3) \n"
                                + "select attribute_0, attribute_1, attribute_2, attribute_3, "
                                + "accuracy insert into OutputStream;",
                        description = "This query builds/updates a HoeffdingTree model named `model1` under "
                                + "3 classes using `attribute_0`, `attribute_1`, `attribute_2`, and `attribute_3` as" +
                                " features, and `attribute_4` as the label. The accuracy evaluation is output to " +
                                "the `OutputStream` stream"
                ),
                @Example(
                        syntax = "define stream StreamA (attribute_0 double, attribute_1 double, "
                                + "attribute_2 double, attribute_3 double, attribute_4 string );\n"
                                + "\n"
                                + "from StreamA#streamingml:updateHoeffdingTree('model1', 3, 200, 0, 1e-7, 1.0D, "
                                + "true, true, 2) \n"
                                + "select attribute_0, attribute_1, attribute_2, attribute_3, "
                                + "accuracy insert into OutputStream;",
                        description = "This query builds/updates a Hoeffding Tree model named `model1` with a " +
                                "grace period of 200, an information gain split criterion of 0, 1e-7 of allowable " +
                                "error in split decision, 1.0D of breaktie threshold, allowing only binary splits. " +
                                "Pre-pruning is disabled, and `Naive Bayes Adaptive` is used as the leaf prediction " +
                                "strategy. 'attribute_0', `attribute_1`, `attribute_2`, and `attribute_3` are used as "
                                + "features, and `attribute_4` as the label. The accuracy evaluation is output to " +
                                "the OutputStream stream."
                )
        }
)
public class HoeffdingClassifierUpdaterStreamProcessorExtension extends StreamProcessor {

    private static final Logger logger = Logger.getLogger(HoeffdingClassifierUpdaterStreamProcessorExtension.class);

    private static final int MINIMUM_NUMBER_OF_FEATURES = 3;
    private static final int MINIMUM_NUMBER_OF_PARAMETERS = 2;
    private static final int NUMBER_OF_HYPER_PARAMETERS = 7;

    private int noOfFeatures;
    private int noOfParameters;
    private int noOfClasses;
    private String modelName;

    private List<VariableExpressionExecutor> featureVariableExpressionExecutors = new ArrayList<>();
    private VariableExpressionExecutor classLabelVariableExecutor;

    private double[] cepEvent;
    private ClassifierPrequentialModelEvaluation evolutionModel;

    @Override
    protected List<Attribute> init(AbstractDefinition abstractDefinition, ExpressionExecutor[] expressionExecutors,
                                   ConfigReader configReader, SiddhiAppContext siddhiAppContext) {
        String siddhiAppName = siddhiAppContext.getName();
        String modelPrefix;
        noOfFeatures = inputDefinition.getAttributeList().size();
        noOfParameters = attributeExpressionLength - noOfFeatures;
        int classIndex = attributeExpressionLength - 1;

        if (attributeExpressionLength >= MINIMUM_NUMBER_OF_PARAMETERS + MINIMUM_NUMBER_OF_FEATURES) {
            if (attributeExpressionExecutors[0] instanceof ConstantExpressionExecutor) {
                ConstantExpressionExecutor modelNameExecutor =
                        (ConstantExpressionExecutor) attributeExpressionExecutors[0];
                if (modelNameExecutor.getReturnType() == Attribute.Type.STRING) {
                    modelPrefix = (String) modelNameExecutor.getValue();
                    // model name = user given name + siddhi app name
                    modelName = siddhiAppName + "." + modelPrefix;
                } else {
                    throw new SiddhiAppValidationException(
                            "Invalid parameter type found for the model.name argument, "
                                    + "required " + Attribute.Type.STRING + " but found "
                                    + modelNameExecutor.getReturnType().toString());
                }
            } else {
                throw new SiddhiAppValidationException("Model.name must be (ConstantExpressionExecutor) but found "
                        + attributeExpressionExecutors[0].getClass().getCanonicalName());
            }

            //2nd parameter
            if (attributeExpressionExecutors[1] instanceof ConstantExpressionExecutor) {
                ConstantExpressionExecutor numberOfClassesExecutor =
                        (ConstantExpressionExecutor) attributeExpressionExecutors[1];
                if (numberOfClassesExecutor.getReturnType() == Attribute.Type.INT) {
                    noOfClasses = (Integer) numberOfClassesExecutor.getValue();
                    if (noOfClasses < 2) {
                        throw new SiddhiAppValidationException(
                                "Number of classes must be greater than 1 but found " + noOfClasses);
                    }
                } else {
                    throw new SiddhiAppValidationException(
                            "Invalid parameter type found for the number_of_classes argument, required "
                                    + Attribute.Type.INT + " but found " +
                                    numberOfClassesExecutor.getReturnType().toString());
                }
            } else {
                throw new SiddhiAppValidationException(
                        "Number of classes must be (ConstantExpressionExecutor) but found "
                                + attributeExpressionExecutors[1].getClass().getCanonicalName());
            }
            if (noOfFeatures > 2) {
                featureVariableExpressionExecutors = CoreUtils
                        .extractAndValidateFeatures(inputDefinition, attributeExpressionExecutors,
                                (attributeExpressionLength - noOfFeatures), (noOfFeatures - 1));

                classLabelVariableExecutor = CoreUtils
                        .extractAndValidateClassLabel(inputDefinition, attributeExpressionExecutors,
                                classIndex);
            } else {
                throw new SiddhiAppValidationException(
                        "Number of features must be greater than 2 but" + " found "
                                + noOfFeatures);
            }
            AdaptiveHoeffdingTreeModel model
                    = AdaptiveHoeffdingModelsHolder.getInstance().getHoeffdingModel(modelName);
            if (!CoreUtils.isInitialized(model, noOfFeatures)) {
                if (logger.isDebugEnabled()) {
                    logger.debug(String.format("Model [%s] has not been initialized.", modelName));
                }
                model.init(noOfFeatures, noOfClasses);
            }
            if (noOfParameters > MINIMUM_NUMBER_OF_PARAMETERS) {
                //configuation with hyper-parameters
                if (noOfParameters == (MINIMUM_NUMBER_OF_PARAMETERS + NUMBER_OF_HYPER_PARAMETERS)) {
                    //configuring hoeffding tree model with hyper-parameters
                    if (logger.isDebugEnabled()) {
                        logger.debug("Hoeffding Adaptive Tree is configured with hyper-parameters");
                    }
                    configureModelWithHyperParameters(modelName);
                } else {
                    throw new SiddhiAppValidationException(String.format("Number of hyper-parameters needed for model"
                                    + " manual configuration is %s but found %s",
                            NUMBER_OF_HYPER_PARAMETERS, (noOfParameters - MINIMUM_NUMBER_OF_PARAMETERS)));
                }

            }
            evolutionModel = new ClassifierPrequentialModelEvaluation();
            evolutionModel.reset(noOfClasses);
        } else {
            throw new SiddhiAppValidationException(String.format("Invalid number of attributes for "
                            + "streamingml:updateHoeffdingTree. This Stream Processor requires at least %s ,"
                            + "parameters namely, model.name, number_of_classes and %s features but found %s "
                            + "parameters and %s features", MINIMUM_NUMBER_OF_PARAMETERS, MINIMUM_NUMBER_OF_FEATURES,
                    (attributeExpressionLength - noOfFeatures), noOfFeatures));
        }
        //set attributes for OutputStream
        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("accuracy", Attribute.Type.DOUBLE));
        return attributes;
    }


    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor
            processor, StreamEventCloner streamEventCloner, ComplexEventPopulater complexEventPopulater) {
        synchronized (this) {
            while (streamEventChunk.hasNext()) {
                ComplexEvent complexEvent = streamEventChunk.next();
                String classValue = classLabelVariableExecutor.execute(complexEvent).toString();
                cepEvent = new double[noOfFeatures];
                for (int i = 0; i < noOfFeatures - 1; i++) {
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
                double accuracy;
                if (model.getClasses().size() == noOfClasses) {
                    accuracy = model.evaluationTrainOnEvent(evolutionModel, cepEvent, classValue);
                } else {
                    model.trainOnEvent(cepEvent, classValue);
                    accuracy = 0;
                }
                complexEventPopulater.populateComplexEvent(complexEvent, new Object[]{accuracy});
            }
            nextProcessor.process(streamEventChunk);
        }
    }

    private void configureModelWithHyperParameters(String modelName) {
        //default configurations for Hoeffding Adaptive tree
        int gracePeriod = 200;
        int splittingCriteria = 1;
        double allowableSplitError = 1e-7;
        double tieBreakThreshold = 0.05;
        boolean binarySplit = false;
        boolean prePruning = false;
        int leafPredictionStrategy = 2;

        int parameterPosition = MINIMUM_NUMBER_OF_PARAMETERS;

        List<String> hyperParameters = new ArrayList<>();
        hyperParameters.add("GracePeriod");
        hyperParameters.add("Splitting Criteria");
        hyperParameters.add("Allowable Split Error");
        hyperParameters.add("Tie Break Threshold");
        hyperParameters.add("Binary Split");
        hyperParameters.add("Prepruning");
        hyperParameters.add("Leaf Prediction Strategy");

        for (int i = parameterPosition; i < noOfParameters; i++) {
            if (attributeExpressionExecutors[i] instanceof ConstantExpressionExecutor) {
                switch (i) {
                    case 2:
                        if (attributeExpressionExecutors[2].getReturnType() == Attribute.Type.INT) {
                            gracePeriod = (Integer) ((ConstantExpressionExecutor)
                                    attributeExpressionExecutors[2])
                                    .getValue();
                            parameterPosition++;
                        } else {
                            throw new SiddhiAppValidationException(String.format("GracePeriod must be an %s."
                                            + " But found %s at position %s", Attribute.Type.INT,
                                    attributeExpressionExecutors[2].getReturnType(), (i + 1)));
                        }
                        break;
                    case 3:
                        if (attributeExpressionExecutors[3].getReturnType() == Attribute.Type.INT) {
                            splittingCriteria = (Integer) ((ConstantExpressionExecutor)
                                    attributeExpressionExecutors[3]).getValue();
                            parameterPosition++;
                        } else {
                            throw new SiddhiAppValidationException(String.format("Splitting Criteria must be an %s. "
                                            + "0=InfoGainSplitCriterion and 1=GiniSplitCriterion"
                                            + " But found %s in position %s.", Attribute.Type.INT,
                                    attributeExpressionExecutors[3].getReturnType(), (i + 1)));
                        }
                        break;
                    case 4:
                        if (attributeExpressionExecutors[4].getReturnType() == Attribute.Type.DOUBLE) {
                            allowableSplitError = (double) ((ConstantExpressionExecutor)
                                    attributeExpressionExecutors[4])
                                    .getValue();
                            parameterPosition++;
                        } else {
                            throw new SiddhiAppValidationException(String.format("Allowable Split Error must be a "
                                            + "%s. But found %s at position %s.", Attribute.Type.DOUBLE,
                                    attributeExpressionExecutors[4].getReturnType(), (i + 1)));
                        }
                        break;
                    case 5:
                        Attribute.Type tieThresholdType = attributeExpressionExecutors[5].getReturnType();
                        if (CoreUtils.isNumeric(tieThresholdType)) {
                            tieBreakThreshold = ((Number) ((ConstantExpressionExecutor)
                                    attributeExpressionExecutors[5]).getValue()).doubleValue();
                            parameterPosition++;
                        } else {
                            throw new SiddhiAppValidationException(String.format("Tie Break Threshold must be "
                                            + "a %s. But found %s in position %s.", Attribute.Type.DOUBLE,
                                    attributeExpressionExecutors[5].getReturnType(), (i + 1)));
                        }
                        break;
                    case 6:
                        if (attributeExpressionExecutors[6].getReturnType() == Attribute.Type.BOOL) {
                            binarySplit = (boolean) ((ConstantExpressionExecutor)
                                    attributeExpressionExecutors[6]).getValue();
                            parameterPosition++;
                        } else {
                            throw new SiddhiAppValidationException(String.format("Enabling Binary Split must be "
                                            + "a %s. But found %s in position %s.", Attribute.Type.BOOL,
                                    attributeExpressionExecutors[6].getReturnType(), (i + 1)));
                        }
                        break;
                    case 7:
                        if (attributeExpressionExecutors[7].getReturnType() == Attribute.Type.BOOL) {
                            prePruning = (boolean) ((ConstantExpressionExecutor)
                                    attributeExpressionExecutors[7])
                                    .getValue();
                            parameterPosition++;
                        } else {
                            throw new SiddhiAppValidationException(String.format("Disabling PrePruning must be "
                                            + "a %s. But found %s in position %s.", Attribute.Type.BOOL,
                                    attributeExpressionExecutors[7].getReturnType(), (i + 1)));
                        }
                        break;
                    case 8:
                        if (attributeExpressionExecutors[8].getReturnType() == Attribute.Type.INT) {
                            leafPredictionStrategy = (int) ((ConstantExpressionExecutor)
                                    attributeExpressionExecutors[8])
                                    .getValue();
                            parameterPosition++;
                        } else {
                            throw new SiddhiAppValidationException(String.format("Leaf Prediction Strategy must "
                                            + "be an %s. 0=majority class, 1=naive Bayes, 2=naive Bayes adaptive. "
                                            + "But found %s in position %s.", Attribute.Type.INT,
                                    attributeExpressionExecutors[8].getReturnType(), (i + 1)));
                        }
                        break;
                    default:
                }
            } else {
                throw new SiddhiAppValidationException(String.format("%s must be (ConstantExpressionExecutor) "
                                + "but found %s in position %s.", hyperParameters.get(i - MINIMUM_NUMBER_OF_PARAMETERS),
                        attributeExpressionExecutors[i].getClass().getCanonicalName(), (i + 1)));
            }
        }

        if (parameterPosition == (NUMBER_OF_HYPER_PARAMETERS + MINIMUM_NUMBER_OF_PARAMETERS)) {
            AdaptiveHoeffdingTreeModel model = AdaptiveHoeffdingModelsHolder.getInstance()
                    .getHoeffdingModel(modelName);
            model.setConfigurations(gracePeriod, splittingCriteria, allowableSplitError,
                    tieBreakThreshold, binarySplit, prePruning, leafPredictionStrategy);
        } else {
            throw new SiddhiAppValidationException("Number of hyper-parameters needed for model "
                    + "manual configuration is " + NUMBER_OF_HYPER_PARAMETERS + " but found "
                    + (parameterPosition - MINIMUM_NUMBER_OF_PARAMETERS));
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
    public Map<String, Object> currentState() {
        Map<String, Object> currentState = new HashMap<>();
        currentState.put("AdaptiveHoeffdingModelsMap", AdaptiveHoeffdingModelsHolder.
                getInstance().getClonedHoeffdingModelMap());
        return currentState;
    }

    @Override
    public void restoreState(Map<String, Object> state) {
        AdaptiveHoeffdingModelsHolder.getInstance().
                setHoeffdingModelMap((Map<String, AdaptiveHoeffdingTreeModel>) state.
                        get("AdaptiveHoeffdingModelsMap"));
    }
}
