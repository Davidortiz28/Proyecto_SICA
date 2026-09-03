package com.sica.exception;

/**
 * Excepción lanzada cuando las credenciales de login son inválidas.
 * 
 * @author SICA Team
 * @version 1.0
 */
public class CredencialesInvalidasException extends SicaException {
    
    public CredencialesInvalidasException() {
        super("Email o contraseña incorrectos");
    }
    
    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}
