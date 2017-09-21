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
import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * stores info about the kmeans model
 */
public class KMeansModel implements Serializable {
    private static final long serialVersionUID = 7997333339345312740L;
    private List<Cluster> clusterList;
    private boolean trained;
    private static final Logger logger = Logger.getLogger(KMeansModel.class.getName());

    public KMeansModel() {
        clusterList = new LinkedList<>();
    }

    public synchronized List<Cluster> getClusterList() {
        return clusterList;
    }

    public synchronized void setClusterList(List<Cluster> clusterList) {
        this.clusterList = clusterList;
    }

    public synchronized boolean isTrained() {
        return trained;
    }

    public synchronized void clearClusterMembers() {
        for (Cluster c: clusterList) {
            if (c != null) {
                c.clearDataPointsInCluster();
            }
        }
    }

    public synchronized boolean contains(DataPoint x) {
        for (Cluster c: clusterList) {
            if (c.getCentroid().equals(x)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void add(DataPoint x) {
        if (logger.isDebugEnabled()) {
            logger.debug("adding a new cluster with centroid " + Arrays.toString(x.getCoordinates()));
        }
        Cluster c = new Cluster(x);
        clusterList.add(c);
    }

    public synchronized int size() {
        return clusterList.size();
    }

    public synchronized double[] getCoordinatesOfCentroidOfCluster(int index) {
        return clusterList.get(index).getCentroid().getCoordinates();
    }


    public synchronized String getModelInfo() {
        StringBuilder s = new StringBuilder();
        for (Cluster c: clusterList) {
            s.append(Arrays.toString(c.getCentroid().getCoordinates())).append(" with members : ")
                    .append(c.getMemberInfo()).append("\n");
        }
        return s.toString();
    }

    public synchronized void setTrained(boolean trained) {
        this.trained = trained;
    }

    public void refresh(List<DataPoint> dataPointsArray, int noOfClusters, int maxIterations,
                        int noOfDimensions) {
        this.setClusterList(WeightedKMeans.run(dataPointsArray, noOfClusters, maxIterations,
                noOfDimensions));
        this.setTrained(true);
    }

    public synchronized Object[] getPrediction(double[] coordinateValuesOfCurrentDataPoint) {
        DataPoint d = new DataPoint();
        d.setCoordinates(coordinateValuesOfCurrentDataPoint);
        return WeightedKMeans.getAssociatedCentroidInfo(d, this);
    }
}
