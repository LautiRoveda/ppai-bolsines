-- =====================================================================
-- PPAI Bolsines - Grupo 10 - Datos de demo (seed)
-- Lo ejecuta InicializadorBaseDeDatos.reiniciar() despues de schema.sql.
-- Usuario logueado: Ana Perez, Encargada de Bolsines de la CM Rosario.
--
--   Bolsin 105 (precinto 4471, desde CM Cordoba)  -> flujo basico
--   Bolsin 106 (precinto 4472, desde CM Mendoza)  -> A3 (documentacion no recibida)
--   Bolsin 107 (precinto 4473, desde CM Salta)    -> A4 (documentacion que no corresponde)
--   Bolsin 108 (precinto 4480, Cordoba -> Mendoza) -> NO aparece: es de otra CM destino
--   Bolsin 109 (precinto 4490, Cordoba -> Rosario) -> NO aparece: esta Cerrado, no Enviado
--   Bolsin 110 (precinto 4500, Cordoba -> CMC)     -> A5 (redirigir a otra area): lo ve Elena Sosa, de la CMC
-- =====================================================================

INSERT INTO comision_medica (id, codigo, nombre, direccion, email, telefono) VALUES
  (1, 'CM-ROS', 'CM Rosario',  'Corrientes 850, Rosario',       'rosario@comisionesmedicas.gob.ar', '0341-4210001'),
  (2, 'CM-CBA', 'CM Córdoba',  'Av. Colón 1200, Córdoba',       'cordoba@comisionesmedicas.gob.ar', '0351-4210002'),
  (3, 'CM-MZA', 'CM Mendoza',  'San Martín 450, Mendoza',       'mendoza@comisionesmedicas.gob.ar', '0261-4210003'),
  (4, 'CM-SAL', 'CM Salta',    'Belgrano 300, Salta',           'salta@comisionesmedicas.gob.ar',   '0387-4210004'),
  (5, 'CMC',    'Comisión Médica Central', 'Av. de Mayo 600, CABA', 'central@comisionesmedicas.gob.ar', '011-4210005');

INSERT INTO empleado (id, apellido, nombre, email, comision_medica_id) VALUES
  (1, 'Pérez', 'Ana',   'ana.perez@comisionesmedicas.gob.ar',   1),
  (2, 'Gómez', 'Luis',  'luis.gomez@comisionesmedicas.gob.ar',  2),
  (3, 'Díaz',  'Marta', 'marta.diaz@comisionesmedicas.gob.ar',  3),
  (4, 'Ruiz',  'Pablo', 'pablo.ruiz@comisionesmedicas.gob.ar',  4),
  (5, 'Sosa',  'Elena', 'elena.sosa@comisionesmedicas.gob.ar',  5);

INSERT INTO usuario (id, nombre, password, fecha_alta, empleado_id) VALUES
  (1, 'aperez', 'demo', '2026-03-02 09:00:00', 1),
  (2, 'lgomez', 'demo', '2026-03-02 09:00:00', 2),
  (3, 'esosa',  'demo', '2026-03-02 09:00:00', 5);

-- Sesion activa: la de aperez (CM Rosario). La otra es de esosa (Comision Medica Central), para A5.
-- El boton "Cambiar usuario" del inicio deja activa una u otra.
INSERT INTO sesion (id, esta_activa, usuario_id) VALUES
  (1, TRUE, 1),
  (2, FALSE, 3);

-- Estados de Bolsin y de Remito (la documentacion no usa esta tabla)
INSERT INTO estado (id, nombre, descripcion, ambito) VALUES
  (1,  'Creado',                       'Bolsín armado, todavía abierto',                 'Bolsin'),
  (2,  'Cerrado',                      'Bolsín con precinto colocado',                   'Bolsin'),
  (3,  'Enviado',                      'Bolsín retirado por el correo',                  'Bolsin'),
  (4,  'Recibido en CM destino',       'Bolsín recibido por la CM destino',              'Bolsin'),
  (5,  'De baja',                      'Bolsín eliminado',                               'Bolsin'),
  (6,  'Creado',                       'Remito generado',                                'Remito'),
  (7,  'En bolsín saliente',           'Remito incluido en un bolsín',                   'Remito'),
  (8,  'En bolsín enviado',            'Remito en un bolsín retirado por el correo',     'Remito'),
  (9,  'Recibido y Aceptado',          'Se recibió toda su documentación',               'Remito'),
  (10, 'Recibido y Aceptado Parcial',  'Parte de su documentación no fue aceptada',      'Remito');

