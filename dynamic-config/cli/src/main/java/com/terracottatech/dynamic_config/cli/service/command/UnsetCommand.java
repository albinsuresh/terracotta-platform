/*
 * Copyright (c) 2011-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in your License Agreement with Software AG.
 */
package com.terracottatech.dynamic_config.cli.service.command;

import com.beust.jcommander.Parameters;
import com.terracottatech.dynamic_config.cli.common.Usage;
import com.terracottatech.dynamic_config.model.Operation;

/**
 * @author Mathieu Carbou
 */
@Parameters(commandNames = "unset", commandDescription = "Unset properties from the cluster or a node")
@Usage("unset -s HOST -c NAMESPACE1.PROPERTY1 [-c NAMESPACE2.PROPERTY2]...")
public class UnsetCommand extends ConfigurationMutationCommand {
  public UnsetCommand() {
    super(Operation.UNSET);
  }
}
