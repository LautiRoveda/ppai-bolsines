# Diseño de experiencia de usuario y de interfaz

PPAI Bolsines · Grupo 10 · 3K3 · CU 28 Registrar recepción de bolsín.

La consigna de la entrega 2 pide dos cosas, y las dos se califican en la defensa oral:

1. Defender el **diseño de experiencia de usuario** que salió de las actividades de descubrimiento hechas en el aula (MoodBoard, mapa de empatía y user journey).
2. El **diseño de las interfaces de usuario considerando patrones para el diseño de GUI**.

Este documento cubre el punto 2 completo y deja preparado el punto 1 para completarlo con el material del grupo.

## 1. Experiencia de usuario: lo que hay que traer del aula

> **Esta parte la tiene que completar el grupo.** Son los tres afiches que hicieron en clase. La consigna pedía sacarles una foto "para tener el registro en el momento de la presentación". Sin esas fotos este punto no se puede defender.

| Entregable del aula | Qué era | Qué llevar a la defensa |
| --- | --- | --- |
| MoodBoard | Afiche con la problemática de los actores, un elemento asociado al sistema y un slogan, logo o referencia | La foto del afiche |
| Mapa de empatía | De una Persona vinculada al actor Encargado de Bolsines (EB) | La foto del afiche y el nombre de la Persona |
| User Journey | El viaje de esa Persona: antes, durante y después de usar el sistema | La foto del afiche |

### Cómo conectar ese material con la pantalla

Completar la columna del medio con lo que dice **su** afiche. La columna de la derecha ya está: es lo que la pantalla hace hoy.

| Del material del aula | Lo que dice el afiche del grupo | Cómo responde la pantalla |
| --- | --- | --- |
| MoodBoard: slogan o logo | *(completar)* | El nombre y el lema del encabezado. Hoy dice "Cada documento, en su destino", que es un **texto provisorio**: hay que cambiarlo por el del afiche (ver sección 3). |
| MoodBoard: colores y clima | *(completar)* | La paleta de colores de toda la aplicación (ver sección 3). |
| Mapa de empatía: qué le duele o le preocupa al EB | *(completar)* | Se le muestra solo lo que le llegó a su comisión médica, identificado por el precinto que tiene en la mano; antes de registrar ve un resumen y tiene que confirmar. |
| Mapa de empatía: qué necesita o qué gana | *(completar)* | Cuatro pasos cortos con indicador de avance; mensaje claro de que la recepción quedó registrada y de que se avisó a la CM origen. |
| Journey, "antes" (llega el bolsín físico) | *(completar)* | El listado con filtro por número de precinto o por CM origen. |
| Journey, "durante" (abre el bolsín y controla contra los remitos) | *(completar)* | La tabla con cada remito y su documentación, con una casilla por fila para marcar lo que no llegó, lo que no corresponde o lo que hay que redirigir. |
| Journey, "después" (la documentación sigue su camino) | *(completar)* | La notificación a la CM origen y el historial con responsable, fecha y hora (trazabilidad). |

## 2. Patrones de diseño de GUI aplicados

Los nombres son los habituales en la bibliografía de diseño de interfaces. **Conviene compararlos con los que dio la cátedra** y, si alguno se llama distinto en sus diapositivas, usar ese nombre en la defensa.