INSERT INTO tipo_documento (id, nombre, descripcion) VALUES
  (1, 'Expediente',      'Expediente administrativo'),
  (2, 'Dictamen',        'Dictamen de la comisión médica'),
  (3, 'Estudio médico',  'Estudio o informe médico'),
  (4, 'Carta documento', 'Notificación fehaciente');


-- ---------------------------------------------------------------------
-- Bolsin 105: flujo basico
-- ---------------------------------------------------------------------
INSERT INTO bolsin (id, numero_bolsin, numero_precinto, fecha, peso, cm_origen_id, cm_destino_id) VALUES
  (105, 105, 4471, '2026-11-05 11:00:00', 2.4, 2, 1);
INSERT INTO cambio_estado_bolsin (bolsin_id, fecha_hora_inicio, fecha_hora_fin, estado_id, responsable_ce_id) VALUES
  (105, '2026-11-05 11:00:00', '2026-11-05 17:00:00', 1, 2),
  (105, '2026-11-05 17:00:00', '2026-11-06 14:00:00', 2, 2),
  (105, '2026-11-06 14:00:00', NULL, 3, 2);
INSERT INTO remito (id, fecha, numero, bolsin_id, estado_id, cm_origen_id, cm_destino_id) VALUES
  (301, '2026-11-03 09:10:00', 301, 105, 8, 2, 1),
  (302, '2026-11-03 09:10:00', 302, 105, 8, 2, 1);
INSERT INTO documentacion (id, numero, asunto, fecha_pase, tipo_documento_id, cm_origen_id, estado_actual) VALUES
  (9001, 9001, 'Dictamen médico', '2026-11-02 00:00:00', 2, 2, 'EnBolsinEnviado'),
  (9002, 9002, 'Historia clínica', '2026-11-02 00:00:00', 1, 2, 'EnBolsinEnviado'),
  (9010, 9010, 'Estudio complementario', '2026-11-02 00:00:00', 3, 2, 'EnBolsinEnviado');
INSERT INTO detalle_remito (id, remito_id, documentacion_id, area_cmc_destino) VALUES
  (1, 301, 9001, NULL),
  (2, 301, 9002, NULL),
  (3, 302, 9010, NULL);
INSERT INTO cambio_estado_documentacion (documentacion_id, fecha_hora_inicio, fecha_hora_fin, estado, responsable_ce_id) VALUES
  (9001, '2026-11-01 08:00:00', '2026-11-03 09:10:00', 'Registrada', 2),
  (9001, '2026-11-03 09:10:00', '2026-11-05 11:00:00', 'EnRemito', 2),
  (9001, '2026-11-05 11:00:00', '2026-11-06 14:00:00', 'EnBolsinSaliente', 2),
  (9001, '2026-11-06 14:00:00', NULL, 'EnBolsinEnviado', 2),
  (9002, '2026-11-01 08:00:00', '2026-11-03 09:10:00', 'Registrada', 2),
  (9002, '2026-11-03 09:10:00', '2026-11-05 11:00:00', 'EnRemito', 2),
  (9002, '2026-11-05 11:00:00', '2026-11-06 14:00:00', 'EnBolsinSaliente', 2),
  (9002, '2026-11-06 14:00:00', NULL, 'EnBolsinEnviado', 2),
  (9010, '2026-11-01 08:00:00', '2026-11-03 09:10:00', 'Registrada', 2),
  (9010, '2026-11-03 09:10:00', '2026-11-05 11:00:00', 'EnRemito', 2),
  (9010, '2026-11-05 11:00:00', '2026-11-06 14:00:00', 'EnBolsinSaliente', 2),
  (9010, '2026-11-06 14:00:00', NULL, 'EnBolsinEnviado', 2);

