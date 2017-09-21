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

import com.yahoo.labs.samoa.instances.Attribute;
import com.yahoo.labs.samoa.instances.DenseInstance;
import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.Instances;
import com.yahoo.labs.samoa.instances.InstancesHeader;
import moa.options.AbstractOptionHandler;
import moa.streams.InstanceStream;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract Regressor for Regression models.
 * built via @{@link AbstractRegressor}
 */
public abstract class AbstractRegressor extends AbstractOptionHandler {
    protected InstancesHeader streamHeader;

    protected void generateHeader(int noOfAttributes) {
        List<Attribute> attributes = new ArrayList<Attribute>();
        for (int i = 0; i < noOfAttributes; i++) {
            attributes.add(new Attribute("numeric" + (i + 1)));
        }
        streamHeader = new InstancesHeader(new Instances(getCLICreationString(InstanceStream.class),
                attributes, 0));
        streamHeader.setClassIndex(noOfAttributes - 1);
    }

    protected Instance createMOAInstance(double[] cepEvent) {
        Instance instance = new DenseInstance(1.0D, cepEvent);
        instance.setDataset(streamHeader);
        return instance;
    }
}
