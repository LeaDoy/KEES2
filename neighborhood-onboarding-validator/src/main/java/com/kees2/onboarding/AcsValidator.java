package com.kees2.onboarding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class AcsValidator {

  private AcsValidator() {}

  /** @return 0 if valid, 1 if invalid (messages on stderr). */
  public static int validate(Path acsFile) throws Exception {
    var mapper = new ObjectMapper(new YAMLFactory());
    JsonNode n = mapper.readTree(Files.readString(acsFile));
    List<String> errors = new ArrayList<>();
    require(n, "neighborhoodName", errors);
    require(n, "dataCenter", errors);
    require(n, "sizingProfile", errors);
    require(n, "status", errors);
    if (!"APPROVED".equals(text(n, "status"))) {
      errors.add("status must be APPROVED");
    }
    JsonNode po = n.path("platformOwnerApproval");
    if (!po.hasNonNull("approvedBy") || !po.hasNonNull("approvedAt")) {
      errors.add("platformOwnerApproval.approvedBy and approvedAt required");
    }
    JsonNode no = n.path("neighborhoodOwnerApproval");
    if (!no.hasNonNull("approvedBy") || !no.hasNonNull("approvedAt")) {
      errors.add("neighborhoodOwnerApproval.approvedBy and approvedAt required");
    }
    JsonNode cap = n.path("capacityAssessment");
    require(cap, "peakProducerThroughputMBps", errors);
    require(cap, "peakConsumerThroughputMBps", errors);
    require(cap, "estimatedPartitionCount", errors);
    require(cap, "storageGrowthGbPerMonth", errors);
    require(cap, "consumerGroupCount", errors);
    require(cap, "applicationCount", errors);
    if ("KEES1_0".equals(text(n, "migrationSource"))) {
      if (!n.path("currentEstateDocumented").asBoolean(false)) {
        errors.add("currentEstateDocumented must be true for KEES1_0");
      }
      if (!n.path("impactAnalysisComplete").asBoolean(false)) {
        errors.add("impactAnalysisComplete must be true for KEES1_0");
      }
    }
    if (!errors.isEmpty()) {
      errors.forEach(System.err::println);
      return 1;
    }
    System.err.println("WO-17: ACS validation passed.");
    return 0;
  }

  private static void require(JsonNode parent, String field, List<String> errors) {
    if (parent == null || !parent.has(field) || parent.get(field).isNull()) {
      errors.add("Missing required field: " + field);
    }
  }

  private static String text(JsonNode parent, String field) {
    return parent.path(field).asText("");
  }
}
