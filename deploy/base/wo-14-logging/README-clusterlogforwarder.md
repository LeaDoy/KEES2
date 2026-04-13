# WO-14 ClusterLogForwarder

Add a `pipelines[].inputRefs` / `namespaceSelector` matching `platform.kees2.io/managed: "true"` per blueprint (OpenShift Logging 5.x+ API varies by version).

Apply `clusterlogforwarder.openshift.yaml` after Splunk indexes and HEC tokens exist.
