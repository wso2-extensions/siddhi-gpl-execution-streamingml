# API Docs - v2.0.1-SNAPSHOT

## Streamingml

### AMRulesRegressor *<a target="_blank" href="http://siddhi.io/documentation/siddhi-5.x/query-guide-5.x/#stream-processor">(Stream Processor)</a>*

<p style="word-wrap: break-word">This extension performs regression tasks using the <code>AMRulesRegressor</code> algorithm.</p>

<span id="syntax" class="md-typeset" style="display: block; font-weight: bold;">Syntax</span>
```
streamingml:AMRulesRegressor(<STRING> model.name, <INT|FLOAT|FLOAT|DOUBLE> model.feature)
```

<span id="query-parameters" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">QUERY PARAMETERS</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Default Value</th>
        <th>Possible Data Types</th>
        <th>Optional</th>
        <th>Dynamic</th>
    </tr>
    <tr>
        <td style="vertical-align: top">model.name</td>
        <td style="vertical-align: top; word-wrap: break-word">The name of the model to be used for prediction.</td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">model.feature</td>
        <td style="vertical-align: top; word-wrap: break-word">The feature vector for the regression analysis.</td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">INT<br>FLOAT<br>FLOAT<br>DOUBLE</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
</table>
<span id="extra-return-attributes" class="md-typeset" style="display: block; font-weight: bold;">Extra Return Attributes</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Possible Types</th>
    </tr>
    <tr>
        <td style="vertical-align: top">prediction</td>
        <td style="vertical-align: top; word-wrap: break-word">The predicted value.</td>
        <td style="vertical-align: top">DOUBLE</td>
    </tr>
    <tr>
        <td style="vertical-align: top">meanSquaredError</td>
        <td style="vertical-align: top; word-wrap: break-word">The <code>MeanSquaredError</code> of the predicting model.</td>
        <td style="vertical-align: top">DOUBLE</td>
    </tr>
</table>

<span id="examples" class="md-typeset" style="display: block; font-weight: bold;">Examples</span>
<span id="example-1" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 1</span>
```
define stream StreamA (attribute_0 double, attribute_1 double, attribute_2 double, attribute_3 double);

from StreamA#streamingml:AMRulesRegressor('model1',  attribute_0, attribute_1, attribute_2, attribute_3) 
select attribute_0, attribute_1, attribute_2, attribute_3, prediction, meanSquaredError insert into OutputStream;
```
<p style="word-wrap: break-word">This query uses an <code>AMRules</code> model named <code>model1</code> that is used to predict the value for the feature vector represented by <code>attribute_0</code>, <code>attribute_1</code>, <code>attribute_2</code>, and <code>attribute_3</code>. The predicted value along with the <code>MeanSquaredError</code> and the feature vector are output to a stream named <code>OutputStream</code>. The resulting definition of the <code>OutputStream</code> stream is as follows:<br>(attribute_0 double, attribute_1 double, attribute_2 double, attribute_3 double, prediction double, meanSquaredError double).</p>

### clusTree *<a target="_blank" href="http://siddhi.io/documentation/siddhi-5.x/query-guide-5.x/#stream-processor">(Stream Processor)</a>*

<p style="word-wrap: break-word">This extension performs clustering on a streaming data set. Initially a micro cluster model is generated using the ClusTree algorithm, and weighted k-means is periodically applied to micro clusters to generate a macro cluster model with the required number of clusters. Data points can be of any dimensionality, but the dimensionality should be constant throughout the stream. Euclidean distance is taken as the distance metric.</p>

<span id="syntax" class="md-typeset" style="display: block; font-weight: bold;">Syntax</span>
```
streamingml:clusTree(<INT> no.of.clusters, <INT> max.iterations, <INT> no.of.events.to.refresh.macro.model, <INT> max.height.of.tree, <INT> horizon, <DOUBLE|FLOAT|INT|LONG> model.features)
```

