package com.sica.service;

import com.sica.exception.PermisoDenegadoException;
import com.sica.exception.SicaException;
import com.sica.exception.UsuarioNoEncontradoException;
import com.sica.model.entity.Usuario;
import com.sica.model.enums.AccionAuditoria;
import com.sica.repository.UsuarioRepository;
import com.sica.security.SessionManager;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de lógica de negocio para la gestión de Usuarios.
 * 
 * Responsabilidades:
 * - CRUD de usuarios con validaciones de negocio
 * - Verificación de permisos RBAC antes de cada operación
 * - Registro de auditoría de operaciones críticas
 * - Validación de reglas de negocio
 * 
 * Principios SOLID aplicados:
 * - Single Responsibility: Solo gestiona lógica de negocio de usuarios
 * - Dependency Inversion: Depende de abstracciones (repository, services)
 * - Open/Closed: Abierto para extensión mediante métodos adicionales
 * 
 * @author SICA Team
 * @version 1.0
 */
public class UsuarioService {
    
    private final UsuarioRepository usuarioRepository;
    private final AuthorizationService authorizationService;
    private final AuditoriaService auditoriaService;
    private final SessionManager sessionManager;
    
    /**
     * Constructor con inyección de dependencias.
     */
    public UsuarioService() throws SQLException {
        this.usuarioRepository = new UsuarioRepository();
        this.authorizationService = new AuthorizationService();
        this.auditoriaService = new AuditoriaService();
        this.sessionManager = SessionManager.getInstance();
    }
    
