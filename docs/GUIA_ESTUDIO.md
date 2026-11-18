# Guía de estudio · CU 28 con patrón State

Grupo 10 · 3K3 · DSI 2026. Para leer antes de la defensa del viernes 20-11-26.

Esta guía explica **qué hicimos y por qué**, de cero, para que cualquiera del grupo pueda contestar. El paso a paso de la demo (qué tocar y qué mostrar) está en `GUIA_DEFENSA.md`. Las líneas exactas de cada método están en `TABLA_MODELO_CODIGO.md`.

Si tenés cinco minutos, leé las secciones 3, 5 y 12. Si tenés media hora, leé todo con el código abierto al lado.

---

## 1. El caso de uso, en criollo

Las comisiones médicas (CM) se mandan documentación entre sí por correo, adentro de un **bolsín**: una bolsa cerrada con un **precinto** numerado. Adentro del bolsín van uno o más **remitos**, y cada remito lista la **documentación** que viaja (expedientes, estudios, cartas documento).

El **CU 28 Registrar recepción de bolsín** es lo que hace el **Encargado de Bolsines (EB)** de la comisión de destino cuando el bolsín llega:

1. Entra a la opción. El sistema le muestra su comisión médica.
2. El sistema busca los bolsines **en estado Enviado** cuyo **destino es su comisión** y los lista con la CM de origen y el número de precinto.
3. El EB elige un bolsín.
4. El sistema muestra los remitos y la documentación de ese bolsín, y las opciones de recepción.
5. El EB elige una opción y confirma.
6. El sistema registra: el bolsín pasa a **Recibido en CM destino**, los remitos a **Recibido y Aceptado**, y cada documentación a **RecibidoYAceptado**. Todo con la misma fecha y hora y con el empleado responsable.
7. El sistema notifica a la CM de origen (eso es el CU 29, que el nuestro incluye).

Las **alternativas** que implementamos:

| Alternativa | Qué pasa | Cómo queda la documentación marcada |
| --- | --- | --- |
| A1 | No hay bolsín con el precinto buscado | No se registra nada, se avisa |
| A2 | No hay bolsín con la CM de origen buscada | No se registra nada, se avisa |
| A3 | No llegó toda la documentación | `NoRecibida` |
| A4 | Hay documentación que no era para esta CM | `RecibidoYRechazado` |
| A5 | Hay documentación que hay que redirigir (solo la CMC) | `ParaRedirigir` |
| A6 | El EB no confirma | No se registra nada |

En A3, A4 y A5 la documentación **no marcada** se acepta igual que en el flujo básico, y el remito que tiene algo marcado queda en **Recibido y Aceptado Parcial**.

La consigna pide el flujo básico y al menos dos alternativas. Tenemos las seis.

---

## 2. El problema que resuelve el patrón

Una documentación pasa por muchos estados a lo largo de su vida: se registra, se pone en un remito, entra a un bolsín, se envía, se recibe, se rechaza, se redirige, se da de baja. Son **9 estados**, y lo que se puede hacer con ella **depende de en qué estado está**: una documentación que todavía está `Registrada` no puede "aceptarse", porque nunca viajó.

Sin el patrón, cada operación de `Documentacion` tendría que preguntar por el estado:

```java
// ASÍ NO LO HICIMOS
public void aceptarDocumentacion(...) {
    if (estado.equals("EnBolsinEnviado")) {
        // cerrar el cambio de estado, crear el nuevo, pasar a RecibidoYAceptado
    } else if (estado.equals("Registrada")) {
        throw new IllegalStateException(...);
    } else if (estado.equals("EnRemito")) {
        ...
    }
}
```

Con 9 estados y 11 operaciones, eso es una clase llena de `if` repetidos. Cada estado nuevo obliga a tocar todas las operaciones, y es fácil olvidarse de un caso.

**El patrón State saca esos `if` y los convierte en clases.** Cada estado es una clase que sabe qué puede hacer la documentación cuando está en ese estado y a qué estado pasa después.

---

## 3. El patrón State en una página

State es un patrón de comportamiento (GoF). Tiene tres roles:

| Rol | Qué hace | En nuestro proyecto |
| --- | --- | --- |
| **Contexto** | El objeto cuyo comportamiento cambia según su estado. Tiene una referencia al estado actual y **le delega** las operaciones. | `Documentacion` |
| **Estado abstracto** | Declara todas las operaciones que dependen del estado. | `EstadoDocumentacion` |
| **Estados concretos** | Uno por estado. Cada uno implementa las operaciones que **ese** estado admite y decide cuál es el estado siguiente. | `EnBolsinEnviado`, `RecibidoYAceptado` y los otros 7 |

Tres ideas que hay que poder decir:

1. **El contexto no pregunta en qué estado está.** Solo dice "estado actual, hacé esto". No hay `if` ni `switch` sobre el estado.
2. **La transición la decide el estado concreto**, no el gestor ni la documentación. `EnBolsinEnviado` es el que sabe que después de aceptar viene `RecibidoYAceptado`.
3. **Agregar un estado es agregar una clase**, sin tocar las que ya existen.

```
                     delega                    hereda
   Documentacion  ----------->  EstadoDocumentacion  <|----  EnBolsinEnviado
   (contexto)                   (abstracta)          <|----  RecibidoYAceptado
   estadoActual                 11 operaciones       <|----  NoRecibida
   cambioEstado[]               buscarCEActual()     <|----  ... (9 en total)
```

---

## 4. Las clases del patrón en nuestro código

Todo está en `src/main/java/bolsines/`.

### 4.1 `dominio/estados/EstadoDocumentacion.java` (estado abstracto)

- Tiene `nombre` y `descripcion`.
- Declara las **11 operaciones** de transición: `aceptarDocumentacion`, `asignarRemito`, `cancelarRemito`, `incorporarEnBolsin`, `quitarDeBolsin`, `enviarBolsin`, `registrarDocumentacionNoRecibida`, `registrarRedireccion`, `rechazarDocumentacion`, `reenviarDocumentacion`, `darDeBaja`.
- Todas reciben lo mismo: `(doc, fechaHora, empleado, cambioEstado)`.
- **Por defecto, todas lanzan una excepción** ("La documentación no puede aceptarse en este estado"). Es la clave: un estado que no sobrescribe una operación la rechaza solo.
- Tiene `buscarCEActual()`, protegido, que usan todos los estados: recorre el historial y devuelve el cambio de estado que no tiene fecha de fin.

**Por qué es clase abstracta y no interfaz:** porque tiene comportamiento por defecto (la excepción), atributos comunes (`nombre`, `descripcion`) y un método compartido (`buscarCEActual`). Una interfaz obligaría a cada uno de los 9 estados a escribir las 11 operaciones.

### 4.2 `dominio/estados/EnBolsinEnviado.java` (el estado concreto que trabaja)

Es el estado en que está la documentación cuando el bolsín ya salió. Es **el único estado con comportamiento** en nuestro caso de uso, porque el CU 28 siempre parte de ahí. Sobrescribe 4 operaciones:

| Operación | Estado al que pasa | Cuándo |
| --- | --- | --- |
| `aceptarDocumentacion` | `RecibidoYAceptado` | Flujo básico, y documentación no marcada en A3, A4, A5 |
| `registrarDocumentacionNoRecibida` | `NoRecibida` | A3 |
| `rechazarDocumentacion` | `RecibidoYRechazado` | A4 |
| `registrarRedireccion` | `ParaRedirigir` | A5 |

Las otras 7 operaciones las hereda con la excepción.

### 4.3 Los otros 8 estados

`Registrada`, `EnRemito`, `EnBolsinSaliente`, `RecibidoYAceptado`, `RecibidoYRechazado`, `NoRecibida`, `ParaRedirigir`, `DeBaja`. Son clases con solo el constructor: no sobrescriben nada, así que heredan la excepción en todo.

**Por qué están vacíos:** su comportamiento corresponde a otros casos de uso (armar remito, armar bolsín, enviar), que no nos tocó implementar. Están porque el diseño los tiene y porque la base de datos guarda documentación en esos estados. Cuando se implementen esos CU, se completa cada clase sin tocar las demás.

