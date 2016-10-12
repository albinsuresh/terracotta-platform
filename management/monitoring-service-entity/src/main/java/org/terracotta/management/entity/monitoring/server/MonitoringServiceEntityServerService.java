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
package org.terracotta.management.entity.monitoring.server;

import org.terracotta.entity.ServiceRegistry;
import org.terracotta.management.entity.monitoring.MonitoringServiceProxy;
import org.terracotta.management.service.monitoring.MonitoringService;
import org.terracotta.management.service.monitoring.MonitoringServiceConfiguration;
import org.terracotta.voltron.proxy.SerializationCodec;
import org.terracotta.voltron.proxy.server.ProxyServerEntityService;

/**
 * @author Mathieu Carbou
 */
public class MonitoringServiceEntityServerService extends ProxyServerEntityService<Void> {
  public MonitoringServiceEntityServerService() {
    super(MonitoringServiceProxy.class, Void.class, new SerializationCodec());
  }

  @Override
  public MonitoringServiceEntity createActiveEntity(ServiceRegistry registry, Void config) {
    MonitoringService monitoringService = registry.getService(new MonitoringServiceConfiguration());
    return new MonitoringServiceEntity(monitoringService);
  }

  @Override
  public long getVersion() {
    return 1;
  }

  @Override
  public boolean handlesEntityType(String typeName) {
    return "org.terracotta.management.entity.monitoring.client.MonitoringServiceProxyEntity".equals(typeName);
  }

}