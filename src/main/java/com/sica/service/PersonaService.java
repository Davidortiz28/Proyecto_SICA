package com.sica.service;

import com.sica.exception.PermisoDenegadoException;
import com.sica.exception.SicaException;
import com.sica.model.entity.Persona;
import com.sica.model.entity.PersonaEstadoAcceso;
import com.sica.model.enums.AccionAuditoria;
import com.sica.model.enums.TipoPersona;
import com.sica.repository.PersonaRepository;
import com.sica.security.SessionManager;

import java.sql.SQLException;
import java.util.List;

/**
 * Servicio de lógica de negocio para la gestión de Personas.
 * 
 * Responsabilidades:
 * - CRUD de personas con validaciones de negocio
 * - Verificación de permisos RBAC antes de cada operación
 * - Registro de auditoría de operaciones críticas
 * - Gestión de estados de acceso (bloqueo/desbloqueo)
 * - Validación de reglas de negocio
 * 
 * Principios SOLID aplicados:
 * - Single Responsibility: Solo gestiona lógica de negocio de personas
 * - Dependency Inversion: Depende de abstracciones (repository, services)
 * - Open/Closed: Abierto para extensión mediante métodos adicionales
 * 
 * @author SICA Team
 * @version 1.0
 */
public class PersonaService {
    
    private final PersonaRepository personaRepository;
    private final AuthorizationService authorizationService;
    private final AuditoriaService auditoriaService;
    private final SessionManager sessionManager;
    
    /**
     * Constructor con inyección de dependencias.
     */
    public PersonaService() throws SQLException {
        this.personaRepository = new PersonaRepository();
        this.authorizationService = new AuthorizationService();
        this.auditoriaService = new AuditoriaService();
        this.sessionManager = SessionManager.getInstance();
    }
    
    /**
     * Crea una nueva persona en el sistema.
     * 
     * Validaciones:
     * - Usuario debe tener permiso "gestionar_personas"
     * - Documento no debe existir previamente
     * - Todos los campos obligatorios deben estar completos
     * - Si es TRABAJADOR, debe tener empresa asignada
     * 
     * @param persona Persona a crear
     * @return Persona creada con ID asignado
     * @throws PermisoDenegadoException si no tiene permiso
     * @throws SicaException si hay error de validación o BD
     */
    public Persona crear(Persona persona) throws SicaException {
        // 1. Verificar permiso RBAC
        authorizationService.requirePermiso("gestionar_personas");
        
        try {
            // 2. Validaciones de negocio
            validarPersona(persona);
            
            // Verificar que el documento no exista
            if (personaRepository.findByDocumento(persona.getDocumentoIdentidad()).isPresent()) {
                throw new SicaException("Ya existe una persona con el documento: " + 
                    persona.getDocumentoIdentidad());
            }
            
            // 3. Ejecutar operación
            Persona nuevaPersona = personaRepository.save(persona);
            
            // 4. Registrar en auditoría
            auditoriaService.registrar(
                AccionAuditoria.CREACION_PERSONA,
                "personas",
                nuevaPersona.getId(),
                String.format("Persona creada: %s (Doc: %s, Tipo: %s)", 
                    nuevaPersona.getNombre(), 
                    nuevaPersona.getDocumentoIdentidad(),
                    nuevaPersona.getTipoPersona())
            );
            
            return nuevaPersona;
            
        } catch (SQLException e) {
            throw new SicaException("Error al crear persona: " + e.getMessage(), e);
        }
    }
    
