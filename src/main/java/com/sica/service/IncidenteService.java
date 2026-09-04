package com.sica.service;

import com.sica.exception.PermisoDenegadoException;
import com.sica.exception.SicaException;
import com.sica.model.entity.Incidente;
import com.sica.model.entity.Usuario;
import com.sica.model.enums.AccionAuditoria;
import com.sica.repository.IncidenteRepository;
import com.sica.security.SessionManager;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio de lógica de negocio para la gestión de Incidentes.
 * 
 * Responsabilidades:
 * - CRUD de incidentes
 * - Validaciones de negocio
 * - Verificación de permisos RBAC
 * - Auditoría de operaciones
 * 
 * @author SICA Team
 * @version 1.0
 */
public class IncidenteService {
    
    private final IncidenteRepository incidenteRepository;
    private final AuthorizationService authorizationService;
    private final AuditoriaService auditoriaService;
    private final SessionManager sessionManager;
    
    public IncidenteService() throws SQLException {
        this.incidenteRepository = new IncidenteRepository();
        this.authorizationService = new AuthorizationService();
        this.auditoriaService = new AuditoriaService();
        this.sessionManager = SessionManager.getInstance();
    }
    
    /**
     * Registra un nuevo incidente.
     * 
     * Permisos: requiere "registrar_incidente"
     * 
     * @param incidente Incidente a registrar
     * @return Incidente creado
     * @throws PermisoDenegadoException si no tiene permiso
     * @throws SicaException si hay error de validación o BD
     */
    public Incidente registrar(Incidente incidente) throws SicaException {
        // 1. Verificar permiso
        authorizationService.requirePermiso("registrar_incidente");
        
        try {
            // 2. Validaciones
            validarIncidente(incidente);
            
            // 3. Asignar usuario reportador y fecha
            Usuario usuarioActual = sessionManager.getUsuarioActual();
            incidente.setReportadoPor(usuarioActual);
            
            if (incidente.getFechaIncidente() == null) {
                incidente.setFechaIncidente(LocalDateTime.now());
            }
            
            // Estado inicial si no se especifica
            if (incidente.getEstadoResolucion() == null || 
                incidente.getEstadoResolucion().trim().isEmpty()) {
                incidente.setEstadoResolucion("Pendiente");
            }
            
            // 4. Guardar
            Incidente nuevoIncidente = incidenteRepository.save(incidente);
            
            // 5. Auditoría
            auditoriaService.registrar(
                AccionAuditoria.CREACION_INCIDENTE,
                "incidentes",
                nuevoIncidente.getId(),
                String.format("Incidente registrado: %s. Gravedad: %s",
                    incidente.getDescripcion().substring(0, 
                        Math.min(50, incidente.getDescripcion().length())),
                    incidente.getGravedad())
            );
            
            return nuevoIncidente;
            
        } catch (SQLException e) {
            throw new SicaException("Error al registrar incidente: " + e.getMessage(), e);
        }
    }
    
    /**
     * Actualiza un incidente existente.
     * 
     * Permisos: requiere "registrar_incidente"
     * 
     * @param incidente Incidente con datos actualizados
     * @return Incidente actualizado
     * @throws PermisoDenegadoException si no tiene permiso
     * @throws SicaException si hay error
     */
    public Incidente actualizar(Incidente incidente) throws SicaException {
        // 1. Verificar permiso
        authorizationService.requirePermiso("registrar_incidente");
        
        try {
            // 2. Validaciones
            validarIncidente(incidente);
            
            // 3. Verificar que existe
            incidenteRepository.findById(incidente.getId())
                .orElseThrow(() -> new SicaException(
                    "No se encontró el incidente con ID: " + incidente.getId()));
            
            // 4. Actualizar
            Incidente incidenteActualizado = incidenteRepository.update(incidente);
            
            // 5. Auditoría
            auditoriaService.registrar(
                AccionAuditoria.ACTUALIZACION_INCIDENTE,
                "incidentes",
                incidenteActualizado.getId(),
                String.format("Incidente actualizado. Estado: %s",
                    incidente.getEstadoResolucion())
            );
            
            return incidenteActualizado;
            
        } catch (SQLException e) {
            throw new SicaException("Error al actualizar incidente: " + e.getMessage(), e);
        }
    }
    
