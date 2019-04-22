/*
 * Copyright (c) 2011-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in your License Agreement with Software AG.
 */
package com.terracottatech.dynamic_config.config;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class Node {
  private String nodeName;
  private String nodeHostname;
  private int nodePort;
  private int nodeGroupPort;
  private String nodeBindAddress;
  private String nodeGroupBindAddress;
  private Path nodeConfigDir;
  private Path nodeMetadataDir;
  private Path nodeLogDir;
  private Path nodeBackupDir;
  private Path securityDir;
  private Path securityAuditLogDir;
  private String securityAuthc;
  private boolean securitySslTls;
  private boolean securityWhitelist;
  private String failoverPriority;
  private String clientReconnectWindow;
  private String clientLeaseDuration;
  private Map<String, String> offheapResources = new HashMap<>();
  private Map<String, Path> dataDirs = new HashMap<>();
  private String clusterName;

  public String getNodeName() {
    return nodeName;
  }

  public String getNodeHostname() {
    return nodeHostname;
  }

  public int getNodePort() {
    return nodePort;
  }

  public int getNodeGroupPort() {
    return nodeGroupPort;
  }

  public String getNodeBindAddress() {
    return nodeBindAddress;
  }

  public String getNodeGroupBindAddress() {
    return nodeGroupBindAddress;
  }

  public Path getNodeConfigDir() {
    return nodeConfigDir;
  }

  public Path getNodeMetadataDir() {
    return nodeMetadataDir;
  }

  public Path getNodeLogDir() {
    return nodeLogDir;
  }

  public Path getNodeBackupDir() {
    return nodeBackupDir;
  }

  public Path getSecurityDir() {
    return securityDir;
  }

  public Path getSecurityAuditLogDir() {
    return securityAuditLogDir;
  }

  public String getSecurityAuthc() {
    return securityAuthc;
  }

  public boolean isSecuritySslTls() {
    return securitySslTls;
  }

  public boolean isSecurityWhitelist() {
    return securityWhitelist;
  }

  public String getFailoverPriority() {
    return failoverPriority;
  }

  public String getClientReconnectWindow() {
    return clientReconnectWindow;
  }

  public String getClientLeaseDuration() {
    return clientLeaseDuration;
  }

  public Map<String, String> getOffheapResources() {
    return Collections.unmodifiableMap(offheapResources);
  }

  public Map<String, Path> getDataDirs() {
    return Collections.unmodifiableMap(dataDirs);
  }

  public String getClusterName() {
    return clusterName;
  }

  public void setNodeName(String nodeName) {
    this.nodeName = nodeName;
  }

  public void setNodeHostname(String nodeHostname) {
    this.nodeHostname = nodeHostname;
  }

  public void setNodePort(int nodePort) {
    this.nodePort = nodePort;
  }

  public void setNodeGroupPort(int nodeGroupPort) {
    this.nodeGroupPort = nodeGroupPort;
  }

  public void setNodeBindAddress(String nodeBindAddress) {
    this.nodeBindAddress = nodeBindAddress;
  }

  public void setNodeGroupBindAddress(String nodeGroupBindAddress) {
    this.nodeGroupBindAddress = nodeGroupBindAddress;
  }

  public void setNodeConfigDir(Path nodeConfigDir) {
    this.nodeConfigDir = nodeConfigDir;
  }

  public void setNodeMetadataDir(Path nodeMetadataDir) {
    this.nodeMetadataDir = nodeMetadataDir;
  }

  public void setNodeLogDir(Path nodeLogDir) {
    this.nodeLogDir = nodeLogDir;
  }

  public void setNodeBackupDir(Path nodeBackupDir) {
    this.nodeBackupDir = nodeBackupDir;
  }

  public void setSecurityDir(Path securityDir) {
    this.securityDir = securityDir;
  }

  public void setSecurityAuditLogDir(Path securityAuditLogDir) {
    this.securityAuditLogDir = securityAuditLogDir;
  }

  public void setSecurityAuthc(String securityAuthc) {
    this.securityAuthc = securityAuthc;
  }

  public void setSecuritySslTls(boolean securitySslTls) {
    this.securitySslTls = securitySslTls;
  }

  public void setSecurityWhitelist(boolean securityWhitelist) {
    this.securityWhitelist = securityWhitelist;
  }

  public void setFailoverPriority(String failoverPriority) {
    this.failoverPriority = failoverPriority;
  }

  public void setClientReconnectWindow(String clientReconnectWindow) {
    this.clientReconnectWindow = clientReconnectWindow;
  }

  public void setClientLeaseDuration(String clientLeaseDuration) {
    this.clientLeaseDuration = clientLeaseDuration;
  }

  public void setOffheapResource(String name, String quantity) {
    this.offheapResources.put(name, quantity);
  }

  public void setDataDir(String name, Path path) {
    this.dataDirs.put(name, path);
  }

  public void setClusterName(String clusterName) {
    this.clusterName = clusterName;
  }

  @Override
  public String toString() {
    return "Node{" +
        "nodeName='" + nodeName + '\'' +
        ", nodeHostname='" + nodeHostname + '\'' +
        ", nodePort=" + nodePort +
        ", nodeGroupPort=" + nodeGroupPort +
        ", nodeBindAddress='" + nodeBindAddress + '\'' +
        ", nodeGroupBindAddress='" + nodeGroupBindAddress + '\'' +
        ", nodeConfigDir='" + nodeConfigDir + '\'' +
        ", nodeMetadataDir='" + nodeMetadataDir + '\'' +
        ", nodeLogDir='" + nodeLogDir + '\'' +
        ", nodeBackupDir='" + nodeBackupDir + '\'' +
        ", securityDir='" + securityDir + '\'' +
        ", securityAuditLogDir='" + securityAuditLogDir + '\'' +
        ", securityAuthc='" + securityAuthc + '\'' +
        ", securitySslTls=" + securitySslTls +
        ", securityWhitelist=" + securityWhitelist +
        ", failoverPriority='" + failoverPriority + '\'' +
        ", clientReconnectWindow=" + clientReconnectWindow +
        ", clientLeaseDuration=" + clientLeaseDuration +
        ", offheapResources=" + offheapResources +
        ", dataDirs=" + dataDirs +
        ", clusterName='" + clusterName + '\'' +
        '}';
  }
}