    /**
     * Actualiza una persona existente.
     * 
     * Validaciones:
     * - Usuario debe tener permiso "gestionar_personas"
     * - Persona debe existir
     * - Si se cambia el documento, el nuevo no debe existir
     * - Si es TRABAJADOR, debe tener empresa
     * 
     * @param persona Persona con datos actualizados
     * @return Persona actualizada
     * @throws PermisoDenegadoException si no tiene permiso
     * @throws SicaException si hay error de validación o BD
     */
    public Persona actualizar(Persona persona) throws SicaException {
        // 1. Verificar permiso RBAC
        authorizationService.requirePermiso("gestionar_personas");
        
        try {
            // 2. Validaciones de negocio
            validarPersona(persona);
            
            // Verificar que la persona exista
            Persona personaExistente = personaRepository.findById(persona.getId())
                .orElseThrow(() -> new SicaException(
                    "No se encontró la persona con ID: " + persona.getId()));
            
            // Verificar cambio de documento
            if (!personaExistente.getDocumentoIdentidad().equals(persona.getDocumentoIdentidad())) {
                if (personaRepository.findByDocumento(persona.getDocumentoIdentidad()).isPresent()) {
                    throw new SicaException("El documento " + persona.getDocumentoIdentidad() + 
                        " ya está en uso");
                }
            }
            
            // 3. Ejecutar operación
            Persona personaActualizada = personaRepository.update(persona);
            
            // 4. Registrar en auditoría
            auditoriaService.registrar(
                AccionAuditoria.ACTUALIZACION_PERSONA,
                "personas",
                personaActualizada.getId(),
                String.format("Persona actualizada: %s (Doc: %s)", 
                    personaActualizada.getNombre(), 
                    personaActualizada.getDocumentoIdentidad())
            );
            
            return personaActualizada;
            
        } catch (SQLException e) {
            throw new SicaException("Error al actualizar persona: " + e.getMessage(), e);
        }
    }
    
    /**
     * Elimina una persona del sistema.
     * 
     * Validaciones:
     * - Usuario debe tener permiso "gestionar_personas"
     * - Persona debe existir
     * - No puede tener visitas activas
     * 
     * @param id ID de la persona a eliminar
     * @throws PermisoDenegadoException si no tiene permiso
     * @throws SicaException si hay error de validación o BD
     */
    public void eliminar(Integer id) throws SicaException {
        // 1. Verificar permiso RBAC
        authorizationService.requirePermiso("gestionar_personas");
        
        try {
            // 2. Validaciones de negocio
            Persona persona = personaRepository.findById(id)
                .orElseThrow(() -> new SicaException(
                    "No se encontró la persona con ID: " + id));
            
            // TODO: En etapas futuras, verificar que no tenga visitas activas
            
            // 3. Ejecutar operación
            personaRepository.delete(id);
            
            // 4. Registrar en auditoría
            auditoriaService.registrar(
                AccionAuditoria.ELIMINACION_PERSONA,
                "personas",
                id,
                String.format("Persona eliminada: %s (Doc: %s)", 
                    persona.getNombre(), persona.getDocumentoIdentidad())
            );
            
        } catch (SQLException e) {
            throw new SicaException("Error al eliminar persona: " + e.getMessage(), e);
        }
    }
    
    /**
     * Busca una persona por su ID.
     * 
     * Permisos: requiere "gestionar_personas" o "buscar_persona"
     * 
     * @param id ID de la persona
     * @return Persona encontrada
     * @throws SicaException si no existe o hay error de BD
     * @throws PermisoDenegadoException si no tiene permiso
     */
    public Persona buscarPorId(Integer id) throws SicaException {
        // Verificar permiso RBAC
        if (!authorizationService.tieneAlgunoDeEstosPermisos("gestionar_personas", "buscar_persona")) {
            throw new PermisoDenegadoException("buscar_persona", 
                sessionManager.getUsuarioActual().getEmail());
        }
        
        try {
            return personaRepository.findById(id)
                .orElseThrow(() -> new SicaException(
                    "No se encontró la persona con ID: " + id));
        } catch (SQLException e) {
            throw new SicaException("Error al buscar persona: " + e.getMessage(), e);
        }
    }
    
    /**
     * Busca una persona por documento de identidad.
     * 
     * Permisos: requiere "gestionar_personas" o "buscar_persona"
     * 
     * @param documento Documento de identidad
     * @return Persona encontrada
     * @throws SicaException si no existe o hay error de BD
     * @throws PermisoDenegadoException si no tiene permiso
     */
    public Persona buscarPorDocumento(String documento) throws SicaException {
        // Verificar permiso RBAC
        if (!authorizationService.tieneAlgunoDeEstosPermisos("gestionar_personas", "buscar_persona")) {
            throw new PermisoDenegadoException("buscar_persona", 
                sessionManager.getUsuarioActual().getEmail());
        }
        
        try {
            return personaRepository.findByDocumento(documento)
                .orElseThrow(() -> new SicaException(
                    "No se encontró la persona con documento: " + documento));
        } catch (SQLException e) {
            throw new SicaException("Error al buscar persona por documento: " + e.getMessage(), e);
        }
    }
    
