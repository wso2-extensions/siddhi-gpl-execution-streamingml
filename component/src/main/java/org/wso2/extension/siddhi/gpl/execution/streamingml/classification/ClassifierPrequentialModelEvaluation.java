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
package org.wso2.extension.siddhi.gpl.execution.streamingml.classification;

import com.yahoo.labs.samoa.instances.Instance;
import org.wso2.extension.siddhi.gpl.execution.streamingml.util.CoreUtils;

/**
 * Prequential or interleaved-test-then-train evolution.
 * Each instance is first to test the model, and then to train the model.
 * The Prequential Evaluation task evaluates the performance of online classifiers doing this.
 * Measures the accuracy of the classifier model since the start of the evaluation
 */
public class ClassifierPrequentialModelEvaluation {
    private int numClasses = -1;

    private double weightObserved;
    private double weightCorrect;

    public void reset(int numClasses) {
        this.numClasses = numClasses;
        this.weightObserved = 0.0D;
        this.weightCorrect = 0.0D;
    }

    /**
     * @param inst       MOAInstance representing the cepEvent
     * @param classVotes Prediction votes for each class label
     */
    public void addResult(Instance inst, double[] classVotes) {
        if (this.numClasses == -1) {
            this.reset(inst.numClasses());
        }
        double weight = inst.weight();
        int trueClass = (int) inst.classValue();

        if (weight > 0.0D) {
            if (this.weightObserved == 0.0D) {
                this.reset(inst.numClasses());
            }
            this.weightObserved += weight;
            int predictedClass = CoreUtils.argMaxIndex(classVotes);
            if (predictedClass == trueClass) {
                this.weightCorrect += weight;
            }
        }
    }

    /**
     * Classification Prequential Evaluation accuracy
     *
     * @return
     */
    public double getFractionCorrectlyClassified() {
        return this.weightObserved > 0.0D ? this.weightCorrect / this.weightObserved : 0.0D;
    }
}
