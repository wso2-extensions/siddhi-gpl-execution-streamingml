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
import org.wso2.siddhi.core.util.SiddhiTestHelper;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class AdaptiveModelRulesRegressorStreamProcessorExtensionTestcase {
    private static final Logger logger = Logger
            .getLogger(AdaptiveModelRulesRegressorStreamProcessorExtensionTestcase.class);


    private AtomicInteger count;
    private String trainingStream = "@App:name('AmRulesRegressorTestApp') \n" +
            "define stream StreamTrain (attribute_0 double, " +
            "attribute_1 double, attribute_2 double, attribute_3 double, attribute_4 double );";
    private String trainingQuery = ("@info(name = 'query-train') " +
            "from StreamTrain#streamingml:updateAMRulesRegressor('ml', attribute_0, attribute_1, "
            + "attribute_2, attribute_3, attribute_4) \n"
            + "insert all events into trainOutputStream;\n");

    @BeforeMethod
    public void init() {
        count = new AtomicInteger(0);
    }


    @Test
    public void testRegressionStreamProcessorExtension1() throws InterruptedException {
        logger.info("AMRulesRegressor UpdaterStreamProcessorExtension TestCase " +
                "- Assert predictions and evolution");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream StreamA (attribute_0 double, attribute_1 double, " +
                "attribute_2 double, attribute_3 double);";
        String query = ("@info(name = 'query1') from StreamA#streamingml:AMRulesRegressor('ml', " +
                " attribute_0, attribute_1, attribute_2, attribute_3) " +
                "select attribute_0, attribute_1, attribute_2, attribute_3, prediction, meanSquaredError " +
                "insert into outputStream;");

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(trainingStream + inStreamDefinition
                + trainingQuery + query);

        siddhiAppRuntime.addCallback("query1", new QueryCallback() {

            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                count.incrementAndGet();
                //  EventPrinter.print(inEvents);
                if (count.get() == 3) {
                    AssertJUnit.assertArrayEquals(new Object[]{5.11, 39.4, 1012.16, 92.14, 460.992, 11578.597},
                            inEvents[0].getData());
                }
            }
        });
        try {
            InputHandler inputHandler = siddhiAppRuntime.getInputHandler("StreamTrain");
            siddhiAppRuntime.start();

            inputHandler.send(new Object[]{14.96, 41.76, 1024.07, 73.17, 463.26});
            inputHandler.send(new Object[]{25.18, 62.96, 1020.04, 59.08, 444.37});
            inputHandler.send(new Object[]{5.11, 39.4, 1012.16, 92.14, 488.56});
            inputHandler.send(new Object[]{20.86, 57.32, 1010.24, 76.64, 446.48});
            inputHandler.send(new Object[]{10.82, 37.5, 1009.23, 96.62, 473.9});
            inputHandler.send(new Object[]{26.27, 59.44, 1012.23, 58.77, 443.67});
            inputHandler.send(new Object[]{15.89, 43.96, 1014.02, 75.24, 467.35});
            inputHandler.send(new Object[]{9.48, 44.71, 1019.12, 66.43, 478.42});
            inputHandler.send(new Object[]{14.64, 45, 1021.78, 41.25, 475.98});
            inputHandler.send(new Object[]{11.74, 43.56, 1015.14, 70.72, 477.5});
            inputHandler.send(new Object[]{17.99, 43.72, 1008.64, 75.04, 453.02});
            inputHandler.send(new Object[]{20.14, 46.93, 1014.66, 64.22, 453.99});
            inputHandler.send(new Object[]{24.34, 73.5, 1011.31, 84.15, 440.29});
            inputHandler.send(new Object[]{25.71, 58.59, 1012.77, 61.83, 451.28});
            inputHandler.send(new Object[]{26.19, 69.34, 1009.48, 87.59, 433.99});
            inputHandler.send(new Object[]{21.42, 43.79, 1015.76, 43.08, 462.19});
            inputHandler.send(new Object[]{18.21, 45, 1022.86, 48.84, 467.54});
            inputHandler.send(new Object[]{11.04, 41.74, 1022.6, 77.51, 477.2});
            inputHandler.send(new Object[]{14.45, 52.75, 1023.97, 63.59, 459.85});

            Thread.sleep(1100);

            InputHandler inputHandler1 = siddhiAppRuntime.getInputHandler("StreamA");
            // send some unseen data for prediction
            inputHandler1.send(new Object[]{14.96, 41.76, 1024.07, 73.17});
            inputHandler1.send(new Object[]{25.18, 62.96, 1020.04, 59.08});
            inputHandler1.send(new Object[]{5.11, 39.4, 1012.16, 92.14});

            SiddhiTestHelper.waitForEvents(200, 3, count, 60000);
        } catch (Exception e) {
            logger.error(e.getCause().getMessage());
        } finally {
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void testRegressionStreamProcessorExtension2() throws InterruptedException {
        logger.info("AMRulesRegressor UpdaterStreamProcessorExtension TestCase - Features are not of numeric type");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream StreamA (attribute_0 double, attribute_1 double, attribute_2 " +
                "double, attribute_3 bool );";
        String query = ("@info(name = 'query1') from StreamA#streamingml:AMRulesRegressor('model1', " +
                " attribute_0, attribute_1, attribute_2, attribute_3) \n" +
                "select attribute_0, attribute_1, attribute_2, attribute_3, prediction, meanSquaredError " +
                "insert into outputStream;");
        try {
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
            AssertJUnit.fail();
        } catch (Exception e) {
            logger.error(e.getCause().getMessage());
            AssertJUnit.assertTrue(e instanceof SiddhiAppCreationException);
        }
    }

    @Test
    public void testRegressionStreamProcessorExtension3() throws InterruptedException {
        logger.info("AMRulesRegressor UpdaterStreamProcessorExtension TestCase - model.name is not String");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream StreamA (attribute_0 double, attribute_1 double, attribute_2 " +
                "double, attribute_3 double );";
        String query = ("@info(name = 'query1') from StreamA#streamingml:AMRulesRegressor(123, " +
                "attribute_0, attribute_1, attribute_2, attribute_3) \n" + "" +
                "select attribute_0, attribute_1, attribute_2, attribute_3, prediction, meanSquaredError " +
                "insert into outputStream;");

        try {
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
            AssertJUnit.fail();
        } catch (Exception e) {
            logger.error(e.getCause().getMessage());
            AssertJUnit.assertTrue(e instanceof SiddhiAppCreationException);
            AssertJUnit.assertTrue(e.getCause().getMessage().contains("Invalid parameter type found for " +
                    "the model.name argument, required STRING but found INT"));
        }
    }

    @Test
    public void testRegressionStreamProcessorExtension4() throws InterruptedException {
        logger.info("AMRulesRegressor UpdaterStreamProcessorExtension TestCase - invalid model name");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream StreamA (attribute_0 double, attribute_1 double, attribute_2 " +
                "double, attribute_3 double, attribute_4 string );";
        String query = ("@info(name = 'query1') from StreamA#streamingml:AMRulesRegressor(attribute_4, " +
                "attribute_0, attribute_1, attribute_2, attribute_3, attribute_4) \n"
                + "select attribute_0, attribute_1, attribute_2, attribute_3, prediction, meanSquaredError" +
                " insert into outputStream;");

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
    public void testRegressionStreamProcessorExtension5() throws InterruptedException {
        logger.info("AMRulesRegressor UpdaterStreamProcessorExtension TestCase - incorrect initialization");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream StreamA (attribute_0 double, attribute_1 double, attribute_2 " +
                "double, attribute_3 double, attribute_4 string );";
        String query = ("@info(name = 'query1') from StreamA#streamingml:AMRulesRegressor() \n"
                + "select attribute_0, attribute_1, attribute_2, attribute_3, prediction, meanSquaredError " +
                "insert into outputStream;");

        try {
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
            AssertJUnit.fail();
        } catch (Exception e) {
            logger.error(e.getCause().getMessage());
            AssertJUnit.assertTrue(e instanceof SiddhiAppCreationException);
        }
    }


    @Test
    public void testRegressionStreamProcessorExtension6() throws InterruptedException {
        logger.info("AMRulesRegressor UpdaterStreamProcessorExtension TestCase - Incompatible model");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream StreamA (attribute_0 double, attribute_1 double, attribute_2 " +
                "double, attribute_3 double);";
        String query = ("@info(name = 'query1') from StreamA#streamingml:AMRulesRegressor('model1', " +
                "attribute_0, attribute_1, attribute_2) \n"
                + "select attribute_0, attribute_1, attribute_2, attribute_3, prediction, meanSquaredError " +
                "insert into outputStream;");

        try {
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(trainingStream +
                    inStreamDefinition + trainingQuery + query);
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
    public void testRegressionStreamProcessorExtension7() throws InterruptedException {
        logger.info("AMRulesRegressor UpdaterStreamProcessorExtension TestCase - invalid model name type");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream StreamA (attribute_0 double, attribute_1 double, attribute_2 " +
                "double, attribute_3 double);";
        String query = ("@info(name = 'query1') from StreamA#streamingml:AMRulesRegressor(0.2, " +
                "attribute_0, attribute_1, attribute_2, attribute_3) \n" +
                "select attribute_0, attribute_1, attribute_2, attribute_3, prediction, meanSquaredError " +
                "insert into outputStream;");

        try {
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
        } catch (Exception e) {
            logger.error(e.getCause().getMessage());
            AssertJUnit.assertTrue(e instanceof SiddhiAppCreationException);
            AssertJUnit.assertTrue(e.getCause().getMessage().contains("Invalid parameter type found " +
                    "for the model.name argument, required STRING but found DOUBLE"));
        }
    }

    @Test
    public void testRegressionStreamProcessorExtension8() throws InterruptedException {
        logger.info("AMRulesRegressor UpdaterStreamProcessorExtension TestCase - init predict before " +
                "training the model");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream StreamA (attribute_0 double, attribute_1 double, attribute_2 " +
                "double, attribute_3 double);";
        String query = ("@info(name = 'query1') " +
                "from StreamA#streamingml:AMRulesRegressor('model1', attribute_0, " +
                "attribute_1, attribute_2, attribute_3) \n"
                + "select attribute_0, attribute_1, attribute_2, attribute_3, prediction, confidenceLevel " +
                "insert into outputStream;");

        try {
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(trainingStream
                    + inStreamDefinition + query + trainingQuery);

        } catch (Exception e) {
            logger.error(e.getCause().getMessage());
            AssertJUnit.assertTrue(e instanceof SiddhiAppCreationException);
            AssertJUnit.assertTrue(e.getCause().getMessage().contains("Model [AmRulesRegressorTestApp.model1] "
                    + "needs to initialized prior to be used with streamingml:AMRulesRegressor. Perform "
                    + "streamingml:updateAMRulesRegressor process first."));
        }
    }
}
