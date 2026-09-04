package com.sica.service;

import com.sica.exception.PermisoDenegadoException;
import com.sica.exception.SicaException;
import com.sica.model.entity.Empresa;
import com.sica.model.enums.AccionAuditoria;
import com.sica.repository.EmpresaRepository;
import com.sica.security.SessionManager;

import java.sql.SQLException;
import java.util.List;

/**
 * Servicio de lógica de negocio para la gestión de Empresas.
 * 
 * Responsabilidades:
 * - CRUD de empresas con validaciones de negocio
 * - Verificación de permisos RBAC antes de cada operación
 * - Registro de auditoría de operaciones críticas
 * - Validación de reglas de negocio
 * 
 * Principios SOLID aplicados:
 * - Single Responsibility: Solo gestiona lógica de negocio de empresas
 * - Dependency Inversion: Depende de abstracciones (repository, services)
 * - Open/Closed: Abierto para extensión mediante métodos adicionales
 * 
 * @author SICA Team
 * @version 1.0
 */
public class EmpresaService {
    
    private final EmpresaRepository empresaRepository;
    private final AuthorizationService authorizationService;
    private final AuditoriaService auditoriaService;
    private final SessionManager sessionManager;
    
    /**
     * Constructor con inyección de dependencias.
     */
    public EmpresaService() throws SQLException {
        this.empresaRepository = new EmpresaRepository();
        this.authorizationService = new AuthorizationService();
        this.auditoriaService = new AuditoriaService();
        this.sessionManager = SessionManager.getInstance();
    }
    
    /**
     * Crea una nueva empresa en el sistema.
     * 
     * Validaciones:
     * - Usuario debe tener permiso "gestionar_empresas"
     * - NIT no debe existir previamente
     * - Todos los campos obligatorios deben estar completos
     * 
     * @param empresa Empresa a crear
     * @return Empresa creada con ID asignado
     * @throws PermisoDenegadoException si no tiene permiso
     * @throws SicaException si hay error de validación o BD
     */
    public Empresa crear(Empresa empresa) throws SicaException {
        // 1. Verificar permiso RBAC
        authorizationService.requirePermiso("gestionar_empresas");
        
        try {
            // 2. Validaciones de negocio
            validarEmpresa(empresa);
            
            // Verificar que el NIT no exista
            if (empresaRepository.findByNit(empresa.getNit()).isPresent()) {
                throw new SicaException("Ya existe una empresa con el NIT: " + empresa.getNit());
            }
            
            // 3. Ejecutar operación
            Empresa nuevaEmpresa = empresaRepository.save(empresa);
            
            // 4. Registrar en auditoría
            auditoriaService.registrar(
                AccionAuditoria.CREACION_EMPRESA,
                "empresas",
                nuevaEmpresa.getId(),
                String.format("Empresa creada: %s (NIT: %s)", 
                    nuevaEmpresa.getNombreEmpresa(), nuevaEmpresa.getNit())
            );
            
            return nuevaEmpresa;
            
        } catch (SQLException e) {
            throw new SicaException("Error al crear empresa: " + e.getMessage(), e);
        }
    }
    
    /**
     * Actualiza una empresa existente.
     * 
     * Validaciones:
     * - Usuario debe tener permiso "gestionar_empresas"
     * - Empresa debe existir
     * - Si se cambia el NIT, el nuevo no debe existir
     * 
     * @param empresa Empresa con datos actualizados
     * @return Empresa actualizada
     * @throws PermisoDenegadoException si no tiene permiso
     * @throws SicaException si hay error de validación o BD
     */
    public Empresa actualizar(Empresa empresa) throws SicaException {
        // 1. Verificar permiso RBAC
        authorizationService.requirePermiso("gestionar_empresas");
        
        try {
            // 2. Validaciones de negocio
            validarEmpresa(empresa);
            
            // Verificar que la empresa exista
            Empresa empresaExistente = empresaRepository.findById(empresa.getId())
                .orElseThrow(() -> new SicaException(
                    "No se encontró la empresa con ID: " + empresa.getId()));
            
            // Verificar cambio de NIT
            if (!empresaExistente.getNit().equals(empresa.getNit())) {
                if (empresaRepository.findByNit(empresa.getNit()).isPresent()) {
                    throw new SicaException("El NIT " + empresa.getNit() + " ya está en uso");
                }
            }
            
            // 3. Ejecutar operación
            Empresa empresaActualizada = empresaRepository.update(empresa);
            
            // 4. Registrar en auditoría
            auditoriaService.registrar(
                AccionAuditoria.ACTUALIZACION_EMPRESA,
                "empresas",
                empresaActualizada.getId(),
                String.format("Empresa actualizada: %s (NIT: %s)", 
                    empresaActualizada.getNombreEmpresa(), empresaActualizada.getNit())
            );
            
            return empresaActualizada;
            
        } catch (SQLException e) {
            throw new SicaException("Error al actualizar empresa: " + e.getMessage(), e);
        }
    }
    
