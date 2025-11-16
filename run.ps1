$ErrorActionPreference = 'Stop'

function Test-PortListening {
    param(
        [int]$Port,
        [int]$TimeoutSec = 120
    )
    $deadline = (Get-Date).AddSeconds($TimeoutSec)
    while ((Get-Date) -lt $deadline) {
        try {
            $client = New-Object System.Net.Sockets.TcpClient
            $iar = $client.BeginConnect('127.0.0.1', $Port, $null, $null)
            $wait = $iar.AsyncWaitHandle.WaitOne(1000, $false)
            if ($wait -and $client.Connected) {
                $client.Close()
                return $true
            }
            $client.Close()
        } catch {
            # ignore and retry
        }
    }
    return $false
}

function Ensure-Command {
    param([string]$Name)
    $null -ne (Get-Command $Name -ErrorAction SilentlyContinue)
}

Write-Host "Starting Billing Software (backend + frontend)..." -ForegroundColor Cyan

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$backendDir = Join-Path $root 'billingsoftware'
$frontendDir = Join-Path $root 'client'

if (-not (Test-Path $backendDir)) { throw "Backend directory not found: $backendDir" }
if (-not (Test-Path $frontendDir)) { throw "Frontend directory not found: $frontendDir" }

# Start backend
Push-Location $backendDir
try {
    if (-not (Ensure-Command 'java')) { throw 'Java runtime (java) not found in PATH.' }

    $mvnwPath = if (Test-Path (Join-Path $backendDir 'mvnw.cmd')) { Join-Path $backendDir 'mvnw.cmd' } else { Join-Path $backendDir 'mvnw' }
    $backendOutLog = Join-Path $backendDir 'backend.out.log'
    $backendErrLog = Join-Path $backendDir 'backend.err.log'
    Write-Host "Launching backend (Spring Boot) on port 8082..." -ForegroundColor Yellow
    Start-Process -FilePath $mvnwPath -WorkingDirectory $backendDir -ArgumentList 'spring-boot:run' -NoNewWindow -RedirectStandardOutput $backendOutLog -RedirectStandardError $backendErrLog -PassThru | Out-Null
} finally {
    Pop-Location
}

if (-not (Test-PortListening -Port 8082 -TimeoutSec 180)) {
    Write-Host "Backend did not start on port 8082 in time. See logs: $backendOutLog , $backendErrLog" -ForegroundColor Red
    exit 1
}
Write-Host "Backend is up at http://localhost:8082" -ForegroundColor Green

# Start frontend
Push-Location $frontendDir
try {
    if (-not (Test-Path (Join-Path $frontendDir 'node_modules'))) {
        if (-not (Ensure-Command 'npm')) { throw 'npm not found in PATH.' }
        Write-Host 'Installing frontend dependencies (npm install)...' -ForegroundColor Yellow
        $npmCmd = (Get-Command npm.cmd -ErrorAction SilentlyContinue).Path
        if (-not $npmCmd) { $npmCmd = (Get-Command npm -ErrorAction SilentlyContinue).Path }
        if (-not $npmCmd) { throw 'Unable to resolve npm executable path.' }
        Start-Process -FilePath $npmCmd -WorkingDirectory $frontendDir -ArgumentList 'install','--silent' -NoNewWindow -Wait | Out-Null
    }
    $frontendOutLog = Join-Path $frontendDir 'frontend.out.log'
    $frontendErrLog = Join-Path $frontendDir 'frontend.err.log'
    Write-Host 'Launching frontend (Vite)...' -ForegroundColor Yellow
    $npmRunCmd = (Get-Command npm.cmd -ErrorAction SilentlyContinue).Path
    if (-not $npmRunCmd) { $npmRunCmd = (Get-Command npm -ErrorAction SilentlyContinue).Path }
    if (-not $npmRunCmd) { throw 'Unable to resolve npm executable path.' }
    $vite = Start-Process -FilePath $npmRunCmd -WorkingDirectory $frontendDir -ArgumentList 'run','dev' -NoNewWindow -RedirectStandardOutput $frontendOutLog -RedirectStandardError $frontendErrLog -PassThru
} finally {
    Pop-Location
}

# Wait for Vite default port 5173 (or a nearby open port)
$portsToTry = 5173..5200
$frontendPort = $null
$deadline = (Get-Date).AddSeconds(180)
while ((Get-Date) -lt $deadline -and -not $frontendPort) {
    foreach ($p in $portsToTry) {
        if (Test-PortListening -Port $p -TimeoutSec 1) { $frontendPort = $p; break }
    }
}
if (-not $frontendPort) {
    Write-Host "Frontend did not start in time. See logs: $frontendOutLog , $frontendErrLog" -ForegroundColor Red
    exit 1
}

Write-Host "Frontend is up at http://localhost:$frontendPort" -ForegroundColor Green

# Open browser
try {
    Start-Process "http://localhost:$frontendPort"
} catch {}

Write-Host 'All set! Press Ctrl+C in this window to stop.' -ForegroundColor Cyan