### 4.4 `dominio/Documentacion.java` (contexto)

Tiene dos atributos del patrón:

- `estadoActual`: el objeto estado en que está ahora.
- `cambioEstado`: la lista con el historial (`CambioEstadoDocumentacion`: estado, fecha de inicio, fecha de fin, empleado).

Cada operación es **una sola línea** que delega:

```java
public void aceptarDocumentacion(LocalDateTime fechaHora, Empleado empleado) {
    estadoActual.aceptarDocumentacion(this, fechaHora, empleado, cambioEstado);
}
```

Se pasa a sí misma (`this`) para que el estado pueda devolverle el resultado con dos métodos:

- `agregarCambioEstado(ce)`: suma una fila al historial.
- `setEstado(estado)`: cambia el estado actual.

---

## 5. Recorrido completo de una recepción (flujo básico)

Esto es lo que pasa desde que el EB aprieta **Confirmar** hasta que queda guardado. Es lo más importante de la defensa.

```
PantallaRegistrarRecepcion.confirmacionSeleccion()
  -> GestorRegistrarRecepcion.tomarConfirmacion()
       -> actualizarBolsinRecibido()
            1. busca el Estado "Recibido en CM destino" (ámbito Bolsín)
            2. busca el Estado "Recibido y Aceptado" (ámbito Remito)
            3. getFechaActual()                      una sola vez
            4. registrarCambioEstadoBolsin()         el bolsín cambia de estado
            5. registrarEstadoRemito()               los remitos cambian de estado
            6. registrarCambioEstadoDocumentacion()  <-- ACÁ ENTRA EL STATE
            7. saveAndFlush(bolsinSelec)             se guarda todo junto
       -> notificarRegistrarRecepcionCMOrigen()      CU 29
       -> finCU()
```

El paso 6 es el **método enganche** con el patrón. En el flujo básico es una línea:

```java
bolsinSelec.aceptarDocumentacion(fechaHoraActual, empleadoLog);
```

Y de ahí el pedido baja por la cadena de objetos, cada uno delegando en el siguiente:

```
Gestor
  -> Bolsin.aceptarDocumentacion()              recorre sus remitos
       -> Remito.aceptarDocumentacion()         recorre sus detalles
            -> DetalleRemito.aceptarDocumentacion()
                 -> Documentacion.aceptarDocumentacion()      contexto: delega
                      -> estadoActual.aceptarDocumentacion()  estado concreto: resuelve
```

**Lo que hay que remarcar:** el gestor no busca ningún estado de documentación y no sabe a qué estado va a pasar. Solo dice "aceptá la documentación" y pasa la fecha y el empleado. Para el bolsín y el remito sí busca el estado (pasos 1 y 2), porque esos no usan State. Esa diferencia se ve en el mismo método y es la mejor forma de mostrar qué cambió con el patrón.

### 5.1 Los seis pasos de `EnBolsinEnviado.aceptarDocumentacion`

```java
CambioEstadoDocumentacion actual = this.buscarCEActual(cambioEstado);  // 1
actual.setFechaHoraFin(fechaHora);                                     // 2
EstadoDocumentacion nuevoEstado = this.crearProximoEstado();           // 3
CambioEstadoDocumentacion nuevoCE =
        this.crearCambioEstado(fechaHora, nuevoEstado, empleado);      // 4
doc.agregarCambioEstado(nuevoCE);                                      // 5
doc.setEstado(nuevoEstado);                                            // 6
```

1. **Busca el cambio de estado actual** en el historial (el que no tiene fecha de fin). Método heredado.
2. **Lo cierra** poniéndole la fecha y hora de fin.
3. **Crea el próximo estado.** `crearProximoEstado()` hace `new RecibidoYAceptado()`. Acá es donde el estado concreto decide la transición.
4. **Crea el nuevo cambio de estado** con la fecha de inicio, el estado nuevo y el empleado responsable.
5. **Lo agrega al historial** de la documentación.
6. **Le cambia el estado actual** a la documentación.

Son los mismos seis pasos del pseudocódigo del diseño, en el mismo orden.

