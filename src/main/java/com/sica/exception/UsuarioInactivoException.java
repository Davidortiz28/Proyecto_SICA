package com.sica.exception;

/**
 * Excepción lanzada cuando se intenta autenticar un usuario inactivo.
 * 
 * @author SICA Team
 * @version 1.0
 */
public class UsuarioInactivoException extends SicaException {
    
    public UsuarioInactivoException(String email) {
        super("El usuario '" + email + "' está inactivo. Contacte al administrador.");
    }
}
