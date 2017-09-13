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

package org.wso2.extension.siddhi.gpl.execution.streamingml.clustering.clustree;

import org.apache.log4j.Logger;
import org.wso2.extension.siddhi.gpl.execution.streamingml.clustering.clustree.util.ClusTreeModel;
import org.wso2.extension.siddhi.gpl.execution.streamingml.clustering.clustree.util.ClusTreeModelHolder;
import org.wso2.extension.siddhi.gpl.execution.streamingml.clustering.clustree.util.KMeansModel;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.populater.ComplexEventPopulater;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
import org.wso2.siddhi.core.executor.ConstantExpressionExecutor;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.stream.StreamProcessor;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * performs clustree with batch update to kmeans model
 */
public class ClusTreeStreamProcessorExtension extends StreamProcessor {
    private String microModelName;
    private int noOfClusters;
    private int noOfEventsToRefreshMacroModel;
    private int maxHeightOfTree;
    private int horizon;
    private int noOfDimensions;
    private double[] coordinateValuesOfCurrentDataPoint;
    private int noOfEventsReceived;
    private ClusTreeModel clusTreeModel;
    private KMeansModel kMeansModel;
    private final int NO_OF_CONSTANT_PARAMETERS = 6;
    private static final Logger logger = Logger.getLogger(ClusTreeStreamProcessorExtension.class.getName());


    @Override
    protected List<Attribute> init(AbstractDefinition abstractDefinition, ExpressionExecutor[] expressionExecutors,
                                   ConfigReader configReader, SiddhiAppContext siddhiAppContext) {

        //expressionExecutors[0] --> microModelName
        if (!(attributeExpressionExecutors[0] instanceof ConstantExpressionExecutor)) {
            throw new SiddhiAppCreationException("microModelName has to be a constant but found " +
                    this.attributeExpressionExecutors[0].getClass().getCanonicalName());
        }

        if (attributeExpressionExecutors[0].getReturnType() == Attribute.Type.STRING) {
            microModelName = (String) ((ConstantExpressionExecutor) attributeExpressionExecutors[0]).getValue();
        } else {
            throw new SiddhiAppCreationException("microModelName should be of type String but found " +
                    attributeExpressionExecutors[0].getReturnType());
        }

        //expressionExecutors[1] --> numberOfClusters
        if (!(attributeExpressionExecutors[1] instanceof ConstantExpressionExecutor)) {
            throw new SiddhiAppCreationException("noOfClusters has to be a constant but found " +
                    this.attributeExpressionExecutors[2].getClass().getCanonicalName());
        }
        if (attributeExpressionExecutors[1].getReturnType() == Attribute.Type.INT) {
            noOfClusters = (Integer) ((ConstantExpressionExecutor) attributeExpressionExecutors[1]).getValue();
            if (noOfClusters <= 0) {
                throw new SiddhiAppCreationException("noOfClusters should be a positive integer " +
                        "but found " + noOfClusters);
            }
        } else {
            throw new SiddhiAppCreationException("noOfClusters should be of type int but found " +
                    attributeExpressionExecutors[1].getReturnType());
        }

        //expressionExecutors[2] --> maxIterations
        if (!(attributeExpressionExecutors[2] instanceof ConstantExpressionExecutor)) {
            throw new SiddhiAppCreationException("Maximum iterations has to be a constant but found " +
                    this.attributeExpressionExecutors[2].getClass().getCanonicalName());
        }
        int maxIterations;
        if (attributeExpressionExecutors[2].getReturnType() == Attribute.Type.INT) {
            maxIterations = (Integer) ((ConstantExpressionExecutor)
                    attributeExpressionExecutors[2]).getValue();
            if (maxIterations <= 0) {
                throw new SiddhiAppCreationException("maxIterations should be a positive integer " +
                        "but found " + maxIterations);
            }
        } else {
            throw new SiddhiAppCreationException("Maximum iterations should be of type int but found " +
                    attributeExpressionExecutors[2].getReturnType());
        }

        //expressionExecutors[3] --> noOfEventsToRefreshMacroModel
        if (!(attributeExpressionExecutors[3] instanceof ConstantExpressionExecutor)) {
            throw new SiddhiAppCreationException("noOfEventsToRefreshMacroModel has to be a constant but found " +
                    this.attributeExpressionExecutors[3].getClass().getCanonicalName());
        }
        if (attributeExpressionExecutors[3].getReturnType() == Attribute.Type.INT) {
            noOfEventsToRefreshMacroModel = (Integer) ((ConstantExpressionExecutor)
                    attributeExpressionExecutors[3]).getValue();
            if (noOfEventsToRefreshMacroModel <= 0) {
                throw new SiddhiAppCreationException("noOfEventsToRefreshMacroModel should be a positive integer " +
                        "but found " + noOfEventsToRefreshMacroModel);
            }
        } else {
            throw new SiddhiAppCreationException("noOfEventsToRefreshMacroModel should be of type int but found " +
                    attributeExpressionExecutors[3].getReturnType());
        }

        //expressionExecutors[4] --> maxHeightOfTree
        if (!(attributeExpressionExecutors[4] instanceof ConstantExpressionExecutor)) {
            throw new SiddhiAppCreationException("noOfEventsToRefreshMacroModel has to be a constant but found " +
                    this.attributeExpressionExecutors[4].getClass().getCanonicalName());
        }
        if (attributeExpressionExecutors[4].getReturnType() == Attribute.Type.INT) {
            maxHeightOfTree = (Integer) ((ConstantExpressionExecutor)
                    attributeExpressionExecutors[4]).getValue();
            if (maxHeightOfTree < (Math.log(noOfClusters)/Math.log(3)) - 1) {
                throw new SiddhiAppCreationException("noOfEventsToRefreshMacroModel should be a positive integer " +
                        "but found " + maxHeightOfTree);
            }
        } else {
            throw new SiddhiAppCreationException("noOfEventsToRefreshMacroModel should be of type int but found " +
                    attributeExpressionExecutors[4].getReturnType());
        }

        //expressionExecutors[5] --> horizon
        if (!(attributeExpressionExecutors[5] instanceof ConstantExpressionExecutor)) {
            throw new SiddhiAppCreationException("horizon has to be a constant but found " +
                    this.attributeExpressionExecutors[5].getClass().getCanonicalName());
        }
        if (attributeExpressionExecutors[5].getReturnType() == Attribute.Type.INT) {
            horizon = (Integer) ((ConstantExpressionExecutor)
                    attributeExpressionExecutors[5]).getValue();
            if (horizon <= 0) {
                throw new SiddhiAppCreationException("horizon should be a positive integer " +
                        "but found " + horizon);
            }
        } else {
            throw new SiddhiAppCreationException("horizon should be of type int but found " +
                    attributeExpressionExecutors[5].getReturnType());
        }

        noOfDimensions = attributeExpressionExecutors.length - NO_OF_CONSTANT_PARAMETERS;
        coordinateValuesOfCurrentDataPoint = new double[noOfDimensions];

        //validating all the attributes to be variables
        for (int i = NO_OF_CONSTANT_PARAMETERS; i < NO_OF_CONSTANT_PARAMETERS + noOfDimensions; i++) {
            if (!(this.attributeExpressionExecutors[i] instanceof VariableExpressionExecutor)) {
                throw new SiddhiAppCreationException("The attributes should be variable but found a " +
                        this.attributeExpressionExecutors[i].getClass().getCanonicalName());
            }
        }
        String siddhiAppName = siddhiAppContext.getName();
        microModelName = microModelName + "." + siddhiAppName;
        if (logger.isDebugEnabled()) {
            logger.debug("model name is " + microModelName);
        }

        clusTreeModel = ClusTreeModelHolder.getInstance().getClusTreeModel(microModelName);


        List<Attribute> attributeList = new ArrayList<>(1 + noOfDimensions);
        attributeList.add(new Attribute("euclideanDistanceToClosestCentroid", Attribute.Type.DOUBLE));
        for (int i = 1; i <= noOfDimensions; i++) {
            attributeList.add(new Attribute("closestCentroidCoordinate" + i, Attribute.Type.DOUBLE));
        }
        return attributeList;
    }

