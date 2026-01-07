$ErrorActionPreference = "Stop"

<#
OnlyCare Admin - Windows Deployment Helper (SSH)

This script runs the standard Laravel production deployment steps on a remote Linux server over SSH.

Prereqs:
- You can SSH to the server (key-based auth recommended)
- The server has PHP + Composer installed
- Your deployed folder is a git repo (or at least has the latest code)

Usage examples:
  powershell -ExecutionPolicy Bypass -File .\deploy_windows.ps1 -Host "your.server.com" -User "root" -Path "/var/www/onlycare_admin" -Branch "main"

If your SSH uses a non-default port:
  powershell -ExecutionPolicy Bypass -File .\deploy_windows.ps1 -Host "your.server.com" -User "root" -Port 2222 -Path "/var/www/onlycare_admin"
#>

param(
  [Parameter(Mandatory=$true)][string]$Host,
  [Parameter(Mandatory=$true)][string]$User,
  [Parameter(Mandatory=$true)][string]$Path,
  [int]$Port = 22,
  [string]$Branch = "main"
)

function Write-Step($msg) { Write-Host "==> $msg" -ForegroundColor Cyan }
function Write-Ok($msg) { Write-Host "OK: $msg" -ForegroundColor Green }

$sshTarget = "$User@$Host"

Write-Step "Deploy target: $sshTarget:$Path (branch: $Branch)"

Write-Step "Running remote deployment commands..."

$remote = @"
set -e
cd "$Path"

echo "PWD: $(pwd)"

if [ -d .git ]; then
  echo "Pulling latest code..."
  git fetch --all
  git checkout "$Branch"
  git pull origin "$Branch"
else
  echo "WARNING: $Path is not a git repo. Skipping git pull."
fi

echo "Installing composer deps..."
composer install --no-dev --optimize-autoloader --no-interaction

echo "Clearing caches..."
php artisan optimize:clear

echo "Running migrations..."
php artisan migrate --force

echo "Rebuilding caches..."
php artisan config:cache
php artisan route:cache
php artisan view:cache

echo "DONE"
"@

# Use ssh.exe (Windows built-in OpenSSH)
& ssh.exe -p $Port $sshTarget $remote

Write-Ok "Deployment finished. Now hard-refresh the browser (Ctrl+F5)."



