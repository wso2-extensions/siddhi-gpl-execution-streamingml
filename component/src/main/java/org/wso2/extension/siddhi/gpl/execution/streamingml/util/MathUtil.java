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
    /**
     * Dot product of two 'double' vectors.
     *
     * @param vector1 vector 1
     * @param vector2 vector 2
     * @return the dot product.
     */
    public static double dot(double[] vector1, double[] vector2) {
        if (vector1.length != vector2.length) {
            throw new IllegalArgumentException("The dimensions have to be equal!");
        }

        double sum = 0;
        for (int i = 0; i < vector1.length; i++) {
            sum += vector1[i] * vector2[i];
        }

        return sum;
    }


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

    public static double meanSquaredError(double truth, double prediction) {
        return Math.sqrt(Math.abs(truth - prediction));
    }
}
