# Tabla modelo → código

PPAI Bolsines · Grupo 10 · 3K3 · CU 28 Registrar recepción de bolsín con patrón State.

Cada clase, atributo y método del diseño (E2) con el archivo y la línea donde está en el código. La columna **Visib.** es la visibilidad real en el código (`+` público, `-` privado, `#` protegido). Las rutas son relativas a `src/main/java/bolsines/`. Las filas marcadas AGREGADO no están en el diagrama; el motivo de cada una está en `DECISIONES.md`, que además lista lo que agrega JPA (`id`, `version`, constructores vacíos) y los atributos de apoyo de la pantalla.

## Boundary y control

### PantallaRegistrarRecepcion · «boundary»

Archivo: `pantalla/PantallaRegistrarRecepcion.java`

| Elemento del modelo | Visib. | Línea | Nota |
| --- | --- | --- | --- |
| `cmUsuario` | `-` | 39 |  |
| `listadoBolsinEnviados` | `-` | 40 |  |
| `bolsinSeleccionado` | `-` | 41 |  |
| `nroRemito` | `-` | 42 |  |
| `tipoDocumentacion` | `-` | 43 |  |
| `asuntoDocumentacion` | `-` | 44 |  |
| `opcionesRecepcion` | `-` | 45 |  |
| `opcionSeleccionada` | `-` | 46 |  |
| `+ new()` | `+` | 81 | Paso 1. Con Spring es `opcionRegistrarRecepcion()`: crea el gestor |
| `+ mostrarCmEmpleado()` | `+` | 97 | Paso 2 |
| `+ mostrarCmOrigen()` | `+` | 101 | Paso 4 |
| `+ mostrarNroPrecinto()` | `+` | 105 | Paso 4 |
| `+ solicitarSeleccionarBolsin()` | `+` | 109 | Paso 4 |
| `+ seleccionarBolsin()` | `+` | 142 | Paso 5 |
| `+ mostrarDatosBolsin()` | `+` | 115 | Paso 6 |
| `+ solicitarSeleccionarOpcionRecepcion()` | `+` | 127 | Paso 7 |
| `+ opcionSeleccionada()` | `+` | 168 | Paso 8 |
| `+ solicitarConfirmacion()` | `+` | 132 | Paso 9 |
| `+ confirmacionSeleccion()` | `+` | 213 | Paso 10 |
| `seleccionarDocumentacion()` | `-` | 198 | AGREGADO para A3, A4 y A5 |
| `ver()` | `+` | 235 | AGREGADO: muestra la página del paso actual y recibe el filtro del paso 4 (A1 y A2) |

### GestorRegistrarRecepcion · «control»

Archivo: `gestor/GestorRegistrarRecepcion.java`

| Elemento del modelo | Visib. | Línea | Nota |
| --- | --- | --- | --- |
| `- empleadoLog: Empleado` | `-` | 51 |  |
| `- cmEmpleadoLog: ComisionMedica` | `-` | 52 |  |
| `- bolsinesEnviados: Bolsin[]` | `-` | 53 |  |
| `- bolsinSelec: Bolsin` | `-` | 54 |  |
| `- remito: Remito` | `-` | 55 |  |
| `- documentacion: Documentacion` | `-` | 56 |  |
| `- cmOrigen: String` | `-` | 57 |  |
| `- opcionRegistroSeleccionada: String` | `-` | 58 |  |
| `- fechaHoraActual: DateTime` | `-` | 59 |  |
| `- estadoRecibidoCmDestino: Estado` | `-` | 60 |  |
| `- estadoRecibidoYAceptado: Estado` | `-` | 61 |  |
| `documentacionSeleccionada` | `-` | 64 | AGREGADO para A3, A4 y A5 |
| `+ new()` | `+` | 73 | Constructor. Pasos 2 a 4 |
| `- buscarCmEmpleado()` | `-` | 86 | Paso 2 |
| `- buscarBolsinesEnviados()` | `-` | 95 | Paso 3 |
| `- mostrarBolsinEncontrado()` | `-` | 107 | Paso 4 |
| `+ tomarSeleccionBolsin(bolsin: Bolsin)` | `+` | 120 | Paso 5 |
| `- mostrarBolsinSeleccionado()` | `-` | 128 | Pasos 6 y 7 |
| `+ tomarOpcionSeleccionada(opcion: String)` | `+` | 161 | Pasos 8 y 9 |
| `+ tomarConfirmacion()` | `+` | 175 | Pasos 10 a 12 |
| `- actualizarBolsinRecibido()` | `-` | 182 | Paso 11 |
| `- getFechaActual(): DateTime` | `-` | 210 | Una sola vez por confirmación |
| `- registrarCambioEstadoBolsin()` | `-` | 215 |  |
| `- registrarEstadoRemito()` | `-` | 224 |  |
| `- registrarCambioEstadoDocumentacion()` | `-` | 266 | **Método enganche del State** |
| `- notificarRegistrarRecepcionCMOrigen()` | `-` | 295 | Stub del CU 29 |
| `- finCU()` | `-` | 302 |  |
| `+ tomarSeleccionDocumentacion()` | `+` | 147 | AGREGADO para A3, A4 y A5 |