    /**
     * Crea un nuevo usuario en el sistema.
     * 
     * Validaciones:
     * - Usuario debe tener permiso "gestionar_usuarios"
     * - Email no debe existir previamente
     * - Todos los campos obligatorios deben estar completos
     * - Rol debe existir
     * 
     * @param usuario Usuario a crear
     * @return Usuario creado con ID asignado
     * @throws PermisoDenegadoException si no tiene permiso
     * @throws SicaException si hay error de validación o BD
     */
    public Usuario crear(Usuario usuario) throws SicaException {
        // 1. Verificar permiso RBAC
        authorizationService.requirePermiso("gestionar_usuarios");
        
        try {
            // 2. Validaciones de negocio
            validarUsuario(usuario);
            
            // Verificar que el email no exista
            if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
                throw new SicaException("Ya existe un usuario con el email: " + usuario.getEmail());
            }
            
            // 3. Ejecutar operación
            Usuario nuevoUsuario = usuarioRepository.save(usuario);
            
            // 4. Registrar en auditoría
            auditoriaService.registrar(
                AccionAuditoria.CREACION_USUARIO,
                "usuarios",
                nuevoUsuario.getId(),
                String.format("Usuario creado: %s (%s)", 
                    nuevoUsuario.getNombre(), nuevoUsuario.getEmail())
            );
            
            return nuevoUsuario;
            
        } catch (SQLException e) {
            throw new SicaException("Error al crear usuario: " + e.getMessage(), e);
        }
    }
    
    /**
     * Actualiza un usuario existente.
     * 
     * Validaciones:
     * - Usuario debe tener permiso "gestionar_usuarios"
     * - Usuario debe existir
     * - Si se cambia el email, el nuevo no debe existir
     * - No puede desactivar su propio usuario
     * 
     * @param usuario Usuario con datos actualizados
     * @return Usuario actualizado
     * @throws PermisoDenegadoException si no tiene permiso
     * @throws SicaException si hay error de validación o BD
     */
    public Usuario actualizar(Usuario usuario) throws SicaException {
        // 1. Verificar permiso RBAC
        authorizationService.requirePermiso("gestionar_usuarios");
        
        try {
            // 2. Validaciones de negocio
            validarUsuario(usuario);
            
            // Verificar que el usuario exista
            Usuario usuarioExistente = usuarioRepository.findById(usuario.getId())
                .orElseThrow(() -> new UsuarioNoEncontradoException(
                    "No se encontró el usuario con ID: " + usuario.getId()));
            
            // Verificar cambio de email
            if (!usuarioExistente.getEmail().equals(usuario.getEmail())) {
                Optional<Usuario> usuarioConEmail = usuarioRepository.findByEmail(usuario.getEmail());
                if (usuarioConEmail.isPresent() && 
                    !usuarioConEmail.get().getId().equals(usuario.getId())) {
                    throw new SicaException("El email " + usuario.getEmail() + " ya está en uso");
                }
            }
            
            // No puede desactivar su propio usuario
            Usuario usuarioActual = sessionManager.getUsuarioActual();
            if (usuarioActual.getId().equals(usuario.getId()) && !usuario.isEstaActivo()) {
                throw new SicaException("No puede desactivar su propio usuario");
            }
            
            // 3. Ejecutar operación
            Usuario usuarioActualizado = usuarioRepository.update(usuario);
            
            // 4. Registrar en auditoría
            auditoriaService.registrar(
                AccionAuditoria.ACTUALIZACION_USUARIO,
                "usuarios",
                usuarioActualizado.getId(),
                String.format("Usuario actualizado: %s (%s)", 
                    usuarioActualizado.getNombre(), usuarioActualizado.getEmail())
            );
            
            return usuarioActualizado;
            
        } catch (SQLException e) {
            throw new SicaException("Error al actualizar usuario: " + e.getMessage(), e);
        }
    }
    
    /**
     * Elimina (lógicamente) un usuario.
     * En este sistema, eliminación es cambiar esta_activo = FALSE.
     * 
     * Validaciones:
     * - Usuario debe tener permiso "gestionar_usuarios"
     * - Usuario debe existir
     * - No puede eliminar su propio usuario
     * 
     * @param id ID del usuario a eliminar
     * @throws PermisoDenegadoException si no tiene permiso
     * @throws SicaException si hay error de validación o BD
     */
    public void eliminar(Integer id) throws SicaException {
        // 1. Verificar permiso RBAC
        authorizationService.requirePermiso("gestionar_usuarios");
        
        try {
            // 2. Validaciones de negocio
            Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(
                    "No se encontró el usuario con ID: " + id));
            
            // No puede eliminar su propio usuario
            Usuario usuarioActual = sessionManager.getUsuarioActual();
            if (usuarioActual.getId().equals(id)) {
                throw new SicaException("No puede eliminar su propio usuario");
            }
            
            // 3. Ejecutar operación (eliminación lógica)
            usuario.setEstaActivo(false);
            usuarioRepository.update(usuario);
            
            // 4. Registrar en auditoría
            auditoriaService.registrar(
                AccionAuditoria.ELIMINACION_USUARIO,
                "usuarios",
                id,
                String.format("Usuario eliminado (desactivado): %s (%s)", 
                    usuario.getNombre(), usuario.getEmail())
            );
            
        } catch (SQLException e) {
            throw new SicaException("Error al eliminar usuario: " + e.getMessage(), e);
        }
    }
    
    /**
     * Busca un usuario por su ID.
     * 
     * Permisos:
     * - "gestionar_usuarios" para ver cualquier usuario
     * - Sin permiso solo puede ver su propio usuario
     * 
     * @param id ID del usuario
     * @return Usuario encontrado
     * @throws UsuarioNoEncontradoException si no existe
     * @throws PermisoDenegadoException si no tiene permiso
     * @throws SicaException si hay error de BD
     */
    public Usuario buscarPorId(Integer id) throws SicaException {
        try {
            Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(
                    "No se encontró el usuario con ID: " + id));
            
            // Verificar permiso
            Usuario usuarioActual = sessionManager.getUsuarioActual();
            boolean puedeVerCualquiera = authorizationService.tienePermiso("gestionar_usuarios") ||
                                        authorizationService.tienePermiso("ver_usuarios");
            
            if (!puedeVerCualquiera && !usuarioActual.getId().equals(id)) {
                throw new PermisoDenegadoException("ver_usuarios", usuarioActual.getEmail());
            }
            
            return usuario;
            
        } catch (SQLException e) {
            throw new SicaException("Error al buscar usuario: " + e.getMessage(), e);
        }
    }
    
    /**
     * Busca un usuario por su email.
     * 
     * Permisos: requiere "gestionar_usuarios" o "ver_usuarios"
     * 
     * @param email Email del usuario
     * @return Usuario encontrado
     * @throws UsuarioNoEncontradoException si no existe
     * @throws PermisoDenegadoException si no tiene permiso
     * @throws SicaException si hay error de BD
     */
    public Usuario buscarPorEmail(String email) throws SicaException {
        // Verificar permiso RBAC
        if (!authorizationService.tieneAlgunoDeEstosPermisos("gestionar_usuarios", "ver_usuarios")) {
            Usuario usuarioActual = sessionManager.getUsuarioActual();
            throw new PermisoDenegadoException("ver_usuarios", usuarioActual.getEmail());
        }
        
        try {
            return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsuarioNoEncontradoException(
                    "No se encontró el usuario con email: " + email));
        } catch (SQLException e) {
            throw new SicaException("Error al buscar usuario por email: " + e.getMessage(), e);
        }
    }
    
    /**
     * Lista todos los usuarios del sistema.
     * 
     * Permisos: requiere "gestionar_usuarios" o "ver_usuarios"
     * 
     * @return Lista de usuarios
     * @throws PermisoDenegadoException si no tiene permiso
     * @throws SicaException si hay error de BD
     */
    public List<Usuario> listarTodos() throws SicaException {
        // Verificar permiso RBAC
        if (!authorizationService.tieneAlgunoDeEstosPermisos("gestionar_usuarios", "ver_usuarios")) {
            Usuario usuarioActual = sessionManager.getUsuarioActual();
            throw new PermisoDenegadoException("ver_usuarios", usuarioActual.getEmail());
        }
        
        try {
            return usuarioRepository.findAll();
        } catch (SQLException e) {
            throw new SicaException("Error al listar usuarios: " + e.getMessage(), e);
        }
    }
    
    /**
     * Lista usuarios activos o inactivos.
     * 
     * Permisos: requiere "gestionar_usuarios" o "ver_usuarios"
     * 
     * @param activos true para listar activos, false para inactivos
     * @return Lista de usuarios filtrada
     * @throws PermisoDenegadoException si no tiene permiso
     * @throws SicaException si hay error de BD
     */
    public List<Usuario> listarPorEstado(boolean activos) throws SicaException {
        // Verificar permiso RBAC
        if (!authorizationService.tieneAlgunoDeEstosPermisos("gestionar_usuarios", "ver_usuarios")) {
            Usuario usuarioActual = sessionManager.getUsuarioActual();
            throw new PermisoDenegadoException("ver_usuarios", usuarioActual.getEmail());
        }
        
        try {
            return usuarioRepository.findByEstaActivo(activos);
        } catch (SQLException e) {
            throw new SicaException("Error al listar usuarios por estado: " + e.getMessage(), e);
        }
    }
    
    /**
     * Activa un usuario previamente desactivado.
     * 
     * Permisos: requiere "gestionar_usuarios"
     * 
     * @param id ID del usuario a activar
     * @throws PermisoDenegadoException si no tiene permiso
     * @throws SicaException si hay error de BD
     */
    public void activar(Integer id) throws SicaException {
        // Verificar permiso RBAC
        authorizationService.requirePermiso("gestionar_usuarios");
        
        try {
            Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(
                    "No se encontró el usuario con ID: " + id));
            
            usuario.setEstaActivo(true);
            usuarioRepository.update(usuario);
            
            // Registrar en auditoría
            auditoriaService.registrar(
                AccionAuditoria.ACTUALIZACION_USUARIO,
                "usuarios",
                id,
                String.format("Usuario activado: %s (%s)", 
                    usuario.getNombre(), usuario.getEmail())
            );
            
        } catch (SQLException e) {
            throw new SicaException("Error al activar usuario: " + e.getMessage(), e);
        }
    }
    
    /**
     * Valida que un usuario tenga todos los campos obligatorios.
     * 
     * @param usuario Usuario a validar
     * @throws SicaException si falla alguna validación
     */
    private void validarUsuario(Usuario usuario) throws SicaException {
        if (usuario == null) {
            throw new SicaException("El usuario no puede ser nulo");
        }
        
        if (usuario.getNombre() == null || usuario.getNombre().trim().isEmpty()) {
            throw new SicaException("El nombre del usuario es obligatorio");
        }
        
        if (usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) {
            throw new SicaException("El email del usuario es obligatorio");
        }
        
        // Validar formato de email básico
        if (!usuario.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new SicaException("El formato del email no es válido");
        }
        
        if (usuario.getPassword() == null || usuario.getPassword().trim().isEmpty()) {
            throw new SicaException("La contraseña del usuario es obligatoria");
        }
        
        // Validar longitud mínima de contraseña
        if (usuario.getPassword().length() < 6) {
            throw new SicaException("La contraseña debe tener al menos 6 caracteres");
        }
        
        if (usuario.getRol() == null || usuario.getRol().getId() == null) {
            throw new SicaException("El rol del usuario es obligatorio");
        }
    }
}