-- ---------------------------------------------------------------------
-- Bolsin 106: A3: no se recibe toda la documentacion
-- ---------------------------------------------------------------------
INSERT INTO bolsin (id, numero_bolsin, numero_precinto, fecha, peso, cm_origen_id, cm_destino_id) VALUES
  (106, 106, 4472, '2026-11-05 11:00:00', 1.8, 3, 1);
INSERT INTO cambio_estado_bolsin (bolsin_id, fecha_hora_inicio, fecha_hora_fin, estado_id, responsable_ce_id) VALUES
  (106, '2026-11-05 11:00:00', '2026-11-05 17:00:00', 1, 3),
  (106, '2026-11-05 17:00:00', '2026-11-06 14:00:00', 2, 3),
  (106, '2026-11-06 14:00:00', NULL, 3, 3);
INSERT INTO remito (id, fecha, numero, bolsin_id, estado_id, cm_origen_id, cm_destino_id) VALUES
  (303, '2026-11-03 09:10:00', 303, 106, 8, 3, 1),
  (304, '2026-11-03 09:10:00', 304, 106, 8, 3, 1);
INSERT INTO documentacion (id, numero, asunto, fecha_pase, tipo_documento_id, cm_origen_id, estado_actual) VALUES
  (9020, 9020, 'Expediente de accidente laboral', '2026-11-02 00:00:00', 1, 3, 'EnBolsinEnviado'),
  (9021, 9021, 'Carta documento a la ART', '2026-11-02 00:00:00', 4, 3, 'EnBolsinEnviado'),
  (9022, 9022, 'Dictamen de incapacidad', '2026-11-02 00:00:00', 2, 3, 'EnBolsinEnviado');
INSERT INTO detalle_remito (id, remito_id, documentacion_id, area_cmc_destino) VALUES
  (4, 303, 9020, NULL),
  (5, 303, 9021, NULL),
  (6, 304, 9022, NULL);
INSERT INTO cambio_estado_documentacion (documentacion_id, fecha_hora_inicio, fecha_hora_fin, estado, responsable_ce_id) VALUES
  (9020, '2026-11-01 08:00:00', '2026-11-03 09:10:00', 'Registrada', 3),
  (9020, '2026-11-03 09:10:00', '2026-11-05 11:00:00', 'EnRemito', 3),
  (9020, '2026-11-05 11:00:00', '2026-11-06 14:00:00', 'EnBolsinSaliente', 3),
  (9020, '2026-11-06 14:00:00', NULL, 'EnBolsinEnviado', 3),
  (9021, '2026-11-01 08:00:00', '2026-11-03 09:10:00', 'Registrada', 3),
  (9021, '2026-11-03 09:10:00', '2026-11-05 11:00:00', 'EnRemito', 3),
  (9021, '2026-11-05 11:00:00', '2026-11-06 14:00:00', 'EnBolsinSaliente', 3),
  (9021, '2026-11-06 14:00:00', NULL, 'EnBolsinEnviado', 3),
  (9022, '2026-11-01 08:00:00', '2026-11-03 09:10:00', 'Registrada', 3),
  (9022, '2026-11-03 09:10:00', '2026-11-05 11:00:00', 'EnRemito', 3),
  (9022, '2026-11-05 11:00:00', '2026-11-06 14:00:00', 'EnBolsinSaliente', 3),
  (9022, '2026-11-06 14:00:00', NULL, 'EnBolsinEnviado', 3);

-- ---------------------------------------------------------------------
-- Bolsin 107: A4: documentacion que no corresponde al destino
-- ---------------------------------------------------------------------
INSERT INTO bolsin (id, numero_bolsin, numero_precinto, fecha, peso, cm_origen_id, cm_destino_id) VALUES
  (107, 107, 4473, '2026-11-05 11:00:00', 3.1, 4, 1);
INSERT INTO cambio_estado_bolsin (bolsin_id, fecha_hora_inicio, fecha_hora_fin, estado_id, responsable_ce_id) VALUES
  (107, '2026-11-05 11:00:00', '2026-11-05 17:00:00', 1, 4),
  (107, '2026-11-05 17:00:00', '2026-11-06 14:00:00', 2, 4),
  (107, '2026-11-06 14:00:00', NULL, 3, 4);
