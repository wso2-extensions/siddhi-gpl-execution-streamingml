package org.wso2.extension.siddhi.gpl.execution.streamingml.clustering.clustree;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Scanner;

public class ClusTreeStandardDatasetTest {
    private static final Logger logger = Logger.getLogger(ClusTreeStandardDatasetTest.class);
    @Test
    public void testClusTree_1() throws Exception {
        logger.info("ClusTreeStreamProcessorExtension Test - standard dataset at " +
                "https://archive.ics.uci.edu/ml/datasets/3D+Road+Network+%28North+Jutland%2C+Denmark%29");
        SiddhiManager siddhiManager = new SiddhiManager();
        String inputStream = "define stream InputStream (x1 double, x2 double, x3 double, x4 double);";

        String query = (
                "@info(name = 'query2') " +
                        "from InputStream#streamingml:clusTree(2, 10, 100000, 10, 1000000, x1, x2, x3, x4) " +
                        "select closestCentroidCoordinate1, closestCentroidCoordinate2, " +
                        "closestCentroidCoordinate3, closestCentroidCoordinate4 " +
                        "insert into OutputStream;");
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inputStream + query);
        siddhiAppRuntime.addCallback("query2", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(inEvents);
            }
        });
        Scanner scanner = null;
        siddhiAppRuntime.start();
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("InputStream");
        try {
            File file = new File("src/test/resources/3D_spatial_network.csv");
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            scanner = new Scanner(bufferedReader);

            while (scanner.hasNext()) {
                String eventStr = scanner.nextLine();
                String[] event = eventStr.split(",");
                //logger.info(Arrays.toString(event));
                inputHandler.send(new Object[]{Double.valueOf(event[0]), Double.valueOf(event[1]),
                        Double.valueOf(event[2]), Double.valueOf(event[3])});
            }

        } catch (Exception e) {
            logger.error(e.getCause().getMessage());
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }
}
