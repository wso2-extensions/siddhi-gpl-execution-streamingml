package org.wso2.extension.siddhi.gpl.execution.streamingml.regression;

import com.yahoo.labs.samoa.instances.Attribute;
import com.yahoo.labs.samoa.instances.DenseInstance;
import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.Instances;
import com.yahoo.labs.samoa.instances.InstancesHeader;
import moa.options.AbstractOptionHandler;
import moa.streams.InstanceStream;

import java.util.ArrayList;

/**
 *
 */
public abstract class AbstractRegressor extends AbstractOptionHandler {
    protected InstancesHeader streamHeader;

    protected void generateHeader(int noOfFeatures) {
        ArrayList<Attribute> attributes = new ArrayList<Attribute>();
        for (int i = 0; i < noOfFeatures; i++) {
            attributes.add(new Attribute("numeric" + (i + 1)));
        }
        streamHeader = new InstancesHeader(new Instances(getCLICreationString(InstanceStream.class),
                attributes, 0));
        streamHeader.setClassIndex(streamHeader.numAttributes() - 1);
    }

    protected Instance createMOAInstance(double[] cepEvent) {
        Instance instance = new DenseInstance(1.0D, cepEvent);
        instance.setDataset(streamHeader);
        return instance;
    }
}
