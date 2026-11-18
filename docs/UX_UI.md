# Diseño de experiencia de usuario y de interfaz

PPAI Bolsines · Grupo 10 · 3K3 · CU 28 Registrar recepción de bolsín.

La consigna de la entrega 2 pide dos cosas, y las dos se califican en la defensa oral:

1. Defender el **diseño de experiencia de usuario** que salió de las actividades de descubrimiento hechas en el aula (MoodBoard, mapa de empatía y user journey).
2. El **diseño de las interfaces de usuario considerando patrones para el diseño de GUI**.

Este documento cubre el punto 2 completo. El punto 1 está en `docs/UX_Bolsines_G10.pdf`.

## 1. Experiencia de usuario: MoodBoard, mapa de empatía y user journey

Los tres entregables están en **`docs/UX_Bolsines_G10.pdf`** (4 páginas, para proyectar o imprimir):

| Página | Contenido |
| --- | --- |
| 1 | **MoodBoard**: nombre, logo, lema, colores, problema que resuelve, elementos del sistema (bolsín, precinto, remito, comisión médica) y palabras guía. |
| 2 | **Mapa de empatía** de Ana Pérez, Encargada de Bolsines de la CM Rosario (la Persona vinculada al actor EB): qué piensa y siente, ve, oye, dice y hace, esfuerzos y resultados. |
| 3 | **User Journey** de Ana: antes, durante y después de usar el sistema, en 8 momentos, con su ánimo, los puntos de dolor y qué hace el sistema en cada uno. |
| 4 | **Del descubrimiento a la pantalla**: cada hallazgo, la decisión de interfaz que provocó y el patrón de GUI que aplica. |

Es una **versión digital** armada para la defensa, no la foto de los afiches del aula. Si alguien del grupo tiene los afiches originales, mandan los originales y este PDF se ajusta a lo que digan.

El nombre, el lema y los colores del MoodBoard son los mismos que usa la aplicación (sección 3), y Ana Pérez es el usuario con el que arranca la demo.

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
| Lema | 15 | "Cada documento, en su destino" (el del MoodBoard) |
| Color principal | 19 | azul `#1f4e79` |
| Resto de la paleta | 18 en adelante | fondo, texto, bordes y colores de mensajes |

Cambiando esas líneas cambia toda la aplicación, porque todas las páginas usan la misma plantilla.
