param(
    [Parameter(Mandatory=$true)][string]$Archivo,
    [string]$HostBD = $(if ($env:REYSALUD_DB_HOST) { $env:REYSALUD_DB_HOST } else { 'localhost' }),
    [string]$Puerto = $(if ($env:REYSALUD_DB_PORT) { $env:REYSALUD_DB_PORT } else { '5432' }),
    [string]$Base = $(if ($env:REYSALUD_DB_NAME) { $env:REYSALUD_DB_NAME } else { 'clinica2' }),
    [string]$Usuario = $(if ($env:REYSALUD_DB_USER) { $env:REYSALUD_DB_USER } else { 'postgres' })
)

$ErrorActionPreference = 'Stop'
$archivoResuelto = (Resolve-Path -LiteralPath $Archivo).Path
$confirmacion = Read-Host "Se reemplazará el contenido de '$Base' usando '$archivoResuelto'. Escriba RESTAURAR"
if ($confirmacion -cne 'RESTAURAR') { throw 'Restauración cancelada.' }

& pg_restore --host=$HostBD --port=$Puerto --username=$Usuario --dbname=$Base --clean --if-exists --single-transaction $archivoResuelto
if ($LASTEXITCODE -ne 0) { throw "pg_restore terminó con código $LASTEXITCODE" }
Write-Host 'Restauración completada.'
