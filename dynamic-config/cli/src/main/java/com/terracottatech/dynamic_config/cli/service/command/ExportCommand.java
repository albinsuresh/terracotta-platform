/*
 * Copyright (c) 2011-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in your License Agreement with Software AG.
 */
package com.terracottatech.dynamic_config.cli.service.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.BooleanConverter;
import com.beust.jcommander.converters.PathConverter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.terracottatech.dynamic_config.cli.common.FormatConverter;
import com.terracottatech.dynamic_config.cli.common.InetSocketAddressConverter;
import com.terracottatech.dynamic_config.cli.common.Usage;
import com.terracottatech.dynamic_config.model.Cluster;
import com.terracottatech.dynamic_config.util.PropertiesWriter;
import com.terracottatech.utilities.Json;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

@Parameters(commandNames = "export", commandDescription = "Export the cluster topology")
@Usage("export -s HOST[:PORT] [-d DESTINATION_FILE] [-i]")
public class ExportCommand extends RemoteCommand {

  public enum Format {JSON, PROPERTIES}

  @Parameter(names = {"-s"}, required = true, description = "Node to connect to for topology information", converter = InetSocketAddressConverter.class)
  private InetSocketAddress node;

  @Parameter(names = {"-d"}, description = "Destination directory", converter = PathConverter.class)
  private Path outputFile;

  @Parameter(names = {"-i"}, description = "Ignore default values", converter = BooleanConverter.class)
  private boolean ignoreDefaultValues;

  @Parameter(names = {"-f"}, hidden = true, description = "Output format", converter = FormatConverter.class)
  private Format format = Format.PROPERTIES;

  @Override
  public void validate() {
    if (outputFile != null && Files.exists(outputFile) && !Files.isRegularFile(outputFile)) {
      throw new IllegalArgumentException(outputFile + " is not a file");
    }
  }

  @Override
  public final void run() {
    Cluster cluster = getRemoteTopology(node);
    String output = buildOutput(cluster, format);

    if (outputFile == null) {
      // \n to make sure the content is outputted in one block without any logging prefixes ofr the first line
      logger.info("\n{}", output);

    } else {
      try {
        if (Files.exists(outputFile)) {
          logger.warn(outputFile + " already exists. Replacing this file.");
        } else {
          // try to create the parent directories
          Path dir = outputFile.toAbsolutePath().getParent();
          if (dir != null) {
            Files.createDirectories(dir);
          }
        }
        Files.write(outputFile, output.getBytes(StandardCharsets.UTF_8));
        logger.info("Output saved to: {}\n", outputFile);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
  }

  private String buildOutput(Cluster cluster, Format format) {
    switch (format) {
      case JSON:
        try {
          // shows optional values that are unset
          return Json.copyObjectMapper(true)
              .setSerializationInclusion(JsonInclude.Include.ALWAYS)
              .setDefaultPropertyInclusion(JsonInclude.Include.ALWAYS)
              .writeValueAsString(cluster);
        } catch (JsonProcessingException e) {
          throw new AssertionError(format);
        }
      case PROPERTIES:
        Properties nonDefaults = cluster.toProperties(false, false);
        try (StringWriter out = new StringWriter()) {
          PropertiesWriter.store(out, nonDefaults, "Non-default configurations:");
          if (!this.ignoreDefaultValues) {
            Properties defaults = cluster.toProperties(false, true);
            defaults.keySet().removeAll(nonDefaults.keySet());
            out.write(System.lineSeparator());
            PropertiesWriter.store(out, defaults, "Default configurations:");
          }
          return out.toString();
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      default:
        throw new AssertionError(format);
    }
  }
}
