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
import org.wso2.extension.siddhi.gpl.execution.streamingml.clustering.clustree.util.DataPoint;
import org.wso2.extension.siddhi.gpl.execution.streamingml.clustering.clustree.util.KMeansModel;
import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.ReturnAttribute;
import org.wso2.siddhi.annotation.util.DataType;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * performs clustree with batch update of kmeans model using ClusTree model
 */
@Extension(
        name = "ClusTree",
        namespace = "streamingml",
        description = "Performs clustering on a streaming data set. Initially a micro cluster model is generated " +
                "using  ClusTree algorithm and weighted k-means is applied on micro clusters periodically to " +
                "generate a macro cluster model with required number of clusters. Data points can be of any " +
                "dimensionality but the dimensionality should be constant throughout the stream. Euclidean distance " +
                "is taken as the distance metric. ",
        parameters = {
                @Parameter(
                        name = "model.name",
                        description = "The name for the model that is going to be created/reused for prediction",
                        type = {DataType.STRING}
                ),
                @Parameter(
                        name = "no.of.clusters",
                        description = "The assumed number of natural clusters (numberOfClusters) in the data set.",
                        type = {DataType.INT}
                ),
                @Parameter(
                        name = "max.iterations",
                        description = "Number of iterations, the process iterates until the number of maximum " +
                                "iterations is reached or the centroids do not change",
                        type = {DataType.INT}
                ),
                @Parameter(
                        name = "no.of.events.to.refresh.macro.model",
                        description = "number of events to recalculate the k-means macro cluster centers. ",
                        type = DataType.INT
                ),
                @Parameter(
                        name = "max.height.of.tree",
                        description = "The maximum no of levels in the ClusTree. If it is given as 10 then at most " +
                                "there can be 3^10 micro clusters in the micro cluster model. Advisable to set " +
                                "within 5-8 since having a lot of micro-clusters will consume lot of memory and will " +
                                "take longer to build macro cluster model.",
                        type = DataType.INT
                ),
                @Parameter(
                        name = "horizon",
                        description = "This controls the decay of weights of old micro-clusters. This helps manage " +
                                "the concept drift. If horizon is set as 1000, then a micro cluster which hasn't " +
                                "been updated recently will lose its weight by half after 1000 events. Horizon is " +
                                "technically the half-life of micro-cluster weights.",
                        type = DataType.INT
                ),
                @Parameter(
                        name = "model.features",
                        description = "This is a variable length argument. Depending on the dimensionality of " +
                                "data points we will receive coordinates as features along each axis.",
                        type = {DataType.DOUBLE, DataType.FLOAT, DataType.INT, DataType.LONG}
                )

        },
        returnAttributes = {
                @ReturnAttribute(
                        name = "euclideanDistanceToClosestCentroid",
                        description = "Represents the Euclidean distance between the current data point and the " +
                                "closest centroid.",
                        type = {DataType.DOUBLE}
                ),
                @ReturnAttribute(
                        name = "closestCentroidCoordinate",
                        description = "This is a variable length attribute. Depending on the dimensionality(d) " +
                                "we will return closestCentroidCoordinate1 to closestCentroidCoordinated which are " +
                                "the d dimensional coordinates of the closest centroid from the model to the " +
                                "current event. This is the prediction result and this represents the cluster to" +
                                "which the current event belongs to.",
                        type = {DataType.DOUBLE}
                )
        },
        examples = {
                @Example(
                        syntax = "@App:name('ClusTreeTestSiddhiApp') \n" +
                                "define stream InputStream (x double, y double);\n" +
                                "@info(name = 'query1') \n" +
                                "from InputStream#streamingml:ClusTree('model0', 2, 10, 20, 5, 50, x, y) \n" +
                                "select closestCentroidCoordinate1, closestCentroidCoordinate2, x, y \n" +
                                "insert into OutputStream;",
                        description = "This query will create a Siddhi app named ClusTreeTestSiddhiApp and will " +
                                "accept 2D inputs od doubles. The query which is named query1 will create a ClusTree " +
                                "model named model0 and will create a kmeans model after firsat 20 events and will " +
                                "refresh it every 20 events after. Number of macro clusters will be 2 and the " +
                                "maximum iterations of kmeans to converge will be 10. The max height of tree is " +
                                "set to 5 so at maximum we will get 3^5 micro clusters from ClusTree and the " +
                                "horizon is set as 50, so after 50 events micro clusters that were not updated " +
                                "will lose their weight by half."
                ),
        }
)
public class ClusTreeStreamProcessorExtension extends StreamProcessor {
    private int noOfClusters;
    private int noOfEventsToRefreshMacroModel;
    private int noOfDimensions;
    private int maxHeightOfTree;
    private int maxIterations;
    private int horizon;
    private double[] coordinateValuesOfCurrentDataPoint;
    private int noOfEventsReceived;
    private ClusTreeModel clusTreeModel;
    private KMeansModel kMeansModel;
    private int attributeStartIndex;
    private static final Logger logger = Logger.getLogger(ClusTreeStreamProcessorExtension.class.getName());


