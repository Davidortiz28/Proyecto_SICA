package com.sica.service;

import com.sica.exception.PermisoDenegadoException;
import com.sica.model.entity.Usuario;
import com.sica.security.SessionManager;

/**
 * Servicio de autorización basado en roles y permisos (RBAC).
 * 
 * Este servicio es el CORAZÓN del sistema RBAC.
 * Verifica que los usuarios tengan los permisos necesarios antes de ejecutar operaciones.
 * 
 * IMPORTANTE: Los permisos se obtienen desde la base de datos, NO están hardcodeados.
 * 
 * Principios SOLID aplicados:
 * - Single Responsibility: Solo verifica permisos
 * - Dependency Inversion: Usa abstracciones (SessionManager)
 * 
 * @author SICA Team
 * @version 1.0
 */
public class AuthorizationService {
    
    private final SessionManager sessionManager;
    
    /**
     * Constructor que obtiene el SessionManager (Singleton).
     */
    public AuthorizationService() {
        this.sessionManager = SessionManager.getInstance();
    }
    
    /**
     * Verifica si el usuario actual tiene un permiso específico.
     * 
     * @param nombrePermiso Nombre del permiso a verificar
     * @return true si tiene el permiso, false en caso contrario
     */
    public boolean tienePermiso(String nombrePermiso) {
        if (!sessionManager.haySesionActiva()) {
            return false;
        }
        
        Usuario usuarioActual = sessionManager.getUsuarioActual();
        return usuarioActual.tienePermiso(nombrePermiso);
    }
    
    /**
     * Requiere que el usuario actual tenga un permiso específico.
     * Si no tiene el permiso, lanza PermisoDenegadoException.
     * 
     * USO: Llamar este método al inicio de operaciones protegidas.
     * 
     * @param nombrePermiso Nombre del permiso requerido
     * @throws PermisoDenegadoException si el usuario no tiene el permiso
     * @throws IllegalStateException si no hay sesión activa
     */
    public void requirePermiso(String nombrePermiso) throws PermisoDenegadoException {
        sessionManager.requireSesionActiva();
        
        Usuario usuarioActual = sessionManager.getUsuarioActual();
        
        if (!usuarioActual.tienePermiso(nombrePermiso)) {
            throw new PermisoDenegadoException(nombrePermiso, usuarioActual.getEmail());
        }
    }
    
    /**
     * Verifica si el usuario actual tiene alguno de los permisos dados.
     * 
     * @param permisos Array de permisos a verificar
     * @return true si tiene al menos uno de los permisos, false en caso contrario
     */
    public boolean tieneAlgunoDeEstosPermisos(String... permisos) {
        if (!sessionManager.haySesionActiva()) {
            return false;
        }
        
        Usuario usuarioActual = sessionManager.getUsuarioActual();
        
        for (String permiso : permisos) {
            if (usuarioActual.tienePermiso(permiso)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Verifica si el usuario actual tiene todos los permisos dados.
     * 
     * @param permisos Array de permisos a verificar
     * @return true si tiene todos los permisos, false en caso contrario
     */
    public boolean tieneTodosLosPermisos(String... permisos) {
        if (!sessionManager.haySesionActiva()) {
            return false;
        }
        
        Usuario usuarioActual = sessionManager.getUsuarioActual();
        
        for (String permiso : permisos) {
            if (!usuarioActual.tienePermiso(permiso)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Verifica si el usuario actual es Superusuario.
     * 
     * NOTA: Aunque es útil, en general es mejor verificar permisos específicos.
     * 
     * @return true si es Superusuario, false en caso contrario
     */
    public boolean esSuperusuario() {
        if (!sessionManager.haySesionActiva()) {
            return false;
        }
        
        Usuario usuarioActual = sessionManager.getUsuarioActual();
        return "Superusuario".equalsIgnoreCase(usuarioActual.getRol().getNombreRol());
    }
    
    /**
     * Obtiene el nombre del rol del usuario actual.
     * 
     * @return Nombre del rol o null si no hay sesión
     */
    public String getRolActual() {
        if (!sessionManager.haySesionActiva()) {
            return null;
        }
        
        return sessionManager.getUsuarioActual().getRol().getNombreRol();
    }
    
    /**
     * Lista todos los permisos del usuario actual.
     * Útil para debugging y mostrar en UI.
     * 
     * @return Array de nombres de permisos
     */
    public String[] listarPermisosActuales() {
        if (!sessionManager.haySesionActiva()) {
            return new String[0];
        }
        
        Usuario usuarioActual = sessionManager.getUsuarioActual();
        return usuarioActual.getPermisos().stream()
                .map(p -> p.getNombrePermiso())
                .toArray(String[]::new);
    }
}