INSERT INTO remito (id, fecha, numero, bolsin_id, estado_id, cm_origen_id, cm_destino_id) VALUES
  (305, '2026-11-03 09:10:00', 305, 107, 8, 4, 1),
  (306, '2026-11-03 09:10:00', 306, 107, 8, 4, 1);
INSERT INTO documentacion (id, numero, asunto, fecha_pase, tipo_documento_id, cm_origen_id, estado_actual) VALUES
  (9030, 9030, 'Estudio radiológico', '2026-11-02 00:00:00', 3, 4, 'EnBolsinEnviado'),
  (9031, 9031, 'Expediente de reclamo', '2026-11-02 00:00:00', 1, 4, 'EnBolsinEnviado'),
  (9032, 9032, 'Dictamen de alta médica', '2026-11-02 00:00:00', 2, 4, 'EnBolsinEnviado'),
  (9033, 9033, 'Carta documento al empleador', '2026-11-02 00:00:00', 4, 4, 'EnBolsinEnviado');
INSERT INTO detalle_remito (id, remito_id, documentacion_id, area_cmc_destino) VALUES
  (7, 305, 9030, NULL),
  (8, 305, 9031, NULL),
  (9, 306, 9032, NULL),
  (10, 306, 9033, NULL);
INSERT INTO cambio_estado_documentacion (documentacion_id, fecha_hora_inicio, fecha_hora_fin, estado, responsable_ce_id) VALUES
  (9030, '2026-11-01 08:00:00', '2026-11-03 09:10:00', 'Registrada', 4),
  (9030, '2026-11-03 09:10:00', '2026-11-05 11:00:00', 'EnRemito', 4),
  (9030, '2026-11-05 11:00:00', '2026-11-06 14:00:00', 'EnBolsinSaliente', 4),
  (9030, '2026-11-06 14:00:00', NULL, 'EnBolsinEnviado', 4),
  (9031, '2026-11-01 08:00:00', '2026-11-03 09:10:00', 'Registrada', 4),
  (9031, '2026-11-03 09:10:00', '2026-11-05 11:00:00', 'EnRemito', 4),
  (9031, '2026-11-05 11:00:00', '2026-11-06 14:00:00', 'EnBolsinSaliente', 4),
  (9031, '2026-11-06 14:00:00', NULL, 'EnBolsinEnviado', 4),
  (9032, '2026-11-01 08:00:00', '2026-11-03 09:10:00', 'Registrada', 4),
  (9032, '2026-11-03 09:10:00', '2026-11-05 11:00:00', 'EnRemito', 4),
  (9032, '2026-11-05 11:00:00', '2026-11-06 14:00:00', 'EnBolsinSaliente', 4),
  (9032, '2026-11-06 14:00:00', NULL, 'EnBolsinEnviado', 4),
  (9033, '2026-11-01 08:00:00', '2026-11-03 09:10:00', 'Registrada', 4),
  (9033, '2026-11-03 09:10:00', '2026-11-05 11:00:00', 'EnRemito', 4),
  (9033, '2026-11-05 11:00:00', '2026-11-06 14:00:00', 'EnBolsinSaliente', 4),
  (9033, '2026-11-06 14:00:00', NULL, 'EnBolsinEnviado', 4);

-- ---------------------------------------------------------------------
-- Bolsin 108: enviado a OTRA CM (Mendoza): no debe aparecer
-- ---------------------------------------------------------------------
INSERT INTO bolsin (id, numero_bolsin, numero_precinto, fecha, peso, cm_origen_id, cm_destino_id) VALUES
  (108, 108, 4480, '2026-11-05 11:00:00', 1.2, 2, 3);
INSERT INTO cambio_estado_bolsin (bolsin_id, fecha_hora_inicio, fecha_hora_fin, estado_id, responsable_ce_id) VALUES
  (108, '2026-11-05 11:00:00', '2026-11-05 17:00:00', 1, 2),
  (108, '2026-11-05 17:00:00', '2026-11-06 14:00:00', 2, 2),
  (108, '2026-11-06 14:00:00', NULL, 3, 2);
INSERT INTO remito (id, fecha, numero, bolsin_id, estado_id, cm_origen_id, cm_destino_id) VALUES
  (307, '2026-11-03 09:10:00', 307, 108, 8, 2, 3),
  (308, '2026-11-03 09:10:00', 308, 108, 8, 2, 3);
