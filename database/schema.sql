-- ============================================================================
-- SICA - Sistema Integrado de Control de Acceso para Zona Acme
-- Script de Creación de Base de Datos
-- ============================================================================

-- Eliminar la base de datos si existe (solo para desarrollo)
DROP DATABASE IF EXISTS sica;

-- Crear la base de datos
CREATE DATABASE sica CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Usar la base de datos
USE sica;

-- ============================================================================
-- TABLAS DE SEGURIDAD Y RBAC
-- ============================================================================

-- Tabla: roles
-- Descripción: Almacena los roles del sistema (Superusuario, Guarda, etc.)
CREATE TABLE roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre_rol VARCHAR(50) UNIQUE NOT NULL,
    descripcion TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: permisos
-- Descripción: Almacena los permisos del sistema (gestionar_usuarios, registrar_visita, etc.)
CREATE TABLE permisos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre_permiso VARCHAR(100) UNIQUE NOT NULL,
    descripcion TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: rol_permisos
-- Descripción: Relación muchos a muchos entre roles y permisos
CREATE TABLE rol_permisos (
    rol_id INT NOT NULL,
    permiso_id INT NOT NULL,
    PRIMARY KEY (rol_id, permiso_id),
    FOREIGN KEY (rol_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permiso_id) REFERENCES permisos(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: usuarios
-- Descripción: Almacena los usuarios del sistema
CREATE TABLE usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    rol_id INT NOT NULL,
    esta_activo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (rol_id) REFERENCES roles(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- TABLAS DE ESTADOS
-- ============================================================================

-- Tabla: persona_estados_acceso
-- Descripción: Estados de acceso de personas (Activo, Con Prohibición de Ingreso)
CREATE TABLE persona_estados_acceso (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre_estado VARCHAR(50) UNIQUE NOT NULL,
    descripcion TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: visita_estados
-- Descripción: Estados de visitas (Dentro, Fuera, Pendiente de Aprobación, etc.)
CREATE TABLE visita_estados (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre_estado VARCHAR(50) UNIQUE NOT NULL,
    descripcion TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- TABLAS DE DOMINIO
-- ============================================================================

-- Tabla: empresas
-- Descripción: Empresas del complejo Zona Acme
CREATE TABLE empresas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre_empresa VARCHAR(100) NOT NULL,
    nit VARCHAR(50) NOT NULL UNIQUE,
    telefono VARCHAR(20),
    email VARCHAR(100),
    direccion VARCHAR(255),
    esta_activa BOOLEAN DEFAULT TRUE,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: personas
-- Descripción: Personas (Trabajadores e Invitados)
CREATE TABLE personas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    documento_identidad VARCHAR(20) UNIQUE NOT NULL,
    empresa_id INT,
    tipo_persona ENUM('Trabajador', 'Invitado') NOT NULL,
    estado_acceso_id INT NOT NULL,
    url_foto VARCHAR(255),
    telefono VARCHAR(20),
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id),
    FOREIGN KEY (estado_acceso_id) REFERENCES persona_estados_acceso(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: visitas
-- Descripción: Registro de visitas (entradas y salidas)
CREATE TABLE visitas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    persona_id INT NOT NULL,
    fecha_entrada DATETIME,
    fecha_salida DATETIME,
    estado_visita_id INT NOT NULL,
    vehiculo_placa VARCHAR(10),
    visita_aprobada_por INT,
    motivo_visita TEXT,
    observaciones TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (persona_id) REFERENCES personas(id),
    FOREIGN KEY (estado_visita_id) REFERENCES visita_estados(id),
    FOREIGN KEY (visita_aprobada_por) REFERENCES usuarios(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: incidentes
-- Descripción: Registro de incidentes de seguridad
CREATE TABLE incidentes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    visita_id INT,
    reportado_por_id INT NOT NULL,
    fecha DATETIME NOT NULL,
    descripcion TEXT NOT NULL,
    tipo_incidente VARCHAR(50),
    nivel_gravedad ENUM('Bajo', 'Medio', 'Alto', 'Crítico') DEFAULT 'Medio',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (visita_id) REFERENCES visitas(id),
    FOREIGN KEY (reportado_por_id) REFERENCES usuarios(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- TABLA DE AUDITORÍA
-- ============================================================================

-- Tabla: bitacora_auditoria
-- Descripción: Registro de todas las operaciones críticas del sistema
CREATE TABLE bitacora_auditoria (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT,
    fecha_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    accion_realizada VARCHAR(255) NOT NULL,
    tabla_afectada VARCHAR(100),
    registro_id_afectado INT,
    detalles TEXT,
    ip_address VARCHAR(45),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- ÍNDICES PARA MEJORAR EL RENDIMIENTO
-- ============================================================================

-- Índices para búsquedas frecuentes
CREATE INDEX idx_usuarios_email ON usuarios(email);
CREATE INDEX idx_usuarios_rol_id ON usuarios(rol_id);
CREATE INDEX idx_personas_documento ON personas(documento_identidad);
CREATE INDEX idx_personas_empresa ON personas(empresa_id);
CREATE INDEX idx_visitas_persona ON visitas(persona_id);
CREATE INDEX idx_visitas_estado ON visitas(estado_visita_id);
CREATE INDEX idx_visitas_fecha_entrada ON visitas(fecha_entrada);
CREATE INDEX idx_incidentes_fecha ON incidentes(fecha);
CREATE INDEX idx_bitacora_usuario ON bitacora_auditoria(usuario_id);
CREATE INDEX idx_bitacora_fecha ON bitacora_auditoria(fecha_hora);

-- ============================================================================
-- VISTAS ÚTILES
-- ============================================================================

-- Vista: personas_dentro_complejo
-- Descripción: Muestra todas las personas actualmente dentro del complejo
CREATE VIEW personas_dentro_complejo AS
SELECT 
    p.id AS persona_id,
    p.nombre,
    p.documento_identidad,
    p.tipo_persona,
    e.nombre_empresa AS empresa,
    v.id AS visita_id,
    v.fecha_entrada,
    v.vehiculo_placa
FROM personas p
INNER JOIN visitas v ON p.id = v.persona_id
INNER JOIN visita_estados ve ON v.estado_visita_id = ve.id
LEFT JOIN empresas e ON p.empresa_id = e.id
WHERE ve.nombre_estado = 'Dentro';

-- Vista: visitas_pendientes_aprobacion
-- Descripción: Muestra visitas que esperan aprobación
CREATE VIEW visitas_pendientes_aprobacion AS
SELECT 
    v.id AS visita_id,
    p.nombre AS persona_nombre,
    p.documento_identidad,
    p.tipo_persona,
    e.nombre_empresa AS empresa,
    v.motivo_visita,
    v.created_at AS fecha_solicitud,
    ve.nombre_estado AS estado
FROM visitas v
INNER JOIN personas p ON v.persona_id = p.id
INNER JOIN visita_estados ve ON v.estado_visita_id = ve.id
LEFT JOIN empresas e ON p.empresa_id = e.id
WHERE ve.nombre_estado IN ('Pendiente de Aprobación', 'Pendiente de Aprobación por Olvido');

-- Vista: historial_accesos_reciente
-- Descripción: Historial de los últimos 100 accesos
CREATE VIEW historial_accesos_reciente AS
SELECT 
    v.id AS visita_id,
    p.nombre,
    p.documento_identidad,
    p.tipo_persona,
    e.nombre_empresa AS empresa,
    v.fecha_entrada,
    v.fecha_salida,
    ve.nombre_estado AS estado,
    TIMESTAMPDIFF(MINUTE, v.fecha_entrada, COALESCE(v.fecha_salida, NOW())) AS minutos_permanencia
FROM visitas v
INNER JOIN personas p ON v.persona_id = p.id
INNER JOIN visita_estados ve ON v.estado_visita_id = ve.id
LEFT JOIN empresas e ON p.empresa_id = e.id
ORDER BY v.fecha_entrada DESC
LIMIT 100;

-- ============================================================================
-- SCRIPT COMPLETADO
-- ============================================================================

SELECT 'Base de datos SICA creada exitosamente' AS mensaje;
