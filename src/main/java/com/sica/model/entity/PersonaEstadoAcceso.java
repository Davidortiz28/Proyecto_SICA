package com.sica.model.entity;

import java.time.LocalDateTime;

/**
 * Entidad que representa un estado de acceso para personas.
 * 
 * Estados posibles:
 * - Activo: Puede ingresar normalmente
 * - Con Prohibición de Ingreso: Bloqueado, no puede ingresar
 * 
 * Principios SOLID aplicados:
 * - Single Responsibility: Solo representa un estado de acceso
 * - Encapsulación: Atributos privados con getters/setters
 * 
 * @author SICA Team
 * @version 1.0
 */
public class PersonaEstadoAcceso {
    private Integer id;
    private String nombreEstado;
    private String descripcion;
    private LocalDateTime createdAt;
    
    /**
     * Constructor vacío
     */
    public PersonaEstadoAcceso() {
    }
    
    /**
     * Constructor con parámetros principales
     * 
     * @param nombreEstado Nombre del estado (ej: "Activo")
     * @param descripcion Descripción del estado
     */
    public PersonaEstadoAcceso(String nombreEstado, String descripcion) {
        this.nombreEstado = nombreEstado;
        this.descripcion = descripcion;
    }
    
    /**
     * Constructor completo
     * 
     * @param id ID del estado
     * @param nombreEstado Nombre del estado
     * @param descripcion Descripción del estado
     */
    public PersonaEstadoAcceso(Integer id, String nombreEstado, String descripcion) {
        this.id = id;
        this.nombreEstado = nombreEstado;
        this.descripcion = descripcion;
    }
    
    // Getters y Setters
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getNombreEstado() {
        return nombreEstado;
    }
    
    public void setNombreEstado(String nombreEstado) {
        this.nombreEstado = nombreEstado;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    /**
     * Verifica si el estado permite el ingreso
     * 
     * @return true si el estado es "Activo", false en caso contrario
     */
    public boolean permiteIngreso() {
        return "Activo".equalsIgnoreCase(nombreEstado);
    }
    
    /**
     * Verifica si el estado representa un bloqueo
     * 
     * @return true si el estado es "Con Prohibición de Ingreso", false en caso contrario
     */
    public boolean estaBloqueado() {
        return "Con Prohibición de Ingreso".equalsIgnoreCase(nombreEstado);
    }
    
    @Override
    public String toString() {
        return "PersonaEstadoAcceso{" +
                "id=" + id +
                ", nombreEstado='" + nombreEstado + '\'' +
                ", descripcion='" + descripcion + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersonaEstadoAcceso that = (PersonaEstadoAcceso) o;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
