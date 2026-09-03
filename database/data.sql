-- ============================================================================
-- SICA - Sistema Integrado de Control de Acceso para Zona Acme
-- Script de Datos Iniciales (Datos de Prueba)
-- ============================================================================

USE sica;

-- ============================================================================
-- DATOS: ESTADOS
-- ============================================================================

-- Estados de Acceso de Personas
INSERT INTO persona_estados_acceso (nombre_estado, descripcion) VALUES
('Activo', 'Persona puede ingresar normalmente al complejo'),
('Con Prohibición de Ingreso', 'Persona bloqueada, no puede ingresar al complejo');

-- Estados de Visitas
INSERT INTO visita_estados (nombre_estado, descripcion) VALUES
('Dentro', 'Persona actualmente dentro del complejo'),
('Fuera', 'Persona salió del complejo'),
('Pendiente de Aprobación', 'Visita esperando aprobación de funcionario'),
('Aprobado', 'Visita pre-aprobada, puede hacer check-in'),
('Rechazado', 'Visita rechazada, no puede ingresar'),
('Expirado', 'Visita aprobada pero no se presentó en el tiempo esperado'),
('Cerrada por Sistema (Salida Olvidada)', 'Sistema cerró automáticamente por salida no registrada'),
('Pendiente de Aprobación por Olvido', 'Trabajador sin carnet esperando aprobación');

-- ============================================================================
-- DATOS: ROLES
-- ============================================================================

INSERT INTO roles (nombre_rol, descripcion) VALUES
('Superusuario', 'Acceso completo al sistema'),
('Supervisor de Seguridad', 'Gestión de seguridad, incidentes y reportes'),
('Guarda de Seguridad', 'Control de acceso diario (entradas y salidas)'),
('Funcionario de Empresa', 'Gestión de invitados y aprobaciones de su empresa');

-- ============================================================================
-- DATOS: PERMISOS
-- ============================================================================

INSERT INTO permisos (nombre_permiso, descripcion) VALUES
-- Permisos de Gestión de Usuarios
('gestionar_usuarios', 'Crear, modificar y eliminar usuarios del sistema'),
('ver_usuarios', 'Visualizar lista de usuarios'),

-- Permisos de Gestión de Roles y Permisos
('gestionar_roles', 'Crear, modificar y eliminar roles'),
('gestionar_permisos', 'Asignar y remover permisos a roles'),

-- Permisos de Gestión de Empresas
('gestionar_empresas', 'Crear, modificar y eliminar empresas'),
('ver_empresas', 'Visualizar lista de empresas'),

-- Permisos de Gestión de Personas
('gestionar_personas', 'Crear, modificar y eliminar personas'),
('ver_personas', 'Visualizar lista de personas'),
('buscar_persona', 'Buscar personas por documento o nombre'),
('bloquear_persona', 'Cambiar estado de acceso de una persona'),

-- Permisos de Gestión de Visitas
('registrar_invitado', 'Pre-registrar invitados'),
('registrar_visita', 'Crear visitas no anunciadas'),
('registrar_check_in', 'Registrar entrada de personas'),
('registrar_check_out', 'Registrar salida de personas'),
('aprobar_visita', 'Aprobar visitas pendientes'),
('rechazar_visita', 'Rechazar visitas pendientes'),
('consultar_visitas', 'Consultar historial de visitas'),

-- Permisos de Gestión de Incidentes
('registrar_incidente', 'Registrar incidentes de seguridad'),
('consultar_incidentes', 'Consultar historial de incidentes'),

-- Permisos de Reportes
('generar_reporte', 'Generar reportes del sistema'),
('generar_reporte_auditoria', 'Generar reportes de auditoría'),

-- Permisos de Auditoría
('consultar_bitacora', 'Consultar bitácora de auditoría');

-- ============================================================================
-- DATOS: ASIGNACIÓN DE PERMISOS A ROLES
-- ============================================================================

-- Rol: Superusuario (TODOS los permisos)
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT 1, id FROM permisos;

-- Rol: Supervisor de Seguridad
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT 2, id FROM permisos WHERE nombre_permiso IN (
    'ver_usuarios',
    'ver_empresas',
    'gestionar_personas',
    'ver_personas',
    'buscar_persona',
    'bloquear_persona',
    'registrar_visita',
    'registrar_check_in',
    'registrar_check_out',
    'consultar_visitas',
    'registrar_incidente',
    'consultar_incidentes',
    'generar_reporte',
    'generar_reporte_auditoria',
    'consultar_bitacora'
);

