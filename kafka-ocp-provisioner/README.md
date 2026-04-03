# Kafka OCP provisioner — Confluent for Kubernetes (CFK)

Java CLI that provisions **Apache Kafka in KRaft mode** on **OpenShift** using **Confluent for Kubernetes** (not Strimzi). It applies:

1. **`KRaftController`** (`platform.confluent.io/v1beta1`) — metadata quorum.
2. **`Kafka`** — brokers with `spec.dependencies.kRaftController.clusterRef` pointing at that controller.

Images default to **Confluent Platform** `cp-server` and the **confluent-init-container**; align **`--cp-tag`** and **`--init-tag`** with your [CFK operator version](https://docs.confluent.io/operator/current/co-plan.html) and [Confluent Platform release](https://docs.confluent.io/platform/current/installation/versions-interoperability.html).

## Prerequisites

- JDK 17+, Maven 3.9+
- **Confluent for Kubernetes** operator installed on the cluster (CRDs `kraftcontrollers`, `kafkas`, …)
- Target **namespace** exists; your credentials can create CFK resources there
- Authenticate like `oc` / `kubectl` (kubeconfig or `--master-url` + `--token`)

## Build

```bash
cd kafka-ocp-provisioner
mvn -B package -DskipTests
```

## Parameters

| Option | Required | Description |
|--------|----------|-------------|
| `--namespace` | yes | Target namespace |
| `--size` | yes | **Kafka broker** replicas (`Kafka.spec.replicas`) |
| `--cpu` | yes | CPU requests **and** limits per **broker** |
| `--cluster-name` | no | `Kafka` resource name (default `kafka`) |
| `--kraft-controller-name` | no | `KRaftController` name (default `<cluster-name>-kraft`) |
| `--memory` | no | Broker memory (default `4Gi`) |
| `--controller-replicas` | no | KRaft controllers; **odd**; default **3** (CFK recommends ≥ 3) |
| `--controller-cpu` / `--controller-memory` | no | Controller pod resources (defaults `500m` / `2Gi`) |
| `--cp-tag` | no | CP image tag for `cp-server` (default `7.8.0`) |
| `--init-tag` | no | Init container tag (default `2.9.0`; match your CFK bundle) |
| `--image-registry` | no | Default `docker.io` |
| `--cp-server-image` | no | Full image override for brokers/controllers |
| `--init-image` | no | Full init image override |
| `--kafka-data-volume` / `--controller-data-volume` | no | Volume sizes (default `10Gi`) |
| `--openshift-pod-security` | no | Apply `fsGroup`/`runAsUser` podSecurityContext (default `true`) |

`KRaftController` gets `configOverrides.server` with `default.replication.factor=<size>` per Confluent’s KRaft guidance.

## Example

```bash
java -jar target/kafka-ocp-provisioner-1.0.0-SNAPSHOT.jar \
  --namespace confluent \
  --cluster-name kafka \
  --size 3 \
  --cpu 2 \
  --memory 8Gi \
  --cp-tag 7.8.0 \
  --init-tag 2.9.0
```

```bash
oc get kraftcontrollers,kafka,pods -n confluent
```

## TLS, auth, and production

This generator targets a **minimal internal** stack (no `controllerListener` TLS/auth blocks). For mTLS, SASL, external routes, or Schema Registry dependencies, extend the CRs using [Configure KRaft](https://docs.confluent.io/operator/current/co-configure-kraft.html) and [network encryption](https://docs.confluent.io/operator/current/co-network-encryption.html).

Use **persistent** storage classes instead of defaults for production; tune JVM via `configOverrides.jvm` as in CFK docs.

## OpenShift notes

- If your SCCs reject the default security context, set `--openshift-pod-security=false` and apply an SCC-compatible `podTemplate` pattern from Confluent’s OpenShift guidance.
- Use a pull secret if you pull from `docker.io` or a private registry.
