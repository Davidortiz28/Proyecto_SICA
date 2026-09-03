package com.sica.model.entity;

import java.time.LocalDateTime;

/**
 * Entidad que representa un estado de visita.
 * 
 * Estados posibles:
 * - Dentro: Persona actualmente dentro del complejo
 * - Fuera: Persona salió del complejo
 * - Pendiente de Aprobación: Visita esperando aprobación
 * - Aprobado: Visita pre-aprobada, puede hacer check-in
 * - Rechazado: Visita rechazada, no puede ingresar
 * - Expirado: Visita aprobada pero no se presentó
 * - Cerrada por Sistema (Salida Olvidada): Sistema cerró automáticamente
 * - Pendiente de Aprobación por Olvido: Trabajador sin carnet esperando aprobación
 * 
 * Principios SOLID aplicados:
 * - Single Responsibility: Solo representa un estado de visita
 * - Encapsulación: Atributos privados con getters/setters
 * 
 * @author SICA Team
 * @version 1.0
 */
public class VisitaEstado {
    private Integer id;
    private String nombreEstado;
    private String descripcion;
    private LocalDateTime createdAt;
    
    /**
     * Constructor vacío
     */
    public VisitaEstado() {
    }
    
    /**
     * Constructor con parámetros principales
     * 
     * @param nombreEstado Nombre del estado
     * @param descripcion Descripción del estado
     */
    public VisitaEstado(String nombreEstado, String descripcion) {
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
    public VisitaEstado(Integer id, String nombreEstado, String descripcion) {
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
    
    // Métodos de negocio
    
    /**
     * Verifica si el estado indica que la persona está dentro
     * 
     * @return true si el estado es "Dentro", false en caso contrario
     */
    public boolean estaDentro() {
        return "Dentro".equalsIgnoreCase(nombreEstado);
    }
    
    /**
     * Verifica si el estado indica que la persona está fuera
     * 
     * @return true si el estado es "Fuera", false en caso contrario
     */
    public boolean estaFuera() {
        return "Fuera".equalsIgnoreCase(nombreEstado);
    }
    
    /**
     * Verifica si el estado requiere aprobación
     * 
     * @return true si el estado es pendiente de aprobación, false en caso contrario
     */
    public boolean requiereAprobacion() {
        return nombreEstado != null && (
                nombreEstado.equalsIgnoreCase("Pendiente de Aprobación") ||
                nombreEstado.equalsIgnoreCase("Pendiente de Aprobación por Olvido")
        );
    }
    
    /**
     * Verifica si el estado está aprobado
     * 
     * @return true si el estado es "Aprobado", false en caso contrario
     */
    public boolean estaAprobado() {
        return "Aprobado".equalsIgnoreCase(nombreEstado);
    }
    
    /**
     * Verifica si el estado está rechazado
     * 
     * @return true si el estado es "Rechazado", false en caso contrario
     */
    public boolean estaRechazado() {
        return "Rechazado".equalsIgnoreCase(nombreEstado);
    }
    
    /**
     * Verifica si la visita fue cerrada por el sistema
     * 
     * @return true si fue cerrada por salida olvidada, false en caso contrario
     */
    public boolean cerradaPorSistema() {
        return nombreEstado != null && nombreEstado.contains("Cerrada por Sistema");
    }
    
    @Override
    public String toString() {
        return "VisitaEstado{" +
                "id=" + id +
                ", nombreEstado='" + nombreEstado + '\'' +
                ", descripcion='" + descripcion + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VisitaEstado that = (VisitaEstado) o;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