-- Rol: Guarda de Seguridad
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT 3, id FROM permisos WHERE nombre_permiso IN (
    'buscar_persona',
    'ver_personas',
    'registrar_visita',
    'registrar_check_in',
    'registrar_check_out',
    'consultar_visitas'
);

-- Rol: Funcionario de Empresa
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT 4, id FROM permisos WHERE nombre_permiso IN (
    'buscar_persona',
    'ver_personas',
    'registrar_invitado',
    'aprobar_visita',
    'rechazar_visita',
    'consultar_visitas'
);

-- ============================================================================
-- DATOS: USUARIOS DE PRUEBA
-- ============================================================================

-- NOTA: En un sistema real, las contraseñas deben estar hasheadas (BCrypt, Argon2, etc.)
-- Estas son contraseñas en texto plano SOLO para propósitos de desarrollo/prueba

INSERT INTO usuarios (nombre, email, password, rol_id, esta_activo) VALUES
('Administrador del Sistema', 'superuser@sica.com', 'super123', 1, TRUE),
('Carlos Ramírez', 'supervisor@sica.com', 'supervisor123', 2, TRUE),
('Ana García', 'guarda@sica.com', 'guarda123', 3, TRUE),
('Pedro López', 'guarda2@sica.com', 'guarda123', 3, TRUE),
('María Fernández', 'funcionario@empresaA.com', 'funcionario123', 4, TRUE),
('Juan Martínez', 'funcionario@empresaB.com', 'funcionario123', 4, TRUE),
('Laura Sánchez', 'funcionario@empresaC.com', 'funcionario123', 4, TRUE);

-- ============================================================================
-- DATOS: EMPRESAS
-- ============================================================================

INSERT INTO empresas (nombre, contacto_principal, telefono, email) VALUES
('Tech Solutions S.A.', 'Roberto Gómez', '+1-555-0101', 'contacto@techsolutions.com'),
('Marketing Global Inc.', 'Sofía Morales', '+1-555-0102', 'info@marketingglobal.com'),
('Consulting Partners', 'Diego Torres', '+1-555-0103', 'admin@consultingpartners.com'),
('Design Studio', 'Carmen Ruiz', '+1-555-0104', 'hello@designstudio.com'),
('Financial Services Corp.', 'Andrés Vega', '+1-555-0105', 'contact@financialservices.com'),
('Legal Associates', 'Patricia Medina', '+1-555-0106', 'info@legalassociates.com'),
('Engineering Works', 'Miguel Castro', '+1-555-0107', 'team@engineeringworks.com'),
('Health Care Solutions', 'Elena Romero', '+1-555-0108', 'support@healthcare.com');

-- ============================================================================
-- DATOS: PERSONAS (Trabajadores)
-- ============================================================================

INSERT INTO personas (nombre, documento_identidad, empresa_id, tipo_persona, estado_acceso_id, telefono, email) VALUES
-- Trabajadores de Tech Solutions S.A.
('Luis Hernández', 'DOC-1001', 1, 'Trabajador', 1, '+1-555-1001', 'luis.hernandez@techsolutions.com'),
('Andrea Silva', 'DOC-1002', 1, 'Trabajador', 1, '+1-555-1002', 'andrea.silva@techsolutions.com'),
('Ricardo Ortiz', 'DOC-1003', 1, 'Trabajador', 1, '+1-555-1003', 'ricardo.ortiz@techsolutions.com'),

-- Trabajadores de Marketing Global Inc.
('Gabriela Rojas', 'DOC-2001', 2, 'Trabajador', 1, '+1-555-2001', 'gabriela.rojas@marketingglobal.com'),
('Fernando Paz', 'DOC-2002', 2, 'Trabajador', 1, '+1-555-2002', 'fernando.paz@marketingglobal.com'),

-- Trabajadores de Consulting Partners
('Valeria Campos', 'DOC-3001', 3, 'Trabajador', 1, '+1-555-3001', 'valeria.campos@consultingpartners.com'),
('Sebastián Cruz', 'DOC-3002', 3, 'Trabajador', 1, '+1-555-3002', 'sebastian.cruz@consultingpartners.com'),

-- Trabajadores de Design Studio
('Carolina Méndez', 'DOC-4001', 4, 'Trabajador', 1, '+1-555-4001', 'carolina.mendez@designstudio.com'),

