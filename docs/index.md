# siddhi-gpl-execution-streamingml
The **siddhi-execution-streamingml** is an extension to <a target="_blank" href="https://wso2.github
.io/siddhi">Siddhi</a>  that performs streaming machine learning on event streams.

Find some useful links below:

* <a target="_blank" href="https://github.com/wso2-extensions/siddhi-gpl-execution-streamingml">Source code</a>
* <a target="_blank" href="https://github.com/wso2-extensions/siddhi-gpl-execution-streamingml/releases">Releases</a>
* <a target="_blank" href="https://github.com/wso2-extensions/siddhi-gpl-execution-streamingml/issues">Issue tracker</a>

## Latest API Docs

Latest API Docs is <a target="_blank" href="https://wso2-extensions.github.io/siddhi-gpl-execution-streamingml/api/1.0.4-SNAPSHOT">1.0.4-SNAPSHOT</a>.

## How to use 

**Using the extension in <a target="_blank" href="https://github.com/wso2/product-sp">WSO2 Stream Processor</a>**

* You can use this extension in the latest <a target="_blank" href="https://github.com/wso2/product-sp/releases">WSO2 Stream Processor</a> that is a part of <a target="_blank" href="http://wso2.com/analytics?utm_source=gitanalytics&utm_campaign=gitanalytics_Jul17">WSO2 Analytics</a> offering, with editor, debugger and simulation support. 

* To use this extension, you have to add the component <a target="_blank" href="https://github.com/wso2-extensions/siddhi-gpl-execution-streamingml/releases">jar</a> in to the `<STREAM_PROCESSOR_HOME>/lib` directory.

**Using the extension as a <a target="_blank" href="https://wso2.github.io/siddhi/documentation/running-as-a-java-library">java library</a>**

* This extension can be added as a maven dependency along with other Siddhi dependencies to your project.

```
     <dependency>
        <groupId>org.wso2.extension.siddhi.gpl.execution.streamingml</groupId>
        <artifactId>siddhi-gpl-execution-streamingml</artifactId>
        <version>x.x.x</version>
     </dependency>
```

## Jenkins Build Status

---

|  Branch | Build Status |
| :------ |:------------ | 
| master  | [![Build Status](https://wso2.org/jenkins/view/All%20Builds/job/siddhi/job/siddhi-gpl-execution-streamingml/badge/icon)](https://wso2.org/jenkins/view/All%20Builds/job/siddhi/job/siddhi-gpl-execution-streamingml/) |

---

## Features

* <a target="_blank" href="https://wso2-extensions.github.io/siddhi-gpl-execution-streamingml/api/1.0.4-SNAPSHOT/#updateamrulesregressor-stream-processor">updateAMRulesRegressor</a> *(<a target="_blank" href="https://wso2.github.io/siddhi/documentation/siddhi-4.0/#stream-processors">Stream Processor</a>)*<br><div style="padding-left: 1em;"><p>Performs build/update of AMRules Regressor model for evolving data streams.</p></div>
* <a target="_blank" href="https://wso2-extensions.github.io/siddhi-gpl-execution-streamingml/api/1.0.4-SNAPSHOT/#amrulesregressor-stream-processor">AMRulesRegressor</a> *(<a target="_blank" href="https://wso2.github.io/siddhi/documentation/siddhi-4.0/#stream-processors">Stream Processor</a>)*<br><div style="padding-left: 1em;"><p>Performs Regression task with AMRulesRegressor </p></div>
* <a target="_blank" href="https://wso2-extensions.github.io/siddhi-gpl-execution-streamingml/api/1.0.4-SNAPSHOT/#clustree-stream-processor">clusTree</a> *(<a target="_blank" href="https://wso2.github.io/siddhi/documentation/siddhi-4.0/#stream-processors">Stream Processor</a>)*<br><div style="padding-left: 1em;"><p>Performs clustering on a streaming data set. Initially a micro cluster model is generated using  ClusTree algorithm and weighted k-means is applied on micro clusters periodically to generate a macro cluster model with required number of clusters. Data points can be of any dimensionality but the dimensionality should be constant throughout the stream. Euclidean distance is taken as the distance metric. </p></div>
* <a target="_blank" href="https://wso2-extensions.github.io/siddhi-gpl-execution-streamingml/api/1.0.4-SNAPSHOT/#updatehoeffdingtree-stream-processor">updateHoeffdingTree</a> *(<a target="_blank" href="https://wso2.github.io/siddhi/documentation/siddhi-4.0/#stream-processors">Stream Processor</a>)*<br><div style="padding-left: 1em;"><p>Performs build/update of Hoeffding Adaptive Tree for evolving data streams that uses ADWIN to replace branches for new ones.</p></div>
* <a target="_blank" href="https://wso2-extensions.github.io/siddhi-gpl-execution-streamingml/api/1.0.4-SNAPSHOT/#hoeffdingtreeclassifier-stream-processor">hoeffdingTreeClassifier</a> *(<a target="_blank" href="https://wso2.github.io/siddhi/documentation/siddhi-4.0/#stream-processors">Stream Processor</a>)*<br><div style="padding-left: 1em;"><p>Performs classification with Hoeffding Adaptive Tree for evolving data streams that uses ADWIN to replace branches for new ones.</p></div>

## How to Contribute
 
  * Please report issues at <a target="_blank" href="https://github.com/wso2-extensions/siddhi-gpl-execution-streamingml/issues">GitHub Issue Tracker</a>.
  
  * Send your contributions as pull requests to <a target="_blank" href="https://github.com/wso2-extensions/siddhi-gpl-execution-streamingml/tree/master">master branch</a>. 
 
## Contact us 

 * Post your questions with the <a target="_blank" href="http://stackoverflow.com/search?q=siddhi">"Siddhi"</a> tag in <a target="_blank" href="http://stackoverflow.com/search?q=siddhi">Stackoverflow</a>. 
 
 * Siddhi developers can be contacted via the mailing lists:
 
    Developers List   : [dev@wso2.org](mailto:dev@wso2.org)
    
    Architecture List : [architecture@wso2.org](mailto:architecture@wso2.org)
 
## Support 

* We are committed to ensuring support for this extension in production. Our unique approach ensures that all support leverages our open development methodology and is provided by the very same engineers who build the technology. 

* For more details and to take advantage of this unique opportunity contact us via <a target="_blank" href="http://wso2.com/support?utm_source=gitanalytics&utm_campaign=gitanalytics_Jul17">http://wso2.com/support/</a>. 