    /**
     * Elimina (lógicamente) una empresa.
     * En este sistema, eliminación es cambiar esta_activa = FALSE.
     * 
     * Validaciones:
     * - Usuario debe tener permiso "gestionar_empresas"
     * - Empresa debe existir
     * 
     * @param id ID de la empresa a eliminar
     * @throws PermisoDenegadoException si no tiene permiso
     * @throws SicaException si hay error de validación o BD
     */
    public void eliminar(Integer id) throws SicaException {
        // 1. Verificar permiso RBAC
        authorizationService.requirePermiso("gestionar_empresas");
        
        try {
            // 2. Validaciones de negocio
            Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new SicaException(
                    "No se encontró la empresa con ID: " + id));
            
            // 3. Ejecutar operación (eliminación lógica)
            empresa.setEstaActiva(false);
            empresaRepository.update(empresa);
            
            // 4. Registrar en auditoría
            auditoriaService.registrar(
                AccionAuditoria.ELIMINACION_EMPRESA,
                "empresas",
                id,
                String.format("Empresa eliminada (desactivada): %s (NIT: %s)", 
                    empresa.getNombreEmpresa(), empresa.getNit())
            );
            
        } catch (SQLException e) {
            throw new SicaException("Error al eliminar empresa: " + e.getMessage(), e);
        }
    }
    
    /**
     * Busca una empresa por su ID.
     * 
     * Permisos: requiere "gestionar_empresas" o "ver_empresas"
     * 
     * @param id ID de la empresa
     * @return Empresa encontrada
     * @throws SicaException si no existe o hay error de BD
     * @throws PermisoDenegadoException si no tiene permiso
     */
    public Empresa buscarPorId(Integer id) throws SicaException {
        // Verificar permiso RBAC
        if (!authorizationService.tieneAlgunoDeEstosPermisos("gestionar_empresas", "ver_empresas")) {
            throw new PermisoDenegadoException("ver_empresas", 
                sessionManager.getUsuarioActual().getEmail());
        }
        
        try {
            return empresaRepository.findById(id)
                .orElseThrow(() -> new SicaException(
                    "No se encontró la empresa con ID: " + id));
        } catch (SQLException e) {
            throw new SicaException("Error al buscar empresa: " + e.getMessage(), e);
        }
    }
    
    /**
     * Busca una empresa por su NIT.
     * 
     * Permisos: requiere "gestionar_empresas" o "ver_empresas"
     * 
     * @param nit NIT de la empresa
     * @return Empresa encontrada
     * @throws SicaException si no existe o hay error de BD
     * @throws PermisoDenegadoException si no tiene permiso
     */
    public Empresa buscarPorNit(String nit) throws SicaException {
        // Verificar permiso RBAC
        if (!authorizationService.tieneAlgunoDeEstosPermisos("gestionar_empresas", "ver_empresas")) {
            throw new PermisoDenegadoException("ver_empresas", 
                sessionManager.getUsuarioActual().getEmail());
        }
        
        try {
            return empresaRepository.findByNit(nit)
                .orElseThrow(() -> new SicaException(
                    "No se encontró la empresa con NIT: " + nit));
        } catch (SQLException e) {
            throw new SicaException("Error al buscar empresa por NIT: " + e.getMessage(), e);
        }
    }
    
    /**
     * Lista todas las empresas del sistema.
     * 
     * Permisos: requiere "gestionar_empresas" o "ver_empresas"
     * 
     * @return Lista de empresas
     * @throws PermisoDenegadoException si no tiene permiso
     * @throws SicaException si hay error de BD
     */
    public List<Empresa> listarTodas() throws SicaException {
        // Verificar permiso RBAC
        if (!authorizationService.tieneAlgunoDeEstosPermisos("gestionar_empresas", "ver_empresas")) {
            throw new PermisoDenegadoException("ver_empresas", 
                sessionManager.getUsuarioActual().getEmail());
        }
        
        try {
            return empresaRepository.findAll();
        } catch (SQLException e) {
            throw new SicaException("Error al listar empresas: " + e.getMessage(), e);
        }
    }
    
    /**
     * Lista empresas activas o inactivas.
     * 
     * Permisos: requiere "gestionar_empresas" o "ver_empresas"
     * 
     * @param activas true para listar activas, false para inactivas
     * @return Lista de empresas filtrada
     * @throws PermisoDenegadoException si no tiene permiso
     * @throws SicaException si hay error de BD
     */
    public List<Empresa> listarPorEstado(boolean activas) throws SicaException {
        // Verificar permiso RBAC
        if (!authorizationService.tieneAlgunoDeEstosPermisos("gestionar_empresas", "ver_empresas")) {
            throw new PermisoDenegadoException("ver_empresas", 
                sessionManager.getUsuarioActual().getEmail());
        }
        
        try {
            return empresaRepository.findByEstaActiva(activas);
        } catch (SQLException e) {
            throw new SicaException("Error al listar empresas por estado: " + e.getMessage(), e);
        }
    }
    
    /**
     * Busca empresas por nombre (búsqueda parcial).
     * 
     * Permisos: requiere "gestionar_empresas" o "ver_empresas"
     * 
     * @param nombre Nombre o parte del nombre
     * @return Lista de empresas que coinciden
     * @throws PermisoDenegadoException si no tiene permiso
     * @throws SicaException si hay error de BD
     */
    public List<Empresa> buscarPorNombre(String nombre) throws SicaException {
        // Verificar permiso RBAC
        if (!authorizationService.tieneAlgunoDeEstosPermisos("gestionar_empresas", "ver_empresas")) {
            throw new PermisoDenegadoException("ver_empresas", 
                sessionManager.getUsuarioActual().getEmail());
        }
        
        try {
            return empresaRepository.searchByNombre(nombre);
        } catch (SQLException e) {
            throw new SicaException("Error al buscar empresas por nombre: " + e.getMessage(), e);
        }
    }
    
    /**
     * Activa una empresa previamente desactivada.
     * 
     * Permisos: requiere "gestionar_empresas"
     * 
     * @param id ID de la empresa a activar
     * @throws PermisoDenegadoException si no tiene permiso
     * @throws SicaException si hay error de BD
     */
    public void activar(Integer id) throws SicaException {
        // Verificar permiso RBAC
        authorizationService.requirePermiso("gestionar_empresas");
        
        try {
            Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new SicaException(
                    "No se encontró la empresa con ID: " + id));
            
            empresa.setEstaActiva(true);
            empresaRepository.update(empresa);
            
            // Registrar en auditoría
            auditoriaService.registrar(
                AccionAuditoria.ACTUALIZACION_EMPRESA,
                "empresas",
                id,
                String.format("Empresa activada: %s (NIT: %s)", 
                    empresa.getNombreEmpresa(), empresa.getNit())
            );
            
        } catch (SQLException e) {
            throw new SicaException("Error al activar empresa: " + e.getMessage(), e);
        }
    }
    
    /**
     * Valida que una empresa tenga todos los campos obligatorios.
     * 
     * @param empresa Empresa a validar
     * @throws SicaException si falla alguna validación
     */
    private void validarEmpresa(Empresa empresa) throws SicaException {
        if (empresa == null) {
            throw new SicaException("La empresa no puede ser nula");
        }
        
        if (empresa.getNombreEmpresa() == null || empresa.getNombreEmpresa().trim().isEmpty()) {
            throw new SicaException("El nombre de la empresa es obligatorio");
        }
        
        if (empresa.getNit() == null || empresa.getNit().trim().isEmpty()) {
            throw new SicaException("El NIT de la empresa es obligatorio");
        }
        
        // Validar formato de NIT (solo números y guiones)
        if (!empresa.getNit().matches("^[0-9-]+$")) {
            throw new SicaException("El NIT solo puede contener números y guiones");
        }
        
        if (empresa.getTelefono() == null || empresa.getTelefono().trim().isEmpty()) {
            throw new SicaException("El teléfono de la empresa es obligatorio");
        }
        
        if (empresa.getEmail() == null || empresa.getEmail().trim().isEmpty()) {
            throw new SicaException("El email de la empresa es obligatorio");
        }
        
        // Validar formato de email básico
        if (!empresa.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new SicaException("El formato del email no es válido");
        }
        
        if (empresa.getDireccion() == null || empresa.getDireccion().trim().isEmpty()) {
            throw new SicaException("La dirección de la empresa es obligatoria");
        }
    }
}
