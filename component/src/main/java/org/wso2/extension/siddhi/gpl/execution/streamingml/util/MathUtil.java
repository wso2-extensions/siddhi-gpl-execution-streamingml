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
package org.wso2.extension.siddhi.gpl.execution.streamingml.util;

/**
 * Special mathematical functions used in the ML algorithms.
 */
public class MathUtil {
    public static double sum(double[] val) {
        double sum = 0;
        for (double x : val) {
            sum += x;
        }
        return sum;
    }

    /**
     * @param value  double value
     * @param places number of decimal points
     * @return
     */
    public static double roundOff(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    /**
     * finds the euclidean distance between two input points of equal dimension
     *
     * @param point1 input point one
     * @param point2 input point two
     * @return euclidean distance between point1 and point2
     */
    public static double euclideanDistance(double[] point1, double[] point2) {
        double sum = 0.0;
        int dimensionality = point1.length;
        for (int i = 0; i < dimensionality; i++) {
            sum += Math.pow((point1[i] - point2[i]), 2);
        }
        double dist = Math.sqrt(sum);
        dist = Math.round(dist * 10000.0) / 10000.0;
        return dist;
    }
}