Después de esto la documentación tiene un objeto `RecibidoYAceptado` como estado. Si alguien intenta aceptarla de nuevo, `RecibidoYAceptado` no sobrescribe esa operación y salta la excepción. El comportamiento cambió porque cambió el objeto estado: eso es el patrón.

---

## 6. Qué cambia en las alternativas A3, A4 y A5

El EB marca con una casilla la documentación afectada y elige la opción. Cambian dos cosas:

**En el gestor.** El diagrama de secuencia solo modela la cadena `Bolsin -> Remito -> Detalle` para aceptar. Para las alternativas, el gestor recorre los remitos y detalles y le manda a cada documentación la operación que le toca:

```java
if (!documentacionSeleccionada.contains(documentacion)) {
    documentacion.aceptarDocumentacion(...);                 // llegó bien
} else if (opcion es NO_RECIBIDA) {
    documentacion.registrarDocumentacionNoRecibida(...);     // A3
} else if (opcion es NO_CORRESPONDE) {
    documentacion.rechazarDocumentacion(...);                // A4
} else if (opcion es REDIRIGIR) {
    documentacion.registrarRedireccion(...);                 // A5
}
```

**Ojo con esto, porque lo pueden preguntar:** esos `if` **no rompen el patrón**. Son sobre **lo que eligió y marcó el usuario**, no sobre el estado de la documentación. El gestor sigue sin saber en qué estado está cada una ni a cuál pasa. Qué operación pedir lo decide el usuario; si esa operación es válida y a dónde lleva, lo decide el estado.

**En el estado.** `EnBolsinEnviado` hace los mismos seis pasos, pero en el paso 3 crea otro estado: `new NoRecibida()`, `new RecibidoYRechazado()` o `new ParaRedirigir()`.

**Regla de A5:** redirigir solo aplica cuando la CM destino es la Comisión Médica Central. El gestor le pregunta a la comisión `esCMC()` y solo entonces ofrece la cuarta opción. Por eso Ana (Rosario) ve tres opciones y Elena (CMC) ve cuatro.

**A1, A2 y A6 no tocan el patrón.** A1 y A2 son el filtro del listado que no encuentra nada. A6 es no confirmar: no se llama a `tomarConfirmacion()` y no cambia nada.

---

## 7. Qué pasa con una transición inválida

Si a una documentación en estado `Registrada` le piden `aceptarDocumentacion`:

1. `Documentacion` delega en su `estadoActual`, que es un objeto `Registrada`.
2. `Registrada` no sobrescribe esa operación, así que corre la de `EstadoDocumentacion`.
3. Esa lanza `IllegalStateException("La documentación no puede aceptarse en este estado")`.
4. Como todo está dentro de una transacción, no se guarda nada.
5. La pantalla muestra el mensaje.

Nadie escribió un `if` para ese caso. Lo demuestra el test `aceptarDesdeRegistradaLanzaLaExcepcionYNoCambiaNada`, y en `GUIA_DEFENSA.md` está cómo mostrarlo en vivo.

---

## 8. Por qué Bolsín y Remito no usan State

El diseño aplica State **solo a Documentación**, porque es la que tiene muchos estados y muchas transiciones (9 estados, 17 transiciones repartidas en 9 casos de uso). Bolsín y Remito tienen pocos estados y siguen como en el análisis:

- Hay una entidad **`Estado`** (tabla `estado`) con nombre y ámbito ("Bolsín", "Remito").
- El **gestor busca** el estado que necesita recorriendo los `Estado` y preguntándoles `sosAmbitoBolsin()`, `esRecibidoCmDestino()`, etc.
- El **bolsín** tiene historial (`CambioEstadoBolsin`): cierra el actual y crea uno nuevo con `actualizarRecibido(fechaHora, estado, empleado)`.
- El **remito** guarda solo su estado actual.

Mostrar las dos formas en el mismo método (`actualizarBolsinRecibido`) deja claro el contraste: para el bolsín el gestor busca el estado y se lo pasa; para la documentación no busca nada.

---

## 9. Cómo se guarda en la base de datos

**Tecnología:** PostgreSQL, con Hibernate / Spring Data JPA como framework de persistencia. Son 12 tablas (`docs/BASE_DE_DATOS.md`).

