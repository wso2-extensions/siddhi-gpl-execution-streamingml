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

import java.util.LinkedList;

public class Trainer implements Runnable {
    private KMeansModel kMeansModel;
    private LinkedList<DataPoint> dataPointsArray;
    private int noOfClusters;
    private int maxIterations;
    private int noOfDimensions;


    public Trainer(KMeansModel kMeansModel, LinkedList<DataPoint> dataPointsArray, int noOfClusters, int maxIterations,
                   int noOfDimensions) {
        this.kMeansModel = kMeansModel;
        this.dataPointsArray = dataPointsArray;
        this.noOfClusters = noOfClusters;
        this.maxIterations = maxIterations;
        this.noOfDimensions = noOfDimensions;

    }
    @Override
    public void run() {
        kMeansModel.setClusterList(WeightedKMeans.run(dataPointsArray, noOfClusters, maxIterations,
                noOfDimensions));
        kMeansModel.setTrained(true);
    }
}