INSERT INTO documentacion (id, numero, asunto, fecha_pase, tipo_documento_id, cm_origen_id, estado_actual) VALUES
  (9040, 9040, 'Expediente de enfermedad profesional', '2026-11-02 00:00:00', 1, 2, 'EnBolsinEnviado'),
  (9041, 9041, 'Estudio de laboratorio', '2026-11-02 00:00:00', 3, 2, 'EnBolsinEnviado'),
  (9042, 9042, 'Dictamen de reingreso', '2026-11-02 00:00:00', 2, 2, 'EnBolsinEnviado');
INSERT INTO detalle_remito (id, remito_id, documentacion_id, area_cmc_destino) VALUES
  (11, 307, 9040, NULL),
  (12, 307, 9041, NULL),
  (13, 308, 9042, NULL);
INSERT INTO cambio_estado_documentacion (documentacion_id, fecha_hora_inicio, fecha_hora_fin, estado, responsable_ce_id) VALUES
  (9040, '2026-11-01 08:00:00', '2026-11-03 09:10:00', 'Registrada', 2),
  (9040, '2026-11-03 09:10:00', '2026-11-05 11:00:00', 'EnRemito', 2),
  (9040, '2026-11-05 11:00:00', '2026-11-06 14:00:00', 'EnBolsinSaliente', 2),
  (9040, '2026-11-06 14:00:00', NULL, 'EnBolsinEnviado', 2),
  (9041, '2026-11-01 08:00:00', '2026-11-03 09:10:00', 'Registrada', 2),
  (9041, '2026-11-03 09:10:00', '2026-11-05 11:00:00', 'EnRemito', 2),
  (9041, '2026-11-05 11:00:00', '2026-11-06 14:00:00', 'EnBolsinSaliente', 2),
  (9041, '2026-11-06 14:00:00', NULL, 'EnBolsinEnviado', 2),
  (9042, '2026-11-01 08:00:00', '2026-11-03 09:10:00', 'Registrada', 2),
  (9042, '2026-11-03 09:10:00', '2026-11-05 11:00:00', 'EnRemito', 2),
  (9042, '2026-11-05 11:00:00', '2026-11-06 14:00:00', 'EnBolsinSaliente', 2),
  (9042, '2026-11-06 14:00:00', NULL, 'EnBolsinEnviado', 2);

-- ---------------------------------------------------------------------
-- Bolsin 110: A5: enviado a la CMC, con documentacion para redirigir a un area interna
-- ---------------------------------------------------------------------
INSERT INTO bolsin (id, numero_bolsin, numero_precinto, fecha, peso, cm_origen_id, cm_destino_id) VALUES
  (110, 110, 4500, '2026-11-05 11:00:00', 1.5, 2, 5);
INSERT INTO cambio_estado_bolsin (bolsin_id, fecha_hora_inicio, fecha_hora_fin, estado_id, responsable_ce_id) VALUES
  (110, '2026-11-05 11:00:00', '2026-11-05 17:00:00', 1, 2),
  (110, '2026-11-05 17:00:00', '2026-11-06 14:00:00', 2, 2),
  (110, '2026-11-06 14:00:00', NULL, 3, 2);
INSERT INTO remito (id, fecha, numero, bolsin_id, estado_id, cm_origen_id, cm_destino_id) VALUES
  (311, '2026-11-03 09:10:00', 311, 110, 8, 2, 5),
  (312, '2026-11-03 09:10:00', 312, 110, 8, 2, 5);
INSERT INTO documentacion (id, numero, asunto, fecha_pase, tipo_documento_id, cm_origen_id, estado_actual) VALUES
  (9060, 9060, 'Expediente de apelación', '2026-11-02 00:00:00', 1, 2, 'EnBolsinEnviado'),
  (9061, 9061, 'Dictamen de comisión médica', '2026-11-02 00:00:00', 2, 2, 'EnBolsinEnviado'),
  (9062, 9062, 'Estudio de resonancia', '2026-11-02 00:00:00', 3, 2, 'EnBolsinEnviado');