    @Override
    protected void process(ComplexEventChunk<StreamEvent> complexEventChunk, Processor processor,
                           StreamEventCloner streamEventCloner, ComplexEventPopulater complexEventPopulater) {

        synchronized (this) {
            while (complexEventChunk.hasNext()) {
                StreamEvent streamEvent = complexEventChunk.next();
                noOfEventsReceived++;

                //validating and getting coordinate values
                for (int i = NO_OF_CONSTANT_PARAMETERS; i < NO_OF_CONSTANT_PARAMETERS + noOfDimensions; i++) {
                    try {
                        Number content = (Number) attributeExpressionExecutors[i].execute(streamEvent);
                        coordinateValuesOfCurrentDataPoint[i - NO_OF_CONSTANT_PARAMETERS] = content.doubleValue();
                    } catch (ClassCastException e) {
                        throw new SiddhiAppCreationException("coordinate values should be int/float/double/long " +
                                "but found " +
                                attributeExpressionExecutors[i].execute(streamEvent).getClass());
                    }
                }

                //train the ClusTree Model with the datapoint
                clusTreeModel.trainOnEvent(coordinateValuesOfCurrentDataPoint, null);

                //make prediction if the model is trained
                if (kMeansModel.isTrained()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Populating output");
                    }
//                    complexEventPopulater.populateComplexEvent(streamEvent,
//                            kMeansModel.getPrediction(coordinateValuesOfCurrentDataPoint));
                }

                //train the model periodically
                if (noOfEventsReceived % noOfEventsToRefreshMacroModel == 0) {
                    kMeansModel.refresh(clusTreeModel.getMicroClusteringAsDPArray());
                }
            }
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
        return null;
    }

    @Override
    public void restoreState(Map<String, Object> map) {

    }
}
