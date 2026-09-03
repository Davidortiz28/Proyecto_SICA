package com.sica.exception;

/**
 * Excepción lanzada cuando no se encuentra un usuario en el sistema.
 * 
 * @author SICA Team
 * @version 1.0
 */
public class UsuarioNoEncontradoException extends SicaException {
    
    public UsuarioNoEncontradoException(String email) {
        super("No se encontró el usuario con email: " + email);
    }
    
    public UsuarioNoEncontradoException(Integer id) {
        super("No se encontró el usuario con ID: " + id);
    }
}
