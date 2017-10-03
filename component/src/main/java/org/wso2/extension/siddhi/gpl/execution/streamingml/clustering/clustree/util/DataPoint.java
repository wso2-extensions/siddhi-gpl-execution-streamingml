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

import org.wso2.siddhi.query.api.exception.SiddhiAppValidationException;
import java.io.Serializable;
import java.util.Arrays;

/**
 * Object which holds the data from each event and information about centroids in terms of coordinates
 */
public class DataPoint implements Serializable {
    private static final long serialVersionUID = -3694544849918946452L;
    private double[] coordinates;
    private double weight;

    public double[] getCoordinates() {
        if (coordinates != null) {
            return coordinates.clone();
        } else {
            throw new SiddhiAppValidationException("No coordinates have been set. Hence null return value.");
        }
    }

    public void setCoordinates(double[] coordinates) {
        if (this.coordinates != null) {
            if (this.coordinates.length == coordinates.length) {
                this.coordinates = coordinates.clone();
            } else {
                throw new SiddhiAppValidationException("The dimensionality of the coordinate is " +
                        this.coordinates.length + " but the dimensionality of the received array is " +
                        coordinates.length);
            }
        } else  {
            this.coordinates = coordinates.clone();
        }
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DataPoint)) {
            return false;
        }

        DataPoint that = (DataPoint) o;
        return Arrays.equals(getCoordinates(), that.getCoordinates());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getCoordinates());
    }
}
