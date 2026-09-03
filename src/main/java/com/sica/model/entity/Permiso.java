package com.sica.model.entity;

import java.time.LocalDateTime;

/**
 * Entidad que representa un permiso en el sistema RBAC.
 * Un permiso define una acción específica que puede realizar un usuario.
 * 
 * Principios SOLID aplicados:
 * - Single Responsibility: Solo representa un permiso con sus atributos
 * - Encapsulación: Atributos privados con getters/setters
 * 
 * Ejemplos de permisos:
 * - gestionar_usuarios
 * - registrar_check_in
 * - generar_reporte
 * - bloquear_persona
 * 
 * @author SICA Team
 * @version 1.0
 */
public class Permiso {
    private Integer id;
    private String nombrePermiso;
    private String descripcion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Constructor vacío
     */
    public Permiso() {
    }
    
    /**
     * Constructor con parámetros principales
     * 
     * @param nombrePermiso Nombre del permiso (ej: "registrar_check_in")
     * @param descripcion Descripción del permiso
     */
    public Permiso(String nombrePermiso, String descripcion) {
        this.nombrePermiso = nombrePermiso;
        this.descripcion = descripcion;
    }
    
    /**
     * Constructor completo
     * 
     * @param id ID del permiso
     * @param nombrePermiso Nombre del permiso
     * @param descripcion Descripción del permiso
     */
    public Permiso(Integer id, String nombrePermiso, String descripcion) {
        this.id = id;
        this.nombrePermiso = nombrePermiso;
        this.descripcion = descripcion;
    }
    
    // Getters y Setters
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getNombrePermiso() {
        return nombrePermiso;
    }
    
    public void setNombrePermiso(String nombrePermiso) {
        this.nombrePermiso = nombrePermiso;
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
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "Permiso{" +
                "id=" + id +
                ", nombrePermiso='" + nombrePermiso + '\'' +
                ", descripcion='" + descripcion + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permiso permiso = (Permiso) o;
        return id != null && id.equals(permiso.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
