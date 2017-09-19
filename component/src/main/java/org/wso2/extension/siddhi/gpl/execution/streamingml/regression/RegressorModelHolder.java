/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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

    public void setRegressorModelMap(Map<String, Regressor> modelsMap) {
        this.amRulesModelMap = modelsMap;
    }

    public AdaptiveModelRulesModel getAMRulesRegressorModel(String name) {
        AdaptiveModelRulesModel model = (AdaptiveModelRulesModel) amRulesModelMap.get(name);
        if (model == null) {
            model = new AdaptiveModelRulesModel(name);
            addRegressorModel(name, model);
        }
        return model;
    }

    private void addRegressorModel(String name, Regressor model) {
        amRulesModelMap.put(name, model);
    }

    public void deleteRegressorModel(String name) {
        amRulesModelMap.remove(name);
    }
}
