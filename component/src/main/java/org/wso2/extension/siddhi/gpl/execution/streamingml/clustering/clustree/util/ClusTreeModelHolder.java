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

import java.util.HashMap;
import java.util.Map;

/**
 * singleton to store the kmeans models
 */
public class ClusTreeModelHolder {
    private static final ClusTreeModelHolder instance = new ClusTreeModelHolder();
    private Map<String, ClusTreeModel> clusTreeModelMap = new HashMap<>();
    private static final Logger logger = Logger.getLogger(ClusTreeModelHolder.class.getName());

    private ClusTreeModelHolder(){
    }

    public static ClusTreeModelHolder getInstance() {
        return instance;
    }

    public Map<String, ClusTreeModel> getKMeansModelMap() {
        return clusTreeModelMap;
    }

    public void deleteKMeansModel(String modelName) {
        clusTreeModelMap.remove(modelName);
    }

    public void setKMeansModelMap(Map<String, ClusTreeModel> kMeansModelMap) {
        this.clusTreeModelMap = kMeansModelMap;
    }

    public ClusTreeModel getClusTreeModel(String microModelName, int noOfDimensions, int noOfClusters,
                                          int maxHeightOfTree, int horizon) {
        ClusTreeModel model = clusTreeModelMap.get(microModelName);
        if (model == null) {
            model = new ClusTreeModel(microModelName);
            model.init(noOfDimensions, noOfClusters, maxHeightOfTree, horizon);
            this.addClusTreeModel(microModelName, model);
            if (logger.isDebugEnabled()) {
                logger.debug("New model is created with name " + microModelName);
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Existing model " + microModelName + " is not trained");
            }
        }
        return model;
    }

    public void addClusTreeModel(String name, ClusTreeModel model) {
        clusTreeModelMap.put(name, model);
    }

    public Map<String, ClusTreeModel> getClonedKMeansModelMap() {
        Map<String, ClusTreeModel> clonedMap = new HashMap<>();
        for (Map.Entry<String, ClusTreeModel> entry: clusTreeModelMap.entrySet()) {
            clonedMap.put(entry.getKey(), new ClusTreeModel(entry.getValue()));
        }
        return clonedMap;
    }
}
