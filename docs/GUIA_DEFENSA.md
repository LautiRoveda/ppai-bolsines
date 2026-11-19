# Guía de defensa · CU 28 con patrón State

Grupo 10 · 3K3 · Defensa: viernes 20-11-26.

## Antes de empezar

1. PostgreSQL corriendo y la base `bolsines` creada.
2. `.\mvnw.cmd spring-boot:run` y esperar `PPAI Bolsines listo`. Dejar esa terminal a la vista: ahí sale la notificación del CU 29.
3. Abrir `http://localhost:8080` y tocar **Reiniciar datos de demo**.
4. Elegir siempre el bolsín por su **número de precinto**: el listado se achica a medida que se reciben.

**Plan B si la notebook falla:** la misma aplicación está publicada en `https://bolsines.test-ia.cloud` (usuario `grupo10`, la clave la tiene Lautaro). Necesita internet y ahí no se ve la terminal con la notificación del CU 29. El código se muestra igual desde la notebook o desde GitHub.

**Orden sugerido (el aviso de la cátedra dice que se presenta el modelo de diseño y la implementación):** primero el modelo entregado (`PPAI2026_3K3_G10_E2_Diseño.pdf`: diagrama de clases con el State y la vista dinámica), después la demo (secciones 1 a 6) y al final el código (sección 7), señalando en cada paso a qué parte del diagrama corresponde.

## 1. Flujo básico (bolsín con precinto 4471)

1. **Registrar recepción de bolsín.** Se ve la CM del usuario (CM Rosario) y tres bolsines enviados.
   Decir: el 4480 no aparece porque va a Mendoza, y el 4490 tampoco porque está Cerrado y no Enviado.
2. Elegir el **4471** (CM Córdoba). Se ven los remitos 301 y 302 con tres documentaciones.
3. Elegir la opción **1. El contenido del bolsín es igual al registrado** → **Continuar** → **Confirmar**.
   Señalar el indicador de pasos de arriba y el resumen antes de confirmar (patrones de GUI, ver `UX_UI.md`).
4. Mostrar en la terminal la línea `CU 29 (stub): notificacion enviada a la CM origen CM Córdoba`.
5. **Ver cómo quedó la base de datos** y mostrar:

| Tabla | Qué mostrar |
| --- | --- |
| Bolsines | El 105: "Enviado" quedó cerrado y hay una fila nueva "Recibido en CM destino", resaltada, con responsable **Pérez, Ana**. |
| Remitos | 301 y 302 en "Recibido y Aceptado". |
| Documentación, estado actual | 9001, 9002 y 9010 en `RecibidoYAceptado`. |
| Documentación, historial | Cada una tiene una quinta fila `RecibidoYAceptado` con Pérez, Ana; `EnBolsinEnviado` quedó cerrada con **la misma fecha y hora** que el bolsín. |

## 2. Alternativos A1 y A2: el filtro no encuentra el bolsín

1. **Registrar recepción de bolsín**. En "N° de precinto" escribir `9999` → **Filtrar**. Aparece "No se encuentra bolsín con el número de precinto ingresado" (A1).
2. **Ver todos**. En "CM origen" escribir `Tucumán` → **Filtrar**. Aparece "No se encuentra bolsín con la CM de origen ingresada" (A2).
3. **Ver todos**. En el precinto escribir `44a1` → **Filtrar**: avisa que el precinto tiene que ser un número y no filtra (validación del dato, decisión del grupo).
4. **Ver todos**, escribir `4472` en el precinto → **Filtrar**: queda solo ese bolsín. Seguir con el punto siguiente.

## 3. Alternativo A3: no se recibe toda la documentación (precinto 4472)

1. Elegir el **4472** (CM Mendoza).
2. Marcar la casilla de **Carta documento a la ART** (remito 303).
3. Opción **2. No se recibe toda la documentación...** → **Continuar**. La columna "Situación" muestra cuál queda "No recibida" → **Confirmar**.
4. En la base:
   - Documentación 9021 en `NoRecibida`; 9020 y 9022 en `RecibidoYAceptado`.
   - Remito **303 en "Recibido y Aceptado Parcial"** (regla de negocio); remito 304 en "Recibido y Aceptado".
   - Bolsín 106 en "Recibido en CM destino".

## 4. Alternativo A4: documentación que no corresponde (precinto 4473)

1. **Registrar recepción de bolsín** → elegir el **4473** (CM Salta).
2. Marcar **Estudio radiológico** (remito 305) y **Carta documento al empleador** (remito 306).
3. Opción **3. Existe documentación que no corresponde al destino** → **Continuar** → **Confirmar**.
4. En la base:
   - Documentación 9030 y 9033 en `RecibidoYRechazado`; 9031 y 9032 en `RecibidoYAceptado`.
   - Remitos 305 y 306 en "Recibido y Aceptado Parcial" (supuesto del grupo: la ERS no lo define para A4).

## 5. Alternativo A5: redirigir a otra área (precinto 4500, usuaria de la CMC)

1. En el inicio, tocar **Cambiar usuario a Elena Sosa (Comisión Médica Central)**.
   Decir: redirigir solo ocurre cuando la CM destino es la CMC (regla de negocio), por eso a Rosario no se le ofrece esa opción.
