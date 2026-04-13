# KEES2

Confluent Platform: Kafka Enterprise Event Streams 2nd edittion

## Work order phases (snapshots)

| Phase | Snapshot |
|------|----------|
| 1 | [`work-orders/phase1-snapshot.json`](work-orders/phase1-snapshot.json) |
| 2 | [`work-orders/phase2-snapshot.json`](work-orders/phase2-snapshot.json) |
| 3 | [`work-orders/phase3-snapshot.json`](work-orders/phase3-snapshot.json) |
| 4 | [`work-orders/phase4-snapshot.json`](work-orders/phase4-snapshot.json) |
| 5 | [`work-orders/phase5-snapshot.json`](work-orders/phase5-snapshot.json) |

Kubernetes GitOps layout: [`deploy/README.md`](deploy/README.md). **Kustomize:** `deploy/base`, `deploy/master-cluster/...`, `deploy/phases/phase2` … `phase5`.

**Java:** build all modules with `mvn -B package -DskipTests -f pom.xml` (root [`pom.xml`](pom.xml)).

### Phase 1 (summary)

- **WO-1** — `deploy/base/wo-01-env-setup`, [`scripts/env-setup-pipeline.sh`](scripts/env-setup-pipeline.sh)
- **WO-2** — `deploy/master-cluster/...`, [`gitops/argocd/`](gitops/argocd/)
- **WO-6** — `deploy/base/wo-06-mds-identity`
- **WO-7** — [`service-account-provisioner/`](service-account-provisioner/)
- **WO-10** — `deploy/base/wo-10-cert-manager`
- **WO-11** — `deploy/base/wo-11-external-secrets`, [`secret-rotation-job/`](secret-rotation-job/)
- **WO-14** — `deploy/base/wo-14-logging`

### Phases 2–5 (high level)

- **Phase 2** — CFK deploy pipeline sketch ([`.github/workflows/confluent-deploy-pipeline.yml`](.github/workflows/confluent-deploy-pipeline.yml)), KRaft quorum Job, `configuration-generator`, RBAC/ACL/topic services, PodMonitors — see phase2 snapshot.
- **Phase 3** — Drift CronJob + baseline, Grafana JSON dashboards, mandatory `PrometheusRule`s, SLO CronJobs and tier lag rules — see phase3 snapshot.
- **Phase 4** — Example Neighborhood ACS, registry, quotas/netpol, ClusterLink template, link lag alerts, smoke test Job, provision/decommission CLIs — see phase4 snapshot.
- **Phase 5** — Client bundle CLI, connector allowlist + validator, upgrade compatibility Job — see phase5 snapshot.

## kafka-ocp-provisioner

Java CLI that provisions **Confluent for Kubernetes** KRaft clusters (`KRaftController` + `Kafka`) on **OpenShift**.

See [kafka-ocp-provisioner/README.md](kafka-ocp-provisioner/README.md) for build instructions, parameters, and examples.