-- Trabajadores de Financial Services Corp.
('Javier Núñez', 'DOC-5001', 5, 'Trabajador', 1, '+1-555-5001', 'javier.nunez@financialservices.com'),
('Daniela Paredes', 'DOC-5002', 5, 'Trabajador', 1, '+1-555-5002', 'daniela.paredes@financialservices.com'),

-- Trabajadores de Legal Associates
('Alejandro Guzmán', 'DOC-6001', 6, 'Trabajador', 1, '+1-555-6001', 'alejandro.guzman@legalassociates.com'),

-- Trabajadores de Engineering Works
('Natalia Vargas', 'DOC-7001', 7, 'Trabajador', 1, '+1-555-7001', 'natalia.vargas@engineeringworks.com'),
('Rodrigo Peña', 'DOC-7002', 7, 'Trabajador', 1, '+1-555-7002', 'rodrigo.pena@engineeringworks.com'),

-- Trabajadores de Health Care Solutions
('Camila Reyes', 'DOC-8001', 8, 'Trabajador', 1, '+1-555-8001', 'camila.reyes@healthcare.com'),

-- Trabajador con prohibición de ingreso (ejemplo de bloqueo)
('Pablo Moreno', 'DOC-9999', 1, 'Trabajador', 2, '+1-555-9999', 'pablo.moreno@blocked.com');

-- ============================================================================
-- DATOS: PERSONAS (Invitados Pre-Registrados)
-- ============================================================================

INSERT INTO personas (nombre, documento_identidad, empresa_id, tipo_persona, estado_acceso_id, telefono) VALUES
-- Invitados pre-registrados
('Carlos Visitor', 'INV-1001', 1, 'Invitado', 1, '+1-555-8001'),
('Laura Guest', 'INV-1002', 2, 'Invitado', 1, '+1-555-8002'),
('Martín Cliente', 'INV-1003', 3, 'Invitado', 1, '+1-555-8003'),
('Sandra Proveedor', 'INV-1004', 4, 'Invitado', 1, '+1-555-8004');

-- ============================================================================
-- DATOS: VISITAS (Ejemplos de diferentes estados)
-- ============================================================================

-- Visitas actuales (personas dentro del complejo)
INSERT INTO visitas (persona_id, fecha_entrada, fecha_salida, estado_visita_id, visita_aprobada_por, motivo_visita) VALUES
(1, '2026-09-02 08:30:00', NULL, 1, NULL, 'Jornada laboral normal'),
(2, '2026-09-02 08:45:00', NULL, 1, NULL, 'Jornada laboral normal'),
(4, '2026-09-02 09:00:00', NULL, 1, NULL, 'Jornada laboral normal'),
(16, '2026-09-02 10:00:00', NULL, 1, 5, 'Reunión con Tech Solutions');

-- Visitas completadas (personas que ya salieron)
INSERT INTO visitas (persona_id, fecha_entrada, fecha_salida, estado_visita_id, visita_aprobada_por, motivo_visita) VALUES
(3, '2026-09-01 09:00:00', '2026-09-01 18:30:00', 2, NULL, 'Jornada laboral'),
(5, '2026-09-01 08:30:00', '2026-09-01 17:00:00', 2, NULL, 'Jornada laboral'),
(17, '2026-09-01 14:00:00', '2026-09-01 16:30:00', 2, 5, 'Presentación de propuesta'),
(18, '2026-09-01 10:00:00', '2026-09-01 12:00:00', 2, 6, 'Capacitación');

-- Visitas pre-aprobadas (invitados que aún no llegan)
INSERT INTO visitas (persona_id, fecha_entrada, fecha_salida, estado_visita_id, visita_aprobada_por, motivo_visita) VALUES
(18, NULL, NULL, 4, 6, 'Reunión programada para hoy'),
(19, NULL, NULL, 4, 7, 'Entrevista de trabajo');

-- Visitas pendientes de aprobación
INSERT INTO visitas (persona_id, fecha_entrada, fecha_salida, estado_visita_id, visita_aprobada_por, motivo_visita) VALUES
(17, NULL, NULL, 3, NULL, 'Visita no anunciada');

-- Visita rechazada
INSERT INTO visitas (persona_id, fecha_entrada, fecha_salida, estado_visita_id, visita_aprobada_por, motivo_visita) VALUES
(19, NULL, NULL, 5, 5, 'No autorizado');

-- ============================================================================
-- DATOS: INCIDENTES (Ejemplos)
-- ============================================================================