### 9.1 El estado de la documentación

Los estados concretos son **clases Java comunes**: no son entidades y **no tienen tabla**. En la base se guarda **el nombre del estado** en una columna de texto:

- `documentacion.estado_actual` (por ejemplo, `EnBolsinEnviado`)
- `cambio_estado_documentacion.estado` (el historial)

La traducción la hace `persistencia/EstadoDocumentacionConverter.java`:

- **Al guardar** (desmaterializar): toma el objeto y guarda `estado.getNombre()`.
- **Al leer** (materializar): toma el texto, lo busca en un mapa nombre -> constructor y crea el objeto de la subclase que corresponde (`new EnBolsinEnviado()`).

Tiene `@Converter(autoApply = true)`, así que JPA lo aplica solo a cualquier atributo de tipo `EstadoDocumentacion`.

**Por qué no hay tabla de estados de documentación:** un estado no tiene datos propios que cambien, solo comportamiento. Lo único que hace falta recordar es cuál es, y para eso alcanza el nombre. El conversor es el único lugar del programa que conoce la relación nombre-clase; el dominio no tiene ningún `switch` sobre el nombre.

### 9.2 Un solo guardado

El gestor hace todos los cambios **en los objetos en memoria** y al final llama una vez a `bolsinRepositorio.saveAndFlush(bolsinSelec)`. JPA guarda en cascada todo lo que cuelga del bolsín: su historial, los remitos, y cada documentación con su historial.

### 9.3 Transacción

`tomarConfirmacion()` tiene `@Transactional`: todo se guarda o no se guarda nada. Si una transición falla a mitad de camino, todavía no se escribió nada en la base. Si falla al escribir, se deshace (rollback).

### 9.4 Dos usuarios a la vez

`Bolsin` tiene un atributo `version` con `@Version`. Si dos encargados reciben el mismo bolsín al mismo tiempo, el segundo es rechazado con un mensaje ("Otro usuario ya registró la recepción de ese bolsín").

---

## 10. Arquitectura y tecnologías

Seguimos el esquema de tres tipos de clase del análisis (boundary, control, entity):

| Tipo | Clase | Qué hace |
| --- | --- | --- |
| Pantalla (boundary) | `pantalla/PantallaRegistrarRecepcion.java` | Recibe lo que hace el usuario y arma las páginas. No tiene lógica de negocio. |
| Gestor (control) | `gestor/GestorRegistrarRecepcion.java` | Coordina el caso de uso con los métodos de la secuencia. |
| Entidades | `dominio/*.java` | Tienen los datos y el comportamiento del dominio. |
| Estados | `dominio/estados/*.java` | El patrón State. |
| Persistencia | `persistencia/*.java` | Repositorios y el conversor de estados. |

Tecnologías, para decir cuando pregunten (la consigna lo pide):

| Qué | Cuál |
| --- | --- |
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.5 (Spring MVC para la web) |
| Base de datos | PostgreSQL |
| Framework de persistencia | Hibernate, a través de Spring Data JPA |
| Interfaz | Páginas HTML generadas por la pantalla, sin framework de frontend |
| Pruebas | JUnit 5 (42 tests) |
| Construcción | Maven |

Qué significa cada anotación que se ve en el código:

| Anotación | Dónde | Qué hace |
| --- | --- | --- |
| `@RestController` | Pantalla | La clase atiende pedidos del navegador. |
| `@SessionScope` | Pantalla | Hay una pantalla por cada navegador abierto. |
| `@Service` | Gestor | La clase es un componente que maneja Spring. |
| `@Scope("prototype")` | Gestor | Se crea un gestor nuevo por cada ejecución del caso de uso. Es el `new()` del diagrama de secuencia. |
| `@Transactional` | `tomarConfirmacion()` | Todo lo que se guarda va en una transacción. |
| `@Entity` | Entidades | La clase se guarda en una tabla. |
| `@OneToMany`, `@ManyToOne` | Entidades | Las asociaciones del diagrama de clases. |
| `@Converter` | Conversor | Traduce el objeto estado a texto y al revés. |
| `@Version` | Bolsín | Control de dos usuarios a la vez. |

