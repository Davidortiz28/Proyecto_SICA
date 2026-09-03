package com.sica.service;

import com.sica.exception.CredencialesInvalidasException;
import com.sica.exception.SicaException;
import com.sica.exception.UsuarioInactivoException;
import com.sica.exception.UsuarioNoEncontradoException;
import com.sica.model.entity.Usuario;
import com.sica.repository.UsuarioRepository;
import com.sica.security.SessionManager;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Servicio de autenticación.
 * Maneja el proceso de login y logout de usuarios.
 * 
 * Principios SOLID aplicados:
 * - Single Responsibility: Solo maneja autenticación
 * - Dependency Inversion: Usa interfaces/abstracciones
 * 
 * @author SICA Team
 * @version 1.0
 */
public class AuthService {
    
    private final UsuarioRepository usuarioRepository;
    private final SessionManager sessionManager;
    private final AuditoriaService auditoriaService;
    
    public AuthService() throws SQLException {
        this.usuarioRepository = new UsuarioRepository();
        this.sessionManager = SessionManager.getInstance();
        this.auditoriaService = new AuditoriaService();
    }
    
    /**
     * Realiza el proceso de login.
     * 
     * FLUJO:
     * 1. Buscar usuario por email
     * 2. Verificar que existe
     * 3. Verificar que está activo
     * 4. Verificar contraseña
     * 5. Cargar rol y permisos
     * 6. Crear sesión
     * 7. Registrar en auditoría
     * 
     * @param email Email del usuario
     * @param password Contraseña del usuario
     * @return Usuario autenticado con rol y permisos cargados
     * @throws UsuarioNoEncontradoException si el email no existe
     * @throws UsuarioInactivoException si el usuario está inactivo
     * @throws CredencialesInvalidasException si la contraseña es incorrecta
     * @throws SQLException si hay error en la base de datos
     */
    public Usuario login(String email, String password) 
            throws UsuarioNoEncontradoException, UsuarioInactivoException, 
                   CredencialesInvalidasException, SQLException {
        
        // 1. Buscar usuario por email
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        
        // 2. Verificar que existe
        if (usuarioOpt.isEmpty()) {
            // Registrar intento fallido
            auditoriaService.registrarLoginFallido(email, "Usuario no encontrado");
            throw new UsuarioNoEncontradoException(email);
        }
        
        Usuario usuario = usuarioOpt.get();
        
        // 3. Verificar que está activo
        if (!usuario.isEstaActivo()) {
            // Registrar intento fallido
            auditoriaService.registrarLoginFallido(email, "Usuario inactivo");
            throw new UsuarioInactivoException(email);
        }
        
        // 4. Verificar contraseña
        // NOTA: En este proyecto académico se comparan en texto plano
        // En producción se debe usar BCrypt.checkpw() o similar
        if (!password.equals(usuario.getPassword())) {
            // Registrar intento fallido
            auditoriaService.registrarLoginFallido(email, "Contraseña incorrecta");
            throw new CredencialesInvalidasException();
        }
        
        // 5. El rol y permisos ya se cargaron en el Repository
        
        // 6. Crear sesión
        sessionManager.iniciarSesion(usuario);
        
        // 7. Registrar login exitoso en auditoría
        auditoriaService.registrarLoginExitoso(usuario);
        
        return usuario;
    }
    
    /**
     * Cierra la sesión actual.
     * 
     * @throws IllegalStateException si no hay sesión activa
     * @throws SQLException si hay error al registrar en auditoría
     */
    public void logout() throws SQLException {
        // Verificar que hay sesión
        if (!sessionManager.haySesionActiva()) {
            throw new IllegalStateException("No hay sesión activa para cerrar");
        }
        
        // Obtener usuario antes de cerrar sesión
        Usuario usuarioActual = sessionManager.getUsuarioActual();
        
        // Registrar logout en auditoría
        auditoriaService.registrarLogout(usuarioActual);
        
        // Cerrar sesión
        sessionManager.cerrarSesion();
    }
    
    /**
     * Obtiene el usuario actualmente autenticado.
     * 
     * @return Usuario actual o null si no hay sesión
     */
    public Usuario getUsuarioActual() {
        return sessionManager.getUsuarioActual();
    }
    
    /**
     * Verifica si hay una sesión activa.
     * 
     * @return true si hay sesión activa, false en caso contrario
     */
    public boolean haySesionActiva() {
        return sessionManager.haySesionActiva();
    }
    
    /**
     * Cambia la contraseña del usuario actual.
     * 
     * @param passwordActual Contraseña actual
     * @param passwordNueva Nueva contraseña
     * @throws SicaException si hay error
     */
    public void cambiarPassword(String passwordActual, String passwordNueva) 
            throws SicaException {
        
        sessionManager.requireSesionActiva();
        Usuario usuarioActual = sessionManager.getUsuarioActual();
        
        // Verificar contraseña actual
        if (!passwordActual.equals(usuarioActual.getPassword())) {
            throw new CredencialesInvalidasException("La contraseña actual es incorrecta");
        }
        
        // Actualizar contraseña
        try {
            usuarioRepository.updatePassword(usuarioActual.getId(), passwordNueva);
            usuarioActual.setPassword(passwordNueva);
            
            // Registrar en auditoría
            auditoriaService.registrar(
                com.sica.model.enums.AccionAuditoria.ACTUALIZACION_USUARIO,
                "usuarios",
                usuarioActual.getId(),
                "Usuario cambió su contraseña"
            );
            
        } catch (SQLException e) {
            throw new SicaException("Error al cambiar la contraseña", e);
        }
    }
}
