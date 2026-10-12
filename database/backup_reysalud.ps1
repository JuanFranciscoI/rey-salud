param(
    [string]$Destino = (Join-Path $PSScriptRoot 'backups'),
    [string]$HostBD = $(if ($env:REYSALUD_DB_HOST) { $env:REYSALUD_DB_HOST } else { 'localhost' }),
    [string]$Puerto = $(if ($env:REYSALUD_DB_PORT) { $env:REYSALUD_DB_PORT } else { '5432' }),
    [string]$Base = $(if ($env:REYSALUD_DB_NAME) { $env:REYSALUD_DB_NAME } else { 'clinica2' }),
    [string]$Usuario = $(if ($env:REYSALUD_DB_USER) { $env:REYSALUD_DB_USER } else { 'postgres' })
)

$ErrorActionPreference = 'Stop'
$destinoResuelto = [System.IO.Path]::GetFullPath($Destino)
New-Item -ItemType Directory -Force -Path $destinoResuelto | Out-Null
$marca = Get-Date -Format 'yyyyMMdd_HHmmss'
$archivo = Join-Path $destinoResuelto "reysalud_${marca}.backup"

& pg_dump --host=$HostBD --port=$Puerto --username=$Usuario --format=custom --file=$archivo $Base
if ($LASTEXITCODE -ne 0) { throw "pg_dump terminó con código $LASTEXITCODE" }
Write-Host "Backup creado: $archivo"
