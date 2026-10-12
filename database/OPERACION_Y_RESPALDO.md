# Operación de ReySalud

## Dos o más computadoras

Todas las instalaciones deben conectarse a una única instancia PostgreSQL. En cada equipo cliente se configuran `REYSALUD_DB_HOST`, `REYSALUD_DB_PORT`, `REYSALUD_DB_NAME`, `REYSALUD_DB_USER`, `REYSALUD_DB_PASSWORD` y `REYSALUD_DB_SCHEMA`. Los cambios quedan disponibles para el otro equipo al volver a cargar la pantalla; la aplicación no usa sincronización por archivos.

## Respaldo

Ejecutar diariamente `database/backup_reysalud.ps1`. El script usa `pg_dump` en formato custom y crea archivos fechados dentro de `database/backups` salvo que se indique otro destino. Copiar al menos una copia fuera de la computadora principal.

## Recuperación

Cerrar ReySalud en todos los equipos y ejecutar `database/restore_reysalud.ps1 -Archivo <ruta>`. La palabra `RESTAURAR` se solicita de forma explícita antes de reemplazar datos. Probar la recuperación periódicamente en una base de pruebas; un backup no verificado no debe considerarse una recuperación garantizada.

## Recomendación mínima

- Backup automático diario y retención de 30 días.
- Una copia local y otra externa o cifrada.
- PostgreSQL accesible solo desde la red privada.
- Usuario de aplicación sin permisos de administración del servidor.
- Prueba mensual de restauración.