INSERT INTO detalle_remito (id, remito_id, documentacion_id, area_cmc_destino) VALUES
  (14, 311, 9060, NULL),
  (15, 311, 9061, NULL),
  (16, 312, 9062, NULL);
INSERT INTO cambio_estado_documentacion (documentacion_id, fecha_hora_inicio, fecha_hora_fin, estado, responsable_ce_id) VALUES
  (9060, '2026-11-01 08:00:00', '2026-11-03 09:10:00', 'Registrada', 2),
  (9060, '2026-11-03 09:10:00', '2026-11-05 11:00:00', 'EnRemito', 2),
  (9060, '2026-11-05 11:00:00', '2026-11-06 14:00:00', 'EnBolsinSaliente', 2),
  (9060, '2026-11-06 14:00:00', NULL, 'EnBolsinEnviado', 2),
  (9061, '2026-11-01 08:00:00', '2026-11-03 09:10:00', 'Registrada', 2),
  (9061, '2026-11-03 09:10:00', '2026-11-05 11:00:00', 'EnRemito', 2),
  (9061, '2026-11-05 11:00:00', '2026-11-06 14:00:00', 'EnBolsinSaliente', 2),
  (9061, '2026-11-06 14:00:00', NULL, 'EnBolsinEnviado', 2),
  (9062, '2026-11-01 08:00:00', '2026-11-03 09:10:00', 'Registrada', 2),
  (9062, '2026-11-03 09:10:00', '2026-11-05 11:00:00', 'EnRemito', 2),
  (9062, '2026-11-05 11:00:00', '2026-11-06 14:00:00', 'EnBolsinSaliente', 2),
  (9062, '2026-11-06 14:00:00', NULL, 'EnBolsinEnviado', 2);

-- ---------------------------------------------------------------------
-- Bolsin 109: para Rosario pero CERRADO (no enviado): no debe aparecer
-- ---------------------------------------------------------------------
INSERT INTO bolsin (id, numero_bolsin, numero_precinto, fecha, peso, cm_origen_id, cm_destino_id) VALUES
  (109, 109, 4490, '2026-11-05 11:00:00', 0.9, 2, 1);
INSERT INTO cambio_estado_bolsin (bolsin_id, fecha_hora_inicio, fecha_hora_fin, estado_id, responsable_ce_id) VALUES
  (109, '2026-11-05 11:00:00', '2026-11-05 17:00:00', 1, 2),
  (109, '2026-11-05 17:00:00', NULL, 2, 2);
INSERT INTO remito (id, fecha, numero, bolsin_id, estado_id, cm_origen_id, cm_destino_id) VALUES
  (309, '2026-11-03 09:10:00', 309, 109, 7, 2, 1),
  (310, '2026-11-03 09:10:00', 310, 109, 7, 2, 1);
INSERT INTO documentacion (id, numero, asunto, fecha_pase, tipo_documento_id, cm_origen_id, estado_actual) VALUES
  (9050, 9050, 'Expediente de prestaciones', '2026-11-02 00:00:00', 1, 2, 'EnBolsinSaliente'),
  (9051, 9051, 'Estudio cardiológico', '2026-11-02 00:00:00', 3, 2, 'EnBolsinSaliente');
INSERT INTO detalle_remito (id, remito_id, documentacion_id, area_cmc_destino) VALUES
  (17, 309, 9050, NULL),
  (18, 310, 9051, NULL);
INSERT INTO cambio_estado_documentacion (documentacion_id, fecha_hora_inicio, fecha_hora_fin, estado, responsable_ce_id) VALUES
  (9050, '2026-11-01 08:00:00', '2026-11-03 09:10:00', 'Registrada', 2),
  (9050, '2026-11-03 09:10:00', '2026-11-05 11:00:00', 'EnRemito', 2),
  (9050, '2026-11-05 11:00:00', NULL, 'EnBolsinSaliente', 2),
  (9051, '2026-11-01 08:00:00', '2026-11-03 09:10:00', 'Registrada', 2),
  (9051, '2026-11-03 09:10:00', '2026-11-05 11:00:00', 'EnRemito', 2),
  (9051, '2026-11-05 11:00:00', NULL, 'EnBolsinSaliente', 2);