## Patrón State

### Documentacion · Contexto

Archivo: `dominio/Documentacion.java`

| Elemento del modelo | Visib. | Línea | Nota |
| --- | --- | --- | --- |
| `- asunto: String` | `-` | 38 |  |
| `- fechaPase: DateTime` | `-` | 39 |  |
| `- numero: int` | `-` | 40 |  |
| `tipoDocumento` | `-` | 44 | asociación |
| `cmOrigen` | `-` | 48 | asociación 0..1 (análisis) |
| `cambioEstado` | `-` | 55 | asociación 1..* |
| `estadoActual` | `-` | 63 | asociación con EstadoDocumentacion |
| `+ new()` | `+` | 70 |  |
| `+ getTipoDocumentacion(): String` | `+` | 82 |  |
| `+ getAsunto(): String` | `+` | 86 |  |
| `+ aceptarDocumentacion(fechaHora, empleado)` | `+` | 95 | **Delega en estadoActual** |
| `+ registrarDocumentacionNoRecibida(fechaHora, empleado)` | `+` | 100 | A3. Delega |
| `+ rechazarDocumentacion(fechaHora, empleado)` | `+` | 105 | A4. Delega |
| `+ registrarRedireccion(fechaHora, empleado)` | `+` | 110 | A5. Delega |
| `+ asignarRemito(fechaHora, empleado)` | `+` | 114 | Delega |
| `+ cancelarRemito(fechaHora, empleado)` | `+` | 118 | Delega |
| `+ incorporarEnBolsin(fechaHora, empleado)` | `+` | 122 | Delega |
| `+ quitarDeBolsin(fechaHora, empleado)` | `+` | 126 | Delega |
| `+ enviarBolsin(fechaHora, empleado)` | `+` | 130 | Delega |
| `+ reenviarDocumentacion(fechaHora, empleado)` | `+` | 134 | Delega |
| `+ darDeBaja(fechaHora, empleado)` | `+` | 138 | Delega |
| `+ agregarCambioEstado(ce)` | `+` | 146 | Lo invoca el estado concreto |
| `+ setEstado(estado)` | `+` | 150 | Lo invoca el estado concreto |

### EstadoDocumentacion · Estado abstracto

Archivo: `dominio/estados/EstadoDocumentacion.java`

