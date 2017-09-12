package org.wso2.extension.siddhi.gpl.execution.streamingml.clustering.clustree;

import com.yahoo.labs.samoa.instances.Attribute;
import com.yahoo.labs.samoa.instances.DenseInstance;
import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.Instances;
import com.yahoo.labs.samoa.instances.InstancesHeader;
import moa.cluster.Cluster;
import moa.cluster.Clustering;
import moa.clusterers.clustree.ClusKernel;
import moa.clusterers.clustree.ClusTree;
import moa.core.FastVector;
import moa.streams.InstanceStream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;


/**
 * Created by niruhan on 9/12/17.
 */
public class TryMOA {
    private static ClusTree clusTree;
    private int numberOfAttributes = 2;
    private InstancesHeader streamHeader;

    public static void main(String[] args) throws FileNotFoundException, InterruptedException {
        clusTree = new ClusTree();
        clusTree.prepareForUse();
        List<double[]> list = new LinkedList<>();

        list.add(new double[]{5.0, 6.0});
        list.add(new double[]{5.5, 6.5});
        list.add(new double[]{15.0, 16.0});

        list.add(new double[]{1000, 2000});
        list.add(new double[]{1231231, 23432141});
        list.add(new double[]{0.00000034, 0.00000000123});
        list.add(new double[]{0.00000034, 0.00000000123});
        list.add(new double[]{0.00000034, 0.00000000123});
        list.add(new double[]{0.00000034, 0.00000000123});
        list.add(new double[]{0.00000034, 0.00000000123});
        list.add(new double[]{0.00000034, 0.00000000123});
        list.add(new double[]{0.00000034, 0.00000000123});
        list.add(new double[]{0.00000034, 0.00000000123});
        list.add(new double[]{5.0, 6.0});
        list.add(new double[]{5.0, 6.0});
        list.add(new double[]{5.0, 6.0});
        list.add(new double[]{5.0, 6.0});
        list.add(new double[]{5.0, 6.0});
        list.add(new double[]{5.0, 6.0});



        File file = new File("/home/niruhan/ML Toolkit for Siddhi/niru-clustree/siddhi-gpl-execution-" +
                "streamingml/component/src/main/java/org/wso2/extension/siddhi/gpl/execution/streamingml/clustering/clustree/3D_spatial_network.csv");
        System.out.println(file.canRead());
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        Scanner scanner = new Scanner(bufferedReader);

        /*for (double[] d: list) {
            Instance instance = new DenseInstance(1.0D, d);
            clusTree.trainOnInstanceImpl(instance);
            Thread.sleep(200);
        }*/

        int j=0;
        while (scanner.hasNext()) {
            String eventStr = scanner.nextLine();
            String[] event = eventStr.split(",");
            //System.out.println(Arrays.toString(event));
            double[] d = new double[]{Double.valueOf(event[0]), Double.valueOf(event[1]), Double.valueOf(event[2]),
            Double.valueOf(event[3])};
            Instance instance = new DenseInstance(1.0D, d);
            clusTree.trainOnInstanceImpl(instance);
            j++;
        }

        System.out.println(j);

        Thread.sleep(1000);

        Clustering clustering = clusTree.getMicroClusteringResult();
        System.out.println(clustering.size());
        System.out.println(clusTree.getHeight());
        for (int i = 0; i < clustering.size(); i++) {
            Cluster cluster = clustering.get(i);
            System.out.println(i + " -> " + Arrays.toString(cluster.getCenter()));
        }

    }

//    private Instance createMOAInstance(double[] cepEvent) {
//        Instance instance = new DenseInstance(1.0D, cepEvent);
//        //set schema header for the instance
//        instance.setDataset(streamHeader);
//        return instance;
//    }
//
//    private InstancesHeader createMOAInstanceHeader(int numberOfAttributes) {
//        FastVector headerAttributes = new FastVector();
//        for (int i = 0; i < numberOfAttributes - 1; i++) {
//            headerAttributes.addElement(
//                    new Attribute("att_" + i));
//        }
//        InstancesHeader streamHeader = new InstancesHeader(new Instances
//                (this.getCLICreationString(InstanceStream.class), headerAttributes, 0));
//        streamHeader.setClassIndex(streamHeader.numAttributes());
//        return streamHeader;
//    }
}
