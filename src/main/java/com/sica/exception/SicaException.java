package com.sica.exception;

/**
 * Excepción base para todas las excepciones personalizadas del sistema SICA.
 * 
 * Todas las excepciones de negocio deben heredar de esta clase.
 * 
 * Principio SOLID aplicado:
 * - Liskov Substitution: Todas las excepciones hijas pueden sustituir a SicaException
 * 
 * @author SICA Team
 * @version 1.0
 */
public class SicaException extends Exception {
    
    /**
     * Constructor con mensaje.
     * 
     * @param mensaje Mensaje descriptivo del error
     */
    public SicaException(String mensaje) {
        super(mensaje);
    }
    
    /**
     * Constructor con mensaje y causa.
     * 
     * @param mensaje Mensaje descriptivo del error
     * @param causa Excepción que causó este error
     */
    public SicaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
