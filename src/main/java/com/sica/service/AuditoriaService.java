package com.sica.service;

import com.sica.database.DatabaseConnection;
import com.sica.model.entity.BitacoraAuditoria;
import com.sica.model.entity.Usuario;
import com.sica.model.enums.AccionAuditoria;
import com.sica.security.SessionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Servicio de auditoría para registrar operaciones críticas del sistema.
 * 
 * Este servicio centraliza el registro de auditoría, evitando código repetitivo
 * en los controllers y otros services.
 * 
 * Principios SOLID aplicados:
 * - Single Responsibility: Solo maneja auditoría
 * - Dependency Inversion: Usa Connection (interfaz)
 * 
 * IMPORTANTE: Este servicio debe ser usado desde otros Services,
 * NO desde Controllers directamente.
 * 
 * @author SICA Team
 * @version 1.0
 */
public class AuditoriaService {
    
    private final Connection connection;
    private final SessionManager sessionManager;
    
    public AuditoriaService() throws SQLException {
        this.connection = DatabaseConnection.getInstance().getConnection();
        this.sessionManager = SessionManager.getInstance();
    }
    
    /**
     * Registra una acción en la bitácora de auditoría.
     * 
     * @param accion Acción realizada
     * @param detalles Detalles adicionales de la operación
     * @return ID del registro de auditoría creado
     * @throws SQLException si hay error al registrar
     */
    public long registrar(AccionAuditoria accion, String detalles) throws SQLException {
        return registrar(accion, null, null, detalles);
    }
    
    /**
     * Registra una acción con tabla y registro afectado.
     * 
     * @param accion Acción realizada
     * @param tablaAfectada Nombre de la tabla afectada
     * @param registroIdAfectado ID del registro afectado
     * @param detalles Detalles adicionales
     * @return ID del registro de auditoría creado
     * @throws SQLException si hay error al registrar
     */
    public long registrar(AccionAuditoria accion, String tablaAfectada, 
                         Integer registroIdAfectado, String detalles) throws SQLException {
        
        Usuario usuarioActual = sessionManager.haySesionActiva() 
                ? sessionManager.getUsuarioActual() 
                : null;
        
        return registrar(usuarioActual, accion, tablaAfectada, registroIdAfectado, detalles);
    }
    
    /**
     * Registra una acción con usuario específico (usado para login fallido, etc).
     * 
     * @param usuario Usuario que realiza la acción (puede ser null)
     * @param accion Acción realizada
     * @param tablaAfectada Tabla afectada (puede ser null)
     * @param registroIdAfectado ID del registro afectado (puede ser null)
     * @param detalles Detalles adicionales
     * @return ID del registro de auditoría creado
     * @throws SQLException si hay error al registrar
     */
    public long registrar(Usuario usuario, AccionAuditoria accion, String tablaAfectada,
                         Integer registroIdAfectado, String detalles) throws SQLException {
        
        String sql = """
            INSERT INTO bitacora_auditoria 
            (usuario_id, accion_realizada, tabla_afectada, registro_id_afectado, detalles, ip_address)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, 
                PreparedStatement.RETURN_GENERATED_KEYS)) {
            
            // Usuario (puede ser null para operaciones sin sesión)
            if (usuario != null) {
                pstmt.setInt(1, usuario.getId());
            } else {
                pstmt.setNull(1, java.sql.Types.INTEGER);
            }
            
            // Acción realizada
            pstmt.setString(2, accion.name());
            
            // Tabla afectada (opcional)
            if (tablaAfectada != null) {
                pstmt.setString(3, tablaAfectada);
            } else {
                pstmt.setNull(3, java.sql.Types.VARCHAR);
            }
            
            // ID del registro afectado (opcional)
            if (registroIdAfectado != null) {
                pstmt.setInt(4, registroIdAfectado);
            } else {
                pstmt.setNull(4, java.sql.Types.INTEGER);
            }
            
            // Detalles
            pstmt.setString(5, detalles);
            
            // IP Address (por ahora null, en aplicación de escritorio no aplica)
            pstmt.setNull(6, java.sql.Types.VARCHAR);
            
            pstmt.executeUpdate();
            
            // Obtener ID generado
            try (var generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
            }
        }
        
        return 0;
    }
    
    /**
     * Registra un intento de login exitoso.
     * 
     * @param usuario Usuario que inició sesión
     * @throws SQLException si hay error al registrar
     */
    public void registrarLoginExitoso(Usuario usuario) throws SQLException {
        registrar(usuario, AccionAuditoria.LOGIN_EXITOSO, "usuarios", usuario.getId(),
                "Usuario '" + usuario.getNombre() + "' inició sesión exitosamente");
    }
    
    /**
     * Registra un intento de login fallido.
     * 
     * @param email Email del intento de login
     * @param motivo Motivo del fallo
     * @throws SQLException si hay error al registrar
     */
    public void registrarLoginFallido(String email, String motivo) throws SQLException {
        registrar(null, AccionAuditoria.LOGIN_FALLIDO, null, null,
                "Intento de login fallido para email: " + email + ". Motivo: " + motivo);
    }
    
    /**
     * Registra un logout.
     * 
     * @param usuario Usuario que cerró sesión
     * @throws SQLException si hay error al registrar
     */
    public void registrarLogout(Usuario usuario) throws SQLException {
        registrar(usuario, AccionAuditoria.LOGOUT, "usuarios", usuario.getId(),
                "Usuario '" + usuario.getNombre() + "' cerró sesión");
    }
    
    /**
     * Registra un intento de acceso denegado por permisos.
     * 
     * @param permisoRequerido Permiso que no tenía el usuario
     * @throws SQLException si hay error al registrar
     */
    public void registrarAccesoDenegado(String permisoRequerido) throws SQLException {
        Usuario usuarioActual = sessionManager.getUsuarioActual();
        String detalles = String.format(
            "Usuario '%s' (rol: %s) intentó realizar operación sin permiso: %s",
            usuarioActual.getNombre(),
            usuarioActual.getRol().getNombreRol(),
            permisoRequerido
        );
        
        registrar(AccionAuditoria.INTENTO_ACCESO_DENEGADO, detalles);
    }
    
    /**
     * Verifica si una acción es crítica (debe registrarse obligatoriamente).
     * 
     * @param accion Acción a verificar
     * @return true si es crítica, false en caso contrario
     */
    public boolean esAccionCritica(AccionAuditoria accion) {
        return accion.esCritica();
    }
}
