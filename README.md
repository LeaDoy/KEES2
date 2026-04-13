# KEES2

Confluent Platform: Kafka Enterprise Event Streams 2nd edittion

## Phase 1 (work orders)

Kubernetes GitOps manifests live under [`deploy/`](deploy/README.md). A JSON map of Phase 1 work orders to paths is in [`work-orders/phase1-snapshot.json`](work-orders/phase1-snapshot.json).

- **WO-1** — `deploy/base/wo-01-env-setup`, [`scripts/env-setup-pipeline.sh`](scripts/env-setup-pipeline.sh)
- **WO-2** — `deploy/master-cluster/...`, [`gitops/argocd/`](gitops/argocd/)
- **WO-6** — `deploy/base/wo-06-mds-identity`
- **WO-7** — [`service-account-provisioner/`](service-account-provisioner/)
- **WO-10** — `deploy/base/wo-10-cert-manager`
- **WO-11** — `deploy/base/wo-11-external-secrets`, [`secret-rotation-job/`](secret-rotation-job/)
- **WO-14** — `deploy/base/wo-14-logging` (optional `clusterlogforwarder.openshift.yaml`)

## kafka-ocp-provisioner

Java CLI that provisions **Confluent for Kubernetes** KRaft clusters (`KRaftController` + `Kafka`) on **OpenShift**.

See [kafka-ocp-provisioner/README.md](kafka-ocp-provisioner/README.md) for build instructions, parameters, and examples.
