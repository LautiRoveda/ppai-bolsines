# Decisiones de implementación y flujo del programa

PPAI Bolsines · Grupo 10 · 3K3 · CU 28 Registrar recepción de bolsín con patrón State.

Regla general: **se respetan los diagramas entregados (E2)**: nombres de clases, atributos y métodos, visibilidad y orden de los mensajes. Todo lo que no está en los diagramas figura en este documento, con el motivo.

Stack (según el documento *Framework de Persistencia y Metodología de Implementación*): Java 17+, Spring Boot 3.5, PostgreSQL, Hibernate / Spring Data JPA.

## 1. Flujo básico (opción 1: el contenido es igual al registrado)

1. El EB elige "Registrar recepción de bolsín": `PantallaRegistrarRecepcion.opcionRegistrarRecepcion()` crea el `GestorRegistrarRecepcion` (es el «create» `new()` de la secuencia).
2. `Gestor.new()` → `buscarCmEmpleado()` (Sesion → Usuario → Empleado → ComisionMedica) → `buscarBolsinesEnviados()` (a cada bolsín: `esTuCM(cm)` y `sosEnviado()`) → `mostrarBolsinEncontrado()`.
3. El EB elige un bolsín: `Pantalla.seleccionarBolsin()` → `Gestor.tomarSeleccionBolsin(bolsin)` → `mostrarBolsinSeleccionado()` (`getDatosBolsin()`).
4. El EB elige la opción: `Pantalla.opcionSeleccionada()` → `Gestor.tomarOpcionSeleccionada(opcion)` → la pantalla pide confirmación.
5. El EB confirma: `Pantalla.confirmacionSeleccion()` → `Gestor.tomarConfirmacion()` → `actualizarBolsinRecibido()`:
   - busca el `Estado` del bolsín (`sosAmbitoBolsin()` + `esRecibidoCmDestino()`) y el del remito (`sosAmbitoRemito()` + `esRecibidoYAceptado()`);
   - `getFechaActual()` **una sola vez**;
   - `registrarCambioEstadoBolsin()` → `bolsinSelec.actualizarRecibido(fechaHora, estado, empleado)`;
   - `registrarEstadoRemito()` → `bolsinSelec.actualizarRecibidoYAceptado(estado)`;
   - `registrarCambioEstadoDocumentacion()` → `bolsinSelec.aceptarDocumentacion(fechaHora, empleado)` → Remito → DetalleRemito → Documentacion → **`estadoActual.aceptarDocumentacion(this, fechaHora, empleado, cambioEstado)`** (State);
   - se guarda todo (`bolsinRepositorio.saveAndFlush(bolsinSelec)`);
   - `notificarRegistrarRecepcionCMOrigen()` (stub del CU 29) → `finCU()`.

El gestor **no busca** ningún estado de documentación: lo crea `EnBolsinEnviado`.

## 2. Flujos alternativos

### A3, A4 y A5: cómo se resuelven con lo que hay en el diagrama

El diagrama solo dibuja la cadena Bolsin → Remito → DetalleRemito para `aceptarDocumentacion`. Las operaciones alternativas (`registrarDocumentacionNoRecibida`, `rechazarDocumentacion`, `registrarRedireccion`) están como **públicas en `Documentacion`** y en `EnBolsinEnviado`.

Entonces, en A3, A4 y A5, dentro del mismo método enganche `registrarCambioEstadoDocumentacion()`:

- El gestor recorre remitos → detalles → documentación del bolsín seleccionado.
- A cada documentación **marcada** por el EB le envía `registrarDocumentacionNoRecibida(fechaHora, empleado)` (A3), `rechazarDocumentacion(fechaHora, empleado)` (A4) o `registrarRedireccion(fechaHora, empleado)` (A5).
- A las **no marcadas** les envía `aceptarDocumentacion(fechaHora, empleado)`.
- En los tres casos la `Documentacion` delega en su `estadoActual`: el patrón State se usa igual.