    /**
     * Lista todas las personas del sistema.
     * 
     * Permisos: requiere "gestionar_personas" o "buscar_persona"
     * 
     * @return Lista de personas
     * @throws PermisoDenegadoException si no tiene permiso
     * @throws SicaException si hay error de BD
     */
    public List<Persona> listarTodas() throws SicaException {
        // Verificar permiso RBAC
        if (!authorizationService.tieneAlgunoDeEstosPermisos("gestionar_personas", "buscar_persona")) {
            throw new PermisoDenegadoException("buscar_persona", 
                sessionManager.getUsuarioActual().getEmail());
        }
        
        try {
            return personaRepository.findAll();
        } catch (SQLException e) {
            throw new SicaException("Error al listar personas: " + e.getMessage(), e);
        }
    }
    
    /**
     * Lista personas por tipo (TRABAJADOR o INVITADO).
     * 
     * Permisos: requiere "gestionar_personas" o "buscar_persona"
     * 
     * @param tipo Tipo de persona
     * @return Lista de personas del tipo especificado
     * @throws PermisoDenegadoException si no tiene permiso
     * @throws SicaException si hay error de BD
     */
    public List<Persona> listarPorTipo(TipoPersona tipo) throws SicaException {
        // Verificar permiso RBAC
        if (!authorizationService.tieneAlgunoDeEstosPermisos("gestionar_personas", "buscar_persona")) {
            throw new PermisoDenegadoException("buscar_persona", 
                sessionManager.getUsuarioActual().getEmail());
        }
        
        try {
            return personaRepository.findByTipo(tipo);
        } catch (SQLException e) {
            throw new SicaException("Error al listar personas por tipo: " + e.getMessage(), e);
        }
    }
    
    /**
     * Lista personas de una empresa específica.
     * 
     * Permisos: requiere "gestionar_personas" o "buscar_persona"
     * 
     * @param empresaId ID de la empresa
     * @return Lista de personas de la empresa
     * @throws PermisoDenegadoException si no tiene permiso
     * @throws SicaException si hay error de BD
     */
    public List<Persona> listarPorEmpresa(Integer empresaId) throws SicaException {
        // Verificar permiso RBAC
        if (!authorizationService.tieneAlgunoDeEstosPermisos("gestionar_personas", "buscar_persona")) {
            throw new PermisoDenegadoException("buscar_persona", 
                sessionManager.getUsuarioActual().getEmail());
        }
        
        try {
            return personaRepository.findByEmpresa(empresaId);
        } catch (SQLException e) {
            throw new SicaException("Error al listar personas por empresa: " + e.getMessage(), e);
        }
    }
    
    /**
     * Busca personas por nombre (búsqueda parcial).
     * 
     * Permisos: requiere "gestionar_personas" o "buscar_persona"
     * 
     * @param nombre Nombre o parte del nombre
     * @return Lista de personas que coinciden
     * @throws PermisoDenegadoException si no tiene permiso
     * @throws SicaException si hay error de BD
     */
    public List<Persona> buscarPorNombre(String nombre) throws SicaException {
        // Verificar permiso RBAC
        if (!authorizationService.tieneAlgunoDeEstosPermisos("gestionar_personas", "buscar_persona")) {
            throw new PermisoDenegadoException("buscar_persona", 
                sessionManager.getUsuarioActual().getEmail());
        }
        
        try {
            return personaRepository.searchByNombre(nombre);
        } catch (SQLException e) {
            throw new SicaException("Error al buscar personas por nombre: " + e.getMessage(), e);
        }
    }
    
    /**
     * Cambia el estado de acceso de una persona.
     * Esta es una operación crítica que permite bloquear/desbloquear personas.
     * 
     * Permisos: requiere "bloquear_persona"
     * 
     * @param personaId ID de la persona
     * @param nuevoEstado Nuevo estado de acceso
     * @throws PermisoDenegadoException si no tiene permiso
     * @throws SicaException si hay error de BD
     */
    public void cambiarEstadoAcceso(Integer personaId, PersonaEstadoAcceso nuevoEstado) 
            throws SicaException {
        // 1. Verificar permiso RBAC (operación crítica)
        authorizationService.requirePermiso("bloquear_persona");
        
        try {
            // 2. Validaciones de negocio
            Persona persona = personaRepository.findById(personaId)
                .orElseThrow(() -> new SicaException(
                    "No se encontró la persona con ID: " + personaId));
            
            PersonaEstadoAcceso estadoAnterior = persona.getEstadoAcceso();
            
            // 3. Ejecutar operación
            persona.setEstadoAcceso(nuevoEstado);
            personaRepository.update(persona);
            
            // 4. Registrar en auditoría (operación crítica)
            auditoriaService.registrar(
                AccionAuditoria.CAMBIO_ESTADO_PERSONA,
                "personas",
                personaId,
                String.format("Estado de acceso cambiado: %s -> %s | Persona: %s (Doc: %s)", 
                    estadoAnterior.getNombreEstado(),
                    nuevoEstado.getNombreEstado(),
                    persona.getNombre(),
                    persona.getDocumentoIdentidad())
            );
            
        } catch (SQLException e) {
            throw new SicaException("Error al cambiar estado de acceso: " + e.getMessage(), e);
        }
    }
    
