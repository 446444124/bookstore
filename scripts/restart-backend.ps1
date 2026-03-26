# 停止占用 8080 / 8090 的进程后，在后台启动 store-main 与 store-api。
# 用法：在项目根目录执行 .\scripts\restart-backend.ps1
#Requires -Version 5.1
$ErrorActionPreference = 'SilentlyContinue'
$root = Resolve-Path (Join-Path $PSScriptRoot '..')

function Stop-ListenPort {
    param([int]$Port)
    try {
        Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue | ForEach-Object {
            $pid = $_.OwningProcess
            if ($pid) {
                Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
                Write-Host "Stopped PID $pid on port $Port"
            }
        }
    } catch {
        # 旧环境无 Get-NetTCPConnection 时忽略
    }
}

Write-Host "== Bookstore: restart backend (8080 + 8090) =="
Set-Location $root

Write-Host "Stopping listeners on 8080, 8090..."
Stop-ListenPort -Port 8080
Stop-ListenPort -Port 8090
Start-Sleep -Seconds 2

$mvn = Get-Command mvn -ErrorAction SilentlyContinue
if (-not $mvn) {
    Write-Error "Maven (mvn) not found in PATH."
    exit 1
}

$mainCmd = "Set-Location '$root\store-main'; mvn spring-boot:run"
$apiCmd = "Set-Location '$root\store-api'; mvn spring-boot:run"

Start-Process -FilePath "powershell" -ArgumentList @(
    '-NoProfile', '-ExecutionPolicy', 'Bypass',
    '-WindowStyle', 'Hidden',
    '-Command', $mainCmd
) -WorkingDirectory $root

Start-Process -FilePath "powershell" -ArgumentList @(
    '-NoProfile', '-ExecutionPolicy', 'Bypass',
    '-WindowStyle', 'Hidden',
    '-Command', $apiCmd
) -WorkingDirectory $root

Write-Host "Started store-main (8080) and store-api (8090) in background. Wait ~30-60s for Tomcat."
Write-Host "Logs: check new PowerShell processes or run mvn spring-boot:run in two terminals for live output."
