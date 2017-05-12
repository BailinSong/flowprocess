# in finishing.

# Shell command
```
java  -DUser_home=<LogPath> -DLogger=<LoggerClass> -DLoggerConfig=<LoggerConfigFilePath> -DNodeConfig=<ProcessNodeConfigFilePath> -DCluster=<ClusterName>  -DNodeId=<ProcessNodeId> [-DAction=<start DEFAULT>|<stop>|<exit>] -jar simple-flow-0.0.1-SNAPSHOT.one-jar.jar
```

# start
```
java  -DUser_home=./Log -DLogger=com.flowprocess.cedf.components.log.logger.Log4j2 -DLoggerConfig=./conf/log4j2.xml -DNodeConfig=./conf/ProcessNode/Null.xml -DCluster=simple -DNodeId=simple1 -jar simple-flow-0.0.1-SNAPSHOT.one-jar.jar
```
# stop
```
java  -DUser_home=./Log -DLogger=com.flowprocess.cedf.components.log.logger.Log4j2 -DLoggerConfig=./conf/log4j2.xml -DNodeConfig=./conf/ProcessNode/Null.xml -DCluster=simple -DNodeId=simple1 -DAction=stop -jar simple-flow-0.0.1-SNAPSHOT.one-jar.jar
```