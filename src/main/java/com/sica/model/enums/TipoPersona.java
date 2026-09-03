package com.sica.model.enums;

/**
 * Enumeración que representa los tipos de persona en el sistema.
 * 
 * Principio SOLID aplicado:
 * - Single Responsibility: Solo define los tipos de persona posibles
 * 
 * @author SICA Team
 * @version 1.0
 */
public enum TipoPersona {
    /**
     * Persona que trabaja regularmente en una empresa del complejo
     */
    TRABAJADOR("Trabajador"),
    
    /**
     * Persona que visita temporalmente el complejo
     */
    INVITADO("Invitado");
    
    private final String descripcion;
    
    /**
     * Constructor privado del enum
     * 
     * @param descripcion Descripción legible del tipo de persona
     */
    TipoPersona(String descripcion) {
        this.descripcion = descripcion;
    }
    
    /**
     * Obtiene la descripción del tipo de persona
     * 
     * @return Descripción legible
     */
    public String getDescripcion() {
        return descripcion;
    }
    
    /**
     * Convierte una cadena a su tipo correspondiente
     * 
     * @param texto Texto a convertir ("Trabajador" o "Invitado")
     * @return TipoPersona correspondiente
     * @throws IllegalArgumentException si el texto no coincide con ningún tipo
     */
    public static TipoPersona fromString(String texto) {
        for (TipoPersona tipo : TipoPersona.values()) {
            if (tipo.name().equalsIgnoreCase(texto) || 
                tipo.descripcion.equalsIgnoreCase(texto)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Tipo de persona no válido: " + texto);
    }
    
    @Override
    public String toString() {
        return descripcion;
    }
}
