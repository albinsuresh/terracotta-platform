/*
 * Copyright Terracotta, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terracotta.dynamic_config.cli.api.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terracotta.common.struct.Tuple2;
import org.terracotta.diagnostic.client.connection.DiagnosticServices;
import org.terracotta.diagnostic.model.LogicalServerState;
import org.terracotta.dynamic_config.api.model.Cluster;
import org.terracotta.dynamic_config.api.model.Configuration;
import org.terracotta.dynamic_config.api.model.Node.Endpoint;
import org.terracotta.dynamic_config.api.model.Operation;
import org.terracotta.dynamic_config.api.model.PropertyHolder;
import org.terracotta.dynamic_config.api.model.UID;
import org.terracotta.dynamic_config.api.model.nomad.MultiSettingNomadChange;
import org.terracotta.dynamic_config.api.model.nomad.SettingNomadChange;
import org.terracotta.dynamic_config.api.service.ClusterValidator;

import java.util.Collection;
import java.util.Map;
import java.util.TreeSet;

import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.toList;
import static org.terracotta.dynamic_config.api.model.Requirement.CLUSTER_ONLINE;
import static org.terracotta.dynamic_config.api.model.Requirement.CLUSTER_RESTART;
import static org.terracotta.dynamic_config.api.model.Requirement.NODE_RESTART;
import static org.terracotta.dynamic_config.api.model.Scope.NODE;

public abstract class ConfigurationMutationAction extends ConfigurationAction {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationMutationAction.class);

  protected ConfigurationMutationAction(Operation operation) {
    super(operation);
  }

  @Override
  public void run() {
    validate();
    if (configurations.size() == 0) {
      // no remaining configuration changes left to process
      output.info("Command successful!");
      return;
    }
    LOGGER.debug("Validating the new configuration change(s) against the topology of: {}", node);

    // get the remote topology, apply the parameters, and validate that the cluster is still valid
    Cluster originalCluster = getUpcomingCluster(node);
    Cluster updatedCluster = originalCluster.clone();

    // will keep track of the targeted nodes for the changes of a node setting
    Collection<String> nodesRequiringRestart = new TreeSet<>();
    Collection<String> targetedNodes = new TreeSet<>();

    // applying the set/unset operation to the cluster in memory for validation
    for (Configuration c : configurations) {
      Collection<? extends PropertyHolder> targets = c.apply(updatedCluster);

      // keep track of the node targeted by this configuration line
      // remember: a node setting can be set using a cluster or stripe namespace to target several nodes at once
      // Note: this validation is only for node-specific settings
      targets.stream()
          .filter(o -> o.getScope() == NODE)
          .map(PropertyHolder::getName)
          .filter(originalCluster::containsNode)
          .forEach(name -> {
            targetedNodes.add(name);
            if (c.getSetting().requires(NODE_RESTART)) {
              nodesRequiringRestart.add(name);
            }
          });
    }
    if (updatedCluster.equals(originalCluster)) {
      LOGGER.warn(lineSeparator() +
          "=======================================================================================" + lineSeparator() +
          "The requested update will not result in any change to the cluster configuration." + lineSeparator() +
          "=======================================================================================" + lineSeparator());
      return;
    }
    new ClusterValidator(updatedCluster).validate();

    // get the current state of the nodes
    // this call can take some time and we can have some timeout
    Map<Endpoint, LogicalServerState> onlineNodes = findOnlineRuntimePeers(node);
    LOGGER.debug("Online nodes: {}", onlineNodes);

    boolean allOnlineNodesActivated = areAllNodesActivated(onlineNodes.keySet());
    if (allOnlineNodesActivated) {
      licenseValidation(node, updatedCluster);
    }

    // ensure that the nodes targeted by the set or unset command in the namespaces are all online so that they can validate the change
    Collection<String> missingTargetedNodes = new TreeSet<>(targetedNodes);
    onlineNodes.keySet().stream().map(Endpoint::getNodeName).forEach(missingTargetedNodes::remove);
    if (!missingTargetedNodes.isEmpty()) {
      throw new IllegalStateException("Some nodes that are targeted by the change are not reachable and thus cannot be validated. " +
          "Please ensure these nodes are online, or remove them from the request: " + toString(missingTargetedNodes));
    }

    LOGGER.debug("New configuration change(s) can be sent");

    if (allOnlineNodesActivated) {
      // cluster is active, we need to run a nomad change and eventually a restart

      // validate that all the online nodes are either actives or passives
      ensureNodesAreEitherActiveOrPassive(onlineNodes);

      if (requiresAllNodesAlive()) {
        // Check passive nodes as well if the setting requires all nodes to be online
        ensurePassivesAreAllOnline(originalCluster, onlineNodes);
      }

      ensureActivesAreAllOnline(originalCluster, onlineNodes);
      output.info("Applying new configuration change(s) to activated nodes: {}", toString(onlineNodes.keySet()));
      MultiSettingNomadChange changes = getNomadChanges(updatedCluster);
      if (!changes.getChanges().isEmpty()) {
        runConfigurationChange(updatedCluster, onlineNodes, changes);
      }

      // do we need to restart to apply the changes ?
      if (changes.getChanges().stream().map(SettingNomadChange::getSetting).anyMatch(setting -> setting.requires(CLUSTER_RESTART))) {
        LOGGER.warn(lineSeparator() +
            "====================================================================" + lineSeparator() +
            "IMPORTANT: A restart of the cluster is required to apply the changes" + lineSeparator() +
            "====================================================================" + lineSeparator());

      } else {
        final long numberOfDifferentSettingsToChange = configurations.stream()
            .map(Configuration::getSetting)
            .distinct()
            .count();
        final long numberOfDifferentSettingsRequiringRestart = configurations.stream()
            .map(Configuration::getSetting)
            .distinct()
            .filter(setting -> setting.requires(NODE_RESTART))
            .count();

        if (numberOfDifferentSettingsToChange != numberOfDifferentSettingsRequiringRestart) {
          // if the user updates more than 1 setting that does not require a restart, or a mix of settings which
          // do and do not require a restart, we will go there.
          // I.e. set backup-dir
          // I.e. set log-dir + backup-dir
          // In that case, we might already have some nodes inside nodesRequiringRestart.
          // But we need to add into this collection the other nodes that are targeted AND could have vetoed a change to be applied at runtime
          for (Endpoint endpoint : onlineNodes.keySet()) {
            try {
              if (targetedNodes.contains(endpoint.getNodeName()) && mustBeRestarted(endpoint)) {
                nodesRequiringRestart.add(endpoint.getNodeName());
              }
            } catch (RuntimeException e) {
              // some nodes might have failed over and not be reachable anymore
              LOGGER.warn("Node " + endpoint + " is not reachable anymore: {}", e.getMessage(), e);
            }
          }
        }

        if (!nodesRequiringRestart.isEmpty()) {
          LOGGER.warn(lineSeparator() +
              "=======================================================================================" + lineSeparator() +
              "IMPORTANT: A restart of nodes: " + toString(nodesRequiringRestart) + " is required to apply the changes" + lineSeparator() +
              "=======================================================================================" + lineSeparator());

        }
      }

    } else {
      // cluster is not active, we just need to replace the topology
      output.info("Applying new configuration change(s) to nodes: {}", toString(onlineNodes.keySet()));
      try (DiagnosticServices<UID> diagnosticServices = multiDiagnosticServiceProvider.fetchOnlineDiagnosticServices(endpointsToMap(onlineNodes.keySet()))) {
        dynamicConfigServices(diagnosticServices)
            .map(Tuple2::getT2)
            .forEach(dynamicConfigService -> dynamicConfigService.setUpcomingCluster(updatedCluster));
      }
    }

    output.info("Command successful!");
  }

  private MultiSettingNomadChange getNomadChanges(Cluster cluster) {
    // MultiSettingNomadChange will apply to whole change set given by the user as an atomic operation
    return new MultiSettingNomadChange(configurations.stream()
        .map(configuration -> {
          configuration.validate(clusterState, operation);
          return SettingNomadChange.fromConfiguration(configuration, operation, cluster);
        })
        .collect(toList()));
  }

  private boolean requiresAllNodesAlive() {
    return configurations.stream().map(Configuration::getSetting).anyMatch(setting -> setting.requires(CLUSTER_ONLINE));
  }
}
