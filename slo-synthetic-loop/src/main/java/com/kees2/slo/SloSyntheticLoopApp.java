package com.kees2.slo;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Callable;

@Command(name = "slo-synthetic-loop", mixinStandardHelpOptions = true)
public class SloSyntheticLoopApp implements Callable<Integer> {

  @Option(names = "--neighborhood", env = "NEIGHBORHOOD_NAME", required = true)
  String neighborhood;

  @Option(names = "--bootstrap-servers", env = "KAFKA_BOOTSTRAP_SERVERS")
  String bootstrap;

  @Option(names = "--latency-threshold-ms", defaultValue = "5000")
  long latencyThresholdMs;

  @Override
  public Integer call() throws Exception {
    if (bootstrap == null || bootstrap.isBlank()) {
      System.err.println("WO-15 stub: set KAFKA_BOOTSTRAP_SERVERS to run live probe");
      return 0;
    }
    String topic = "_slo-probe-" + neighborhood.replace('_', '-');
    String key = UUID.randomUUID().toString();
    long start = System.currentTimeMillis();
    Properties pp = new Properties();
    pp.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
    pp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    pp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    Properties cp = new Properties();
    cp.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
    cp.put(ConsumerConfig.GROUP_ID_CONFIG, "slo-probe-" + neighborhood);
    cp.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    cp.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    cp.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    try (var producer = new KafkaProducer<String, String>(pp);
        var consumer = new KafkaConsumer<String, String>(cp)) {
      consumer.subscribe(List.of(topic));
      producer.send(new ProducerRecord<>(topic, key, "probe")).get();
      long deadline = System.currentTimeMillis() + latencyThresholdMs;
      while (System.currentTimeMillis() < deadline) {
        ConsumerRecords<String, String> recs = consumer.poll(Duration.ofMillis(500));
        if (!recs.isEmpty()) {
          long rt = System.currentTimeMillis() - start;
          System.err.printf("WO-15: roundTripMs=%d%n", rt);
          return 0;
        }
      }
    }
    System.err.println("WO-15: SloProbeFailureException — poll timeout");
    return 1;
  }

  public static void main(String[] args) {
    System.exit(new CommandLine(new SloSyntheticLoopApp()).execute(args));
  }
}
