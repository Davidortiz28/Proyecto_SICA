package com.sica.model.enums;

/**
 * Enumeración que representa las acciones auditables del sistema.
 * 
 * Todas las operaciones críticas deben registrarse en la bitácora de auditoría
 * usando una de estas acciones predefinidas.
 * 
 * Principio SOLID aplicado:
 * - Single Responsibility: Solo define las acciones auditables
 * - Open/Closed: Se pueden agregar nuevas acciones sin modificar código existente
 * 
 * @author SICA Team
 * @version 1.0
 */
public enum AccionAuditoria {
    // Acciones de Autenticación
    LOGIN_EXITOSO("Login exitoso"),
    LOGIN_FALLIDO("Login fallido"),
    LOGOUT("Cierre de sesión"),
    
    // Acciones de Gestión de Usuarios
    CREACION_USUARIO("Creación de usuario"),
    ACTUALIZACION_USUARIO("Actualización de usuario"),
    ELIMINACION_USUARIO("Eliminación de usuario"),
    CAMBIO_ESTADO_USUARIO("Cambio de estado de usuario"),
    
    // Acciones de Gestión de Roles y Permisos
    CREACION_ROL("Creación de rol"),
    ACTUALIZACION_ROL("Actualización de rol"),
    ELIMINACION_ROL("Eliminación de rol"),
    ASIGNACION_PERMISO("Asignación de permiso a rol"),
    REMOCION_PERMISO("Remoción de permiso de rol"),
    
    // Acciones de Gestión de Empresas
    CREACION_EMPRESA("Creación de empresa"),
    ACTUALIZACION_EMPRESA("Actualización de empresa"),
    ELIMINACION_EMPRESA("Eliminación de empresa"),
    
    // Acciones de Gestión de Personas
    CREACION_PERSONA("Creación de persona"),
    ACTUALIZACION_PERSONA("Actualización de persona"),
    ELIMINACION_PERSONA("Eliminación de persona"),
    CAMBIO_ESTADO_PERSONA("Cambio de estado de acceso de persona"),
    
    // Acciones de Control de Acceso (Visitas)
    CREACION_VISITA("Creación de visita"),
    CHECK_IN("Registro de entrada (check-in)"),
    CHECK_OUT("Registro de salida (check-out)"),
    APROBACION_VISITA("Aprobación de visita"),
    RECHAZO_VISITA("Rechazo de visita"),
    REGULARIZACION_SALIDA_OLVIDADA("Regularización de salida olvidada"),
    APROBACION_TRABAJADOR_SIN_CARNET("Aprobación de trabajador sin carnet"),
    
    // Acciones de Gestión de Incidentes
    CREACION_INCIDENTE("Creación de incidente"),
    ACTUALIZACION_INCIDENTE("Actualización de incidente"),
    
    // Acciones de Reportes
    GENERACION_REPORTE("Generación de reporte"),
    CONSULTA_BITACORA("Consulta de bitácora de auditoría"),
    
    // Acciones de Sistema
    INTENTO_ACCESO_DENEGADO("Intento de acceso denegado por permisos"),
    ERROR_SISTEMA("Error del sistema");
    
    private final String descripcion;
    
    /**
     * Constructor privado del enum
     * 
     * @param descripcion Descripción legible de la acción
     */
    AccionAuditoria(String descripcion) {
        this.descripcion = descripcion;
    }
    
    /**
     * Obtiene la descripción de la acción de auditoría
     * 
     * @return Descripción legible
     */
    public String getDescripcion() {
        return descripcion;
    }
    
    /**
     * Convierte una cadena a su acción correspondiente
     * 
     * @param texto Texto a convertir
     * @return AccionAuditoria correspondiente
     * @throws IllegalArgumentException si el texto no coincide con ninguna acción
     */
    public static AccionAuditoria fromString(String texto) {
        for (AccionAuditoria accion : AccionAuditoria.values()) {
            if (accion.name().equalsIgnoreCase(texto)) {
                return accion;
            }
        }
        throw new IllegalArgumentException("Acción de auditoría no válida: " + texto);
    }
    
    /**
     * Verifica si la acción es crítica (requiere registro obligatorio)
     * 
     * @return true si es crítica, false en caso contrario
     */
    public boolean esCritica() {
        switch (this) {
            case LOGIN_EXITOSO:
            case LOGIN_FALLIDO:
            case CREACION_USUARIO:
            case ELIMINACION_USUARIO:
            case CAMBIO_ESTADO_PERSONA:
            case CHECK_IN:
            case CHECK_OUT:
            case APROBACION_VISITA:
            case RECHAZO_VISITA:
            case REGULARIZACION_SALIDA_OLVIDADA:
            case CREACION_INCIDENTE:
            case INTENTO_ACCESO_DENEGADO:
                return true;
            default:
                return false;
        }
    }
    
    @Override
    public String toString() {
        return descripcion;
    }
}
