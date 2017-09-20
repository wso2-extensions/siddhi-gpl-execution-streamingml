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
import org.wso2.extension.siddhi.gpl.execution.streamingml.clustering.clustree.util.DataPoint;
import org.wso2.extension.siddhi.gpl.execution.streamingml.clustering.clustree.util.KMeansModel;
import org.wso2.extension.siddhi.gpl.execution.streamingml.clustering.clustree.util.Trainer;
import org.wso2.extension.siddhi.gpl.execution.streamingml.util.CoreUtils;
import org.wso2.extension.siddhi.gpl.execution.streamingml.util.MathUtil;
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
import org.wso2.siddhi.core.exception.SiddhiAppRuntimeException;
import org.wso2.siddhi.core.executor.ConstantExpressionExecutor;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.stream.StreamProcessor;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * performs clustree with batch update of kmeans model using ClusTree model
 */
@Extension(
        name = "clusTree",
        namespace = "streamingml",
        description = "Performs clustering on a streaming data set. Initially a micro cluster model is generated " +
                "using  ClusTree algorithm and weighted k-means is applied on micro clusters periodically to " +
                "generate a macro cluster model with required number of clusters. Data points can be of any " +
                "dimensionality but the dimensionality should be constant throughout the stream. Euclidean distance " +
                "is taken as the distance metric. ",
        parameters = {
                @Parameter(
                        name = "no.of.clusters",
                        description = "The assumed number of natural clusters (numberOfClusters) in the data set.",
                        type = {DataType.INT}
                ),
                @Parameter(
                        name = "max.iterations",
                        description = "Number of iterations, the process iterates until the number of maximum " +
                                "iterations is reached or the centroids do not change",
                        type = {DataType.INT},
                        optional = true,
                        defaultValue = "40"
                ),
                @Parameter(
                        name = "no.of.events.to.refresh.macro.model",
                        description = "number of events to recalculate the k-means macro cluster centers. ",
                        type = DataType.INT,
                        optional = true,
                        defaultValue = "100"
                ),
                @Parameter(
                        name = "max.height.of.tree",
                        description = "The maximum no of levels in the ClusTree. If it is given as 10 then at most " +
                                "there can be 3^10 micro clusters in the micro cluster model. Advisable to set " +
                                "within 5-8 since having a lot of micro-clusters will consume lot of memory and will " +
                                "take longer to build macro cluster model.",
                        type = DataType.INT,
                        optional = true,
                        defaultValue = "8"
                ),
                @Parameter(
                        name = "horizon",
                        description = "This controls the decay of weights of old micro-clusters. This helps manage " +
                                "the concept drift. If horizon is set as 1000, then a micro cluster which hasn't " +
                                "been updated recently will lose its weight by half after 1000 events. Horizon is " +
                                "technically the half-life of micro-cluster weights.",
                        type = DataType.INT,
                        optional = true,
                        defaultValue = "1000"
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
                                "from InputStream#streamingml:clusTree(2, 10, 20, 5, 50, x, y) \n" +
                                "select closestCentroidCoordinate1, closestCentroidCoordinate2, x, y \n" +
                                "insert into OutputStream;",
                        description = "This query will create a Siddhi app named ClusTreeTestSiddhiApp and will " +
                                "accept 2D inputs of doubles. The query which is named query1 will create a ClusTree " +
                                "model and will create a kmeans model after firsat 20 events and will " +
                                "refresh it every 20 events after. Number of macro clusters will be 2 and the " +
                                "maximum iterations of kmeans to converge will be 10. The max height of tree is " +
                                "set to 5 so at maximum we will get 3^5 micro clusters from ClusTree and the " +
                                "horizon is set as 50, so after 50 events micro clusters that were not updated " +
                                "will lose their weight by half."
                ),
                @Example(
                        syntax = "@App:name('ClusTreeTestSiddhiApp') \n" +
                                "define stream InputStream (x double, y double);\n" +
                                "@info(name = 'query1') \n" +
                                "from InputStream#streamingml:ClusTree(2, x, y) \n" +
                                "select closestCentroidCoordinate1, closestCentroidCoordinate2, x, y \n" +
                                "insert into OutputStream;",
                        description = "In this query one can note that the hyper parameters are not given. So they " +
                                "will be set to their default values which are mentioned above. This mode of " +
                                "querying is suggested for users who are not familier with ClusTree/KMeans algorithms."
                )
        }
)
public class ClusTreeStreamProcessorExtension extends StreamProcessor {
    private final int minConstantParams = 1;
    private final int maxConstantParams = 5;
    private final int separateThreadThreshold = 5000;
    private int noOfClusters;
    private int noOfEventsToRefreshMacroModel = 500;
    private int noOfDimensions;
    private int maxIterations = 40;
    private double[] coordinateValuesOfCurrentDataPoint;
    private int noOfEventsReceived;
    private ExecutorService executorService;
    private ClusTreeModel clusTreeModel;
    private KMeansModel kMeansModel;
    private List<VariableExpressionExecutor> featureVariableExpressionExecutors = new LinkedList<>();
    private static final Logger logger = Logger.getLogger(ClusTreeStreamProcessorExtension.class.getName());