    @Override
    protected List<Attribute> init(AbstractDefinition abstractDefinition, ExpressionExecutor[] expressionExecutors,
                                   ConfigReader configReader, SiddhiAppContext siddhiAppContext) {

        //expressionExecutors[0] --> microModelName
        if (!(attributeExpressionExecutors[0] instanceof ConstantExpressionExecutor)) {
            throw new SiddhiAppCreationException("microModelName has to be a constant but found " +
                    this.attributeExpressionExecutors[0].getClass().getCanonicalName());
        }

        String microModelName;
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

        if (attributeExpressionExecutors[2] instanceof VariableExpressionExecutor) {
            attributeStartIndex = 2;
            //default values
            maxIterations = 10;
            noOfEventsToRefreshMacroModel = 1000;
            maxHeightOfTree = 8;
            horizon = 1000;
        } else {
            attributeStartIndex = 6;
            //expressionExecutors[2] --> maxIterations
            if (!(attributeExpressionExecutors[2] instanceof ConstantExpressionExecutor)) {
                throw new SiddhiAppCreationException("Maximum iterations has to be a constant but found " +
                        this.attributeExpressionExecutors[2].getClass().getCanonicalName());
            }
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
                throw new SiddhiAppCreationException("maxHeightOfTree has to be a constant but found " +
                        this.attributeExpressionExecutors[4].getClass().getCanonicalName());
            }

            if (attributeExpressionExecutors[4].getReturnType() == Attribute.Type.INT) {
                maxHeightOfTree = (Integer) ((ConstantExpressionExecutor)
                        attributeExpressionExecutors[4]).getValue();
                if (maxHeightOfTree < (Math.log(noOfClusters) / Math.log(3))) {
                    throw new SiddhiAppCreationException("maxHeightOfTree should be a greater than " +
                            "(Math.log(noOfClusters) / Math.log(3) but found " + maxHeightOfTree);
                }
            } else {
                throw new SiddhiAppCreationException("maxHeightOfTree should be of type int but found " +
                        attributeExpressionExecutors[4].getReturnType());
            }
            maxHeightOfTree -= 1; // since this parameter when set to 0 MOA ClusTree implementation builds one root
            // and 3 children. i.e one level

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
        }



        noOfDimensions = attributeExpressionExecutors.length - attributeStartIndex;
        coordinateValuesOfCurrentDataPoint = new double[noOfDimensions];

        //validating all the attributes to be variables
        for (int i = attributeStartIndex; i < attributeStartIndex + noOfDimensions; i++) {
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

        clusTreeModel = ClusTreeModelHolder.getInstance().getClusTreeModel(microModelName, noOfDimensions,
                noOfClusters, maxHeightOfTree, horizon);
        kMeansModel = new KMeansModel();


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
                for (int i = attributeStartIndex; i < attributeStartIndex + noOfDimensions; i++) {
                    try {
                        Number content = (Number) attributeExpressionExecutors[i].execute(streamEvent);
                        coordinateValuesOfCurrentDataPoint[i - attributeStartIndex] = content.doubleValue();
                    } catch (ClassCastException e) {
                        throw new SiddhiAppCreationException("coordinate values should be int/float/double/long " +
                                "but found " +
                                attributeExpressionExecutors[i].execute(streamEvent).getClass());
                    }
                }

                //train the ClusTree Model with the datapoint
                clusTreeModel.trainOnEvent(coordinateValuesOfCurrentDataPoint, null);

                //train the model periodically
                if (noOfEventsReceived % noOfEventsToRefreshMacroModel == 0) {
                    LinkedList<DataPoint> dpa = clusTreeModel.getMicroClusteringAsDPArray();
                    kMeansModel.refresh(dpa, noOfClusters, maxIterations,
                            noOfDimensions);
                }

                //make prediction if the model is trained
                if (kMeansModel.isTrained()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Populating output");
                    }
                    complexEventPopulater.populateComplexEvent(streamEvent,
                            kMeansModel.getPrediction(coordinateValuesOfCurrentDataPoint));
                }
            }
        }
        nextProcessor.process(complexEventChunk);
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
