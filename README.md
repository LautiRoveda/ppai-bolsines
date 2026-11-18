# PPAI Bolsines · CU 28 Registrar recepción de bolsín (patrón State)

UTN FRC · Diseño de Sistemas de Información · Curso 3K3 · Grupo 10 · Entrega 2 (implementación).

Implementa el **CU 28 Registrar recepción de bolsín** con el **patrón State** aplicado a `Documentacion`, tal como está en el diseño entregado (`PPAI2026_3K3_G10_E2_Diseño.pdf`).

## Tecnología

| Dato de la consigna | Qué se usó |
| --- | --- |
| Lenguaje | Java (17 o superior) |
| Framework | Spring Boot 3.5 |
| Tecnología | Aplicación web (HTML armado del lado del servidor) |
| Base de datos | PostgreSQL |
| Framework de persistencia | Hibernate / Spring Data JPA |

Otras herramientas: Maven (con Maven Wrapper, no hace falta instalarlo) y JUnit 5 para los tests.

## Qué hace falta instalar

1. **JDK 17 o superior.** Verificar con `java -version`.
2. **PostgreSQL** corriendo en esta máquina, puerto 5432.

## Preparar la base (una sola vez)

Crear una base vacía llamada `bolsines`. Desde `psql` o pgAdmin:

```sql
CREATE DATABASE bolsines;
```

El programa se conecta con usuario `postgres` y clave `postgres`. Si tu PostgreSQL usa otros datos, cambialos en `src/main/resources/application.properties` (las tres líneas `spring.datasource...`).

Las tablas y los datos de demo los crea el programa solo, la primera vez que arranca.

Alternativa con Docker (levanta PostgreSQL con la base ya creada): `docker compose up -d`.

## Cómo correr

Desde la carpeta del proyecto, en PowerShell:

```
.\mvnw.cmd spring-boot:run
```

En CMD es igual pero sin `.\` (`mvnw.cmd spring-boot:run`). La primera vez baja Maven y las dependencias y tarda unos minutos; necesita internet.

Cuando aparece `PPAI Bolsines listo`, abrir en el navegador:

**http://localhost:8080**

Para detenerlo: `Ctrl + C` en la terminal.

## Tests

```
.\mvnw.cmd test
```

No necesitan PostgreSQL: usan una base H2 en memoria. Son 42 tests:

- `EstadoDocumentacionTest`: el patrón State (aceptar desde `EnBolsinEnviado`, los alternativos, y la excepción desde los demás estados).
- `EstadoDocumentacionConverterTest`: el estado se guarda por nombre y se lee como su subclase.
- `RegistrarRecepcionTest`: el CU de punta a punta (flujo básico, A1 a A6, validaciones, dos usuarios sobre el mismo bolsín, rollback).
- `DatosDeDemoTest`: esquema, datos de demo y reinicio.

## Qué se puede probar

Usuario logueado al empezar: Ana Pérez, Encargada de Bolsines de la CM Rosario.

| Flujo | Usuario | Bolsín (precinto) | Qué hacer |
| --- | --- | --- | --- |
| A1 y A2: el filtro no encuentra el bolsín | Ana Pérez | ninguno | En el listado, filtrar por un precinto o una CM origen que no existan |
| Básico | Ana Pérez (CM Rosario) | 4471 (CM Córdoba) | Opción 1 y confirmar |
| A3: no se recibe toda la documentación | Ana Pérez | 4472 (CM Mendoza) | Marcar documentación, opción 2 y confirmar |
| A4: documentación que no corresponde | Ana Pérez | 4473 (CM Salta) | Marcar documentación, opción 3 y confirmar |
| A5: redirigir a otra área | Elena Sosa (CMC) | 4500 (CM Córdoba) | Cambiar usuario, marcar documentación, opción 4 y confirmar |
| A6: no confirma | cualquiera | cualquiera | "No confirmar" en el último paso |

En el inicio hay tres herramientas para la demo: **Ver la base de datos** (estados e historiales tal como están guardados en PostgreSQL), **Cambiar usuario** (simula que se loguea la usuaria de la Comisión Médica Central, o vuelve a Ana Pérez) y **Reiniciar datos de demo** (deja la base como al principio).

El paso a paso para la defensa está en `docs/GUIA_DEFENSA.md`.

## Estructura

```
src/main/java/bolsines/
  pantalla/      PantallaRegistrarRecepcion («boundary»)
  gestor/        GestorRegistrarRecepcion («control»)
  dominio/       entidades: Bolsin, Remito, DetalleRemito, Documentacion, CambioEstado..., Estado, ...
  dominio/estados/   patrón State: EstadoDocumentacion y sus 9 estados concretos
  persistencia/  repositorios JPA y EstadoDocumentacionConverter
  demo/          herramientas de la demo (no son del modelo)
src/main/resources/
  application.properties   conexión a PostgreSQL
  db/schema.sql            tablas
  db/data.sql              datos de demo
docs/
  DECISIONES.md            flujo del programa, supuestos y todo lo que no está en los diagramas
  TABLA_MODELO_CODIGO.md   cada clase y método del diseño con su archivo y línea
  GUIA_DEFENSA.md          pasos para la demo
  UX_UI.md                 patrones de GUI aplicados y qué traer del trabajo de UX hecho en el aula
  BASE_DE_DATOS.md         las 12 tablas, sus columnas y relaciones
```

## Dónde está el patrón State

| Rol | Clase |
| --- | --- |
| Contexto | `dominio/Documentacion.java`: tiene `estadoActual` y delega en él. Sin `if` ni `switch` sobre el estado. |
| Estado abstracto | `dominio/estados/EstadoDocumentacion.java`: las 11 operaciones, que por defecto lanzan excepción, y `buscarCEActual()`. |
| Estados concretos | `dominio/estados/`: `EnBolsinEnviado` (el único con comportamiento en el CU 28) y los otros 8. |
| Enganche | `GestorRegistrarRecepcion.registrarCambioEstadoDocumentacion()` |
| Persistencia | `persistencia/EstadoDocumentacionConverter.java`: el estado se guarda por nombre, sin tabla propia. |

## Supuestos y clases agregadas

Están todos en `docs/DECISIONES.md`. Los principales:

- En A3, A4 y A5 queda en "Recibido y Aceptado Parcial" solo el remito que tiene documentación marcada. Para A4 y A5 la ERS no lo define: es un supuesto del grupo.
- El CU 29 (notificar la recepción) es un stub: escribe la notificación en la consola.
- La opción 4 (redirigir a otra área, A5) solo se le ofrece a la Comisión Médica Central, como dice la regla de negocio.
- Clases que no son del modelo: los repositorios JPA, el conversor del estado, `Plantilla` y el paquete `demo`.

## Problemas frecuentes

| Qué se ve | Causa y solución |
| --- | --- |
| `Connection to localhost:5432 refused` | PostgreSQL no está corriendo. Iniciar el servicio. |
| `FATAL: database "bolsines" does not exist` | Falta crear la base (ver "Preparar la base"). |
| `FATAL: password authentication failed` | Usuario o clave distintos: cambiarlos en `application.properties`. |
| `Port 8080 was already in use` | Quedó otra copia del programa abierta (cerrar esa terminal), o cambiar `server.port` en `application.properties`. |
| `mvnw.cmd` no se reconoce (PowerShell) | Falta el `.\` adelante: `.\mvnw.cmd spring-boot:run`. |
| No baja las dependencias | La primera corrida necesita internet. Después se puede arrancar sin red con `.\mvnw.cmd -o spring-boot:run`. |
