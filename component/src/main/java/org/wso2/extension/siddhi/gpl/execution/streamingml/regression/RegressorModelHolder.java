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
package org.wso2.extension.siddhi.gpl.execution.streamingml.regression;

import org.wso2.extension.siddhi.gpl.execution.streamingml.regression.adaptivemodelrules.util.AdaptiveModelRulesModel;

import java.util.HashMap;
import java.util.Map;

/**
 * Data holder which keeps the instances of @{@link Regressor}
 */
public class RegressorModelHolder {
    private static final RegressorModelHolder instance = new RegressorModelHolder();

    /**
     * Key - name of the model
     * Value - @{@link Regressor}
     */
    private Map<String, Regressor> amRulesModelMap = new HashMap();

    private RegressorModelHolder() {
    }

    public static RegressorModelHolder getInstance() {
        return instance;
    }

    public AdaptiveModelRulesModel createAMRulesRegressorModel(String name) {
        AdaptiveModelRulesModel model = new AdaptiveModelRulesModel(name);
        addRegressorModel(name, model);
        return model;
    }

    public AdaptiveModelRulesModel getAMRulesRegressorModel(String name) {
        return (AdaptiveModelRulesModel) amRulesModelMap.get(name);
    }

    public AdaptiveModelRulesModel getClonedPerceptronModel(String modelName) {
        return new AdaptiveModelRulesModel(getAMRulesRegressorModel(modelName));
    }

    public void addRegressorModel(String name, Regressor model) {
        amRulesModelMap.put(name, model);
    }

    public void deleteRegressorModel(String name) {
        amRulesModelMap.remove(name);
    }

}
