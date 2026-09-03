package com.sica.model.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un rol en el sistema RBAC.
 * Un rol agrupa permisos que luego se asignan a usuarios.
 * 
 * Principios SOLID aplicados:
 * - Single Responsibility: Solo representa un rol con sus atributos
 * - Encapsulación: Atributos privados con getters/setters
 * 
 * @author SICA Team
 * @version 1.0
 */
public class Rol {
    private Integer id;
    private String nombreRol;
    private String descripcion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Permiso> permisos;
    
    /**
     * Constructor vacío
     */
    public Rol() {
        this.permisos = new ArrayList<>();
    }
    
    /**
     * Constructor con parámetros principales
     * 
     * @param nombreRol Nombre del rol (ej: "Superusuario")
     * @param descripcion Descripción del rol
     */
    public Rol(String nombreRol, String descripcion) {
        this();
        this.nombreRol = nombreRol;
        this.descripcion = descripcion;
    }
    
    /**
     * Constructor completo
     * 
     * @param id ID del rol
     * @param nombreRol Nombre del rol
     * @param descripcion Descripción del rol
     */
    public Rol(Integer id, String nombreRol, String descripcion) {
        this(nombreRol, descripcion);
        this.id = id;
    }
    
    // Getters y Setters
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getNombreRol() {
        return nombreRol;
    }
    
    public void setNombreRol(String nombreRol) {
        this.nombreRol = nombreRol;
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
    
    public List<Permiso> getPermisos() {
        return permisos;
    }
    
    public void setPermisos(List<Permiso> permisos) {
        this.permisos = permisos;
    }
    
    /**
     * Agrega un permiso a la lista de permisos del rol
     * 
     * @param permiso Permiso a agregar
     */
    public void agregarPermiso(Permiso permiso) {
        if (permiso != null && !this.permisos.contains(permiso)) {
            this.permisos.add(permiso);
        }
    }
    
    /**
     * Verifica si el rol tiene un permiso específico
     * 
     * @param nombrePermiso Nombre del permiso a verificar
     * @return true si el rol tiene el permiso, false en caso contrario
     */
    public boolean tienePermiso(String nombrePermiso) {
        return permisos.stream()
                .anyMatch(p -> p.getNombrePermiso().equals(nombrePermiso));
    }
    
    @Override
    public String toString() {
        return "Rol{" +
                "id=" + id +
                ", nombreRol='" + nombreRol + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", cantidadPermisos=" + (permisos != null ? permisos.size() : 0) +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rol rol = (Rol) o;
        return id != null && id.equals(rol.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
