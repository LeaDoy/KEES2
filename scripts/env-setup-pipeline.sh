#!/usr/bin/env bash
# WO-1 — Idempotent environment bootstrap (run from repo root with kube context set).
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
echo "WO-1: rendering manifests from ${ROOT}/deploy/base"
if command -v kubectl >/dev/null 2>&1; then
  kubectl kustomize "${ROOT}/deploy/base" >/dev/null
  echo "WO-1: kubectl kustomize OK (apply with: kubectl apply -k ${ROOT}/deploy/base)"
elif command -v kustomize >/dev/null 2>&1; then
  kustomize build "${ROOT}/deploy/base" >/dev/null
  echo "WO-1: kustomize build OK"
else
  echo "WO-1: install kubectl or kustomize to validate/render."
  exit 1
fi