Por qué no se usa `Bolsin.aceptarDocumentacion` en los alternativos: acepta **todas** las documentaciones, y una que ya pasó a `NoRecibida` lanzaría la excepción por defecto del estado.

Los `if` del gestor son sobre **lo que eligió y marcó el usuario**, no sobre el estado de la documentación. `Documentacion` no tiene ningún `if`/`switch`.

### Cómo llega al gestor lo que marcó el EB (agregado al diagrama)

`tomarOpcionSeleccionada(opcion: String)` solo lleva la opción, y ningún método del diagrama lleva la documentación marcada. Se agregó:

- `GestorRegistrarRecepcion.tomarSeleccionDocumentacion(posiciones)`: traduce las posiciones (filas de la tabla, en el orden de `getDatosBolsin()`) a objetos `Documentacion`, recorriendo remitos y detalles, y los guarda en el atributo nuevo `documentacionSeleccionada`.
- `PantallaRegistrarRecepcion.seleccionarDocumentacion(marcadas)` (privado): toma las casillas marcadas del formulario del paso 7 y se las pasa al gestor, justo antes de la opción.

Con la opción 1 las marcas se ignoran. Con las opciones 2, 3 y 4 la pantalla exige al menos una marca válida.

### A5: redirigir a otra área, solo para la CMC

- La regla de negocio de la ERS dice que esto ocurre "cuando la CM destino es la CMC". Por eso el gestor le ofrece la opción 4 **solo si la CM del usuario logueado es la CMC**: en `mostrarBolsinSeleccionado()` pregunta `cmEmpleadoLog.esCMC()`. A las demás CM se les muestran las tres primeras opciones (supuesto del grupo: el paso 7 de la ERS lista las cuatro, pero la cuarta no les aplica).
- `ComisionMedica.esCMC()` es un método **agregado**: no está en el diagrama. Compara el código de la CM con `"CMC"`.
- La documentación marcada pasa a `ParaRedirigir` (`EnBolsinEnviado.registrarRedireccion()`); la demás, a `RecibidoYAceptado`.
- El remito con documentación marcada queda "Recibido y Aceptado Parcial". **La ERS no lo define para A5**; se usa el mismo criterio que en A3 (supuesto del grupo).
- Para la demo hay un segundo usuario, Elena Sosa, de la CMC, y un botón "Cambiar usuario" en el inicio.

### Remito en A3, A4 y A5

- `Remito.actualizarRecibidoYAceptado(estado: Estado)` recibe cualquier `Estado`.
- Al remito que tiene **al menos una documentación marcada**, el gestor le pasa el estado "Recibido y Aceptado Parcial". A los demás remitos del bolsín, "Recibido y Aceptado".
- A3: lo define la regla de negocio de la ERS. A4 y A5: **la ERS no lo define**; se asume el mismo criterio (supuesto del grupo).
- Si se marcan **todas** las documentaciones de un remito, también queda "Recibido y Aceptado Parcial": la ERS no define otro estado para ese caso (supuesto del grupo).

### Estado destino en los alternativos

`crearProximoEstado()` no tiene parámetros y retorna `RecibidoYAceptado`. En `registrarDocumentacionNoRecibida`, `rechazarDocumentacion` y `registrarRedireccion` el estado destino se crea con `new` directo (`new NoRecibida()`, `new RecibidoYRechazado()`, `new ParaRedirigir()`), que es lo que muestran las flechas «create» del diagrama de clases.

### A6: no confirma

En `Pantalla.confirmacionSeleccion()`, si el EB elige "No confirmar" no se llama al gestor y no se modifica nada.

### A1 y A2: el filtro no encuentra el bolsín