| Patrón | Qué problema resuelve | Dónde se ve | Dónde está en el código |
| --- | --- | --- | --- |
| **Asistente (wizard)** | Una tarea larga se parte en pasos cortos, uno por página | Los cuatro pasos: elegir el bolsín, indicar cómo llegó, confirmar, recepción registrada | `PantallaRegistrarRecepcion.html()`, línea 285 |
| **Indicador de progreso** | El usuario sabe en qué paso está, cuáles hizo y cuántos faltan | La barra "Paso 2 de 4" arriba de cada página | `Plantilla.pasos()`, línea 102 |
| **Visibilidad del estado del sistema** | El usuario siempre sabe sobre qué está trabajando | La comisión médica del usuario logueado y el bolsín seleccionado (CM origen y precinto), fijos arriba | `html()` y `htmlDatosBolsin()`, línea 398 |
| **Lista con selección única** | Elegir un elemento entre pocos, viéndolos todos | El listado de bolsines con un botón de opción por fila | `htmlBolsines()`, línea 309 |
| **Filtro de lista** | Encontrar rápido un elemento cuando la lista crece | Los campos "N° de precinto" y "CM origen" con el botón Filtrar | `htmlBolsines()` y `pasaElFiltro()`, línea 386 |
| **Estado vacío** | Cuando no hay nada para mostrar, se explica por qué y qué hacer | "No se encuentra bolsín con el número de precinto ingresado" (A1 y A2) y "No hay bolsines en estado Enviado" | `htmlBolsines()` |
| **Selección múltiple en la fila** | La marca va al lado del dato al que se refiere | La casilla "Marcar" en cada fila de documentación | `htmlDatosBolsin()` |
| **Opciones excluyentes visibles** | Pocas opciones que se excluyen: se muestran todas, sin desplegable | Las opciones de recepción como botones de opción grandes | `htmlOpciones()`, línea 447 |
| **Acción principal destacada** | En cada página se ve cuál es el paso siguiente | Un solo botón lleno por página (Seleccionar, Continuar, Confirmar); los demás, con borde | estilos `button` y `.secundario` en `Plantilla` |
| **Salida siempre disponible** | El usuario puede abandonar sin consecuencias | "Cancelar" en todos los pasos y "Elegir otro bolsín" en el paso 2 | `htmlBolsines()`, `htmlOpciones()` |
| **Resumen antes de confirmar** | Revisar lo que se va a registrar antes de hacerlo | La página de confirmación repite el bolsín y agrega la columna "Situación" de cada documentación | `htmlConfirmacion()`, línea 474 |
| **Confirmación de acción irreversible** | Evitar registrar por error algo que no se puede deshacer | "¿Confirma el registro...?" con la aclaración de qué va a pasar, y "No confirmar" (A6) | `htmlConfirmacion()` |
| **Mensajes de retroalimentación** | El sistema avisa qué pasó, con color y texto | Verde: recepción registrada. Amarillo: avisos. Rojo: errores de carga | estilos `.ok`, `.aviso`, `.error` en `Plantilla`; `htmlFin()`, línea 489 |
| **Prevención y explicación de errores** | El error se evita antes, y si ocurre se explica en el lugar | No deja continuar sin elegir; el filtro solo acepta números en el precinto y letras en la CM origen; si falta marcar documentación, el mensaje aparece sobre las opciones y se conservan las marcas | `opcionSeleccionada()`, línea 168 |
| **Tolerancia a errores del usuario** | Equivocarse no rompe nada | Se puede volver atrás y cambiar el bolsín o la opción; recargar o reenviar un formulario no registra dos veces | `verPagina()`, línea 262 |
| **Consistencia** | Todas las páginas se ven y se usan igual | Mismo encabezado, colores, botones y tablas | `Plantilla.pagina()`, línea 84 |
| **Lenguaje del usuario** | Se habla con las palabras del EB, no con las del sistema | Precinto, remito, comisión médica, "¿Cómo llegó el bolsín?" | textos de `PantallaRegistrarRecepcion` |
| **Diseño adaptable** | Se usa en pantallas de distinto ancho | Los pasos y los botones se reacomodan; las tablas se desplazan de costado | estilos de `Plantilla` |

Dos decisiones de accesibilidad que también se pueden mencionar: el foco del teclado se ve (borde naranja al tabular) y cada control tiene su etiqueta (el nombre de la CM es la etiqueta de su botón de opción; cada casilla dice qué documentación marca).

### Lo que no se hizo

- No hay validación ni comportamiento con JavaScript: todo se resuelve del lado del servidor, que es más simple de explicar.
- El filtro lo aplica la pantalla sobre el listado que ya recibió del gestor. La secuencia de análisis no modela el filtro, así que no pasa por el gestor.

## 3. Identidad visual: dónde cambiarla

Todo está junto al principio de `src/main/java/bolsines/pantalla/Plantilla.java`:

| Qué | Línea | Valor actual |
| --- | --- | --- |
| Nombre del sistema | 14 | Bolsines |
| Lema | 15 | "Cada documento, en su destino" (provisorio: poner el del MoodBoard) |
| Color principal | 19 | azul `#1f4e79` |
| Resto de la paleta | 18 en adelante | fondo, texto, bordes y colores de mensajes |

Cambiando esas líneas cambia toda la aplicación, porque todas las páginas usan la misma plantilla.
