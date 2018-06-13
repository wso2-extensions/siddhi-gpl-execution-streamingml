# siddhi-gpl-execution-streamingml
The **siddhi-execution-streamingml** is an extension to <a target="_blank" href="https://wso2.github
.io/siddhi">Siddhi</a>  that performs streaming machine learning on event streams.

Find some useful links below:

* <a target="_blank" href="https://github.com/wso2-extensions/siddhi-gpl-execution-streamingml">Source code</a>
* <a target="_blank" href="https://github.com/wso2-extensions/siddhi-gpl-execution-streamingml/releases">Releases</a>
* <a target="_blank" href="https://github.com/wso2-extensions/siddhi-gpl-execution-streamingml/issues">Issue tracker</a>

## Latest API Docs

Latest API Docs is <a target="_blank" href="https://wso2-extensions.github.io/siddhi-gpl-execution-streamingml/api/1.0.15">1.0.15</a>.

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

* <a target="_blank" href="https://wso2-extensions.github.io/siddhi-gpl-execution-streamingml/api/1.0.15/#amrulesregressor-stream-processor">AMRulesRegressor</a> *<a target="_blank" href="https://wso2.github.io/siddhi/documentation/siddhi-4.0/#stream-processor">(Stream Processor)</a>*<br><div style="padding-left: 1em;"><p>This extension performs regression tasks using the <code>AMRulesRegressor</code> algorithm.</p></div>
* <a target="_blank" href="https://wso2-extensions.github.io/siddhi-gpl-execution-streamingml/api/1.0.15/#clustree-stream-processor">clusTree</a> *<a target="_blank" href="https://wso2.github.io/siddhi/documentation/siddhi-4.0/#stream-processor">(Stream Processor)</a>*<br><div style="padding-left: 1em;"><p>This extension performs clustering on a streaming data set. Initially a micro cluster model is generated using the ClusTree algorithm, and weighted k-means is periodically applied to micro clusters to generate a macro cluster model with the required number of clusters. Data points can be of any dimensionality, but the dimensionality should be constant throughout the stream. Euclidean distance is taken as the distance metric.</p></div>
* <a target="_blank" href="https://wso2-extensions.github.io/siddhi-gpl-execution-streamingml/api/1.0.15/#hoeffdingtreeclassifier-stream-processor">hoeffdingTreeClassifier</a> *<a target="_blank" href="https://wso2.github.io/siddhi/documentation/siddhi-4.0/#stream-processor">(Stream Processor)</a>*<br><div style="padding-left: 1em;"><p>This extension performs classification using the Hoeffding Adaptive Tree algorithm for evolving data streams that use <code>ADWIN</code> to replace branches with new ones.</p></div>
* <a target="_blank" href="https://wso2-extensions.github.io/siddhi-gpl-execution-streamingml/api/1.0.15/#updateamrulesregressor-stream-processor">updateAMRulesRegressor</a> *<a target="_blank" href="https://wso2.github.io/siddhi/documentation/siddhi-4.0/#stream-processor">(Stream Processor)</a>*<br><div style="padding-left: 1em;"><p>This extension performs the build/update of the AMRules Regressor model for evolving data streams.</p></div>
* <a target="_blank" href="https://wso2-extensions.github.io/siddhi-gpl-execution-streamingml/api/1.0.15/#updatehoeffdingtree-stream-processor">updateHoeffdingTree</a> *<a target="_blank" href="https://wso2.github.io/siddhi/documentation/siddhi-4.0/#stream-processor">(Stream Processor)</a>*<br><div style="padding-left: 1em;"><p>This extension performs the build/update of Hoeffding Adaptive Tree for evolving data streams that use <code>ADWIN</code> to replace branches for new ones.</p></div>

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
