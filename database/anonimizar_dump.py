# -*- coding: utf-8 -*-
"""
Reemplaza los datos personales de un dump de PostgreSQL por datos inventados.

Uso:
    python anonimizar_dump.py ORIGEN.sql DESTINO.sql

Qué hace:
- Recorre los bloques "COPY tabla (columnas) FROM stdin;" del dump.
- En las tablas con datos personales cambia DNI, nombres, teléfonos, mails,
  direcciones, matrículas y CUIT por valores inventados.
- Mantiene los ids, así no se rompen las relaciones entre tablas.
- A todos los usuarios les pone la contraseña demo "demo1234", ya hasheada con
  el PasswordUtil del proyecto (PBKDF2), para que el login funcione.
"""

import random
import sys

random.seed(7)  # misma semilla = mismos datos inventados en cada corrida

BARRA = chr(92)  # la barra invertida: el dump usa \N para NULL y \. para fin de bloque

NOMBRES = ["Lucía", "Martín", "Sofía", "Tomás", "Valentina", "Mateo", "Camila", "Joaquín",
           "Julieta", "Bruno", "Agustina", "Lautaro", "Florencia", "Nicolás", "Paula", "Facundo"]
APELLIDOS = ["Pérez", "Gómez", "Rodríguez", "Fernández", "López", "Díaz", "Martínez",
             "Sosa", "Romero", "Álvarez", "Torres", "Ruiz", "Benítez", "Molina"]
CALLES = ["San Martín", "Belgrano", "Rivadavia", "Mitre", "Urquiza", "Moreno", "Sarmiento"]


def tel(): return "342000" + str(random.randint(1000, 9999))
def dni(): return str(random.randint(20000000, 49999999))
def fecha(): return f"{random.randint(1950, 2015)}-{random.randint(1, 12):02d}-{random.randint(1, 28):02d}"
def direccion(): return f"{random.choice(CALLES)} {random.randint(100, 4999)}"
def nombre_completo(): return f"{random.choice(NOMBRES)} {random.choice(APELLIDOS)}"


def reglas(hash_demo):
    """Por tabla: qué columna se reemplaza y con qué. 'n' es el número de fila."""
    return {
        "public.paciente": {
            "dni": lambda n: dni(), "nombre": lambda n: random.choice(NOMBRES),
            "apellido": lambda n: random.choice(APELLIDOS), "fechanacimiento": lambda n: fecha(),
            "direccion": lambda n: direccion(), "telefono": lambda n: tel(),
            "email": lambda n: f"paciente{n}@ejemplo.com",
            "numeroafiliado": lambda n: str(random.randint(100000, 999999)),
            "contacto_emergencia": lambda n: tel(),
        },
        "public.personal": {
            "dni": lambda n: dni(), "direccion": lambda n: direccion(),
            "correoelectronico": lambda n: f"personal{n}@ejemplo.com", "telefono": lambda n: tel(),
            "nombreyapellido": lambda n: nombre_completo(),
            "matricula": lambda n: "MP-" + str(random.randint(1000, 9999)),
        },
        "public.usuario": {"contrasena": lambda n: hash_demo},
        "public.obrasocial": {
            "cuit": lambda n: "30-" + str(random.randint(10000000, 99999999)) + "-1",
            "telefono": lambda n: tel(), "mail": lambda n: f"obrasocial{n}@ejemplo.com",
        },
    }


def anonimizar(origen, destino, hash_demo):
    tabla_reglas = reglas(hash_demo)
    salida, tabla, columnas, n = [], None, [], 0
    for linea in open(origen, encoding="utf-8"):
        if linea.startswith("COPY "):
            tabla = linea.split()[1]
            columnas = [c.strip() for c in linea[linea.index("(") + 1:linea.index(")")].split(",")]
            n = 0
        elif tabla and linea.startswith(BARRA + "."):
            tabla = None
        elif tabla in tabla_reglas:
            n += 1
            campos = linea.rstrip("\n").split("\t")
            for columna, generar in tabla_reglas[tabla].items():
                i = columnas.index(columna)
                if campos[i] != BARRA + "N":      # si era NULL, queda NULL
                    campos[i] = generar(n)
            linea = "\t".join(campos) + "\n"
        salida.append(linea)
    open(destino, "w", encoding="utf-8", newline="\n").write("".join(salida))


if __name__ == "__main__":
    hash_demo = open(sys.argv[3]).read().strip() if len(sys.argv) > 3 else None
    if not hash_demo:
        sys.exit("Falta el archivo con el hash de la contraseña demo (3er argumento)")
    anonimizar(sys.argv[1], sys.argv[2], hash_demo)
    print("Listo:", sys.argv[2])
