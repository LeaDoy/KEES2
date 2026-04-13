package com.kees2.quota;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AlterClientQuotasOptions;
import org.apache.kafka.common.quota.ClientQuotaAlteration;
import org.apache.kafka.common.quota.ClientQuotaEntity;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public final class NeighborhoodQuotaService implements AutoCloseable {

  private final AdminClient admin;

  public NeighborhoodQuotaService(Properties kafkaProps) {
    this.admin = AdminClient.create(kafkaProps);
  }

  public void applyClientQuotas(
      String neighborhoodPrincipal,
      double producerByteRate,
      double consumerByteRate,
      double requestPercentage)
      throws ExecutionException, InterruptedException {
    var entity = new ClientQuotaEntity(Map.of(ClientQuotaEntity.USER, neighborhoodPrincipal));
    Collection<ClientQuotaAlteration.Op> ops =
        List.of(
            new ClientQuotaAlteration.Op("producer_byte_rate", producerByteRate),
            new ClientQuotaAlteration.Op("consumer_byte_rate", consumerByteRate),
            new ClientQuotaAlteration.Op("request_percentage", requestPercentage));
    admin
        .alterClientQuotas(List.of(new ClientQuotaAlteration(entity, ops)), new AlterClientQuotasOptions())
        .all()
        .get();
  }

  @Override
  public void close() {
    admin.close();
  }
}
