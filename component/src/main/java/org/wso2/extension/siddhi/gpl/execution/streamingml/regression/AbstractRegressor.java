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