| Elemento del modelo | Visib. | Línea | Nota |
| --- | --- | --- | --- |
| `# nombre: String` | `#` | 25 |  |
| `# descripcion: String` | `#` | 26 |  |
| `# buscarCEActual(cambioEstado): CambioEstadoDocumentacion` | `#` | 34 | Compartido por todos los estados |
| `+ aceptarDocumentacion(doc, fechaHora, empleado, cambioEstado)` | `+` | 47 | Por defecto lanza excepción |
| `+ asignarRemito(...)` | `+` | 52 | Por defecto lanza excepción |
| `+ cancelarRemito(...)` | `+` | 57 | ídem |
| `+ incorporarEnBolsin(...)` | `+` | 62 | ídem |
| `+ quitarDeBolsin(...)` | `+` | 67 | ídem |
| `+ enviarBolsin(...)` | `+` | 72 | ídem |
| `+ registrarDocumentacionNoRecibida(...)` | `+` | 77 | ídem |
| `+ registrarRedireccion(...)` | `+` | 82 | ídem |
| `+ rechazarDocumentacion(...)` | `+` | 87 | ídem |
| `+ reenviarDocumentacion(...)` | `+` | 92 | ídem |
| `+ darDeBaja(...)` | `+` | 97 | ídem |
| `getNombre()` | `+` | 104 | AGREGADO: el nombre es lo que se guarda en la base |

### EnBolsinEnviado · Estado concreto

Archivo: `dominio/estados/EnBolsinEnviado.java`

| Elemento del modelo | Visib. | Línea | Nota |
| --- | --- | --- | --- |
| `+ aceptarDocumentacion(doc, fechaHora, empleado, cambioEstado)` | `+` | 24 | **Flujo básico: los 6 pasos del pseudocódigo** |
| `+ registrarDocumentacionNoRecibida(...)` | `+` | 36 | A3 → NoRecibida |
| `+ rechazarDocumentacion(...)` | `+` | 50 | A4 → RecibidoYRechazado |
| `+ registrarRedireccion(...)` | `+` | 63 | A5 → ParaRedirigir |
| `- crearProximoEstado(): EstadoDocumentacion` | `-` | 74 | return new RecibidoYAceptado() |
| `- crearCambioEstado(fechaHora, estado, empleado): CambioEstadoDocumentacion` | `-` | 78 |  |

### RecibidoYAceptado · Estado concreto

Archivo: `dominio/estados/RecibidoYAceptado.java`

| Elemento del modelo | Visib. | Línea | Nota |
| --- | --- | --- | --- |
| `+ new()` | `+` | 9 | Destino del flujo básico |

### NoRecibida · Estado concreto

Archivo: `dominio/estados/NoRecibida.java`

| Elemento del modelo | Visib. | Línea | Nota |
| --- | --- | --- | --- |
| `+ new()` | `+` | 9 | Destino de A3 |

### RecibidoYRechazado · Estado concreto

Archivo: `dominio/estados/RecibidoYRechazado.java`

| Elemento del modelo | Visib. | Línea | Nota |
| --- | --- | --- | --- |
| `+ new()` | `+` | 9 | Destino de A4 |

### ParaRedirigir · Estado concreto

Archivo: `dominio/estados/ParaRedirigir.java`

| Elemento del modelo | Visib. | Línea | Nota |
| --- | --- | --- | --- |
| `+ new()` | `+` | 9 | Destino de A5 |

### Registrada · Estado concreto (vacío)

Archivo: `dominio/estados/Registrada.java`

| Elemento del modelo | Visib. | Línea | Nota |
| --- | --- | --- | --- |
| `clase` | `+` | 7 | Hereda la excepción en todo |

### EnRemito · Estado concreto (vacío)

Archivo: `dominio/estados/EnRemito.java`

| Elemento del modelo | Visib. | Línea | Nota |
| --- | --- | --- | --- |
| `clase` | `+` | 7 | ídem |

### EnBolsinSaliente · Estado concreto (vacío)

Archivo: `dominio/estados/EnBolsinSaliente.java`

| Elemento del modelo | Visib. | Línea | Nota |
| --- | --- | --- | --- |
| `clase` | `+` | 7 | ídem |

### DeBaja · Estado concreto (vacío)

Archivo: `dominio/estados/DeBaja.java`

| Elemento del modelo | Visib. | Línea | Nota |
| --- | --- | --- | --- |
| `clase` | `+` | 7 | ídem |

### CambioEstadoDocumentacion · Entidad

Archivo: `dominio/CambioEstadoDocumentacion.java`

