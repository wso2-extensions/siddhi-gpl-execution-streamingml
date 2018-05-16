/*
 * Copyright (C) 2018 WSO2 Inc. (http://wso2.com)
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
package org.wso2.extension.siddhi.gpl.execution.streamingml.classification.hoeffdingtree.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Data holder which keeps the instances of @{@link AdaptiveHoeffdingTreeModel}
 */
public class AdaptiveHoeffdingModelsHolder {
    private static final AdaptiveHoeffdingModelsHolder instance = new AdaptiveHoeffdingModelsHolder();

    /**
     * Key - name of the model
     * Value - @{@link AdaptiveHoeffdingTreeModel}
     */
    private Map<String, AdaptiveHoeffdingTreeModel> hoeffdingModelMap = new HashMap();

    private AdaptiveHoeffdingModelsHolder() {
    }

    public static AdaptiveHoeffdingModelsHolder getInstance() {
        return instance;
    }

    public void setHoeffdingModelMap(Map<String, AdaptiveHoeffdingTreeModel> modelsMap) {
        this.hoeffdingModelMap = modelsMap;
    }

    public AdaptiveHoeffdingTreeModel getHoeffdingModel(String name) {
        AdaptiveHoeffdingTreeModel model = hoeffdingModelMap.get(name);
        if (model == null) {
            model = new AdaptiveHoeffdingTreeModel(name);
            addHoeffdingModel(name, model);
        }
        return model;
    }

    private void addHoeffdingModel(String name, AdaptiveHoeffdingTreeModel model) {
        hoeffdingModelMap.put(name, model);
    }

    public void deleteHoeffdingModel(String name) {
        hoeffdingModelMap.remove(name);
    }

    public Map<String, AdaptiveHoeffdingTreeModel> getClonedHoeffdingModelMap() {
        Map<String, AdaptiveHoeffdingTreeModel> clonedMap = new HashMap<>();
        for (Map.Entry<String, AdaptiveHoeffdingTreeModel> entry : hoeffdingModelMap.entrySet()) {
            clonedMap.put(entry.getKey(), new AdaptiveHoeffdingTreeModel(entry.getValue()));
        }
        return clonedMap;
    }


}
