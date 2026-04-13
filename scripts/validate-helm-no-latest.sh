#!/usr/bin/env bash
# WO-3 — Reject floating "latest" image tags in deploy/gitops YAML.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
if grep -RInE 'tag:\s*latest\b|image:.*:latest\b' "$ROOT/deploy" "$ROOT/gitops" 2>/dev/null; then
  echo "WO-3: disallowed latest reference found"
  exit 1
fi
echo "WO-3: no 'latest' tag patterns found under deploy/ and gitops/."