| Elemento del modelo | Visib. | Línea | Nota |
| --- | --- | --- | --- |
| `- fechaHoraInicio: DateTime` | `-` | 28 |  |
| `- fechaHoraFin: DateTime` | `-` | 29 |  |
| `estado: EstadoDocumentacion` | `-` | 36 | asociación (ya no apunta a Estado) |
| `responsableCE: Empleado` | `-` | 41 | asociación. Trazabilidad |
| `+ new(fechaHoraInicio, estado, responsable)` | `+` | 48 |  |
| `+ sosActual(): Boolean` | `+` | 55 |  |
| `+ setFechaHoraFin(fechaHoraFin)` | `+` | 59 |  |

## Persistencia del State (infraestructura)

### EstadoDocumentacionConverter · AttributeConverter

Archivo: `persistencia/EstadoDocumentacionConverter.java`

| Elemento del modelo | Visib. | Línea | Nota |
| --- | --- | --- | --- |
| `convertToDatabaseColumn(estado): String` | `+` | 54 | Objeto → nombre (al guardar) |
| `convertToEntityAttribute(nombre): EstadoDocumentacion` | `+` | 63 | Nombre → objeto de la subclase (al leer) |

## Entidades

### Bolsin · Entidad

Archivo: `dominio/Bolsin.java`

| Elemento del modelo | Visib. | Línea | Nota |
| --- | --- | --- | --- |
| `- fecha: DateTime` | `-` | 39 |  |
| `- numeroBolsin: int` | `-` | 40 |  |
| `- numeroPrecinto: int` | `-` | 41 |  |
| `- peso: double` | `-` | 42 |  |
| `remito` | `-` | 49 | asociación 1..* |
| `origen` | `-` | 53 | asociación |
| `destino` | `-` | 57 | asociación |
| `cambioEstadoBolsin` | `-` | 64 | asociación 1..* |
| `+ esTuCM(cm): Boolean` | `+` | 84 | Paso 3 |
| `+ sosEnviado(): Boolean` | `+` | 89 | Paso 3 |
| `+ getCmOrigen(): String` | `+` | 94 | Paso 4 |
| `+ getNroPrecinto(): int` | `+` | 98 | Paso 4 |
| `+ getDatosBolsin(): String[]` | `+` | 107 | Paso 6 |
| `+ actualizarRecibido(fechaHora, estado, empleado)` | `+` | 119 | Paso 11 |
| `- buscarCEActual(): CambioEstadoBolsin` | `-` | 125 |  |
| `- crearCEBolsin(fechaHora, estado, empleado)` | `-` | 134 |  |
| `+ actualizarRecibidoYAceptado(estado)` | `+` | 140 | Paso 11 |
| `+ aceptarDocumentacion(fechaHora, empleado)` | `+` | 147 | Cadena de delegación |
| `getRemito()` | `+` | 154 | AGREGADO: getter de la asociación (alternativos) |

### CambioEstadoBolsin · Entidad

Archivo: `dominio/CambioEstadoBolsin.java`

| Elemento del modelo | Visib. | Línea | Nota |
| --- | --- | --- | --- |
| `fechaHoraInicio` | `-` | 22 |  |
| `fechaHoraFin` | `-` | 23 |  |
| `estado: Estado` | `-` | 27 | asociación |
| `responsableCE: Empleado` | `-` | 32 | asociación. Trazabilidad |
| `new()` | `+` | 39 |  |
| `sosActual()` | `+` | 46 |  |
| `sosEnviado()` | `+` | 50 |  |
| `setFechaHoraFin()` | `+` | 54 |  |

### Remito · Entidad

Archivo: `dominio/Remito.java`

| Elemento del modelo | Visib. | Línea | Nota |
| --- | --- | --- | --- |
| `- fecha: DateTime` | `-` | 31 |  |
| `- numero: int` | `-` | 32 |  |
| `detalleRemito` | `-` | 39 | asociación 1..* |
| `estado` | `-` | 43 | asociación |
| `cmOrigen` | `-` | 47 | asociación |
| `cmDestino` | `-` | 51 | asociación |
| `+ getNumero(): int` | `+` | 67 | Paso 6 |
| `+ getTipoDocumentacion(): String[]` | `+` | 72 | Paso 6 |
| `+ actualizarRecibidoYAceptado(estado)` | `+` | 80 | Paso 11 |
| `- setEstadoRecibidoYAceptado(estado)` | `-` | 84 |  |
| `+ aceptarDocumentacion(fechaHora, empleado)` | `+` | 89 | Cadena de delegación |
| `getDetalleRemito()` | `+` | 96 | AGREGADO: getter de la asociación (alternativos) |

