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

import com.yahoo.labs.samoa.instances.DenseInstance;
import com.yahoo.labs.samoa.instances.Instance;
import moa.cluster.Clustering;
import moa.clusterers.clustree.ClusTree;
import moa.core.ObjectRepository;
import moa.options.AbstractOptionHandler;
import moa.tasks.TaskMonitor;
import org.apache.log4j.Logger;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents the ClusTree model
 */
public class ClusTreeModel  extends AbstractOptionHandler {
    private static final long serialVersionUID = -7485124336894867529L;
    private static final Logger logger = Logger.getLogger(ClusTreeModel.class);
    private int noOfDimensions;
    private int noOfClusters;
    private ClusTree clusTree;

    public ClusTreeModel() {
    }

    public ClusTreeModel(ClusTreeModel model) {
        this.clusTree = model.clusTree;
        this.noOfDimensions = model.noOfDimensions;
        this.noOfClusters = model.noOfClusters;
    }

    /**
     * Initialize the model with input stream definition.
     *
     * @param noOfDimensions number of feature attributes
     * @param noOfClusters    number of classes
     */
    public void init(int noOfDimensions, int noOfClusters, int maxHeightOfTree, int horizon) {
        this.noOfDimensions = noOfDimensions;
        this.noOfClusters = noOfClusters;
        this.clusTree = new ClusTree();
        this.clusTree.maxHeightOption.setValue(maxHeightOfTree);
        this.clusTree.horizonOption.setValue(horizon);
        this.clusTree.prepareForUse();
    }

    /**
     * @param cepEvent   event data
     * @param classLabel class  label of the cepEvent
     */
    public void trainOnEvent(double[] cepEvent, String classLabel) {
        Instance trainInstance = createMOAInstance(cepEvent);
        //training on the event instance
        clusTree.trainOnInstanceImpl(trainInstance);
    }

    @Override
    protected void prepareForUseImpl(TaskMonitor monitor, ObjectRepository repository) {

    }

    @Override
    public void getDescription(StringBuilder sb, int indent) {

    }

    /**
     * @param cepEvent Event Data
     * @return represents a single Event
     */
    private Instance createMOAInstance(double[] cepEvent) {
        Instance instance = new DenseInstance(1.0D, cepEvent);
        return instance;
    }

    public Clustering getMicroClustering() {
        return clusTree.getMicroClusteringResult();
    }

    public List<DataPoint> getMicroClusteringAsDPArray() {
        List<DataPoint> microClusterDPArray = new LinkedList<>();
        Clustering microClusters = getMicroClustering();
        for (int i = 0; i < microClusters.size(); i++) {
            DataPoint dp = new DataPoint();
            dp.setCoordinates(microClusters.get(i).getCenter());
            dp.setWeight(microClusters.get(i).getWeight());
            microClusterDPArray.add(dp);
        }
        return microClusterDPArray;
    }
}
