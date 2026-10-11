# Sync a host-built AgentGo UI dist into local-deployment/ui-dist for Dockerfile.ui.
$ErrorActionPreference = "Stop"
$localDeployment = Split-Path $PSScriptRoot -Parent
$backendRoot = Split-Path $localDeployment -Parent
$workspaceRoot = Split-Path $backendRoot -Parent
$uiRoot = Join-Path $workspaceRoot "AgentGo-UI"
$dist = Join-Path $uiRoot "app\dist"
$stage = Join-Path $localDeployment "ui-dist"
$nginxSrc = Join-Path $uiRoot "docker\nginx.conf"
$nginxDst = Join-Path $localDeployment "ui-nginx.conf"

if (-not (Test-Path (Join-Path $dist "index.html"))) {
    throw "UI dist missing at $dist. Run: `$env:VITE_AGENTGO_BACKEND_URL='http://localhost:20082'; pnpm --dir '$uiRoot' build"
}

Remove-Item -Recurse -Force $stage -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path $stage | Out-Null
Copy-Item -Recurse (Join-Path $dist "*") $stage
Copy-Item $nginxSrc $nginxDst -Force
Write-Host "Synced UI dist -> $stage"