### DetalleRemito · Entidad

Archivo: `dominio/DetalleRemito.java`

| Elemento del modelo | Visib. | Línea | Nota |
| --- | --- | --- | --- |
| `- areaCMCDestino: String` | `-` | 25 |  |
| `documentacion` | `-` | 30 | asociación |
| `+ getTipoDocumentacion(): String` | `+` | 45 | Paso 6 |
| `+ aceptarDocumentacion(fechaHora, empleado)` | `+` | 52 | Cadena de delegación |
| `getDocumentacion()` | `+` | 57 | AGREGADO: modelo de dominio de la cátedra (alternativos) |

### Estado · Entidad (solo bolsín y remito)

Archivo: `dominio/Estado.java`

| Elemento del modelo | Visib. | Línea | Nota |
| --- | --- | --- | --- |
| `nombre` | `-` | 21 |  |
| `descripcion` | `-` | 22 |  |
| `ambito` | `-` | 23 |  |
| `sosEnviado()` | `+` | 43 |  |
| `sosAmbitoBolsin()` | `+` | 35 |  |
| `esRecibidoCmDestino()` | `+` | 47 |  |
| `sosAmbitoRemito()` | `+` | 39 |  |
| `esRecibidoYAceptado()` | `+` | 51 |  |
| `getNombre()` | `+` | 56 | AGREGADO: modelo de dominio de la cátedra (alternativos) |

### ComisionMedica · Entidad

Archivo: `dominio/ComisionMedica.java`

| Elemento del modelo | Visib. | Línea | Nota |
| --- | --- | --- | --- |
| `codigo` | `-` | 18 |  |
| `nombre` | `-` | 19 |  |
| `direccion` | `-` | 20 |  |
| `email` | `-` | 21 |  |
| `telefono` | `-` | 22 |  |
| `getNombre()` | `+` | 36 |  |
| `esTuCmDestino()` | `+` | 49 | Paso 3 |
| `esCMC()` | `+` | 44 | AGREGADO para A5: la opción de redirigir solo se ofrece a la CMC |

### Empleado · Entidad

Archivo: `dominio/Empleado.java`

| Elemento del modelo | Visib. | Línea | Nota |
| --- | --- | --- | --- |
| `apellido` | `-` | 20 |  |
| `nombre` | `-` | 21 |  |
| `email` | `-` | 22 |  |
| `comisionMedica` | `-` | 26 | asociación |
| `getCM()` | `+` | 39 | Paso 2 |

### Usuario · Entidad

Archivo: `dominio/Usuario.java`

| Elemento del modelo | Visib. | Línea | Nota |
| --- | --- | --- | --- |
| `nombre` | `-` | 22 |  |
| `password` | `-` | 23 |  |
| `fechaAlta` | `-` | 24 |  |
| `empleado` | `-` | 28 | asociación |
| `getEmpleado()` | `+` | 41 | Paso 2 |

### Sesion · Entidad

Archivo: `dominio/Sesion.java`

| Elemento del modelo | Visib. | Línea | Nota |
| --- | --- | --- | --- |
| `estaActiva` | `-` | 20 |  |
| `usuario` | `-` | 24 | asociación |
| `getUsuarioLogueado()` | `+` | 36 | Paso 2 |

### TipoDocumento · Entidad

Archivo: `dominio/TipoDocumento.java`

| Elemento del modelo | Visib. | Línea | Nota |
| --- | --- | --- | --- |
| `nombre` | `-` | 18 |  |
| `descripcion` | `-` | 19 |  |
| `getNombre()` | `+` | 30 | Paso 6 |