    /**
     * Bloquea el acceso de una persona al complejo.
     * 
     * Permisos: requiere "bloquear_persona"
     * 
     * @param personaId ID de la persona a bloquear
     * @throws PermisoDenegadoException si no tiene permiso
     * @throws SicaException si hay error de BD
     */
    public void bloquear(Integer personaId) throws SicaException {
        // Estado "Con Prohibición de Ingreso" tiene ID 2 (según data.sql)
        PersonaEstadoAcceso estadoBloqueado = new PersonaEstadoAcceso();
        estadoBloqueado.setId(2);
        estadoBloqueado.setNombreEstado("Con Prohibición de Ingreso");
        
        cambiarEstadoAcceso(personaId, estadoBloqueado);
    }
    
    /**
     * Desbloquea el acceso de una persona al complejo.
     * 
     * Permisos: requiere "bloquear_persona"
     * 
     * @param personaId ID de la persona a desbloquear
     * @throws PermisoDenegadoException si no tiene permiso
     * @throws SicaException si hay error de BD
     */
    public void desbloquear(Integer personaId) throws SicaException {
        // Estado "Activo" tiene ID 1 (según data.sql)
        PersonaEstadoAcceso estadoActivo = new PersonaEstadoAcceso();
        estadoActivo.setId(1);
        estadoActivo.setNombreEstado("Activo");
        
        cambiarEstadoAcceso(personaId, estadoActivo);
    }
    
    /**
     * Lista personas bloqueadas (con prohibición de ingreso).
     * 
     * Permisos: requiere "gestionar_personas" o "buscar_persona"
     * 
     * @return Lista de personas bloqueadas
     * @throws PermisoDenegadoException si no tiene permiso
     * @throws SicaException si hay error de BD
     */
    public List<Persona> listarBloqueadas() throws SicaException {
        // Verificar permiso RBAC
        if (!authorizationService.tieneAlgunoDeEstosPermisos("gestionar_personas", "buscar_persona")) {
            throw new PermisoDenegadoException("buscar_persona", 
                sessionManager.getUsuarioActual().getEmail());
        }
        
        try {
            // Estado "Con Prohibición de Ingreso" tiene ID 2
            return personaRepository.findByEstadoAcceso(2);
        } catch (SQLException e) {
            throw new SicaException("Error al listar personas bloqueadas: " + e.getMessage(), e);
        }
    }
    
    /**
     * Valida que una persona tenga todos los campos obligatorios.
     * 
     * @param persona Persona a validar
     * @throws SicaException si falla alguna validación
     */
    private void validarPersona(Persona persona) throws SicaException {
        if (persona == null) {
            throw new SicaException("La persona no puede ser nula");
        }
        
        if (persona.getNombre() == null || persona.getNombre().trim().isEmpty()) {
            throw new SicaException("El nombre de la persona es obligatorio");
        }
        
        if (persona.getDocumentoIdentidad() == null || 
            persona.getDocumentoIdentidad().trim().isEmpty()) {
            throw new SicaException("El documento de identidad es obligatorio");
        }
        
        if (persona.getTipoPersona() == null) {
            throw new SicaException("El tipo de persona es obligatorio");
        }
        
        // Si es TRABAJADOR, debe tener empresa
        if (persona.getTipoPersona() == TipoPersona.TRABAJADOR && persona.getEmpresa() == null) {
            throw new SicaException("Un trabajador debe tener una empresa asignada");
        }
        
        if (persona.getTelefono() == null || persona.getTelefono().trim().isEmpty()) {
            throw new SicaException("El teléfono es obligatorio");
        }
        
        if (persona.getEstadoAcceso() == null || persona.getEstadoAcceso().getId() == null) {
            throw new SicaException("El estado de acceso es obligatorio");
        }
    }
}