<span id="query-parameters" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">QUERY PARAMETERS</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Default Value</th>
        <th>Possible Data Types</th>
        <th>Optional</th>
        <th>Dynamic</th>
    </tr>
    <tr>
        <td style="vertical-align: top">no.of.clusters</td>
        <td style="vertical-align: top; word-wrap: break-word">The assumed number of natural clusters (<code>numberOfClusters</code>) in the data set.</td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">max.iterations</td>
        <td style="vertical-align: top; word-wrap: break-word">The number of times the process should be iterated. The process iterates until the number specified for this parameter is reached, or until iterating the process does not result in a change in the centroids.</td>
        <td style="vertical-align: top">40</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">no.of.events.to.refresh.macro.model</td>
        <td style="vertical-align: top; word-wrap: break-word">The number of new events that should arrive in order to recalculate the k-means macro cluster centers.</td>
        <td style="vertical-align: top">100</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">max.height.of.tree</td>
        <td style="vertical-align: top; word-wrap: break-word">This defines the maximum number of levels that should exist in the ClusTree. The maximum number of levels is calculated as <code>3^&lt;VALUE_SPECIFIED&gt;</code> (e.g., If 10 is specified, there can be a maximum of 3^10 micro clusters in the micro cluster). It is recommended to set the value within the 5-8 range because a lot of micro-clusters can consume a lot of memory, and as a result, creating the macro cluster model will take longer.</td>
        <td style="vertical-align: top">8</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">horizon</td>
        <td style="vertical-align: top; word-wrap: break-word">This controls the decay of weights of old micro-clusters to manage the concept drift. If horizon is set as <code>1000</code>, then a micro cluster that has not been recently updated loses its weight by half after 1000 events.</td>
        <td style="vertical-align: top">1000</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">model.features</td>
        <td style="vertical-align: top; word-wrap: break-word">This is a variable length argument. Depending on the dimensionality of data points, you receive coordinates as features along each axis.</td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">DOUBLE<br>FLOAT<br>INT<br>LONG</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
</table>
<span id="extra-return-attributes" class="md-typeset" style="display: block; font-weight: bold;">Extra Return Attributes</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Possible Types</th>
    </tr>
    <tr>
        <td style="vertical-align: top">euclideanDistanceToClosestCentroid</td>
        <td style="vertical-align: top; word-wrap: break-word">This represents the Euclidean distance between the current data point and the closest centroid.</td>
        <td style="vertical-align: top">DOUBLE</td>
    </tr>
    <tr>
        <td style="vertical-align: top">closestCentroidCoordinate</td>
        <td style="vertical-align: top; word-wrap: break-word">This is a variable length attribute. Depending on the dimensionality(<code>d</code>) <code>closestCentroidCoordinate1</code> is returned to <code>closestCentroidCoordinated that are the </code>d `dimensional coordinates of the closest centroid from the model to the current event. This is the prediction result, and this represents the cluster towhich the current event belongs.</td>
        <td style="vertical-align: top">DOUBLE</td>
    </tr>
</table>

<span id="examples" class="md-typeset" style="display: block; font-weight: bold;">Examples</span>
<span id="example-1" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 1</span>
```
@App:name('ClusTreeTestSiddhiApp') 
define stream InputStream (x double, y double);
@info(name = 'query1') 
from InputStream#streamingml:clusTree(2, 10, 20, 5, 50, x, y) 
select closestCentroidCoordinate1, closestCentroidCoordinate2, x, y 
insert into OutputStream;
```
<p style="word-wrap: break-word">This query creates a Siddhi application named <code>ClusTreeTestSiddhiApp</code>, and it accepts 2D inputs of doubles. The query named <code>query1</code> creates a ClusTree model. It also creates a k-means model after the first 20 events, and refreshes it after every 20 events. Two macro clusters are created, and the process is not iterated more than 10 times. The maximum height of tree is set to 5, and therefore, a maximum of 3^5 micro clusters are generated from the Clus Tree. The horizon is set to 50, and therefore, the weight of each micro cluster that is not updated reduces by half after every 50 events.</p>

<span id="example-2" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 2</span>
```
@App:name('ClusTreeTestSiddhiApp') 
define stream InputStream (x double, y double);
@info(name = 'query1') 
from InputStream#streamingml:ClusTree(2, x, y) 
select closestCentroidCoordinate1, closestCentroidCoordinate2, x, y 
insert into OutputStream;
```
<p style="word-wrap: break-word">This query does not include hyper parameters. Therefore, the default values mentioned above are applied. This mode of querying is recommended if you are not familier with ClusTree/KMeans algorithms.</p>

### hoeffdingTreeClassifier *<a target="_blank" href="http://siddhi.io/documentation/siddhi-5.x/query-guide-5.x/#stream-processor">(Stream Processor)</a>*

<p style="word-wrap: break-word">This extension performs classification using the Hoeffding Adaptive Tree algorithm for evolving data streams that use <code>ADWIN</code> to replace branches with new ones.</p>

<span id="syntax" class="md-typeset" style="display: block; font-weight: bold;">Syntax</span>
```
streamingml:hoeffdingTreeClassifier(<STRING> model.name)
```

<span id="query-parameters" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">QUERY PARAMETERS</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Default Value</th>
        <th>Possible Data Types</th>
        <th>Optional</th>
        <th>Dynamic</th>
    </tr>
    <tr>
        <td style="vertical-align: top">model.name</td>
        <td style="vertical-align: top; word-wrap: break-word">The name of the model to be used for prediction.</td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
</table>
<span id="extra-return-attributes" class="md-typeset" style="display: block; font-weight: bold;">Extra Return Attributes</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Possible Types</th>
    </tr>
    <tr>
        <td style="vertical-align: top">prediction</td>
        <td style="vertical-align: top; word-wrap: break-word">The predicted class label.</td>
        <td style="vertical-align: top">STRING</td>
    </tr>
    <tr>
        <td style="vertical-align: top">confidenceLevel</td>
        <td style="vertical-align: top; word-wrap: break-word">The probability of the prediction.</td>
        <td style="vertical-align: top">DOUBLE</td>
    </tr>
</table>

<span id="examples" class="md-typeset" style="display: block; font-weight: bold;">Examples</span>
<span id="example-1" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 1</span>
```
define stream StreamA (attribute_0 double, attribute_1 double, attribute_2 double, attribute_3 double);

