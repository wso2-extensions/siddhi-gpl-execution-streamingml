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
import org.wso2.siddhi.query.api.exception.AttributeNotExistException;

import java.util.concurrent.atomic.AtomicInteger;

public class AdaptiveModelRulesRegressorStreamProcessorExtensionTestcase {
    private static final Logger logger = Logger
            .getLogger(AdaptiveModelRulesRegressorStreamProcessorExtensionTestcase.class);

    private AtomicInteger count;
    private String trainingStream = "@App:name('AmRulesRegressorTestApp') \n"
            + "define stream StreamTrain (attribute_0 double, "
            + "attribute_1 double, attribute_2 double, attribute_3 double, attribute_4 double );";
    private String trainingQuery = ("@info(name = 'query-train') "
            + "from StreamTrain#streamingml:updateAMRulesRegressor('ml', attribute_0, attribute_1, "
            + "attribute_2, attribute_3, attribute_4) \n"
            + "insert all events into trainOutputStream;\n");

    @BeforeMethod
    public void init() {
        count = new AtomicInteger(0);
    }

    @Test
    public void testRegressionStreamProcessorExtension1() throws InterruptedException {
        logger.info("RegressionLearningStreamProcessorExtension TestCase " +
                "- Assert predictions and evolution");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream StreamA (attribute_0 double, attribute_1 double, "
                + "attribute_2 double, attribute_3 double);";
        String query = ("@info(name = 'query1') from StreamA#streamingml:AMRulesRegressor('ml', "
                + " attribute_0, attribute_1, attribute_2, attribute_3) "
                + "select attribute_0, attribute_1, attribute_2, attribute_3, prediction, meanSquaredError "
                + "insert into outputStream;");

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(trainingStream + inStreamDefinition
                + trainingQuery + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {

            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                count.incrementAndGet();
                EventPrinter.print(inEvents);
                if (count.get() == 1) {
                    AssertJUnit.assertArrayEquals(new Object[]{14.96, 41.76, 1024.07, 73.17, 414.667, 20573.608},
                            inEvents[0].getData());
                } else if (count.get() == 3) {
                    AssertJUnit.assertArrayEquals(new Object[]{5.11, 39.4, 1012.16, 92.14, 441.998, 20573.608},
                            inEvents[0].getData());
                }
            }
        });
        try {
            InputHandler inputHandler = siddhiAppRuntime.getInputHandler("StreamTrain");
            siddhiAppRuntime.start();
            inputHandler.send(new Object[]{27.36, 48.6, 1003.18, 54.93, 436.06});
            inputHandler.send(new Object[]{14.6, 39.31, 1011.11, 72.52, 464.16});
            inputHandler.send(new Object[]{7.91, 39.96, 1023.57, 88.44, 475.52});
            inputHandler.send(new Object[]{5.81, 35.79, 1012.14, 92.28, 484.41});
            inputHandler.send(new Object[]{30.53, 65.18, 1012.69, 41.85, 437.89});
            inputHandler.send(new Object[]{23.87, 63.94, 1019.02, 44.28, 445.11});
            inputHandler.send(new Object[]{26.09, 58.41, 1013.64, 64.58, 438.86});
            inputHandler.send(new Object[]{29.27, 66.85, 1011.11, 63.25, 440.98});
            inputHandler.send(new Object[]{27.38, 74.16, 1010.08, 78.61, 436.65});
            inputHandler.send(new Object[]{24.81, 63.94, 1018.76, 44.51, 444.26});
            inputHandler.send(new Object[]{12.75, 44.03, 1007.29, 89.46, 465.86});
            inputHandler.send(new Object[]{24.66, 63.73, 1011.4, 74.52, 444.37});
            inputHandler.send(new Object[]{16.38, 47.45, 1010.08, 88.86, 450.69});
            inputHandler.send(new Object[]{13.91, 39.35, 1014.69, 75.51, 469.02});
            inputHandler.send(new Object[]{23.18, 51.3, 1012.04, 78.64, 448.86});
            inputHandler.send(new Object[]{22.47, 47.45, 1007.62, 76.65, 447.14});
            inputHandler.send(new Object[]{13.39, 44.85, 1017.24, 80.44, 469.18});
            inputHandler.send(new Object[]{9.28, 41.54, 1018.33, 79.89, -0.0});
            inputHandler.send(new Object[]{11.82, 42.86, 1014.12, 88.28, 476.7});
            inputHandler.send(new Object[]{10.27, 40.64, 1020.63, 84.6, 474.99});

            Thread.sleep(1100);

            InputHandler inputHandler1 = siddhiAppRuntime.getInputHandler("StreamA");
            // send some unseen data for prediction
            inputHandler1.send(new Object[]{14.96, 41.76, 1024.07, 73.17});
            inputHandler1.send(new Object[]{25.18, 62.96, 1020.04, 59.08});
            inputHandler1.send(new Object[]{5.11, 39.4, 1012.16, 92.14});

            SiddhiTestHelper.waitForEvents(200, 3, count, 60000);
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void testRegressionStreamProcessorExtension2() {
        logger.info("RegressionLearningStreamProcessorExtension TestCase - Features are not of numeric type");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream StreamA (attribute_0 double, attribute_1 double, attribute_2 "
                + "double, attribute_3 bool );";
        String query = ("@info(name = 'query1') from StreamA#streamingml:AMRulesRegressor('ml', "
                + " attribute_0, attribute_1, attribute_2, attribute_3) \n"
                + "select attribute_0, attribute_1, attribute_2, attribute_3, prediction, meanSquaredError "
                + "insert into outputStream;");
        try {
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(trainingStream + inStreamDefinition
                    + trainingQuery + query);
            AssertJUnit.fail();
        } catch (Exception e) {
            logger.error(e.getCause().getMessage());
            AssertJUnit.assertTrue(e instanceof SiddhiAppCreationException);
            AssertJUnit.assertTrue(e.getCause().getMessage().contains("model.features in 5th parameter is not a"
                    + " numerical type attribute. Found BOOL. Check the input stream definition."));
        }
    }

    @Test
    public void testRegressionStreamProcessorExtension3() {
        logger.info("RegressionLearningStreamProcessorExtension TestCase - model.name is not String");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream StreamA (attribute_0 double, attribute_1 double, attribute_2 "
                + "double, attribute_3 double );";
        String query = ("@info(name = 'query1') from StreamA#streamingml:AMRulesRegressor(123, "
                + "attribute_0, attribute_1, attribute_2, attribute_3) \n" + ""
                + "select attribute_0, attribute_1, attribute_2, attribute_3, prediction, meanSquaredError "
                + "insert into outputStream;");
        try {
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
            AssertJUnit.fail();
        } catch (Exception e) {
            logger.error(e.getCause().getMessage());
            AssertJUnit.assertTrue(e instanceof SiddhiAppCreationException);
            AssertJUnit.assertTrue(e.getCause().getMessage().contains("Invalid parameter type found for the model.name"
                    + " argument, required STRING, but found INT."));
        }
    }

    @Test
    public void testRegressionStreamProcessorExtension4() {
        logger.info("RegressionLearningStreamProcessorExtension TestCase - invalid model name");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream StreamA (attribute_0 double, attribute_1 double, attribute_2 "
                + "double, attribute_3 double, attribute_4 string );";
        String query = ("@info(name = 'query1') from StreamA#streamingml:AMRulesRegressor(attribute_4, "
                + "attribute_0, attribute_1, attribute_2, attribute_3, attribute_4) \n"
                + "select attribute_0, attribute_1, attribute_2, attribute_3, prediction, meanSquaredError"
                + " insert into outputStream;");
        try {
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
            AssertJUnit.fail();
        } catch (Exception e) {
            logger.error(e.getCause().getMessage());
            AssertJUnit.assertTrue(e instanceof SiddhiAppCreationException);
            AssertJUnit.assertTrue(e.getCause().getMessage().contains("Parameter model.name must be a constant "
                    + "but found org.wso2.siddhi.core.executor.VariableExpressionExecutor"));
        }
    }

    @Test
    public void testRegressionStreamProcessorExtension5() {
        logger.info("RegressionLearningStreamProcessorExtension TestCase - incorrect initialization");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream StreamA (attribute_0 double, attribute_1 double, attribute_2 "
                + "double, attribute_3 double, attribute_4 double );";
        String query = ("@info(name = 'query1') from StreamA#streamingml:AMRulesRegressor() \n"
                + "select attribute_0, attribute_1, attribute_2, attribute_3, prediction, meanSquaredError "
                + "insert into outputStream;");
        try {
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
            AssertJUnit.fail();
        } catch (Exception e) {
            logger.error(e.getCause().getMessage());
            AssertJUnit.assertTrue(e instanceof SiddhiAppCreationException);
            AssertJUnit.assertTrue(e.getCause().getMessage().contains("streamingML:AMRulesRegressor needs exactly"
                    + " model.name and 5 feature atttributes, but found 0."));
        }
    }

    @Test
    public void testRegressionStreamProcessorExtension6() {
        logger.info("RegressionLearningStreamProcessorExtension TestCase - Incompatible model");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream StreamA (attribute_0 double, attribute_1 double, attribute_2 "
                + "double, attribute_3 double);";
        String query = ("@info(name = 'query1') from StreamA#streamingml:AMRulesRegressor('ml', "
                + "attribute_0, attribute_1, attribute_2) \n"
                + "select attribute_0, attribute_1, attribute_2, attribute_3, prediction, meanSquaredError "
                + "insert into outputStream;");
        try {
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(trainingStream +
                    inStreamDefinition + trainingQuery + query);
            AssertJUnit.fail();
        } catch (Exception e) {
            logger.error(e.getCause().getMessage());
            AssertJUnit.assertTrue(e instanceof SiddhiAppCreationException);
            AssertJUnit.assertTrue(e.getCause().getMessage().contains("Invalid number of parameters for "
                    + "streamingml:AMRulesRegressor. This Stream Processor requires  5 parameters, namely, "
                    + "model.name and 4 feature_attributes, but found 3 parameters"));
        }
    }

    @Test
    public void testRegressionStreamProcessorExtension7() {
        logger.info("RegressionLearningStreamProcessorExtension TestCase - invalid model name type");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream StreamA (attribute_0 double, attribute_1 double, attribute_2 "
                + "double, attribute_3 double);";
        String query = ("@info(name = 'query1') from StreamA#streamingml:AMRulesRegressor(0.2, "
                + "attribute_0, attribute_1, attribute_2, attribute_3) \n"
                + "select attribute_0, attribute_1, attribute_2, attribute_3, prediction, meanSquaredError "
                + "insert into outputStream;");
        try {
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
            AssertJUnit.fail();
        } catch (Exception e) {
            logger.error(e.getCause().getMessage());
            AssertJUnit.assertTrue(e instanceof SiddhiAppCreationException);
            AssertJUnit.assertTrue(e.getCause().getMessage().contains("Invalid parameter type found for the model.name"
                    + " argument, required STRING, but found DOUBLE"));
        }
    }

    @Test
    public void testRegressionStreamProcessorExtension8() {
        logger.info("RegressionLearningStreamProcessorExtension TestCase - init predict before "
                + "training the model");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream StreamA (attribute_0 double, attribute_1 double, attribute_2 "
                + "double, attribute_3 double);";
        String query = ("@info(name = 'query1') " +
                "from StreamA#streamingml:AMRulesRegressor('model1', attribute_0, "
                + "attribute_1, attribute_2, attribute_3) \n"
                + "select attribute_0, attribute_1, attribute_2, attribute_3, prediction, confidenceLevel "
                + "insert into outputStream;");
        try {
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(trainingStream
                    + inStreamDefinition + query + trainingQuery);
            AssertJUnit.fail();
        } catch (Exception e) {
            logger.error(e.getCause().getMessage());
            AssertJUnit.assertTrue(e instanceof SiddhiAppCreationException);
            AssertJUnit.assertTrue(e.getCause().getMessage().contains("Model [AmRulesRegressorTestApp.model1] "
                    + "needs to initialized prior to be used with streamingml:AMRulesRegressor. Perform "
                    + "streamingml:updateAMRulesRegressor process first."));
        }
    }

    @Test
    public void testRegressionStreamProcessorExtension9() {
        logger.info("RegressionLearningStreamProcessorExtension TestCase - more parameters than needed");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream StreamA (attribute_0 double, attribute_1 double, attribute_2 " +
                "double, attribute_3 double);";
        String query = ("@info(name = 'query1') from StreamA#streamingml:AMRulesRegressor('ml', " +
                "attribute_0, attribute_1, attribute_2, attribute_3, 2 ) \n" +
                "select attribute_0, attribute_1, attribute_2, attribute_3, prediction, meanSquredError " +
                "insert into outputStream;");
        try {
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(trainingStream +
                    inStreamDefinition + trainingQuery + query);
            AssertJUnit.fail();
        } catch (Exception e) {
            logger.error(e.getCause().getMessage());
            AssertJUnit.assertTrue(e instanceof SiddhiAppCreationException);
            AssertJUnit.assertTrue(e.getCause().getMessage().contains("Invalid number of parameters for " +
                    "streamingml:AMRulesRegressor. This Stream Processor requires  5 parameters, " +
                    "namely, model.name and 4 feature_attributes"));
        }
    }

    @Test
    public void testRegressionStreamProcessorExtension10() throws InterruptedException {
        logger.info("RegressionLearningStreamProcessorExtension TestCase "
                + "- Input feature value attributes mismatch from the feature attribute definition");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream StreamA (attribute_0 double, attribute_1 double, "
                + "attribute_2 double, attribute_3 double);";
        String query = ("@info(name = 'query1') from StreamA#streamingml:AMRulesRegressor('ml', "
                + " attribute_0, attribute_1, attribute_2, attribute_3) "
                + "select attribute_0, attribute_1, attribute_2, attribute_3, prediction, meanSquaredError "
                + "insert into outputStream;");

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(trainingStream + inStreamDefinition
                + trainingQuery + query);
        siddhiAppRuntime.addCallback("query1", new QueryCallback() {

            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(inEvents);
            }
        });
        try {
            InputHandler inputHandler = siddhiAppRuntime.getInputHandler("StreamTrain");
            siddhiAppRuntime.start();

            inputHandler.send(new Object[]{23.87, 63.94, 1019.02, 44.28, 445.11});
            inputHandler.send(new Object[]{26.09, 58.41, 1013.64, 64.58, 438.86});
            inputHandler.send(new Object[]{29.27, 66.85, 1011.11, 63.25, 440.98});

            Thread.sleep(1100);

            InputHandler inputHandler1 = siddhiAppRuntime.getInputHandler("StreamA");
            // send some unseen data for prediction
            inputHandler1.send(new Object[]{5.1, "setosa", 1.6, 0.2});
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void testRegressionStreamProcessorExtension11() {
        logger.info("RegressionLearningStreamProcessorExtension TestCase "
                + "- configure an AMRules Regressor mode with non existing stream");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream StreamA (attribute_0 double, attribute_1 double, attribute_2 "
                + "double, attribute_3 int, attribute_4 double );";
        String query = ("@info(name = 'query1') from StreamA#streamingml:AMRulesRegressor('ml', attribute_5 ) "
                + "select att_0 as attribute_0, "
                + "att_1 as attribute_1,att_2 as attribute_2,att_3 as attribute_3, accuracy insert into"
                + " outputStream;");
        try {
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition +
                    query);
            AssertJUnit.fail();
        } catch (Exception e) {
            logger.error(e.getMessage());
            AssertJUnit.assertTrue(e instanceof AttributeNotExistException);
            AssertJUnit.assertTrue(e.getMessage().contains("Cannot find attribute type as 'attribute_5' " +
                    "does not exist in 'StreamA'"));
        }
    }

    @Test
    public void testRegressionStreamProcessorExtension12() throws InterruptedException {
        logger.info("RegressionLearningStreamProcessorExtension TestCase " +
                "- Send non defined data type values after building an AMRules Regressor model");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream StreamA (attribute_0 double, attribute_1 double, "
                + "attribute_2 double, attribute_3 double);";
        String query = ("@info(name = 'query1') from StreamA#streamingml:AMRulesRegressor('ml', "
                + " attribute_0, attribute_1, attribute_2, attribute_3) "
                + "select attribute_0, attribute_1, attribute_2, attribute_3, prediction, meanSquaredError "
                + "insert into outputStream;");

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(trainingStream + inStreamDefinition
                + trainingQuery + query);
        try {
            InputHandler inputHandler = siddhiAppRuntime.getInputHandler("StreamTrain");
            siddhiAppRuntime.start();
            inputHandler.send(new Object[]{27.36, 48.6, 1003.18, 54.93, 436.06});
            inputHandler.send(new Object[]{14.6, 39.31, 1011.11, 72.52, 464.16});
            inputHandler.send(new Object[]{7.91, 39.96, 1023.57, 88.44, 475.52});
            inputHandler.send(new Object[]{5.81, 35.79, 1012.14, 92.28, 484.41});
            inputHandler.send(new Object[]{30.53, 65.18, 1012.69, 41.85, 437.89});

            Thread.sleep(1100);

            InputHandler inputHandler1 = siddhiAppRuntime.getInputHandler("StreamA");
            // send some unseen data for prediction
            inputHandler1.send(new Object[]{14.96, "string", 1024.07, 73.17});
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }
}
