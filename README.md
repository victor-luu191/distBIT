# distBIT
This repo contains:
+ source code for distributed implementation of BIT model
+ synthetic datasets
+ results of training distBIT on some synthetic datasets

### Build src code
This is built on top of jbosen, a java implementation of Petuum (parameter server framework, see original paper at http://www.cs.cmu.edu/~epxing/papers/2013/SSPTable_NIPS2013.pdf). Thus you need to get petuum-jbosen from https://github.com/petuum/jbosen/ and build it successfully before building this src.

Below are steps
+ Add jar files in folder libs to build path
+ Import and build src as a Gradle project

### Run the app
Run the app from `main()` method in BrandItemTopic.java. You can run the app using default configs (can be found in `BrandItemTopicConfig.java`) but you __must provide the directory of data__ (via `-dataDir`). You can also reset other arguments (a list of all arguments can be found in `BrandItemTopicConfig.java`) by setting a run configuration in Run menu of Eclipse.

Example configurations:
+ Single machine, 4 workers:  _-clientId 0 -numLocalWorkerThreads 4 -dataDir path-to-your-data_
+ Simulated 2 machines, 4 workers (for future use, currently still not work): first create a hostfile with two lines (127.0.0.1:29000 and 127.0.0.1:29100), they are actually two ports on your machine. Then use this run configuration:  _-clientId 0 -hostFile host.txt -numLocalWorkerThreads 4 -dataDir path-to-your-data_
