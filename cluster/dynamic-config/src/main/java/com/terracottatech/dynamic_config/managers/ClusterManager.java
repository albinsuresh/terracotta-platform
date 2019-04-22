/*
 * Copyright (c) 2011-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in your License Agreement with Software AG.
 */
package com.terracottatech.dynamic_config.managers;


import com.terracottatech.dynamic_config.config.Cluster;
import com.terracottatech.dynamic_config.parsing.ConfigFileParser;
import com.terracottatech.dynamic_config.parsing.ConsoleParamsParser;

import java.io.File;
import java.util.Map;

public class ClusterManager {
  public static Cluster createCluster(String configFile) {
    return ConfigFileParser.parse(new File(configFile));
  }

  public static Cluster createCluster(Map<String, String> paramValueMap) {
    return ConsoleParamsParser.parse(paramValueMap);
  }
}