from StreamA#streamingml:hoeffdingTreeClassifier('model1',  attribute_0, attribute_1, attribute_2, attribute_3) 
select attribute_0, attribute_1, attribute_2, attribute_3, prediction, predictionConfidence insert into OutputStream;
```
<p style="word-wrap: break-word">This query uses a Hoeffding Tree model named <code>model1</code> to predict the label of the feature vector represented by <code>attribute_0</code>, <code>attribute_1</code>, <code>attribute_2</code>, and attribute_3 attributes. The predicted label (<code>String/Bool</code>) along with the prediction confidence and the feature vector are output to the <code>OutputStream</code> stream. The expected definition of the <code>OutputStream</code> is as follows:(attribute_0 double, attribute_1 double, attribute_2<br>&nbsp;double, attribute_3 double, prediction string, <br>confidenceLevel double).</p>

### updateAMRulesRegressor *<a target="_blank" href="http://siddhi.io/documentation/siddhi-5.x/query-guide-5.x/#stream-processor">(Stream Processor)</a>*

<p style="word-wrap: break-word">This extension performs the build/update of the AMRules Regressor model for evolving data streams.</p>

<span id="syntax" class="md-typeset" style="display: block; font-weight: bold;">Syntax</span>
```
streamingml:updateAMRulesRegressor(<STRING> model.name, <DOUBLE> split.confidence, <DOUBLE> tie.break.threshold, <INT> grace.period, <INT> change.detector, <INT> anomaly.detector, <DOUBLE|FLOAT|LONG|INT> model.features)
```

<span id="query-parameters" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">QUERY PARAMETERS</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Default Value</th>
        <th>Possible Data Types</th>
        <th>Optional</th>
        <th>Dynamic</th>
    </tr>
    <tr>
        <td style="vertical-align: top">model.name</td>
        <td style="vertical-align: top; word-wrap: break-word">The name of the model to be built/updated.</td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">split.confidence</td>
        <td style="vertical-align: top; word-wrap: break-word">This is a Hoeffding Bound parameter.  It defines the percentage of error that to be allowed in a split decision. When the value specified is closer to 0, it takes longer to output the decision. min:0 max:1</td>
        <td style="vertical-align: top">1.0E-7D</td>
        <td style="vertical-align: top">DOUBLE</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">tie.break.threshold</td>
        <td style="vertical-align: top; word-wrap: break-word">This is a Hoeffding Bound parameter. It specifies the threshold below which a split must be forced to break ties. min:0 max:1</td>
        <td style="vertical-align: top">0.05D</td>
        <td style="vertical-align: top">DOUBLE</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">grace.period</td>
        <td style="vertical-align: top; word-wrap: break-word">This is a Hoeffding Bound parameter. The number of instances a leaf should observe between split attempts.</td>
        <td style="vertical-align: top">200</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">change.detector</td>
        <td style="vertical-align: top; word-wrap: break-word"> The Concept Drift Detection methodology to be used. The possible values are as follows.<br>&nbsp;<code>0</code>:NoChangeDetection<br><code>1</code>:ADWINChangeDetector <br>&nbsp;<code>2</code>:PageHinkleyDM</td>
        <td style="vertical-align: top">2:PageHinkleyDM</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">anomaly.detector</td>
        <td style="vertical-align: top; word-wrap: break-word">The Anomaly Detection methodology to be used. The possible values are as follows:<code>0</code>:NoAnomalyDetection<br><code>1</code>:AnomalinessRatioScore<br><code>2</code>:OddsRatioScore</td>
        <td style="vertical-align: top">2:OddsRatioScore</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">model.features</td>
        <td style="vertical-align: top; word-wrap: break-word">The features of the model that should be attributes of the stream.</td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">DOUBLE<br>FLOAT<br>LONG<br>INT</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
</table>
<span id="extra-return-attributes" class="md-typeset" style="display: block; font-weight: bold;">Extra Return Attributes</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Possible Types</th>
    </tr>
    <tr>
        <td style="vertical-align: top">meanSquaredError</td>
        <td style="vertical-align: top; word-wrap: break-word">The current Mean Squared Error of the model</td>
        <td style="vertical-align: top">DOUBLE</td>
    </tr>
</table>

<span id="examples" class="md-typeset" style="display: block; font-weight: bold;">Examples</span>
<span id="example-1" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 1</span>
```
define stream StreamA (attribute_0 double, attribute_1 double, attribute_2 double, attribute_3 double, attribute_4 string );