---

## 11. Lo que agregamos y no está en los diagramas

Hay que saberlo para no quedar descolocados si lo señalan. Está todo en `DECISIONES.md`.

- **Alternativas A3, A4 y A5 en el gestor.** La vista dinámica solo modela el flujo básico. Agregamos `tomarSeleccionDocumentacion()` y el atributo `documentacionSeleccionada`, y el gestor usa las operaciones públicas de `Documentacion`, que sí están en el diagrama de clases.
- **Getters de asociación** (`getRemito()`, `getDetalleRemito()`, `getDocumentacion()`): los necesita el gestor para recorrer en las alternativas.
- **`esCMC()` en `ComisionMedica`**: para la regla de A5.
- **Remito en "Recibido y Aceptado Parcial"** cuando tiene documentación marcada. La ERS lo define para A3; para A4 y A5 es un supuesto nuestro.
- **`crearProximoEstado()` solo devuelve `RecibidoYAceptado`** (así está en el diseño, sin parámetros). En las alternativas el estado destino se crea con `new` directo.
- **CU 29 simulado**: la notificación a la CM de origen se escribe en la consola, no manda un mail.
- **Sesión simulada**: no hay pantalla de login; el usuario activo sale de la tabla `sesion`.
- **Validaciones del filtro** (precinto solo números, CM de origen solo letras): decisión nuestra.
- **Columnas técnicas**: `bolsin.version` y los `id` de cada tabla.
- **Herramientas de demo** (reiniciar datos, cambiar usuario, ver la base): no son parte del caso de uso.

---

## 12. Lo que hay que saber decir

### En un minuto

> Implementamos el CU 28 Registrar recepción de bolsín. Cuando el encargado confirma, el bolsín, sus remitos y cada documentación cambian de estado. Aplicamos el patrón State a la documentación, porque tiene nueve estados y lo que se puede hacer con ella depende de en cuál está.
>
> `Documentacion` es el contexto: tiene un estado actual y le delega cada operación, sin ningún `if`. `EstadoDocumentacion` es la clase abstracta con las once operaciones, que por defecto lanzan una excepción. Cada estado concreto sobrescribe solo las que admite. En nuestro caso de uso el que trabaja es `EnBolsinEnviado`: cierra el cambio de estado actual, crea el estado siguiente, crea el nuevo cambio de estado y se los pasa a la documentación.
>
> El gestor no busca ni decide el estado de la documentación: solo le pide que se acepte. El estado se guarda en PostgreSQL como texto, y un conversor de JPA reconstruye el objeto al leer.

### Frases que suman

- "El contexto delega, no pregunta."
- "La transición la decide el estado concreto."
- "Una operación inválida falla sola, porque el estado no la sobrescribe."
- "Agregar un estado es agregar una clase."
- "El gestor coordina, no tiene lógica de negocio."

### Errores a evitar al hablar

| No digan | Digan |
| --- | --- |
| "El gestor cambia el estado de la documentación." | "El gestor le pide a la documentación que se acepte; el estado lo cambia el estado concreto." |
| "Los estados están en una tabla." | "Los de documentación no tienen tabla, se guarda el nombre. Los de bolsín y remito sí están en la tabla `estado`." |
| "Usamos State en todo." | "Solo en Documentación. Bolsín y Remito siguen con la entidad `Estado`." |
| "Los `if` del gestor son del patrón." | "Son sobre lo que marcó el usuario, no sobre el estado." |
| "Los otros estados no hacen nada." | "No participan de este caso de uso, por eso heredan la excepción." |

---

## 13. Preguntas probables y respuestas

**¿Qué es el patrón State?**
Un patrón de comportamiento que permite que un objeto cambie su comportamiento cuando cambia su estado interno. Cada estado es una clase, y el objeto delega en el estado que tiene en ese momento.

**¿Por qué lo aplicaron a Documentación?**
Porque tiene 9 estados y muchas transiciones, y su comportamiento depende del estado. Sin el patrón, cada operación tendría un `if` por estado.

