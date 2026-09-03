package com.sica.model.entity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidad que representa un usuario del sistema SICA.
 * Un usuario tiene credenciales, un rol asignado y puede estar activo o inactivo.
 * 
 * Principios SOLID aplicados:
 * - Single Responsibility: Solo representa un usuario con sus atributos
 * - Encapsulación: Atributos privados con getters/setters
 * 
 * @author SICA Team
 * @version 1.0
 */
public class Usuario {
    private Integer id;
    private String nombre;
    private String email;
    private String password;
    private Rol rol;
    private boolean estaActivo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Constructor vacío
     */
    public Usuario() {
        this.estaActivo = true;
    }
    
    /**
     * Constructor con parámetros principales
     * 
     * @param nombre Nombre completo del usuario
     * @param email Email del usuario (único)
     * @param password Contraseña del usuario
     * @param rol Rol asignado al usuario
     */
    public Usuario(String nombre, String email, String password, Rol rol) {
        this();
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.rol = rol;
    }
    
    /**
     * Constructor completo
     * 
     * @param id ID del usuario
     * @param nombre Nombre completo del usuario
     * @param email Email del usuario
     * @param password Contraseña del usuario
     * @param rol Rol asignado al usuario
     * @param estaActivo Estado del usuario
     */
    public Usuario(Integer id, String nombre, String email, String password, Rol rol, boolean estaActivo) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.rol = rol;
        this.estaActivo = estaActivo;
    }
    
    // Getters y Setters
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public Rol getRol() {
        return rol;
    }
    
    public void setRol(Rol rol) {
        this.rol = rol;
    }
    
    public boolean isEstaActivo() {
        return estaActivo;
    }
    
    public void setEstaActivo(boolean estaActivo) {
        this.estaActivo = estaActivo;
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
    
    /**
     * Obtiene la lista de permisos del usuario a través de su rol
     * 
     * @return Lista de permisos del rol del usuario
     */
    public List<Permiso> getPermisos() {
        return rol != null ? rol.getPermisos() : List.of();
    }
    
    /**
     * Verifica si el usuario tiene un permiso específico
     * 
     * @param nombrePermiso Nombre del permiso a verificar
     * @return true si el usuario tiene el permiso, false en caso contrario
     */
    public boolean tienePermiso(String nombrePermiso) {
        return rol != null && rol.tienePermiso(nombrePermiso);
    }
    
    /**
     * Activa el usuario
     */
    public void activar() {
        this.estaActivo = true;
    }
    
    /**
     * Desactiva el usuario
     */
    public void desactivar() {
        this.estaActivo = false;
    }
    
    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", email='" + email + '\'' +
                ", rol=" + (rol != null ? rol.getNombreRol() : "Sin rol") +
                ", estaActivo=" + estaActivo +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return id != null && id.equals(usuario.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