    /**
     * Busca un incidente por ID.
     * 
     * Permisos: requiere "registrar_incidente" o "consultar_bitacora"
     * 
     * @param id ID del incidente
     * @return Incidente encontrado
     * @throws SicaException si no existe o hay error
     */
    public Incidente buscarPorId(Integer id) throws SicaException {
        if (!authorizationService.tieneAlgunoDeEstosPermisos(
                "registrar_incidente", "consultar_bitacora")) {
            throw new PermisoDenegadoException("registrar_incidente", 
                sessionManager.getUsuarioActual().getEmail());
        }
        
        try {
            return incidenteRepository.findById(id)
                .orElseThrow(() -> new SicaException(
                    "No se encontró el incidente con ID: " + id));
        } catch (SQLException e) {
            throw new SicaException("Error al buscar incidente: " + e.getMessage(), e);
        }
    }
    
    /**
     * Lista todos los incidentes.
     * 
     * Permisos: requiere "registrar_incidente" o "consultar_bitacora"
     * 
     * @return Lista de incidentes
     * @throws SicaException si hay error
     */
    public List<Incidente> listarTodos() throws SicaException {
        if (!authorizationService.tieneAlgunoDeEstosPermisos(
                "registrar_incidente", "consultar_bitacora")) {
            throw new PermisoDenegadoException("registrar_incidente", 
                sessionManager.getUsuarioActual().getEmail());
        }
        
        try {
            return incidenteRepository.findAll();
        } catch (SQLException e) {
            throw new SicaException("Error al listar incidentes: " + e.getMessage(), e);
        }
    }
    
    /**
     * Lista incidentes por gravedad.
     * 
     * Permisos: requiere "registrar_incidente" o "consultar_bitacora"
     * 
     * @param gravedad Gravedad (Baja, Media, Alta, Crítica)
     * @return Lista de incidentes
     * @throws SicaException si hay error
     */
    public List<Incidente> listarPorGravedad(String gravedad) throws SicaException {
        if (!authorizationService.tieneAlgunoDeEstosPermisos(
                "registrar_incidente", "consultar_bitacora")) {
            throw new PermisoDenegadoException("registrar_incidente", 
                sessionManager.getUsuarioActual().getEmail());
        }
        
        try {
            return incidenteRepository.findByGravedad(gravedad);
        } catch (SQLException e) {
            throw new SicaException("Error al listar incidentes por gravedad: " + e.getMessage(), e);
        }
    }
    
    /**
     * Lista incidentes por estado de resolución.
     * 
     * Permisos: requiere "registrar_incidente" o "consultar_bitacora"
     * 
     * @param estado Estado (Pendiente, En Proceso, Resuelto)
     * @return Lista de incidentes
     * @throws SicaException si hay error
     */
    public List<Incidente> listarPorEstado(String estado) throws SicaException {
        if (!authorizationService.tieneAlgunoDeEstosPermisos(
                "registrar_incidente", "consultar_bitacora")) {
            throw new PermisoDenegadoException("registrar_incidente", 
                sessionManager.getUsuarioActual().getEmail());
        }
        
        try {
            return incidenteRepository.findByEstado(estado);
        } catch (SQLException e) {
            throw new SicaException("Error al listar incidentes por estado: " + e.getMessage(), e);
        }
    }
    
    /**
     * Valida un incidente.
     */
    private void validarIncidente(Incidente incidente) throws SicaException {
        if (incidente == null) {
            throw new SicaException("El incidente no puede ser nulo");
        }
        
        if (incidente.getDescripcion() == null || incidente.getDescripcion().trim().isEmpty()) {
            throw new SicaException("La descripción del incidente es obligatoria");
        }
        
        if (incidente.getDescripcion().length() < 10) {
            throw new SicaException("La descripción debe tener al menos 10 caracteres");
        }
        
        if (incidente.getGravedad() == null || incidente.getGravedad().trim().isEmpty()) {
            throw new SicaException("La gravedad del incidente es obligatoria");
        }
        
        // Validar que la gravedad sea válida
        String gravedad = incidente.getGravedad();
        if (!gravedad.equals("Baja") && !gravedad.equals("Media") && 
            !gravedad.equals("Alta") && !gravedad.equals("Crítica")) {
            throw new SicaException(
                "Gravedad inválida. Debe ser: Baja, Media, Alta o Crítica");
        }
    }
}