**¿Quién es el contexto y qué responsabilidad tiene?**
`Documentacion`. Conoce su estado actual y su historial, y delega en el estado todo lo que depende de él.

**¿Quién decide a qué estado se pasa?**
El estado concreto. `EnBolsinEnviado` crea `RecibidoYAceptado` en `crearProximoEstado()`.

**¿Por qué clase abstracta y no interfaz?**
Tiene comportamiento por defecto (la excepción), atributos comunes y un método compartido (`buscarCEActual`).

**¿Por qué las operaciones por defecto lanzan excepción?**
Para que cada estado solo escriba lo que admite. Lo que no sobrescribe queda rechazado sin escribir ningún `if`.

**¿Por qué el estado recibe la documentación por parámetro?**
Porque el estado no guarda datos de una documentación en particular. Recibe `doc` para poder agregarle el cambio de estado y cambiarle el estado actual.

**¿Por qué hay estados vacíos?**
Pertenecen a otros casos de uso. Están en el diseño y en los datos; se completan cuando se implementen esos CU.

**¿Cómo se relaciona con el diagrama de secuencia?**
El gestor tiene los mismos métodos y en el mismo orden. La tabla `TABLA_MODELO_CODIGO.md` relaciona cada mensaje del diagrama con su línea de código.

**¿Qué es el método enganche?**
`registrarCambioEstadoDocumentacion()` del gestor: el punto donde el caso de uso se conecta con el patrón.

**¿El gestor tiene lógica de negocio?**
No, coordina. Las reglas están en las entidades y en los estados. Los `if` de las alternativas son sobre la elección del usuario.

**¿Cómo persisten el estado?**
Como texto, con el nombre del estado. `EstadoDocumentacionConverter` lo convierte a objeto al leer y a texto al guardar.

**¿Por qué no una tabla de estados para la documentación?**
Porque el estado no tiene datos propios, solo comportamiento. Alcanza con el nombre.

**¿Qué pasa si falla algo a mitad de la confirmación?**
No se guarda nada: los cambios se hacen en memoria y se guardan juntos al final, dentro de una transacción.

**¿Y si dos usuarios reciben el mismo bolsín?**
El segundo es rechazado, por el control de versión del bolsín.

**¿Por qué todo queda con la misma fecha y hora?**
Porque el gestor obtiene la fecha una sola vez y la pasa a todos.

**¿Qué framework de persistencia usan?**
Hibernate, con Spring Data JPA, sobre PostgreSQL.

**¿Cómo agregarían un estado nuevo?**
Una clase nueva que hereda de `EstadoDocumentacion`, sobrescribe las operaciones que admite, y una línea en el mapa del conversor. No se toca `Documentacion` ni los otros estados.

**¿Qué desventaja tiene el patrón?**
Más clases. Para algo con dos o tres estados no conviene; por eso no lo usamos en Bolsín ni en Remito.

**¿Cómo probaron que funciona?**
42 tests automáticos: los estados solos, el conversor, los datos de demo y el caso de uso completo con sus alternativas, incluidos el rechazo de transiciones inválidas y el de dos usuarios a la vez.

---

## 14. Glosario

| Término | Significado |
| --- | --- |
| Bolsín | Bolsa cerrada con precinto donde viaja la documentación entre comisiones. |
| Precinto | Cierre numerado del bolsín. Su número identifica al bolsín. |
| Remito | Lista de la documentación que va en el bolsín. Un bolsín tiene uno o más. |
| Detalle de remito | Cada renglón del remito; apunta a una documentación. |
| CM | Comisión médica. La CMC es la Comisión Médica Central. |
| EB | Encargado de Bolsines, el actor del caso de uso. |
| Cambio de estado | Fila del historial: estado, fecha de inicio, fecha de fin y empleado. El actual es el que no tiene fecha de fin. |
| Contexto | En State, el objeto que tiene el estado y delega en él. |
| Delegar | Pedirle a otro objeto que haga el trabajo. |
| Materializar | Reconstruir un objeto a partir de lo guardado en la base. |
| Transacción | Conjunto de cambios que se guardan todos o ninguno. |
| Stub | Reemplazo simple de algo que no se implementó (nuestro CU 29). |