    @Override
    protected List<Attribute> init(AbstractDefinition abstractDefinition, ExpressionExecutor[] expressionExecutors,
                                   ConfigReader configReader, SiddhiAppContext siddhiAppContext) {
        int maxNoOfFeatures = inputDefinition.getAttributeList().size();
        int maxHeightOfTree = 8;
        int horizon = 1000;
        int attributeStartIndex;
        if (attributeExpressionLength < minConstantParams || attributeExpressionLength > maxConstantParams +
                maxNoOfFeatures) {
            throw new SiddhiAppCreationException("Invalid number of parameters. User can either choose to give " +
                    "all 4 hyper parameters or none at all. So query can have " + (minConstantParams + maxNoOfFeatures)
                    + " or " + (maxConstantParams + maxNoOfFeatures) + " but found " + attributeExpressionLength +
                    " parameters.");
        }

        //expressionExecutors[0] --> numberOfClusters
        if (!(attributeExpressionExecutors[0] instanceof ConstantExpressionExecutor)) {
            throw new SiddhiAppCreationException("noOfClusters has to be a constant but found " +
                    this.attributeExpressionExecutors[0].getClass().getCanonicalName());
        }
        if (attributeExpressionExecutors[0].getReturnType() == Attribute.Type.INT) {
            noOfClusters = (Integer) ((ConstantExpressionExecutor) attributeExpressionExecutors[0]).getValue();
            if (noOfClusters <= 0) {
                throw new SiddhiAppCreationException("noOfClusters should be a positive integer " +
                        "but found " + noOfClusters);
            }
        } else {
            throw new SiddhiAppCreationException("noOfClusters should be of type int but found " +
                    attributeExpressionExecutors[0].getReturnType());
        }

        if (attributeExpressionExecutors[1] instanceof VariableExpressionExecutor &&
                attributeExpressionLength == minConstantParams + maxNoOfFeatures) {
            attributeStartIndex = 1;
        } else {
            attributeStartIndex = 5;
            //expressionExecutors[1] --> maxIterations
            if (!(attributeExpressionExecutors[1] instanceof ConstantExpressionExecutor)) {
                throw new SiddhiAppCreationException("Maximum iterations has to be a constant but found " +
                        this.attributeExpressionExecutors[1].getClass().getCanonicalName());
            }
            if (attributeExpressionExecutors[1].getReturnType() == Attribute.Type.INT) {
                maxIterations = (Integer) ((ConstantExpressionExecutor)
                        attributeExpressionExecutors[1]).getValue();
                if (maxIterations <= 0) {
                    throw new SiddhiAppCreationException("maxIterations should be a positive integer " +
                            "but found " + maxIterations);
                }
            } else {
                throw new SiddhiAppCreationException("Maximum iterations should be of type int but found " +
                        attributeExpressionExecutors[1].getReturnType());
            }

            //expressionExecutors[2] --> noOfEventsToRefreshMacroModel
            if (!(attributeExpressionExecutors[2] instanceof ConstantExpressionExecutor)) {
                throw new SiddhiAppCreationException("noOfEventsToRefreshMacroModel has to be a constant but found " +
                        this.attributeExpressionExecutors[2].getClass().getCanonicalName());
            }
            if (attributeExpressionExecutors[2].getReturnType() == Attribute.Type.INT) {
                noOfEventsToRefreshMacroModel = (Integer) ((ConstantExpressionExecutor)
                        attributeExpressionExecutors[2]).getValue();
                if (noOfEventsToRefreshMacroModel <= 0) {
                    throw new SiddhiAppCreationException("noOfEventsToRefreshMacroModel should be a positive integer " +
                            "but found " + noOfEventsToRefreshMacroModel);
                }
            } else {
                throw new SiddhiAppCreationException("noOfEventsToRefreshMacroModel should be of type int but found " +
                        attributeExpressionExecutors[2].getReturnType());
            }

            //expressionExecutors[3] --> maxHeightOfTree
            if (!(attributeExpressionExecutors[3] instanceof ConstantExpressionExecutor)) {
                throw new SiddhiAppCreationException("maxHeightOfTree has to be a constant but found " +
                        this.attributeExpressionExecutors[3].getClass().getCanonicalName());
            }

            if (attributeExpressionExecutors[3].getReturnType() == Attribute.Type.INT) {
                maxHeightOfTree = (Integer) ((ConstantExpressionExecutor)
                        attributeExpressionExecutors[3]).getValue();
                double minHeightOfTree = (Math.log(noOfClusters) / Math.log(3));
                minHeightOfTree = MathUtil.roundOff(minHeightOfTree, 4);
                if (maxHeightOfTree < minHeightOfTree) {
                    throw new SiddhiAppCreationException("maxHeightOfTree should be an int greater than " +
                            minHeightOfTree + " but found " + maxHeightOfTree);
                }
            } else {
                throw new SiddhiAppCreationException("maxHeightOfTree should be of type int but found " +
                        attributeExpressionExecutors[3].getReturnType());
            }
            maxHeightOfTree -= 1; //MOA implementation is in such a way that if we pass 0 to maxHeightOfTree
            // it will build a tree with one level. but user should be able to give 1 and get one level.

            //expressionExecutors[4] --> horizon
            if (!(attributeExpressionExecutors[4] instanceof ConstantExpressionExecutor)) {
                throw new SiddhiAppCreationException("horizon has to be a constant but found " +
                        this.attributeExpressionExecutors[4].getClass().getCanonicalName());
            }

            if (attributeExpressionExecutors[4].getReturnType() == Attribute.Type.INT) {
                horizon = (Integer) ((ConstantExpressionExecutor)
                        attributeExpressionExecutors[4]).getValue();
                if (horizon <= 0) {
                    throw new SiddhiAppCreationException("horizon should be a positive integer " +
                            "but found " + horizon);
                }
            } else {
                throw new SiddhiAppCreationException("horizon should be of type int but found " +
                        attributeExpressionExecutors[4].getReturnType());
            }
        }

        noOfDimensions = attributeExpressionExecutors.length - attributeStartIndex;
        coordinateValuesOfCurrentDataPoint = new double[noOfDimensions];

        //validating all the attributes to be variables
        featureVariableExpressionExecutors = CoreUtils.extractAndValidateFeatures(inputDefinition,
                attributeExpressionExecutors, attributeStartIndex, noOfDimensions);

        //creating models
        clusTreeModel = new ClusTreeModel();
        clusTreeModel.init(noOfDimensions, noOfClusters, maxHeightOfTree, horizon);
        kMeansModel = new KMeansModel();

        executorService = siddhiAppContext.getExecutorService();

        //setting return attributes
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
                for (int i = 0; i < noOfDimensions; i++) {
                    try {
                        Number content = (Number) featureVariableExpressionExecutors.get(i).execute(streamEvent);
                        coordinateValuesOfCurrentDataPoint[i] = content.doubleValue();
                    } catch (ClassCastException e) {
                        throw new SiddhiAppRuntimeException("coordinate values should be int/float/double/long " +
                                "but found " +
                                featureVariableExpressionExecutors.get(i).execute(streamEvent).getClass());
                    }
                }

                //train the ClusTree Model with the datapoint
                clusTreeModel.trainOnEvent(coordinateValuesOfCurrentDataPoint, null);

                //train the model periodically
                if (noOfEventsReceived % noOfEventsToRefreshMacroModel == 0) {
                    List<DataPoint> dpa = clusTreeModel.getMicroClusteringAsDPArray();
                    if (noOfEventsToRefreshMacroModel < separateThreadThreshold) {
                        kMeansModel.refresh(dpa, noOfClusters, maxIterations,
                                noOfDimensions);
                    } else {
                        Trainer trainer = new Trainer(kMeansModel, dpa, noOfClusters, maxIterations, noOfDimensions);
                        Future f = executorService.submit(trainer);
                    }
                }

                //make prediction if the model is trained
                if (kMeansModel.isTrained()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Populating the event with the prediction");
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
        synchronized (this) {
            Map<String, Object> map = new HashMap();
            map.put("noOfEventsReceived", noOfEventsReceived);
            map.put("clusTreeModel", clusTreeModel);
            map.put("kMeansModel", kMeansModel);
            return map;

        }
    }

    @Override
    public void restoreState(Map<String, Object> map) {
        synchronized (this) {
            noOfEventsReceived = (Integer) map.get("noOfEventsReceived");
            clusTreeModel = (ClusTreeModel) map.get("clusTreeModel");
            kMeansModel = (KMeansModel) map.get("kMeansModel");
        }
    }
}
