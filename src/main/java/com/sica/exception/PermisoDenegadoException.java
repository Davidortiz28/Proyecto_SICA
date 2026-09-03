package com.sica.exception;

/**
 * Excepción lanzada cuando un usuario intenta realizar una operación
 * para la cual no tiene el permiso requerido.
 * 
 * Esta es una excepción CRÍTICA para el sistema RBAC.
 * 
 * @author SICA Team
 * @version 1.0
 */
public class PermisoDenegadoException extends SicaException {
    
    private final String permisoRequerido;
    
    public PermisoDenegadoException(String permisoRequerido) {
        super("Permiso denegado. Se requiere el permiso: " + permisoRequerido);
        this.permisoRequerido = permisoRequerido;
    }
    
    public PermisoDenegadoException(String permisoRequerido, String usuarioEmail) {
        super("Permiso denegado para el usuario '" + usuarioEmail + 
              "'. Se requiere el permiso: " + permisoRequerido);
        this.permisoRequerido = permisoRequerido;
    }
    
    public String getPermisoRequerido() {
        return permisoRequerido;
    }
}