from StreamA#streamingml:updateAMRulesRegressor('model1',) 
select attribute_0, attribute_1, attribute_2, attribute_3, meanSquaredError insert into OutputStream;
```
<p style="word-wrap: break-word">In this query, an AMRulesRegressor model named 'model1' is built/updated using <code>attribute_0</code>, <code>attribute_1</code>, <code>attribute_2</code>, and <code>attribute_3</code> attributes as features, and <code>attribute_4</code> as the target_value. The accuracy of the evaluation is output to the OutputStream stream.</p>

<span id="example-2" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 2</span>
```
define stream StreamA (attribute_0 double, attribute_1 double, attribute_2 double, attribute_3 double, attribute_4 string );

from StreamA#streamingml:updateAMRulesRegressor('model1', 1.0E-7D, 0.05D, 200, 0, 0) 
select attribute_0, attribute_1, attribute_2, attribute_3, meanSquaredError insert into OutputStream;
```
<p style="word-wrap: break-word">In this query, an <code>AMRulesRegressor</code> model named <code>model1</code> is built/updated with a split confidence of 1.0E-7D, a tie break threshold of 0.05D, and a grace period of 200. The Concept Drift Detection and Anomaly Detection methodologies used are <code>NoChangeDetection</code> and <code>NoAnomalyDetection</code> respectively. <code>attribute_0</code>, <code>attribute_1</code>, <code>attribute_2</code>, and <code>attribute_3</code> are used as features, and <code>attribute_4</code> is used as the target value. The <code>meanSquaredError</code> is output to the <code>OutputStream</code> stream.</p>

### updateHoeffdingTree *<a target="_blank" href="http://siddhi.io/documentation/siddhi-5.x/query-guide-5.x/#stream-processor">(Stream Processor)</a>*

<p style="word-wrap: break-word">This extension performs the build/update of Hoeffding Adaptive Tree for evolving data streams that use <code>ADWIN</code> to replace branches for new ones.</p>

<span id="syntax" class="md-typeset" style="display: block; font-weight: bold;">Syntax</span>
```
streamingml:updateHoeffdingTree(<STRING> model.name, <INT> no.of.classes, <INT> grace.period, <INT> split.criterion, <DOUBLE> split.confidence, <DOUBLE> tie.break.threshold, <BOOL> binary.split, <BOOL> pre.prune, <INT> leaf.prediction.strategy, <DOUBLE|INT> model.features)
```

<span id="query-parameters" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">QUERY PARAMETERS</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Default Value</th>
        <th>Possible Data Types</th>
        <th>Optional</th>
        <th>Dynamic</th>
    </tr>
    <tr>
        <td style="vertical-align: top">model.name</td>
        <td style="vertical-align: top; word-wrap: break-word">The name of the model to be built/updated.</td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">no.of.classes</td>
        <td style="vertical-align: top; word-wrap: break-word">The number of class labels in the datastream.</td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">grace.period</td>
        <td style="vertical-align: top; word-wrap: break-word">The number of instances a leaf should observe between split attempts. A minimum and a maximum value should be specified. e.g., <code>min:0, max:2147483647</code>.</td>
        <td style="vertical-align: top">200</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">split.criterion</td>
        <td style="vertical-align: top; word-wrap: break-word">The split criterion to be used. Possible values are as follows:<br><code>0</code>:InfoGainSplitCriterion<br><code>1</code>:GiniSplitCriterion</td>
        <td style="vertical-align: top">0:InfoGainSplitCriterion</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">split.confidence</td>
        <td style="vertical-align: top; word-wrap: break-word">The amount of error that should be allowed in a split decision. When the value specified is closer to 0, it takes longer to output the decision.</td>
        <td style="vertical-align: top">1e-7</td>
        <td style="vertical-align: top">DOUBLE</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">tie.break.threshold</td>
        <td style="vertical-align: top; word-wrap: break-word">The threshold at which a split must be forced to break ties. A minimum value and a maximum value must be specified. e.g., <code>min:0.0D, max:1.0D</code></td>
        <td style="vertical-align: top">0.05D</td>
        <td style="vertical-align: top">DOUBLE</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">binary.split</td>
        <td style="vertical-align: top; word-wrap: break-word">If this parameter is set to <code>true</code>, onlybinary splits are allowed.</td>
        <td style="vertical-align: top">false</td>
        <td style="vertical-align: top">BOOL</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">pre.prune</td>
        <td style="vertical-align: top; word-wrap: break-word">If this parameter is set to <code>true</code>, pre-pruning is allowed.</td>
        <td style="vertical-align: top">false</td>
        <td style="vertical-align: top">BOOL</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">leaf.prediction.strategy</td>
        <td style="vertical-align: top; word-wrap: break-word">This specifies the leaf prediction strategy to be used. Possible values are as follows:<br><code>0</code>:Majority class <br><code>1</code>:Naive Bayes<br><code>2</code>:Naive Bayes Adaptive.</td>
        <td style="vertical-align: top">2:Naive Bayes Adaptive</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">model.features</td>
        <td style="vertical-align: top; word-wrap: break-word">The features of the model that should be attributes of the stream.</td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">DOUBLE<br>INT</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
</table>
<span id="extra-return-attributes" class="md-typeset" style="display: block; font-weight: bold;">Extra Return Attributes</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Possible Types</th>
    </tr>
    <tr>
        <td style="vertical-align: top">accuracy</td>
        <td style="vertical-align: top; word-wrap: break-word">The accuracy evaluation of the model(Prequnetial Evaluation)</td>
        <td style="vertical-align: top">DOUBLE</td>
    </tr>
</table>

<span id="examples" class="md-typeset" style="display: block; font-weight: bold;">Examples</span>
<span id="example-1" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 1</span>
```
define stream StreamA (attribute_0 double, attribute_1 double, attribute_2 double, attribute_3 double, attribute_4 string );