- El paso 4 de la ERS dice que el bolsín se elige "por listado o filtrando por alguno de estos datos". El listado tiene un filtro por número de precinto (coincidencia exacta) y por CM origen (alcanza con una parte del nombre).
- Validaciones del filtro: el precinto tiene que ser solo dígitos (hasta 9) y la CM origen solo letras y espacios (hasta 60 caracteres). Se quitan los espacios de más. Un dato mal escrito muestra un mensaje de error y no se usa para filtrar. Si se cargan los dos datos, tienen que coincidir los dos.
- Si ningún bolsín coincide, se muestra "No se encuentra bolsín con el número de precinto ingresado" (A1) o "No se encuentra bolsín con la CM de origen ingresada" (A2).
- **El filtro lo aplica la pantalla** sobre el listado que ya le pasó el gestor (`pasaElFiltro()`). La secuencia de análisis no modela el filtro, así que no hay un mensaje al gestor para esto (supuesto del grupo). Es filtrado de lo que se muestra, no lógica del caso de uso.

La consigna pide el flujo básico y al menos dos alternativos: están los seis (A1 a A6).

## 3. Lo que no está en los diagramas

### En el dominio

| Qué | Para qué | ¿Dónde figura? |
| --- | --- | --- |
| `Bolsin.getRemito()` | que el gestor llegue a los remitos (alternativos) | getter de la asociación `remito` |
| `Remito.getDetalleRemito()` | que el gestor llegue a los detalles (alternativos) | getter de la asociación `detalleRemito` |
| `DetalleRemito.getDocumentacion()` | que el gestor llegue a la documentación (alternativos) | modelo de dominio de la cátedra |
| `Estado.getNombre()` | encontrar "Recibido y Aceptado Parcial" (`Estado` solo tiene `esRecibidoYAceptado()`) | modelo de dominio de la cátedra |
| `ComisionMedica.esCMC()` | ofrecer la opción de redirigir solo a la CMC (A5) | agregado; regla de negocio de la ERS |
| `EstadoDocumentacion.getNombre()` | el nombre es lo que se guarda en la base | getter del atributo `# nombre` |
| `Documentacion.getEstadoActual()`, `getCambioEstado()` y los getters de `CambioEstadoDocumentacion` | consultas de solo lectura; las usan los tests | getters de atributos y asociaciones |
| Atributo `id` (`@Id`) en cada entidad | clave primaria: la exige JPA | persistencia |
| Atributo `version` (`@Version`) en `Bolsin` | que dos usuarios no reciban a la vez el mismo bolsín | persistencia |
| Constructor vacío `protected` en cada entidad | lo exige JPA para reconstruir el objeto | persistencia |
| Constructor `protected EstadoDocumentacion(nombre, descripcion)` y constructor sin parámetros en cada estado | fijan `nombre` y `descripcion` | — |

Respuesta para la defensa: *la vista dinámica modela el flujo básico; los alternativos usan las operaciones públicas de `Documentacion` que están en la vista estática, y el gestor llega a ellas por los getters de las asociaciones, que por convención no se dibujan.*

### En el gestor y la pantalla

- `Gestor.tomarSeleccionDocumentacion(posiciones)` y el atributo `documentacionSeleccionada` (A3, A4 y A5, ver sección 2).
- Constantes `OPCION_...` del gestor: los textos de las cuatro opciones del paso 7.
- `Pantalla.opcionRegistrarRecepcion()`: el paso 1. En la secuencia es un «create» `new()`; ver sección 5.
- `Pantalla.ver()`, `seleccionarDocumentacion()`, `pasaElFiltro()` y los atributos de apoyo para dibujar la página (`cmOrigenBolsines`, `nroPrecintoBolsines`, `cmOrigenSeleccionado`, `nroPrecintoSeleccionado`, `documentacionMarcada`, `mensajeError`, `filtroPrecinto`, `filtroCmOrigen`, `paso`).
- Colaboradores inyectados por Spring: en el gestor, la pantalla y los tres repositorios; en la pantalla, el proveedor de gestores.

### Diferencias con la secuencia de análisis

