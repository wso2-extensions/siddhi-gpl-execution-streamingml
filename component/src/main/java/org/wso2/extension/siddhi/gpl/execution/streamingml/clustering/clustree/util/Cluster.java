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

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * This class represents a single cluster in the model
 */
public class Cluster implements Serializable {
    private static final long serialVersionUID = 1917517756301230642L;
    private DataPoint centroid;
    private List<DataPoint> dataPointsInCluster;

    public Cluster(DataPoint centroid) {
        this.centroid = centroid;
        dataPointsInCluster = new LinkedList<>();
    }

    public DataPoint getCentroid() {
        return centroid;
    }

    public void setCentroid(DataPoint centroid) {
        this.centroid = centroid;
    }

    public List<DataPoint> getDataPointsInCluster() {
        return dataPointsInCluster;
    }

    public void clearDataPointsInCluster() {
        dataPointsInCluster.clear();
    }

    public void addToCluster(DataPoint currentDataPoint) {
        dataPointsInCluster.add(currentDataPoint);
    }

    public String getMemberInfo() {
        StringBuilder s = new StringBuilder();
        for (DataPoint d: dataPointsInCluster) {
            s.append(Arrays.toString(d.getCoordinates()));
        }
        return s.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Cluster)) {
            return false;
        }

        Cluster that = (Cluster) o;
        return Arrays.equals(centroid.getCoordinates(), that.getCentroid().getCoordinates());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(centroid.getCoordinates());
    }
}
