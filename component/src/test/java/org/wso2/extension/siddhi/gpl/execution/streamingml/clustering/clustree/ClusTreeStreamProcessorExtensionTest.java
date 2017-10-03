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

package org.wso2.extension.siddhi.gpl.execution.streamingml.clustering.clustree;

import org.apache.log4j.Logger;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.core.util.SiddhiTestHelper;
import org.wso2.siddhi.query.compiler.exception.SiddhiParserException;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class ClusTreeStreamProcessorExtensionTest {
    private static final Logger logger = Logger.getLogger(ClusTreeStreamProcessorExtensionTest.class);
    private volatile AtomicInteger count;
    @BeforeMethod
    public void init() {
        count = new AtomicInteger(0);
    }

    @Test
    public void testClusTree2D_0() throws Exception {
        logger.info("ClusTreeStreamProcessorExtension Test - Test case for 2D data points. Initial test");
        SiddhiManager siddhiManager = new SiddhiManager();
        String inputStream = "define stream InputStream (x double, y double);";

        String query = (
                "@info(name = 'query1') " +
                        "from InputStream#streamingml:clusTree(2, 10, 20, 5, 50, x, y) " +
                        "select closestCentroidCoordinate1, closestCentroidCoordinate2, x, y " +
                        "insert into OutputStream;");
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inputStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(inEvents);
                for (Event event: inEvents) {

                    count.incrementAndGet();

                    switch (count.get()) {
                        case 20:
                            AssertJUnit.assertArrayEquals(new Double[]{25.0406, 25.3906}, new Object[]{
                                    event.getData(0), event.getData(1)});
                            break;
                        case 21:
                            AssertJUnit.assertArrayEquals(new Double[]{25.0406, 25.3906}, new Object[]{
                                    event.getData(0), event.getData(1)});
                            break;
                        case 22:
                            AssertJUnit.assertArrayEquals(new Double[]{4.7675, 6.6013}, new Object[]{
                                    event.getData(0), event.getData(1)});
                            break;
                    }
                }
            }
        });


        siddhiAppRuntime.start();
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("InputStream");
        try {
            inputHandler.send(new Object[]{5.7905, 7.7499});
            inputHandler.send(new Object[]{27.458, 23.8848});
            inputHandler.send(new Object[]{3.078, 9.1072});
            inputHandler.send(new Object[]{28.326, 26.7484});
            inputHandler.send(new Object[]{2.2602, 4.6408});
            inputHandler.send(new Object[]{27.3099, 26.1816});
            inputHandler.send(new Object[]{0.9441, 0.6502});
            inputHandler.send(new Object[]{23.9204, 27.6745});
            inputHandler.send(new Object[]{2.0499, 9.9546});
            inputHandler.send(new Object[]{23.7947, 20.8627});
            inputHandler.send(new Object[]{5.8456, 6.8879});
            inputHandler.send(new Object[]{26.7315, 25.5368});
            inputHandler.send(new Object[]{5.8812, 5.9116});
            inputHandler.send(new Object[]{24.5343, 26.77});
            inputHandler.send(new Object[]{4.3866, 0.3132});
            inputHandler.send(new Object[]{22.7654, 25.1381});
            inputHandler.send(new Object[]{7.7824, 9.2299});
            inputHandler.send(new Object[]{23.5167, 24.1244});
            inputHandler.send(new Object[]{5.3086, 9.7503});
            inputHandler.send(new Object[]{25.47, 25.8574});
            inputHandler.send(new Object[]{20.2568, 28.7882});
            inputHandler.send(new Object[]{2.9951, 3.9887});
            SiddhiTestHelper.waitForEvents(100, 22, count, 1000);
        } catch (Exception e) {
            logger.error(e.getCause().getMessage());
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void testClusTree2D_3() throws Exception {
        logger.info("ClusTreeStreamProcessorExtension Test - Test case to validate noOfClusters to be constant");
        SiddhiManager siddhiManager = new SiddhiManager();
        String inputStream = "define stream InputStream (x double, y double, noOfClusters int);";

        String query = (
                "@info(name = 'query1') " +
                        "from InputStream#streamingml:clusTree(noOfClusters, 10, 20, 5, 50, x, y) " +
                        "select closestCentroidCoordinate1, closestCentroidCoordinate2, x, y " +
                        "insert into OutputStream;");
        try {
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inputStream + query);
        } catch (Exception e) {
            logger.info("Error caught");
            AssertJUnit.assertTrue(e instanceof SiddhiAppCreationException);
            AssertJUnit.assertTrue(e.getCause().getMessage().contains("noOfClusters has to be a constant but " +
                    "found org.wso2.siddhi.core.executor.VariableExpressionExecutor"));
        }
    }

    @Test
    public void testClusTree2D_4() throws Exception {
        logger.info("ClusTreeStreamProcessorExtension Test - Test case to validate maxIterations to be constant");
        SiddhiManager siddhiManager = new SiddhiManager();
        String inputStream = "define stream InputStream (x double, y double, maxIterations int);";

        String query = (
                "@info(name = 'query1') " +
                        "from InputStream#streamingml:clusTree(2, maxIterations, 20, 5, 50, x, y) " +
                        "select closestCentroidCoordinate1, closestCentroidCoordinate2, x, y " +
                        "insert into OutputStream;");
        try {
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inputStream + query);
        } catch (Exception e) {
            logger.info("Error caught");
            AssertJUnit.assertTrue(e instanceof SiddhiAppCreationException);
            AssertJUnit.assertTrue(e.getCause().getMessage().contains("Maximum iterations has to be a constant but " +
                    "found org.wso2.siddhi.core.executor.VariableExpressionExecutor"));
        }
    }

    @Test
    public void testClusTree2D_5() throws Exception {
        logger.info("ClusTreeStreamProcessorExtension Test - Test case to validate " +
                "noOfEventsToRefreshMacroModel to be constant");
        SiddhiManager siddhiManager = new SiddhiManager();
        String inputStream = "define stream InputStream (x double, y double, noOfEventsToRefreshMacroModel int);";

        String query = (
                "@info(name = 'query1') " +
                        "from InputStream#streamingml:clusTree(2, 10, noOfEventsToRefreshMacroModel, " +
                        "5, 50, x, y) " +
                        "select closestCentroidCoordinate1, closestCentroidCoordinate2, x, y " +
                        "insert into OutputStream;");
        try {
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inputStream + query);
        } catch (Exception e) {
            logger.info("Error caught");
            AssertJUnit.assertTrue(e instanceof SiddhiAppCreationException);
            AssertJUnit.assertTrue(e.getCause().getMessage().contains("noOfEventsToRefreshMacroModel has to be a " +
                    "constant but found org.wso2.siddhi.core.executor.VariableExpressionExecutor"));
        }
    }

    @Test
    public void testClusTree2D_6() throws Exception {
        logger.info("ClusTreeStreamProcessorExtension Test - Test case to validate maxHeightOfTree to be constant");
        SiddhiManager siddhiManager = new SiddhiManager();
        String inputStream = "define stream InputStream (x double, y double, maxHeightOfTree int);";

        String query = (
                "@info(name = 'query1') " +
                        "from InputStream#streamingml:clusTree(2, 10, 20, maxHeightOfTree, 50, x, y) " +
                        "select closestCentroidCoordinate1, closestCentroidCoordinate2, x, y " +
                        "insert into OutputStream;");
        try {
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inputStream + query);
        } catch (Exception e) {
            logger.info("Error caught");
            AssertJUnit.assertTrue(e instanceof SiddhiAppCreationException);
            AssertJUnit.assertTrue(e.getCause().getMessage().contains("maxHeightOfTree has to be a constant but " +
                    "found org.wso2.siddhi.core.executor.VariableExpressionExecutor"));
        }
    }

    @Test
    public void testClusTree2D_7() throws Exception {
        logger.info("ClusTreeStreamProcessorExtension Test - Test case to validate horizon to be constant");
        SiddhiManager siddhiManager = new SiddhiManager();
        String inputStream = "define stream InputStream (x double, y double, horizon int);";

        String query = (
                "@info(name = 'query1') " +
                        "from InputStream#streamingml:clusTree(2, 10, 20, 5, horizon, x, y) " +
                        "select closestCentroidCoordinate1, closestCentroidCoordinate2, x, y " +
                        "insert into OutputStream;");
        try {
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inputStream + query);
        } catch (Exception e) {
            logger.info("Error caught");
            AssertJUnit.assertTrue(e instanceof SiddhiAppCreationException);
            AssertJUnit.assertTrue(e.getCause().getMessage().contains("horizon has to be a constant but found " +
                    "org.wso2.siddhi.core.executor.VariableExpressionExecutor"));
        }
    }

    @Test
    public void testClusTree2D_8() throws Exception {
        logger.info("ClusTreeStreamProcessorExtension Test - Test case when no hyper params are given");
        SiddhiManager siddhiManager = new SiddhiManager();
        String inputStream = "define stream InputStream (x double, y double);";

        String query = (
                "@info(name = 'query1') " +
                        "from InputStream#streamingml:clusTree(2, x, y) " +
                        "select closestCentroidCoordinate1, closestCentroidCoordinate2, x, y " +
                        "insert into OutputStream;");
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inputStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(inEvents);
            }
        });


        siddhiAppRuntime.start();
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("InputStream");
        try {
            inputHandler.send(new Object[]{5.7905, 7.7499});
            inputHandler.send(new Object[]{27.458, 23.8848});
            inputHandler.send(new Object[]{3.078, 9.1072});
            inputHandler.send(new Object[]{28.326, 26.7484});
            inputHandler.send(new Object[]{2.2602, 4.6408});
            inputHandler.send(new Object[]{27.3099, 26.1816});
            inputHandler.send(new Object[]{0.9441, 0.6502});
            inputHandler.send(new Object[]{23.9204, 27.6745});
            inputHandler.send(new Object[]{2.0499, 9.9546});
            inputHandler.send(new Object[]{23.7947, 20.8627});
            inputHandler.send(new Object[]{5.8456, 6.8879});
            inputHandler.send(new Object[]{26.7315, 25.5368});
            inputHandler.send(new Object[]{5.8812, 5.9116});
            inputHandler.send(new Object[]{24.5343, 26.77});
            inputHandler.send(new Object[]{4.3866, 0.3132});
            inputHandler.send(new Object[]{22.7654, 25.1381});
            inputHandler.send(new Object[]{7.7824, 9.2299});
            inputHandler.send(new Object[]{23.5167, 24.1244});
            inputHandler.send(new Object[]{5.3086, 9.7503});
            inputHandler.send(new Object[]{25.47, 25.8574});
            inputHandler.send(new Object[]{20.2568, 28.7882});
            inputHandler.send(new Object[]{2.9951, 3.9887});

            //SiddhiTestHelper.waitForEvents(200, 3, count, 5000);
        } catch (Exception e) {
            logger.error(e.getCause().getMessage());
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void testClusTree2D_9() throws Exception {
        logger.info("ClusTreeStreamProcessorExtension Test - Test case to validate attribute_0 to be variable");
        SiddhiManager siddhiManager = new SiddhiManager();
        String inputStream = "define stream InputStream (x double, y double);";

        String query = (
                "@info(name = 'query1') " +
                        "from InputStream#streamingml:clusTree(2, 10, 20, 5, 50, 8, y) " +
                        "select closestCentroidCoordinate1, closestCentroidCoordinate2, x, y " +
                        "insert into OutputStream;");
        try {
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inputStream + query);
        } catch (Exception e) {
            logger.info("Error caught");
            AssertJUnit.assertTrue(e instanceof SiddhiAppCreationException);
            AssertJUnit.assertTrue(e.getCause().getMessage().contains("6th parameter is not an attribute " +
                    "(VariableExpressionExecutor) present in the stream definition. Found a " +
                    "org.wso2.siddhi.core.executor.ConstantExpressionExecutor"));
        }
    }

    @Test
    public void testClusTree2D_10() throws Exception {
        logger.info("ClusTreeStreamProcessorExtension Test - Test case to validate attribute_1 to be variable");
        SiddhiManager siddhiManager = new SiddhiManager();
        String inputStream = "define stream InputStream (x double, y double);";

        String query = (
                "@info(name = 'query1') " +
                        "from InputStream#streamingml:clusTree(2, 10, 20, 5, 50, x, 3.1f) " +
                        "select closestCentroidCoordinate1, closestCentroidCoordinate2, x, y " +
                        "insert into OutputStream;");
        try {
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inputStream + query);
        } catch (Exception e) {
            logger.info("Error caught");
            AssertJUnit.assertTrue(e instanceof SiddhiAppCreationException);
            AssertJUnit.assertTrue(e.getCause().getMessage().contains("7th parameter is not an attribute " +
                    "(VariableExpressionExecutor) present in the stream definition. Found a " +
                    "org.wso2.siddhi.core.executor.ConstantExpressionExecutor"));
        }
    }

    @Test
    public void testClusTree2D_11() throws Exception {
        logger.info("ClusTreeStreamProcessorExtension Test - Test case to validate noOfClusters to be int");
        SiddhiManager siddhiManager = new SiddhiManager();
        String inputStream = "define stream InputStream (x double, y double);";

        String query = (
                "@info(name = 'query1') " +
                        "from InputStream#streamingml:clusTree(2.1, 10, 20, 5, 50, x, y) " +
                        "select closestCentroidCoordinate1, closestCentroidCoordinate2, x, y " +
                        "insert into OutputStream;");
        try {
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inputStream + query);
        } catch (Exception e) {
            logger.info("Error caught");
            AssertJUnit.assertTrue(e instanceof SiddhiAppCreationException);
            AssertJUnit.assertTrue(e.getCause().getMessage().contains("noOfClusters should be of type int but " +
                    "found DOUBLE"));
        }
    }

    @Test
    public void testClusTree2D_12() throws Exception {
        logger.info("ClusTreeStreamProcessorExtension Test - Test case to validate maxIterations to be int");
        SiddhiManager siddhiManager = new SiddhiManager();
        String inputStream = "define stream InputStream (x double, y double);";

        String query = (
                "@info(name = 'query1') " +
                        "from InputStream#streamingml:clusTree(2, 10.3f, 20, 5, 50, x, y) " +
                        "select closestCentroidCoordinate1, closestCentroidCoordinate2, x, y " +
                        "insert into OutputStream;");
        try {
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inputStream + query);
        } catch (Exception e) {
            logger.info("Error caught");
            AssertJUnit.assertTrue(e instanceof SiddhiAppCreationException);
            AssertJUnit.assertTrue(e.getCause().getMessage().contains("Maximum iterations should be of type int " +
                    "but found FLOAT"));
        }
    }

    @Test
    public void testClusTree2D_13() throws Exception {
        logger.info("ClusTreeStreamProcessorExtension Test - Test case to validate maxHeightOfTree " +
                "to be int");
        SiddhiManager siddhiManager = new SiddhiManager();
        String inputStream = "define stream InputStream (x double, y double);";

        String query = (
                "@info(name = 'query1') " +
                        "from InputStream#streamingml:clusTree(2, 10, 20, 5.4, 50, x, y) " +
                        "select closestCentroidCoordinate1, closestCentroidCoordinate2, x, y " +
                        "insert into OutputStream;");
        try {
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inputStream + query);
        } catch (Exception e) {
            logger.info("Error caught");
            AssertJUnit.assertTrue(e instanceof SiddhiAppCreationException);
            AssertJUnit.assertTrue(e.getCause().getMessage().contains("maxHeightOfTree should be of type int but " +
                    "found DOUBLE"));
        }
    }

    @Test
    public void testClusTree2D_15() throws Exception {
        logger.info("ClusTreeStreamProcessorExtension Test - Test case for incorrect type stream definition");
        SiddhiManager siddhiManager = new SiddhiManager();
        String inputStream = "define stream InputStream (x double, y String);";

        String query = (
                "@info(name = 'query1') " +
                        "from InputStream#streamingml:clusTree(2, 10, 20, 5, 50, x, y) " +
                        "select closestCentroidCoordinate1, closestCentroidCoordinate2, attribute_0, y " +
                        "insert into OutputStream;");
        try {
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inputStream + query);
        } catch (Exception e) {
            logger.info("Error caught");
            AssertJUnit.assertTrue(e instanceof SiddhiAppCreationException);
            AssertJUnit.assertTrue(e.getCause().getMessage().contains("model.features in 7th parameter is not a " +
                    "numerical type attribute. Found STRING. Check the input stream definition."));
        }
    }

    @Test
    public void testClusTree2D_16() throws Exception {
        logger.info("ClusTreeStreamProcessorExtension Test - Test case for testing the robustness of validations " +
                "when more stream attributes than needed are given");
        SiddhiManager siddhiManager = new SiddhiManager();
        String inputStream = "define stream InputStream (x double, y double, z String);";

        String query = (
                "@info(name = 'query1') " +
                        "from InputStream#streamingml:clusTree(2, 10, 20, 5, 50, x, y) " +
                        "select closestCentroidCoordinate1, closestCentroidCoordinate2, x, y " +
                        "insert into OutputStream;");
        try {
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inputStream + query);
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
    }

    @Test
    public void testClusTree2D_17() throws Exception {
        logger.info("ClusTreeStreamProcessorExtension Test - Test case to validate horizon to be int");
        SiddhiManager siddhiManager = new SiddhiManager();
        String inputStream = "define stream InputStream (x double, y double);";

        String query = (
                "@info(name = 'query1') " +
                        "from InputStream#streamingml:clusTree(2, 10, 20, 2, 50.3f, x, y) " +
                        "select closestCentroidCoordinate1, closestCentroidCoordinate2, x, y " +
                        "insert into OutputStream;");
        try {
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inputStream + query);
        } catch (Exception e) {
            logger.info("Error caught");
            AssertJUnit.assertTrue(e instanceof SiddhiAppCreationException);
            AssertJUnit.assertTrue(e.getCause().getMessage().contains("horizon should be of type int but found FLOAT"));
        }
    }

    @Test
    public void testClusTree2D_18() throws Exception {
        logger.info("ClusTreeStreamProcessorExtension Test - Test case to validate noOfClusters to be positive");
        SiddhiManager siddhiManager = new SiddhiManager();
        String inputStream = "define stream InputStream (x double, y double);";

        String query = (
                "@info(name = 'query1') " +
                        "from InputStream#streamingml:clusTree(-2, 10, 20, 2, 50, x, y) " +
                        "select closestCentroidCoordinate1, closestCentroidCoordinate2, x, y " +
                        "insert into OutputStream;");
        try {
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inputStream + query);
        } catch (Exception e) {
            logger.info("Error caught");
            AssertJUnit.assertTrue(e instanceof SiddhiAppCreationException);
            AssertJUnit.assertTrue(e.getCause().getMessage().contains("noOfClusters should be a positive integer " +
                    "but found -2"));
        }
    }

    @Test
    public void testClusTree2D_19() throws Exception {
        logger.info("ClusTreeStreamProcessorExtension Test - Test case to validate maxIterations to be positive");
        SiddhiManager siddhiManager = new SiddhiManager();
        String inputStream = "define stream InputStream (x double, y double);";

        String query = (
                "@info(name = 'query1') " +
                        "from InputStream#streamingml:clusTree(2, -10, 20, 2, 50, x, y) " +
                        "select closestCentroidCoordinate1, closestCentroidCoordinate2, x, y " +
                        "insert into OutputStream;");
        try {
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inputStream + query);
        } catch (Exception e) {
            logger.info("Error caught");
            AssertJUnit.assertTrue(e instanceof SiddhiAppCreationException);
            AssertJUnit.assertTrue(e.getCause().getMessage().contains("maxIterations should be a positive integer " +
                    "but found -10"));
        }
    }

    @Test
    public void testClusTree2D_20() throws Exception {
        logger.info("ClusTreeStreamProcessorExtension Test - Test case to validate noOfEventsToRefreshMacroModel " +
                "to be positive");
        SiddhiManager siddhiManager = new SiddhiManager();
        String inputStream = "define stream InputStream (x double, y double);";

        String query = (
                "@info(name = 'query1') " +
                        "from InputStream#streamingml:clusTree(2, 10, -20, 2, 50, x, y) " +
                        "select closestCentroidCoordinate1, closestCentroidCoordinate2, x, y " +
                        "insert into OutputStream;");
        try {
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inputStream + query);
        } catch (Exception e) {
            logger.info("Error caught");
            AssertJUnit.assertTrue(e instanceof SiddhiAppCreationException);
            AssertJUnit.assertTrue(e.getCause().getMessage().contains("noOfEventsToRefreshMacroModel should be a " +
                    "positive integer but found -20"));
        }
    }

    @Test
    public void testClusTree2D_21() throws Exception {
        logger.info("ClusTreeStreamProcessorExtension Test - Test case to validate maxHeightOfTree " +
                "to be above a minimum value to produce required noOfClusters");
        SiddhiManager siddhiManager = new SiddhiManager();
        String inputStream = "define stream InputStream (x double, y double);";

        String query = (
                "@info(name = 'query1') " +
                        "from InputStream#streamingml:clusTree(20, 10, 20, 2, 50, x, y) " +
                        "select closestCentroidCoordinate1, closestCentroidCoordinate2, x, y " +
                        "insert into OutputStream;");
        try {
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inputStream + query);
        } catch (Exception e) {
            logger.info("Error caught");
            AssertJUnit.assertTrue(e instanceof SiddhiAppCreationException);
            AssertJUnit.assertTrue(e.getCause().getMessage().contains("maxHeightOfTree should be an int greater " +
                    "than 2.7268 but found 2"));
        }
    }

    @Test
    public void testClusTree2D_22() throws Exception {
        logger.info("ClusTreeStreamProcessorExtension Test - Test case to validate horizon to be positive");
        SiddhiManager siddhiManager = new SiddhiManager();
        String inputStream = "define stream InputStream (x double, y double);";

        String query = (
                "@info(name = 'query1') " +
                        "from InputStream#streamingml:clusTree(2, 10, 20, 2, -50, x, y) " +
                        "select closestCentroidCoordinate1, closestCentroidCoordinate2, x, y " +
                        "insert into OutputStream;");
        try {
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inputStream + query);
        } catch (Exception e) {
            logger.info("Error caught");
            AssertJUnit.assertTrue(e instanceof SiddhiAppCreationException);
            AssertJUnit.assertTrue(e.getCause().getMessage().contains("horizon should be a positive integer but " +
                    "found -50"));
        }
    }

    @Test
    public void testClusTree2D_23() throws Exception {
        logger.info("ClusTreeStreamProcessorExtension Test - Test case with non existing stream");
        SiddhiManager siddhiManager = new SiddhiManager();
        //String inputStream = "define stream InputStream (x double, y double);";

        String query = (
                "@info(name = 'query1') " +
                        "from InputStream#streamingml:clusTree(2, 10, 20, 2, -50, x, y) " +
                        "select closestCentroidCoordinate1, closestCentroidCoordinate2, x, y " +
                        "insert into OutputStream;");
        try {
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(query);
        } catch (Exception e) {
            logger.info("Error caught");
            AssertJUnit.assertTrue(e instanceof SiddhiParserException);
            AssertJUnit.assertTrue(e.getMessage().contains("Syntax error in SiddhiQL, no viable alternative at input "
                    + "'@info(name = query1)"));
        }
    }

    @Test
    public void testClusTree2D_24() throws Exception {
        logger.info("ClusTreeStreamProcessorExtension Test - Test case with non-numeric event data");
        SiddhiManager siddhiManager = new SiddhiManager();
        String inputStream = "define stream InputStream (x double, y double);";

        String query = (
                "@info(name = 'query1') " +
                        "from InputStream#streamingml:clusTree(2, x, y) " +
                        "select closestCentroidCoordinate1, closestCentroidCoordinate2, x, y " +
                        "insert into OutputStream;");
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inputStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(inEvents);
            }
        });


        siddhiAppRuntime.start();
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("InputStream");
        inputHandler.send(new Object[]{5.7905, "hi"});
    }

    @Test
    public void testClusTree2D_25() throws Exception {
        logger.info("ClusTreeStreamProcessorExtension Test - Test case with less than 2 params");
        SiddhiManager siddhiManager = new SiddhiManager();
        String inputStream = "define stream InputStream (x double, y double);";

        String query = (
                "@info(name = 'query1') " +
                        "from InputStream#streamingml:clusTree(2) " +
                        "select closestCentroidCoordinate1, closestCentroidCoordinate2, x, y " +
                        "insert into OutputStream;");
        try {
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inputStream + query);
        } catch (Exception e) {
            logger.info("Error caught");
            AssertJUnit.assertTrue(e instanceof SiddhiAppCreationException);
            AssertJUnit.assertTrue(e.getCause().getMessage().contains("Invalid number of parameters. User can " +
                    "either choose to give all 4 hyper parameters or none at all. So query can have between 2 or " +
                    "7 but found 1 parameters"));
        }
    }

    @Test
    public void testClusTree2D_26() throws Exception {
        logger.info("ClusTreeStreamProcessorExtension Test - Test case with one empty parameter");
        SiddhiManager siddhiManager = new SiddhiManager();
        String inputStream = "define stream InputStream (x double, y double);";

        String query = (
                "@info(name = 'query1') " +
                        "from InputStream#streamingml:clusTree(2, 10, 20, 2,, x, y) " +
                        "select closestCentroidCoordinate1, closestCentroidCoordinate2, x, y " +
                        "insert into OutputStream;");
        try {
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inputStream + query);
        } catch (Exception e) {
            logger.info("Error caught");
            AssertJUnit.assertTrue(e instanceof SiddhiParserException);
            AssertJUnit.assertTrue(e.getMessage().contains("Syntax error in SiddhiQL, no viable " +
                    "alternative at input 'InputStream#streamingml:clusTree(2, 10, 20, 2,,"));
        }
    }

    @Test
    public void testClusTree2D_27() throws Exception {
        logger.info("ClusTreeStreamProcessorExtension Test - Test case to demo separate thread training");
        SiddhiManager siddhiManager = new SiddhiManager();
        String inputStream = "define stream InputStream (x double, y double);";

        String query = (
                "@info(name = 'query1') " +
                        "from InputStream#streamingml:clusTree(2, x, y) " +
                        "select closestCentroidCoordinate1, closestCentroidCoordinate2, x, y " +
                        "insert into OutputStream;");
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inputStream + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(inEvents);
            }
        });

        siddhiAppRuntime.start();
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("InputStream");
        Random random = new Random();
        try {
            for (int i = 0; i < 1100; i++) {
                inputHandler.send(new Object[]{random.nextInt(50), random.nextInt(50)});
                inputHandler.send(new Object[]{random.nextInt(50) + 100, random.nextInt(50) + 100});
            }
        } catch (Exception e) {
            logger.error(e.getCause().getMessage());
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    /*@Test
    public void testClusTree2D_23() throws Exception {
        logger.info("ClusTreeStreamProcessorExtension Test - Test case for restarting Siddhi app");
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(new InMemoryPersistenceStore());
        String inputStream = "@App:name('ClusTreeApp') \n" +
                "define stream InputStream (x double, y double);";
        String query = (
                "@info(name = 'query1') " +
                        "from InputStream#streamingml:clusTree(2, 10, 20, 5, 50, x, y) " +
                        "select closestCentroidCoordinate1, closestCentroidCoordinate2, x, y " +
                        "insert into OutputStream;");
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inputStream + query);
        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(inEvents);
            }
        });
        siddhiAppRuntime.start();
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("InputStream");
        try {
            inputHandler.send(new Object[]{5.7905, 7.7499});
            inputHandler.send(new Object[]{27.458, 23.8848});
            inputHandler.send(new Object[]{3.078, 9.1072});
            inputHandler.send(new Object[]{28.326, 26.7484});
            inputHandler.send(new Object[]{2.2602, 4.6408});
            inputHandler.send(new Object[]{27.3099, 26.1816});
            inputHandler.send(new Object[]{0.9441, 0.6502});
            siddhiManager.persist();
            Thread.sleep(500);
            siddhiAppRuntime.shutdown();
            siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inputStream + query);
            logger.info("creating siddhiAppRuntime");
            siddhiAppRuntime.addCallback("query1", new QueryCallback() {
                @Override
                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                    logger.info("adding callback");
                    EventPrinter.print(inEvents);
                    for (Event event: inEvents) {
                        count.incrementAndGet();
                        switch (count.get()) {
                            case 20:
                                AssertJUnit.assertArrayEquals(new Double[]{25.0406, 25.3906}, new Object[]{
                                        event.getData(0), event.getData(1)});
                                break;
                            case 21:
                                AssertJUnit.assertArrayEquals(new Double[]{25.0406, 25.3906}, new Object[]{
                                        event.getData(0), event.getData(1)});
                                break;
                            case 22:
                                AssertJUnit.assertArrayEquals(new Double[]{4.7675, 6.6013}, new Object[]{
                                        event.getData(0), event.getData(1)});
                                break;
                        }
                    }
                }
            });
            siddhiAppRuntime.start();
            siddhiManager.restoreLastState();
            inputHandler = siddhiAppRuntime.getInputHandler("InputStream");
            inputHandler.send(new Object[]{23.9204, 27.6745});
            inputHandler.send(new Object[]{2.0499, 9.9546});
            inputHandler.send(new Object[]{23.7947, 20.8627});
            inputHandler.send(new Object[]{5.8456, 6.8879});
            inputHandler.send(new Object[]{26.7315, 25.5368});
            inputHandler.send(new Object[]{5.8812, 5.9116});
            inputHandler.send(new Object[]{24.5343, 26.77});
            inputHandler.send(new Object[]{4.3866, 0.3132});
            inputHandler.send(new Object[]{22.7654, 25.1381});
            inputHandler.send(new Object[]{7.7824, 9.2299});
            inputHandler.send(new Object[]{23.5167, 24.1244});
            inputHandler.send(new Object[]{5.3086, 9.7503});
            inputHandler.send(new Object[]{25.47, 25.8574});
            inputHandler.send(new Object[]{20.2568, 28.7882});
            inputHandler.send(new Object[]{2.9951, 3.9887});
            SiddhiTestHelper.waitForEvents(100, 22, count, 1000);
        } catch (Exception e) {
            logger.error(e.getCause().getMessage());
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }*/
}
