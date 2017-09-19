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

package org.wso2.extension.siddhi.gpl.execution.streamingml.clustering.clustree.util;

import org.apache.log4j.Logger;
import org.wso2.extension.siddhi.gpl.execution.streamingml.util.MathUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * perform weighted kmeans calculations. a singleton class
 */
public class WeightedKMeans {
    private static final Logger logger = Logger.getLogger(WeightedKMeans.class.getName());

    public static List<Cluster> run(LinkedList<DataPoint> dataPointsArray, int noOfClusters, int maximumIterations,
                             int noOfDimensions) {
        KMeansModel model = new KMeansModel();
        cluster(dataPointsArray, model, noOfClusters, maximumIterations, noOfDimensions);
        return model.getClusterList();
    }


    /**
     * Perform clustering
     */
    private static void cluster(List<DataPoint> dataPointsArray, KMeansModel model, int noOfClusters,
                                int maximumIterations, int noOfDimensions) {
        if (logger.isDebugEnabled()) {
            logger.debug("initial Clustering");
        }
        buildModel(dataPointsArray, model, noOfClusters);

        int iter = 0;
        if (dataPointsArray.size() != 0 && (model.size() == noOfClusters)) {
            boolean centroidShifted;
            while (iter < maximumIterations) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Current model : \n" + model.getModelInfo() + "\nclustering iteration : " + iter);
                }
                assignToCluster(dataPointsArray, model);
                if (logger.isDebugEnabled()) {
                    logger.debug("Current model : \n" + model.getModelInfo());
                }
                List<Cluster> newClusterList = calculateNewClusters(model, noOfDimensions);

                centroidShifted = !model.getClusterList().equals(newClusterList);
                if (!centroidShifted) {
                    break;
                }
                model.setClusterList(newClusterList);
                iter++;
            }
        }
    }

    private static void buildModel(List<DataPoint> dataPointsArray, KMeansModel model, int noOfClusters) {
        int distinctCount = model.size();
        for (DataPoint currentDataPoint : dataPointsArray) {
            if (distinctCount >= noOfClusters) {
                break;
            }
            DataPoint coordinatesOfCurrentDataPoint = new DataPoint();
            coordinatesOfCurrentDataPoint.setCoordinates(currentDataPoint.getCoordinates());
            if (!model.contains(coordinatesOfCurrentDataPoint)) {
                model.add(coordinatesOfCurrentDataPoint);
                distinctCount++;
            }
        }
    }

    /**
     * finds the nearest centroid to each data point in the input array
     * @param dataPointsArray arraylist containing datapoints for which we need to assign centroids
     */
    private static void assignToCluster(List<DataPoint> dataPointsArray, KMeansModel model) {
        logger.debug("Running function assignToCluster");
        model.clearClusterMembers();
        for (DataPoint currentDataPoint : dataPointsArray) {
            Cluster associatedCluster = findAssociatedCluster(currentDataPoint, model);
            logger.debug("Associated cluster of " + Arrays.toString(currentDataPoint.getCoordinates()) + " is " +
                    Arrays.toString(associatedCluster.getCentroid().getCoordinates()));
            associatedCluster.addToCluster(currentDataPoint);
        }
    }

    /**
     * after assigning data points to closest centroids this method calculates new centroids using
     * the assigned points
     *
     * @return returns an array list of coordinate objects each representing a centroid
     */
    private static List<Cluster> calculateNewClusters(KMeansModel model, int noOfDimensions) {
        List<Cluster> newClusterList = new LinkedList<>();
        double[] totalOfCoordinates = new double[noOfDimensions];
        double totalWeight;
        for (Cluster c: model.getClusterList()) {
            //clear the array
            for (int j = 0; j < noOfDimensions; j++) {
                totalOfCoordinates[j] = 0;
            }
            totalWeight = 0;
            for (DataPoint d: c.getDataPointsInCluster()) {
                double[] coordinatesOfd = d.getCoordinates();
                totalWeight += d.getWeight();
                for (int i = 0; i < noOfDimensions; i++) {
                    totalOfCoordinates[i] += (coordinatesOfd[i] * d.getWeight());
                }
            }
            for (int i = 0; i < noOfDimensions; i++) {
                totalOfCoordinates[i] = MathUtil.roundOff(totalOfCoordinates[i] / totalWeight, 4);
            }

            DataPoint d1 = new DataPoint();
            d1.setCoordinates(totalOfCoordinates);
            Cluster c1 = new Cluster(d1);
            newClusterList.add(c1);
        }
        return newClusterList;
    }

    /**
     * finds the nearest centroid to a given DataPoint
     *
     * @param currentDatapoint input DataPoint to which we need to find nearest centroid
     * @return centroid - the nearest centroid to the input DataPoint
     */
    private static Cluster findAssociatedCluster(DataPoint currentDatapoint, KMeansModel model) {
        double minDistance = MathUtil.euclideanDistance(model.getCoordinatesOfCentroidOfCluster(0),
                currentDatapoint.getCoordinates());
        Cluster associatedCluster = model.getClusterList().get(0);
        for (int i = 0; i < model.size(); i++) {
            Cluster cluster = model.getClusterList().get(i);
            double dist = MathUtil.euclideanDistance(cluster.getCentroid().getCoordinates(),
                    currentDatapoint.getCoordinates());
            if (dist < minDistance) {
                minDistance = dist;
                associatedCluster = cluster;
            }
        }
        return associatedCluster;
    }

    /**
     * similar to findAssociatedCluster method but return an Object[] array with the distance
     * to closest centroid and the coordinates of the closest centroid
     *
     * @param currentDatapoint the input dataPoint for which the closest centroid needs to be found
     * @return an Object[] array as mentioned above
     */
    public static Object[] getAssociatedCentroidInfo(DataPoint currentDatapoint, KMeansModel model) {
        Cluster associatedCluster = findAssociatedCluster(currentDatapoint, model);
        double minDistance = MathUtil.euclideanDistance(currentDatapoint.getCoordinates(),
                associatedCluster.getCentroid().getCoordinates());
        List<Double> associatedCentroidInfoList = new ArrayList<Double>();
        associatedCentroidInfoList.add(minDistance);

        for (double x : associatedCluster.getCentroid().getCoordinates()) {
            associatedCentroidInfoList.add(x);
        }

        Object[] associatedCentroidInfo = new Object[associatedCentroidInfoList.size()];
        associatedCentroidInfoList.toArray(associatedCentroidInfo);
        return associatedCentroidInfo;
    }
}
