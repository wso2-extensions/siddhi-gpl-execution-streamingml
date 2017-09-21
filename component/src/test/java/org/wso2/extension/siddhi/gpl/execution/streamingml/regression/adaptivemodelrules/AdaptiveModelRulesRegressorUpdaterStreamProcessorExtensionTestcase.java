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
package org.wso2.extension.siddhi.gpl.execution.streamingml.regression.adaptivemodelrules;

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
import org.wso2.siddhi.core.util.persistence.InMemoryPersistenceStore;

import java.util.concurrent.atomic.AtomicInteger;

public class AdaptiveModelRulesRegressorUpdaterStreamProcessorExtensionTestcase {
    private static final Logger logger = Logger
            .getLogger(AdaptiveModelRulesRegressorUpdaterStreamProcessorExtensionTestcase.class);

    private AtomicInteger count;

    @BeforeMethod
    public void init() {
        count = new AtomicInteger(0);
    }

    @Test
    public void testUpdateRegressionLearningStreamProcessorExtension1() throws InterruptedException {
        logger.info("UpdateUpdateRegressionLearningStreamProcessorExtension TestCase - Assert Model Build with"
                + "default parameters");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = " define stream StreamA (attribute_0 double, attribute_1 double, attribute_2 "
                + "double,attribute_3 double, attribute_4 double );";

        String query = ("@info(name = 'query1') from StreamA#streamingml:updateAMRulesRegressor('model1', "
                + "attribute_0, attribute_1 , attribute_2 ,attribute_3,attribute_4) select attribute_0, "
                + "attribute_1, attribute_2, attribute_3, meanSquaredError insert into outputStream;");

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
        siddhiAppRuntime.addCallback("query1", new QueryCallback() {

            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                count.incrementAndGet();
                EventPrinter.print(inEvents);
                if (count.get() == 2) {
                    AssertJUnit.assertArrayEquals(new Object[]{5.4, 3.4, 1.7, 0.2, 96.5}, inEvents[0]
                            .getData());
                } else if (count.get() == 7) {
                    AssertJUnit.assertArrayEquals(new Object[]{5.8, 2.7, 4.1, 1, 42.369}, inEvents[0]
                            .getData());
                }
            }
        });
        try {
            InputHandler inputHandler = siddhiAppRuntime.getInputHandler("StreamA");
            siddhiAppRuntime.start();
            inputHandler.send(new Object[]{6, 2.2, 4, 1, 12});
            inputHandler.send(new Object[]{5.4, 3.4, 1.7, 0.2, 5});
            inputHandler.send(new Object[]{6.9, 3.1, 5.4, 2.1, 3});
            inputHandler.send(new Object[]{4.3, 3, 1.1, 0.1, 9});
            inputHandler.send(new Object[]{6.1, 2.8, 4.7, 1.2, 7});
            inputHandler.send(new Object[]{4.8, 3.4, 1.9, 0.2, 1});
            inputHandler.send(new Object[]{5.8, 2.7, 4.1, 1, 5});

            SiddhiTestHelper.waitForEvents(200, 7, count, 60000);
        } catch (Exception e) {
            logger.error(e.getCause().getMessage());
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void testUpdateUpdateRegressionLearningStreamProcessorExtension2() throws InterruptedException {
        logger.info("UpdateUpdateRegressionLearningStreamProcessorExtension TestCase - Assert model build "
                + "with manual configurations");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = " define stream StreamA (attribute_0 double, attribute_1 double, attribute_2 "
                + "double,attribute_3 double, attribute_4 double );";

        String query = ("@info(name = 'query1') "
                + "from StreamA#streamingml:updateAMRulesRegressor('model1', 1e-7, 0.05, 200, 2, 2, "
                + "attribute_0, attribute_1 , attribute_2 ,attribute_3,attribute_4) select attribute_0, "
                + "attribute_1, attribute_2, attribute_3, meanSquaredError insert into outputStream;");

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
        siddhiAppRuntime.addCallback("query1", new QueryCallback() {

            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                count.incrementAndGet();
                EventPrinter.print(inEvents);
                if (count.get() == 1) {
                    AssertJUnit.assertArrayEquals(new Object[]{6, 2.2, 4, 1, 144.0}, inEvents[0]
                            .getData());
                }
                if (count.get() == 3) {
                    AssertJUnit.assertArrayEquals(new Object[]{6.9, 3.1, 5.4, 2.1, 83.759}, inEvents[0]
                            .getData());
                }
            }
        });
        try {
            InputHandler inputHandler = siddhiAppRuntime.getInputHandler("StreamA");
            siddhiAppRuntime.start();
            inputHandler.send(new Object[]{6, 2.2, 4, 1, 12});
            inputHandler.send(new Object[]{5.4, 3.4, 1.7, 0.2, 5});
            inputHandler.send(new Object[]{6.9, 3.1, 5.4, 2.1, 3});
            inputHandler.send(new Object[]{4.3, 3, 1.1, 0.1, 9});
            inputHandler.send(new Object[]{6.1, 2.8, 4.7, 1.2, 7});

            SiddhiTestHelper.waitForEvents(200, 5, count, 60000);
        } catch (Exception e) {
            logger.error(e.getCause().getMessage());
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void testUpdateUpdateRegressionLearningStreamProcessorExtension3() throws InterruptedException {
        logger.info("UpdateUpdateRegressionLearningStreamProcessorExtension TestCase "
                + "- Target value is not of type double");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream StreamA (attribute_0 double, attribute_1 double, attribute_2 "
                + "double, attribute_3 double, attribute_4 String );";

        String query = ("@info(name = 'query1') from StreamA#streamingml:updateAMRulesRegressor('model1',  "
                + "attribute_0, attribute_1 , attribute_2 ,attribute_3,attribute_4) select attribute_0, "
                + "attribute_1, attribute_2, attribute_3, accuracy insert into"
                + " outputStream;");
        try {
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
        } catch (Exception e) {
            logger.error(e.getCause().getMessage());
            AssertJUnit.assertTrue(e instanceof SiddhiAppCreationException);
            AssertJUnit.assertTrue(e.getCause().getMessage().contains("model.features in 6th parameter is not a " +
                    "numerical type attribute. Found STRING. Check the input stream definition."));
        }
    }

    @Test
    public void testUpdateUpdateRegressionLearningStreamProcessorExtension4() throws InterruptedException {
        logger.info("UpdateUpdateRegressionLearningStreamProcessorExtension TestCase " +
                "- Accept any numerical type for feature attributes");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = " define stream StreamA (attribute_0 float, attribute_1 double, attribute_2 "
                + "int,attribute_3 long, attribute_4 double );";

        String query = ("@info(name = 'query1') from StreamA#streamingml:updateAMRulesRegressor('model1', "
                + "attribute_0, attribute_1 , attribute_2 ,attribute_3,attribute_4) select attribute_0, "
                + "attribute_1, attribute_2, attribute_3, meanSquaredError insert into"
                + " outputStream;");

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
        siddhiAppRuntime.addCallback("query1", new QueryCallback() {

            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                count.incrementAndGet();
                EventPrinter.print(inEvents);
                if (count.get() == 1) {
                    AssertJUnit.assertArrayEquals(new Object[]{6, 2.2, 4, 1, 144.0}, inEvents[0]
                            .getData());
                }
                if (count.get() == 3) {
                    AssertJUnit.assertArrayEquals(new Object[]{6.9, 3.1, 5.4, 2.1, 83.759}, inEvents[0]
                            .getData());
                }
            }
        });
        try {
            InputHandler inputHandler = siddhiAppRuntime.getInputHandler("StreamA");
            siddhiAppRuntime.start();
            inputHandler.send(new Object[]{6, 2.2, 4, 1, 12});
            inputHandler.send(new Object[]{5.4, 3.4, 1.7, 0.2, 5});
            inputHandler.send(new Object[]{6.9, 3.1, 5.4, 2.1, 3});
            inputHandler.send(new Object[]{4.3, 3, 1.1, 0.1, 9});
            inputHandler.send(new Object[]{6.1, 2.8, 4.7, 1.2, 7});

            SiddhiTestHelper.waitForEvents(200, 5, count, 60000);

        } catch (Exception e) {
            logger.error(e.getCause().getMessage());
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }


    @Test
    public void testUpdateUpdateRegressionLearningStreamProcessorExtension5() throws InterruptedException {
        logger.info("UpdateUpdateRegressionLearningStreamProcessorExtension TestCase "
                + "- number of parameters not accepted");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream StreamA (attribute_0 double, attribute_1 double, attribute_2 "
                + "double, attribute_3 int, attribute_4 double );";

        String query = ("@info(name = 'query1') from StreamA#streamingml:updateAMRulesRegressor('model1', 3,  "
                + "attribute_0, attribute_1 , attribute_2 ,attribute_3,attribute_4) select attribute_0, "
                + "attribute_1, attribute_2, attribute_3, accuracy insert into"
                + " outputStream;");

        try {
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
        } catch (Exception e) {
            logger.error(e.getCause().getMessage());
            AssertJUnit.assertTrue(e instanceof SiddhiAppCreationException);
            AssertJUnit.assertTrue(e.getCause().getMessage().contains("Number of hyper-parameters needed for " +
                    "model manual configuration is 5 but found 1"));
        }
    }

    @Test
    public void testUpdateUpdateRegressionLearningStreamProcessorExtension6() throws InterruptedException {
        logger.info("UpdateUpdateRegressionLearningStreamProcessorExtension TestCase "
                + "- Assert Model Prequntial Evaluation");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = " define stream StreamA (attribute_0 double, attribute_1 double, attribute_2 "
                + "double,attribute_3 double, attribute_4 double );";

        String query = ("@info(name = 'query1') from StreamA#streamingml:updateAMRulesRegressor('model1', "
                + "attribute_0, attribute_1 , attribute_2 ,attribute_3,attribute_4) \n"
                + "select attribute_0, attribute_1, attribute_2, attribute_3, meanSquaredError insert " +
                "into outputStream;");

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
        siddhiAppRuntime.addCallback("query1", new QueryCallback() {

            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(inEvents);
                count.incrementAndGet();
                if (count.get() == 1) {
                    AssertJUnit.assertArrayEquals(new Object[]{6, 2.2, 4, 1, 144.0}, inEvents[0]
                            .getData());
                }
                if (count.get() == 6) {
                    AssertJUnit.assertArrayEquals(new Object[]{4.8, 3.4, 1.9, 0.2, 45.204}, inEvents[0]
                            .getData());
                }
            }
        });
        try {
            InputHandler inputHandler = siddhiAppRuntime.getInputHandler("StreamA");
            siddhiAppRuntime.start();
            inputHandler.send(new Object[]{6, 2.2, 4, 1, 12});
            inputHandler.send(new Object[]{5.4, 3.4, 1.7, 0.2, 5});
            inputHandler.send(new Object[]{6.9, 3.1, 5.4, 2.1, 3});
            inputHandler.send(new Object[]{4.3, 3, 1.1, 0.1, 9});
            inputHandler.send(new Object[]{6.1, 2.8, 4.7, 1.2, 7});
            inputHandler.send(new Object[]{4.8, 3.4, 1.9, 0.2, 11});
            inputHandler.send(new Object[]{5.8, 2.7, 4.1, 1, 3});
            inputHandler.send(new Object[]{5.1, 2.5, 3, 1.1, 3});
            inputHandler.send(new Object[]{6.3, 2.8, 5.1, 1.5, 4});
            inputHandler.send(new Object[]{5.1, 3.8, 1.6, 0.2, 5});
            inputHandler.send(new Object[]{6.5, 2.8, 4.6, 1.5, 7});
            inputHandler.send(new Object[]{5.7, 2.5, 5, 2, 9});

            SiddhiTestHelper.waitForEvents(200, 12, count, 60000);
        } catch (Exception e) {
            logger.error(e.getCause().getMessage());
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void testUpdateUpdateRegressionLearningStreamProcessorExtension7() throws InterruptedException {
        logger.info("UpdateUpdateRegressionLearningStreamProcessorExtension TestCase "
                + "- Features are of un-supported type");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream StreamA (attribute_0 double, attribute_1 double, attribute_2 "
                + "double, attribute_3 String, attribute_4 double );";

        String query = ("@info(name = 'query1') from StreamA#streamingml:updateAMRulesRegressor('model1', "
                + "attribute_0, attribute_1 , attribute_2 ,attribute_3,attribute_4) select attribute_0, "
                + " attribute_1, attribute_2, attribute_3, meanSquaredError insert into outputStream;");

        try {
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
        } catch (Exception e) {
            logger.error(e.getCause().getMessage());
            AssertJUnit.assertTrue(e instanceof SiddhiAppCreationException);
            AssertJUnit.assertTrue(e.getCause().getMessage().contains("model.features in 5th parameter is not "
                    + "a numerical type attribute. Found STRING. Check the input stream definition."));
        }
    }

    @Test
    public void testUpdateUpdateRegressionLearningStreamProcessorExtension8() throws InterruptedException {
        logger.info("UpdateUpdateRegressionLearningStreamProcessorExtension TestCase - incorrect initialization");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream StreamA (attribute_0 double, attribute_1 double, attribute_2 "
                + "double, attribute_3 int, attribute_4 double );";
        String query = ("@info(name = 'query1') from StreamA#streamingml:updateAMRulesRegressor() "
                + "select att_0 as attribute_0, "
                + "att_1 as attribute_1,att_2 as attribute_2,att_3 as attribute_3, accuracy insert into"
                + " outputStream;");
        try {
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition +
                    query);
        } catch (Exception e) {
            logger.error(e.getCause().getMessage());
            AssertJUnit.assertTrue(e instanceof SiddhiAppCreationException);
            AssertJUnit.assertTrue(e.getCause().getMessage().contains("Invalid number of attributes for "
                    + "streamingml:updateAMRulesRegressor. This Stream Processor requires at least 1 ," +
                    "parameters namely, model.name and 2 features"));
        }
    }

    @Test
    public void testUpdateUpdateRegressionLearningStreamProcessorExtension9() throws InterruptedException {
        logger.info("UpdateUpdateRegressionLearningStreamProcessorExtension TestCase "
                + "- number of parameters not accepted");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream StreamA (attribute_0 double, attribute_1 double, attribute_2 "
                + "double, attribute_3 int, attribute_4 double );";

        String query = ("@info(name = 'query1') from StreamA#streamingml:updateAMRulesRegressor('model1', 0.3, 0.4, "
                + "attribute_0, attribute_1 , attribute_2 ,attribute_3,attribute_4) select attribute_0, "
                + "attribute_1, attribute_2, attribute_3, meanSquaredError insert into"
                + " outputStream;");
        try {
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
        } catch (Exception e) {
            logger.error(e.getCause().getMessage());
            AssertJUnit.assertTrue(e instanceof SiddhiAppCreationException);
            AssertJUnit.assertTrue(e.getCause().getMessage().contains("Number of hyper-parameters needed for "
                    + "model manual configuration is 5 but found 2"));
        }
    }

    @Test
    public void testUpdateUpdateRegressionLearningStreamProcessorExtension10() throws InterruptedException {
        logger.info("UpdateUpdateRegressionLearningStreamProcessorExtension TestCase - Restore from a restart");
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(new InMemoryPersistenceStore());

        String inStreamDefinition = "@App:name('AMRulesRegressorTestApp') \n"
                + "define stream StreamA (attribute_0 double, attribute_1 double, attribute_2 "
                + "double, attribute_3 double, attribute_4 double);";
        String query = ("@info(name = 'query1') from StreamA#streamingml:updateAMRulesRegressor('model1',  "
                + "attribute_0, attribute_1 , attribute_2 , attribute_3, attribute_4) select attribute_0, "
                + "attribute_1, attribute_2, attribute_3, meanSquaredError insert into outputStream;");

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
        siddhiAppRuntime.addCallback("query1", new QueryCallback() {

            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                count.incrementAndGet();
                EventPrinter.print(inEvents);
                if (count.get() == 1) {
                    AssertJUnit.assertArrayEquals(new Object[]{27.36, 48.6, 1003.18, 54.93, 190148.324},
                            inEvents[0].getData());
                }
                if (count.get() == 3) {
                    AssertJUnit.assertArrayEquals(new Object[]{7.91, 39.96, 1023.57, 88.44, 63699.987},
                            inEvents[0].getData());
                }
            }
        });
        try {
            InputHandler inputHandler = siddhiAppRuntime.getInputHandler("StreamA");
            siddhiAppRuntime.start();
            inputHandler.send(new Object[]{27.36, 48.6, 1003.18, 54.93, 436.06});
            inputHandler.send(new Object[]{14.6, 39.31, 1011.11, 72.52, 464.16});

            // persist
            siddhiManager.persist();
            Thread.sleep(5000);
            // send few more events to change the weights
            inputHandler.send(new Object[]{7.91, 39.96, 1023.57, 88.44, 475.52});
            inputHandler.send(new Object[]{5.81, 35.79, 1012.14, 92.28, 484.41});
            inputHandler.send(new Object[]{30.53, 65.18, 1012.69, 41.85, 437.89});
            inputHandler.send(new Object[]{23.87, 63.94, 1019.02, 44.28, 445.11});
            inputHandler.send(new Object[]{26.09, 58.41, 1013.64, 64.58, 438.86});
            inputHandler.send(new Object[]{29.27, 66.85, 1011.11, 63.25, 440.98});
            Thread.sleep(1000);
            // shutdown the app
            siddhiAppRuntime.shutdown();

            // recreate the same app
            siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
            siddhiAppRuntime.addCallback("query1", new QueryCallback() {
                @Override
                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                    count.incrementAndGet();
                    EventPrinter.print(inEvents);
                    if (count.get() == 9) {
                        // weights should be restored and should be following
                        AssertJUnit.assertArrayEquals(new Object[]{27.38, 74.16, 1010.08, 78.61, 2072.799}, inEvents[0]
                                .getData());
                    }
                }
            });
            // start the app
            siddhiAppRuntime.start();
            // restore
            siddhiManager.restoreLastState();
            inputHandler = siddhiAppRuntime.getInputHandler("StreamA");
            // send a new event
            inputHandler.send(new Object[]{27.38, 74.16, 1010.08, 78.61, 436.65});

            SiddhiTestHelper.waitForEvents(200, 9, count, 60000);
        } catch (Exception e) {
            logger.error(e.getCause().getMessage());
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void testUpdateUpdateRegressionLearningStreamProcessorExtension11() throws InterruptedException {
        logger.info("UpdateUpdateRegressionLearningStreamProcessorExtension TestCase " +
                "- Hyperparameter not type ConstantExecutor");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream StreamA (attribute_0 double, attribute_1 double, attribute_2 "
                + "double, attribute_3 int, attribute_4 double );";

        String query = ("@info(name = 'query1') "
                + "from StreamA#streamingml:updateAMRulesRegressor('model1', 3, 5, 200, "
                + "attribute_0, 0, attribute_0, attribute_1 , "
                + "attribute_2 ,attribute_3,attribute_4)"
                + " select attribute_0, attribute_1, attribute_2, attribute_3, meanSquaredError "
                + "insert into outputStream;");
        try {
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
        } catch (Exception e) {
            logger.error(e.getCause().getMessage());
            AssertJUnit.assertTrue(e instanceof SiddhiAppCreationException);
            AssertJUnit.assertTrue(e.getCause().getMessage().contains("Change Detector must be "
                    + "(ConstantExpressionExecutor) but found org.wso2.siddhi.core.executor.VariableExpressionExecutor"
                    + " in position 5"));
        }
    }

    @Test
    public void testUpdateUpdateRegressionLearningStreamProcessorExtension12() throws InterruptedException {
        logger.info("UpdateUpdateRegressionLearningStreamProcessorExtension TestCase - model is visible only "
                + "within the SiddhiApp");
        SiddhiManager siddhiManager = new SiddhiManager();

        String trainingStream1 = "@App:name('AMRulesRegressorTestApp1') \n"
                + "define stream StreamTrain (attribute_0 double, "
                + "attribute_1 double, attribute_2 double, attribute_3 double, attribute_4 double );";
        String trainingQuery1 = ("@info(name = 'query-train') "
                + "from StreamTrain#streamingml:updateAMRulesRegressor('ml', "
                + "attribute_0, attribute_1, attribute_2, attribute_3, attribute_4) \n"
                + "insert all events into trainOutputStream;\n");

        String trainingStrream2 = "@App:name('AMRulesRegressorTestApp2') define stream StreamTrain "
                + "(attribute_0 double, attribute_1 double, attribute_2 double,attribute_3 double );";
        String trainingQuery2 = ("@info(name = 'query-train') "
                + "from StreamTrain#streamingml:updateAMRulesRegressor('ml', "
                + "attribute_0, attribute_1 , attribute_2 ,attribute_3) "
                + "insert all events into trainOutputStream;\n");
        try {
            SiddhiAppRuntime siddhiAppRuntime1 = siddhiManager
                    .createSiddhiAppRuntime(trainingStream1 + trainingQuery1);
            // should be successful even though both the apps are using the same model name with different feature
            // values
            SiddhiAppRuntime siddhiAppRuntime2 = siddhiManager
                    .createSiddhiAppRuntime(trainingStrream2 + trainingQuery2);
        } catch (Exception e) {
            logger.error(e.getCause().getMessage());
            AssertJUnit.fail("Model is visible across Siddhi Apps which is wrong!");
        }
    }
}
