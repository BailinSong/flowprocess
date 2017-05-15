# In finishing.

# Shell command
``` sh
java  -DUser_home=<LogPath> -DLogger=<LoggerClass> -DLoggerConfig=<LoggerConfigFilePath> -DNodeConfig=<ProcessNodeConfigFilePath> -DCluster=<ClusterName>  -DNodeId=<ProcessNodeId> [-DAction=<start DEFAULT>|<stop>|<exit>] -jar flowprocess
```


# Example

## example start 
```
java  -DUser_home=./Log -DLogger=com.blueline.flowprocess.components.log.logger.Log4j2 -DLoggerConfig=./conf/log4j2.xml -DNodeConfig=./conf/ProcessNode/Null.xml -DCluster=simple -DNodeId=simple1 -jar flowprocess
```
## example stop
```
java  -DUser_home=./Log -DLogger=com.blueline.flowprocess.components.log.logger.Log4j2 -DLoggerConfig=./conf/log4j2.xml -DNodeConfig=./conf/ProcessNode/Null.xml -DCluster=simple -DNodeId=simple1 -DAction=stop -jar flowprocess
```

## example exit
```
java  -DUser_home=./Log -DLogger=com.blueline.flowprocess.components.log.logger.Log4j2 -DLoggerConfig=./conf/log4j2.xml -DNodeConfig=./conf/ProcessNode/Null.xml -DCluster=simple -DNodeId=simple1 -DAction=exit -jar flowprocess
```