from StreamA#streamingml:updateHoeffdingTree('model1', 3) 
select attribute_0, attribute_1, attribute_2, attribute_3, accuracy insert into OutputStream;
```
<p style="word-wrap: break-word">This query builds/updates a HoeffdingTree model named <code>model1</code> under 3 classes using <code>attribute_0</code>, <code>attribute_1</code>, <code>attribute_2</code>, and <code>attribute_3</code> as features, and <code>attribute_4</code> as the label. The accuracy evaluation is output to the <code>OutputStream</code> stream</p>

<span id="example-2" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 2</span>
```
define stream StreamA (attribute_0 double, attribute_1 double, attribute_2 double, attribute_3 double, attribute_4 string );

from StreamA#streamingml:updateHoeffdingTree('model1', 3, 200, 0, 1e-7, 1.0D, true, true, 2) 
select attribute_0, attribute_1, attribute_2, attribute_3, accuracy insert into OutputStream;
```
<p style="word-wrap: break-word">This query builds/updates a Hoeffding Tree model named <code>model1</code> with a grace period of 200, an information gain split criterion of 0, 1e-7 of allowable error in split decision, 1.0D of breaktie threshold, allowing only binary splits. Pre-pruning is disabled, and <code>Naive Bayes Adaptive</code> is used as the leaf prediction strategy. 'attribute_0', <code>attribute_1</code>, <code>attribute_2</code>, and <code>attribute_3</code> are used as features, and <code>attribute_4</code> as the label. The accuracy evaluation is output to the OutputStream stream.</p>