2. **Registrar recepción de bolsín**. Ahora la CM es la Comisión Médica Central y el único bolsín es el **4500** (CM Córdoba). Elegirlo.
3. Marcar **Expediente de apelación** (remito 311).
4. Opción **4. La documentación se debe redirigir a otra área** → **Continuar** → **Confirmar**.
5. En la base:
   - Documentación 9060 en `ParaRedirigir`; 9061 y 9062 en `RecibidoYAceptado`.
   - Remito 311 en "Recibido y Aceptado Parcial" (supuesto del grupo); remito 312 en "Recibido y Aceptado".
   - Bolsín 110 en "Recibido en CM destino", con responsable **Sosa, Elena**.
6. Volver al inicio y tocar **Cambiar usuario a Ana Pérez (CM Rosario)**.

## 6. Alternativo A6: no confirma

**Reiniciar datos de demo** → elegir cualquier bolsín → opción 1 → **No confirmar**. Mostrar en la base que no cambió nada.

## 7. Dónde está el patrón State en el código

Abrir en este orden (las líneas exactas están en `docs/TABLA_MODELO_CODIGO.md`):

1. `gestor/GestorRegistrarRecepcion.java`, método `registrarCambioEstadoDocumentacion()`: el gestor no busca ningún estado de documentación. En el flujo básico solo llama a `bolsinSelec.aceptarDocumentacion(fechaHoraActual, empleadoLog)`. El recorrido de abajo es para A3, A4 y A5, y sus `if` son sobre lo que marcó el EB, no sobre el estado de la documentación.
2. `dominio/Bolsin.java` → `Remito.java` → `DetalleRemito.java`: la cadena de delegación de `aceptarDocumentacion`.
3. `dominio/Documentacion.java`: `aceptarDocumentacion` es una sola línea, `estadoActual.aceptarDocumentacion(this, fechaHora, empleado, cambioEstado)`. No hay `if` ni `switch`.
4. `dominio/estados/EstadoDocumentacion.java`: clase abstracta, las 11 operaciones lanzan excepción por defecto, y `buscarCEActual()` es compartido.
5. `dominio/estados/EnBolsinEnviado.java`: los seis pasos del pseudocódigo, en orden, y los alternativos.
6. `dominio/estados/Registrada.java`: un estado vacío, que hereda la excepción.
7. `persistencia/EstadoDocumentacionConverter.java`: cómo se guarda el estado (por nombre) y cómo se reconstruye el objeto.

Para mostrar que la transición inválida falla sola: correr `.\mvnw.cmd test` y señalar `aceptarDesdeRegistradaLanzaLaExcepcionYNoCambiaNada` en `EstadoDocumentacionTest`.

Si lo quieren mostrar en vivo: **antes** de entrar a "Registrar recepción de bolsín", ejecutar en PostgreSQL `UPDATE documentacion SET estado_actual = 'Registrada' WHERE id = 9002;` y después recibir el bolsín 4471 con la opción 1. Aparece "La documentación no puede aceptarse en este estado" y no se guarda nada. Si el `UPDATE` se hace con el listado ya abierto no se detecta, porque el gestor ya leyó los datos. Al terminar, **Reiniciar datos de demo**.

## 8. Diseño de experiencia de usuario y de interfaz (respaldo)

El aviso de los docentes dice que esta entrega **no incluye** diseño de experiencia de usuario, así que no hay que presentarlo. Queda preparado por si preguntan:

- **`docs/UX_Bolsines_G10.pdf`** (MoodBoard, mapa de empatía y user journey) y poder contar cómo se conecta cada cosa con la pantalla: la página 4 del PDF es el resumen para eso.
- **Patrones de GUI aplicados** (`docs/UX_UI.md`): asistente con indicador de pasos, lista con filtro, estado vacío, acción principal destacada, salida siempre disponible, resumen antes de confirmar, confirmación de acción irreversible y mensajes de retroalimentación. La tabla de `UX_UI.md` dice dónde se ve cada uno.

## Preguntas probables

| Pregunta | Respuesta |
| --- | --- |
| ¿Por qué clase abstracta y no interfaz? | Tiene comportamiento por defecto (la excepción), atributos comunes (`nombre`, `descripcion`) y un método compartido (`buscarCEActual`). |
| ¿Por qué State solo en Documentación? | Tiene 9 estados y 17 transiciones. Bolsín y Remito tienen pocos estados y siguen con la entidad `Estado`. |
| ¿Dónde está la tabla de estados de documentación? | No hay: se guarda el nombre en una columna de texto y `EstadoDocumentacionConverter` crea el objeto al leer. |
| ¿Por qué en los alternativos el gestor recorre los remitos? | La vista dinámica modela el flujo básico. Los alternativos usan las operaciones públicas de `Documentacion` del diagrama de clases. |
| ¿Qué pasa si falla algo a mitad de la confirmación? | Los cambios se hacen en los objetos y se guardan todos juntos al final, dentro de una transacción (`@Transactional`). Si falla antes, no se escribió nada; si falla al escribir, se deshace. |
| ¿Y si dos usuarios reciben el mismo bolsín? | El segundo es rechazado (`@Version` en `Bolsin`). |
| ¿El gestor tiene lógica de negocio? | No: coordina. Los `if` de los alternativos son sobre lo que eligió y marcó el usuario, no sobre el estado. |
| ¿Por qué Rosario ve tres opciones y no cuatro? | La regla de negocio dice que redirigir ocurre cuando la CM destino es la CMC. El gestor le pregunta a la CM `esCMC()` y solo en ese caso ofrece la cuarta. |
