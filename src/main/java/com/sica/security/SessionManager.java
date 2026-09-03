package com.sica.security;

import com.sica.model.entity.Usuario;

import java.time.LocalDateTime;

/**
 * Clase Singleton que gestiona la sesión del usuario actualmente autenticado.
 * 
 * PATRÓN DE DISEÑO: SINGLETON
 * - Garantiza una única sesión activa en el sistema
 * - Proporciona acceso global al usuario actual
 * 
 * Principios SOLID aplicados:
 * - Single Responsibility: Solo gestiona la sesión actual
 * 
 * ¿Por qué Singleton aquí?
 * - Solo puede haber un usuario autenticado a la vez (aplicación de escritorio)
 * - Punto de acceso global para verificar permisos
 * - Evita pasar el usuario como parámetro en cada método
 * 
 * @author SICA Team
 * @version 1.0
 */
public class SessionManager {
    
    // Instancia única (Singleton)
    private static SessionManager instance;
    
    // Usuario actualmente autenticado
    private Usuario usuarioActual;
    
    // Fecha y hora de inicio de sesión
    private LocalDateTime fechaInicioSesion;
    
    /**
     * Constructor privado (Singleton).
     */
    private SessionManager() {
    }
    
    /**
     * Obtiene la instancia única de SessionManager.
     * 
     * @return Instancia única de SessionManager
     */
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    /**
     * Inicia una sesión con el usuario dado.
     * 
     * @param usuario Usuario que inicia sesión
     */
    public void iniciarSesion(Usuario usuario) {
        this.usuarioActual = usuario;
        this.fechaInicioSesion = LocalDateTime.now();
    }
    
    /**
     * Cierra la sesión actual.
     */
    public void cerrarSesion() {
        this.usuarioActual = null;
        this.fechaInicioSesion = null;
    }
    
    /**
     * Verifica si hay una sesión activa.
     * 
     * @return true si hay sesión activa, false en caso contrario
     */
    public boolean haySesionActiva() {
        return usuarioActual != null;
    }
    
    /**
     * Obtiene el usuario actualmente autenticado.
     * 
     * @return Usuario actual o null si no hay sesión
     */
    public Usuario getUsuarioActual() {
        return usuarioActual;
    }
    
    /**
     * Obtiene la fecha y hora de inicio de sesión.
     * 
     * @return Fecha de inicio de sesión o null si no hay sesión
     */
    public LocalDateTime getFechaInicioSesion() {
        return fechaInicioSesion;
    }
    
    /**
     * Verifica si el usuario actual tiene un permiso específico.
     * 
     * @param nombrePermiso Nombre del permiso a verificar
     * @return true si tiene el permiso, false en caso contrario
     */
    public boolean tienePermiso(String nombrePermiso) {
        if (!haySesionActiva()) {
            return false;
        }
        return usuarioActual.tienePermiso(nombrePermiso);
    }
    
    /**
     * Obtiene información de la sesión actual.
     * 
     * @return String con información de la sesión
     */
    public String obtenerInfoSesion() {
        if (!haySesionActiva()) {
            return "No hay sesión activa";
        }
        
        return String.format(
            "Sesión activa:\n" +
            "  Usuario: %s\n" +
            "  Email: %s\n" +
            "  Rol: %s\n" +
            "  Permisos: %d\n" +
            "  Inicio: %s",
            usuarioActual.getNombre(),
            usuarioActual.getEmail(),
            usuarioActual.getRol().getNombreRol(),
            usuarioActual.getPermisos().size(),
            fechaInicioSesion
        );
    }
    
    /**
     * Requiere que haya una sesión activa.
     * Lanza excepción si no hay sesión.
     * 
     * @throws IllegalStateException si no hay sesión activa
     */
    public void requireSesionActiva() {
        if (!haySesionActiva()) {
            throw new IllegalStateException("No hay sesión activa. Debe iniciar sesión primero.");
        }
    }
}