- **`getNombre()` de la CM del empleado.** En la secuencia de análisis lo pide el empleado; en el diagrama de diseño, `Empleado.getCM()` retorna `ComisionMedica` y el gestor guarda `cmEmpleadoLog: ComisionMedica` (la necesita como objeto para `esTuCM(cm)`). Se sigue el diagrama de diseño, que es el tipado: el `getNombre()` lo pide el gestor.
- **`Bolsin.getCmOrigen()`** llama a `origen.getNombre()`, porque el diseño dice que retorna `String`. Esa flecha no está dibujada en la secuencia.
- **`tomarSeleccionBolsin`** guarda en el atributo `cmOrigen` del gestor la CM origen del bolsín elegido, para la notificación del paso 12.
- **`*sosActual()`**: el bolsín busca su cambio de estado actual dentro de `sosEnviado()` (con su método privado `buscarCEActual()`).

### Detalles de tipos

- `DateTime` es `LocalDateTime`; las colecciones `X[]` son `List<X>`.
- Los estados concretos están en el paquete `bolsines.dominio.estados`.
- La excepción por defecto de los estados es `IllegalStateException` (de Java, no es una clase nueva).
- `getDatosBolsin()` retorna una línea por documentación con el formato `numeroRemito;tipo;asunto` (el asunto va último porque puede contener `;`; el nombre del tipo de documento no puede), armada con `Remito.getNumero()`, `DetalleRemito.getTipoDocumentacion()`, `Documentacion.getTipoDocumentacion()` y `getAsunto()`, como en la secuencia.
- `Sesion.getUsuarioLogueado()` retorna el `Empleado` (le pide `getEmpleado()` a su usuario, como en la secuencia).
- `Documentacion.cmOrigen` (asociación 0..1 del diagrama de análisis) está implementada y tiene su columna, aunque el CU 28 no la usa.

## 4. Otros supuestos

- **Cuándo se guarda.** Al final de `actualizarBolsinRecibido()`, antes de la notificación, con un solo `saveAndFlush` del bolsín dentro de la transacción de `tomarConfirmacion()`. Hasta ese momento los cambios están solo en los objetos. Si el EB no confirma (A6), no se toca la base.
- **CU 29** es un stub: `notificarRegistrarRecepcionCMOrigen()` registra en el log de la consola "notificacion enviada a la CM origen X" (usa el atributo `cmOrigen` del gestor) y la pantalla lo informa. No manda mail. La ERS dice "a cada CM de origen que envió documentación": el detalle de a quiénes y cómo es del CU 29.
- **`ParaRedirigir` queda vacío.** La máquina de estados le da transiciones (`aceptarDocumentacion`, `rechazarDocumentacion`), pero el diseño solo le pone comportamiento a `EnBolsinEnviado`. Lo que pasa después dentro de la CMC es de otro caso de uso.
- **Usuario logueado.** No hay pantalla de login: la sesión activa es la fila de la tabla `sesion` con `esta_activa = TRUE`. Al empezar es Ana Pérez (CM Rosario); el botón "Cambiar usuario" del inicio deja activa la de Elena Sosa (CMC), para A5.
- **Casillas en el paso 6/7** para marcar documentación en A3, A4 y A5: es un agregado de pantalla respecto de la secuencia del flujo básico.

## 5. Cómo se lleva el diseño a Spring Boot

| Capa | Clase del diseño | En el código | Regla |
| --- | --- | --- | --- |
| Boundary | `PantallaRegistrarRecepcion` | `@RestController` + `@SessionScope` | Solo muestra y captura datos y delega en el gestor. Sin lógica de negocio ni acceso a la base. |
| Control | `GestorRegistrarRecepcion` | `@Service` + `@Scope("prototype")`, con `@Transactional` en `tomarConfirmacion()` | Coordina el CU. No decide las transiciones de la documentación. No tiene SQL. |
| Entidades | `Bolsin`, `Remito`, `DetalleRemito`, `Documentacion`, `CambioEstado...`, `Estado`, `ComisionMedica`, `TipoDocumento`, `Empleado`, `Usuario`, `Sesion` | clases `@Entity` | Conservan su comportamiento (`esTuCM()`, `sosEnviado()`, `actualizarRecibido()`, `aceptarDocumentacion()`...). No son entidades anémicas. |
| State | `EstadoDocumentacion` + 9 subclases | clases Java comunes | Sin `@Entity` y sin ninguna anotación de persistencia. |
| Persistencia | — | interfaces `JpaRepository` + `EstadoDocumentacionConverter` | El SQL lo genera Hibernate. |

