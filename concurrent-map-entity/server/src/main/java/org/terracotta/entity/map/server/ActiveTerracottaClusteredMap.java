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
package org.terracotta.entity.map.server;

import org.terracotta.entity.ActiveServerEntity;
import org.terracotta.entity.ClientDescriptor;
import org.terracotta.entity.ConcurrencyStrategy;
import org.terracotta.entity.PassiveSynchronizationChannel;
import org.terracotta.entity.ServiceRegistry;
import org.terracotta.entity.map.common.KeyBasedMapOperation;
import org.terracotta.entity.map.common.MapOperation;
import org.terracotta.entity.map.common.MapResponse;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


class ActiveTerracottaClusteredMap extends AbstractClusteredMap implements ActiveServerEntity<MapOperation, MapResponse>  {

  private static final int CONCURRENCY_KEY = 42;

  public ActiveTerracottaClusteredMap(ServiceRegistry services) {
    super(services);
  }

  @Override
  public void connected(ClientDescriptor clientDescriptor) {
  }

  @Override
  public void handleReconnect(ClientDescriptor clientDescriptor, byte[] extendedReconnectData) {
    // Do nothing.
  }

  @Override
  public void disconnected(ClientDescriptor clientDescriptor) {
  }

  @Override
  public MapResponse invoke(ClientDescriptor clientDescriptor, MapOperation input) {
    return super.invokeInternal(input);
  }

  public static class MapConcurrencyStrategy implements ConcurrencyStrategy<MapOperation> {

    private final int bucketCount;

    public MapConcurrencyStrategy(int bucketCount) {
      this.bucketCount = bucketCount;
    }

    @Override
    public int concurrencyKey(MapOperation operation) {
      if (operation instanceof KeyBasedMapOperation) {
        KeyBasedMapOperation keyBasedMapOperation = (KeyBasedMapOperation) operation;
        return (CONCURRENCY_KEY + Math.abs(keyBasedMapOperation.getKey().hashCode() % bucketCount));
      }

      return CONCURRENCY_KEY;
    }

    @Override
    public Set<Integer> getKeysForSynchronization() {
      Set<Integer> result = new HashSet<Integer>();
      for (int i = 0; i < bucketCount; i++ ) {
        result.add(CONCURRENCY_KEY + i);
      }

      return Collections.unmodifiableSet(result);
    }
  }

  @Override
  public void synchronizeKeyToPassive(PassiveSynchronizationChannel<MapOperation> syncChannel, int concurrencyKey) {
    if (concurrencyKey < CONCURRENCY_KEY) {
      throw new IllegalArgumentException("concurrencyKey should only be " + CONCURRENCY_KEY);
    }

    syncChannel.synchronizeToPassive(new SyncOperation(dataMap));
  }
}