INSERT INTO incidentes (visita_id, reportado_por_id, fecha, descripcion, tipo_incidente, nivel_gravedad) VALUES
(1, 2, '2026-09-01 15:30:00', 'Se detectó intento de acceso a zona restringida sin autorización', 'Acceso no autorizado', 'Medio'),
(3, 3, '2026-09-01 12:00:00', 'Vehículo estacionado en zona prohibida', 'Infracción de estacionamiento', 'Bajo'),
(NULL, 2, '2026-08-30 18:00:00', 'Falla en el sistema de identificación de puerta principal', 'Falla técnica', 'Alto');

-- ============================================================================
-- DATOS: AUDITORÍA (Ejemplos de registros iniciales)
-- ============================================================================

INSERT INTO bitacora_auditoria (usuario_id, accion_realizada, tabla_afectada, registro_id_afectado, detalles, ip_address) VALUES
-- Logins exitosos
(1, 'LOGIN_EXITOSO', 'usuarios', 1, 'Superusuario inició sesión', '192.168.1.100'),
(2, 'LOGIN_EXITOSO', 'usuarios', 2, 'Supervisor de Seguridad inició sesión', '192.168.1.101'),
(3, 'LOGIN_EXITOSO', 'usuarios', 3, 'Guarda de Seguridad inició sesión', '192.168.1.102'),

-- Creación de empresas
(1, 'CREACION_EMPRESA', 'empresas', 1, 'Empresa Tech Solutions S.A. creada', '192.168.1.100'),
(1, 'CREACION_EMPRESA', 'empresas', 2, 'Empresa Marketing Global Inc. creada', '192.168.1.100'),

-- Creación de personas
(1, 'CREACION_PERSONA', 'personas', 1, 'Trabajador Luis Hernández registrado', '192.168.1.100'),
(1, 'CREACION_PERSONA', 'personas', 2, 'Trabajador Andrea Silva registrado', '192.168.1.100'),

-- Check-ins
(3, 'CHECK_IN', 'visitas', 1, 'Luis Hernández (DOC-1001) ingresó al complejo', '192.168.1.102'),
(3, 'CHECK_IN', 'visitas', 2, 'Andrea Silva (DOC-1002) ingresó al complejo', '192.168.1.102'),

-- Check-outs
(3, 'CHECK_OUT', 'visitas', 3, 'Ricardo Ortiz (DOC-1003) salió del complejo', '192.168.1.102'),
(3, 'CHECK_OUT', 'visitas', 4, 'Fernando Paz (DOC-2002) salió del complejo', '192.168.1.102'),

-- Bloqueo de persona
(2, 'CAMBIO_ESTADO_PERSONA', 'personas', 15, 'Pablo Moreno bloqueado: Con Prohibición de Ingreso', '192.168.1.101'),

-- Creación de incidente
(2, 'CREACION_INCIDENTE', 'incidentes', 1, 'Incidente de seguridad registrado: Acceso no autorizado', '192.168.1.101'),

-- Aprobación de visita
(5, 'APROBACION_VISITA', 'visitas', 10, 'Visita de Laura Guest aprobada', '192.168.1.105'),

-- Rechazo de visita
(5, 'RECHAZO_VISITA', 'visitas', 12, 'Visita rechazada por no cumplir requisitos', '192.168.1.105');

-- ============================================================================
-- SCRIPT COMPLETADO
-- ============================================================================

SELECT 'Datos iniciales insertados exitosamente' AS mensaje;

-- Verificar datos insertados
SELECT 'Resumen de datos cargados:' AS '';
SELECT COUNT(*) AS total_roles FROM roles;
SELECT COUNT(*) AS total_permisos FROM permisos;
SELECT COUNT(*) AS total_usuarios FROM usuarios;
SELECT COUNT(*) AS total_empresas FROM empresas;
SELECT COUNT(*) AS total_personas FROM personas;
SELECT COUNT(*) AS total_visitas FROM visitas;
SELECT COUNT(*) AS total_incidentes FROM incidentes;
SELECT COUNT(*) AS total_registros_auditoria FROM bitacora_auditoria;

-- Mostrar personas actualmente dentro del complejo
SELECT 'Personas actualmente dentro del complejo:' AS '';
SELECT * FROM personas_dentro_complejo;

-- Mostrar visitas pendientes de aprobación
SELECT 'Visitas pendientes de aprobación:' AS '';
SELECT * FROM visitas_pendientes_aprobacion;
