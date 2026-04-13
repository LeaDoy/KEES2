# KEES2 deploy layout

- **`base/`** — Shared Phase 1 manifests (WO-1, WO-6, WO-10, WO-11, WO-14), composed by `base/kustomization.yaml`.
- **`master-cluster/`** — WO-2 overlays per datacenter and environment (`dc1-virginia|dc2-texas` × `dev|staging|prod`). Each overlay references `base` plus a CFK stub and readiness `Job`.
- **`../gitops/argocd/`** — Example `Application` objects pointing at this repo; set `spec.destination.server` per cluster.

Apply one overlay per OpenShift cluster to avoid duplicate cluster-scoped names. Install CFK, cert-manager, External Secrets, and OpenShift Logging CRDs before syncing. Tune `deploy/base/wo-14-logging/clusterlogforwarder.openshift.yaml` and Splunk HEC secrets for your environment.