- **Una pantalla por navegador (`@SessionScope`).** Spring crea la pantalla; por eso el «create» `new()` del paso 1 es el método `opcionRegistrarRecepcion()`, que la deja en blanco.
- **Un gestor por ejecución del CU (`@Scope("prototype")`).** Cada vez que empieza el CU, la pantalla le pide a Spring un gestor nuevo (`nuevoGestor.getObject()`); ese pedido ejecuta el constructor del gestor, que es su `new()` y hace los pasos 2 a 4. El gestor guarda en sus atributos `bolsinSelec`, la opción y la fecha entre una página y la siguiente.
- **"Mostrar" es guardar el dato en un atributo de la pantalla** (`cmUsuario`, `listadoBolsinEnviados`, `nroRemito`, `asuntoDocumentacion`, `tipoDocumentacion`, `opcionesRecepcion`). Después `html()` arma la página.
- **Los métodos de la pantalla son los que atienden al navegador:**

| URL | Método de la pantalla | Paso ERS |
| --- | --- | --- |
| `GET /registrar-recepcion` | `opcionRegistrarRecepcion()` | 1 a 4 |
| `POST /registrar-recepcion/bolsin` | `seleccionarBolsin(posicion)` | 5 a 7 |
| `POST /registrar-recepcion/opcion` | `opcionSeleccionada(opcion, marcadas)` | 8 y 9 |
| `POST /registrar-recepcion/confirmacion` | `confirmacionSeleccion(confirma)` | 10 a 12 (y A6) |
| `GET /registrar-recepcion/ver` | `ver(precinto, cmOrigen)` | muestra la página del paso actual; en el paso 4 recibe el filtro (A1 y A2) |

- **Selección del bolsín:** el formulario manda la posición en el listado y la pantalla le pasa al gestor el objeto `Bolsin` (`tomarSeleccionBolsin(bolsin: Bolsin)`, como en el diagrama).
- **Cada acción verifica en qué paso está** antes de llamar al gestor: reenviar un formulario no registra dos veces. Se puede volver atrás y cambiar el bolsín o la opción antes de confirmar.
- **Si algo falla** (una transición que el estado no admite, u otro usuario que ya recibió el bolsín), la transacción se deshace, se muestra el mensaje (`PaginaDeError`) y el caso de uso en curso se descarta.

## 6. Persistencia

- **PostgreSQL** con **Hibernate / Spring Data JPA**. Tres repositorios (`BolsinRepositorio`, `EstadoRepositorio`, `SesionRepositorio`): los que el CU necesita.
- **Un solo guardado (`saveAndFlush`).** Guardar el bolsín guarda en cascada su historial, sus remitos, los detalles y cada documentación con su historial.
- **Hibernate reescribe filas del historial que no cambiaron.** Como los estados no definen `equals`, al guardar no distingue un estado leído antes de uno igual, y manda un `UPDATE` con los mismos valores. El resultado en la base es correcto. Se prefirió eso antes que agregarle métodos a `EstadoDocumentacion`.
- **State y la base (`EstadoDocumentacionConverter`).** Los estados no son `@Entity` y no tienen tabla. Se guarda el **nombre** del estado en `documentacion.estado_actual` y en `cambio_estado_documentacion.estado` (texto). El conversor (`AttributeConverter<EstadoDocumentacion, String>`, con `autoApply = true`) hace las dos conversiones: objeto → nombre al guardar, y nombre → objeto de la subclase al leer, con un mapa nombre → constructor. Es el único lugar que conoce esa correspondencia; el dominio no tiene ningún `switch`. El conversor lleva además `@Immutable` (de Hibernate): le avisa que un objeto estado no cambia, así no lo copia para detectar modificaciones.
- La tabla `estado` (con `ambito`) es solo para bolsines y remitos.
- **Esquema y datos:** `src/main/resources/db/schema.sql` y `data.sql`. `InicializadorBaseDeDatos` los ejecuta la primera vez que arranca el programa (si la base no tiene las tablas) y con el botón "Reiniciar datos de demo". Hibernate no crea tablas (`ddl-auto=none`).
- **Dos usuarios sobre el mismo bolsín:** `Bolsin` tiene `@Version`. Si dos navegadores reciben el mismo bolsín, el segundo es rechazado con un mensaje y no se guarda nada.
- **El gestor trabaja con lo que leyó al empezar el CU.** Los bolsines, con sus remitos y su documentación, se leen en el paso 3 y quedan en memoria hasta la confirmación. Un cambio hecho a mano por SQL en una documentación después de abrir el listado no se detecta (solo el bolsín tiene `@Version`).
- **Tests:** usan H2 en memoria en modo PostgreSQL, para poder correrlos sin PostgreSQL levantado. La aplicación siempre usa PostgreSQL.

### Clases de infraestructura (no son del modelo)

| Clase | Qué hace |
| --- | --- |
| `BolsinesApplication` | punto de entrada de Spring Boot |
| `persistencia/EstadoDocumentacionConverter` | conversión objeto estado ↔ nombre en la base |
| `persistencia/BolsinRepositorio`, `EstadoRepositorio`, `SesionRepositorio` | repositorios JPA |
| `pantalla/Plantilla` | HTML común a todas las páginas: encabezado, estilos, indicador de pasos e identidad visual (ver `UX_UI.md`) |
| `demo/InicializadorBaseDeDatos` | crea las tablas y carga los datos de demo |
| `demo/HerramientasDemo` | inicio, página `/base` para ver lo guardado, botón de reinicio y botón para cambiar el usuario logueado |
| `demo/PaginaDeError` | muestra el mensaje cuando algo falla |
| `resources/public/error/4xx.html` y `5xx.html` | páginas para direcciones inexistentes y errores |

## 7. Datos de demo

Usuario logueado al empezar: **Ana Pérez**, CM Rosario. Segundo usuario, para A5: **Elena Sosa**, Comisión Médica Central.

| Bolsín | Precinto | Origen → Destino | Estado | Remitos (y números de documentación) | Para |
| --- | --- | --- | --- | --- | --- |
| 105 | 4471 | Córdoba → Rosario | Enviado | remito 301 (9001, 9002), remito 302 (9010) | flujo básico |
| 106 | 4472 | Mendoza → Rosario | Enviado | remito 303 (9020, 9021), remito 304 (9022) | A3 |
| 107 | 4473 | Salta → Rosario | Enviado | remito 305 (9030, 9031), remito 306 (9032, 9033) | A4 |
| 108 | 4480 | Córdoba → Mendoza | Enviado | remitos 307 y 308 | no aparece (otra CM destino) |
| 109 | 4490 | Córdoba → Rosario | Cerrado | remitos 309 y 310 | no aparece (no está enviado) |
| 110 | 4500 | Córdoba → CMC | Enviado | remito 311 (9060, 9061), remito 312 (9062) | A5 (lo ve Elena Sosa) |

Toda la documentación de los bolsines enviados está en `EnBolsinEnviado` con el historial completo: Registrada → EnRemito → EnBolsinSaliente → EnBolsinEnviado.

## Despliegue en un servidor (fuera del CU 28)

- El repositorio trae un `Dockerfile` para publicar el programa en un servidor (Easypanel). No cambia nada del programa: en la PC y en la facultad se sigue corriendo con `mvnw spring-boot:run`.
- En el servidor la conexión a PostgreSQL se pasa por variables de entorno estándar de Spring Boot (`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`), que pisan lo que dice `application.properties`.
- `ClaveDeAcceso` (paquete `demo`) pide usuario y clave en el navegador solo si existe la variable `BOLSINES_CLAVE`. Está porque el programa no tiene login (no es parte del CU 28) y las páginas de demo permiten reiniciar la base. Sin esa variable no hace nada.
