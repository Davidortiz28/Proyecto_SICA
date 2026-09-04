package com.sica.repository;

import com.sica.database.DatabaseConnection;
import com.sica.model.entity.Empresa;
import com.sica.model.entity.Persona;
import com.sica.model.entity.PersonaEstadoAcceso;
import com.sica.model.enums.TipoPersona;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones CRUD de Personas.
 * 
 * Responsabilidad: Acceso a datos de la tabla 'personas'
 * 
 * Principios SOLID aplicados:
 * - Single Responsibility: Solo acceso a datos de personas
 * - Dependency Inversion: Usa DatabaseConnection (abstracción)
 * 
 * @author SICA Team
 * @version 1.0
 */
public class PersonaRepository implements BaseRepository<Persona, Integer> {
    
    private final DatabaseConnection dbConnection;
    
    public PersonaRepository() throws SQLException {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    @Override
    public Persona save(Persona persona) throws SQLException {
        String sql = """
            INSERT INTO personas 
            (nombre, documento_identidad, tipo_persona, empresa_id, telefono, email, 
             foto_url, estado_acceso_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, persona.getNombre());
            stmt.setString(2, persona.getDocumentoIdentidad());
            stmt.setString(3, persona.getTipoPersona().name());
            
            if (persona.getEmpresa() != null) {
                stmt.setInt(4, persona.getEmpresa().getId());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }
            
            stmt.setString(5, persona.getTelefono());
            stmt.setString(6, persona.getEmail());
            stmt.setString(7, persona.getUrlFoto());
            stmt.setInt(8, persona.getEstadoAcceso().getId());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Error al crear persona, ninguna fila afectada");
            }
            
            // Obtener el ID generado
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    persona.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Error al crear persona, no se obtuvo el ID");
                }
            }
            
            return persona;
        }
    }
    
    @Override
    public Persona update(Persona persona) throws SQLException {
        String sql = """
            UPDATE personas 
            SET nombre = ?, 
                documento_identidad = ?, 
                tipo_persona = ?, 
                empresa_id = ?, 
                telefono = ?, 
                email = ?, 
                foto_url = ?, 
                estado_acceso_id = ?,
                fecha_actualizacion = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, persona.getNombre());
            stmt.setString(2, persona.getDocumentoIdentidad());
            stmt.setString(3, persona.getTipoPersona().name());
            
            if (persona.getEmpresa() != null) {
                stmt.setInt(4, persona.getEmpresa().getId());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }
            
            stmt.setString(5, persona.getTelefono());
            stmt.setString(6, persona.getEmail());
            stmt.setString(7, persona.getUrlFoto());
            stmt.setInt(8, persona.getEstadoAcceso().getId());
            stmt.setInt(9, persona.getId());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Error al actualizar persona, persona no encontrada");
            }
            
            return persona;
        }
    }
    
    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM personas WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    @Override
    public Optional<Persona> findById(Integer id) throws SQLException {
        String sql = """
            SELECT p.id, p.nombre, p.documento_identidad, p.tipo_persona, p.empresa_id, 
                   p.telefono, p.email, p.foto_url, p.estado_acceso_id,
                   p.fecha_registro, p.fecha_actualizacion,
                   e.id as empresa_id, e.nombre_empresa, e.nit,
                   ea.id as estado_id, ea.nombre_estado, ea.permite_acceso
            FROM personas p
            LEFT JOIN empresas e ON p.empresa_id = e.id
            INNER JOIN persona_estados_acceso ea ON p.estado_acceso_id = ea.id
            WHERE p.id = ?
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPersona(rs));
                }
                return Optional.empty();
            }
        }
    }
    
    @Override
    public List<Persona> findAll() throws SQLException {
        String sql = """
            SELECT p.id, p.nombre, p.documento_identidad, p.tipo_persona, p.empresa_id, 
                   p.telefono, p.email, p.foto_url, p.estado_acceso_id,
                   p.fecha_registro, p.fecha_actualizacion,
                   e.id as empresa_id, e.nombre_empresa, e.nit,
                   ea.id as estado_id, ea.nombre_estado, ea.permite_acceso
            FROM personas p
            LEFT JOIN empresas e ON p.empresa_id = e.id
            INNER JOIN persona_estados_acceso ea ON p.estado_acceso_id = ea.id
            ORDER BY p.nombre
            """;
        
        List<Persona> personas = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                personas.add(mapResultSetToPersona(rs));
            }
        }
        
        return personas;
    }
    
    /**
     * Busca una persona por documento de identidad.
     * 
     * @param documento Documento de identidad
     * @return Persona si existe
     * @throws SQLException si hay error de BD
     */
    public Optional<Persona> findByDocumento(String documento) throws SQLException {
        String sql = """
            SELECT p.id, p.nombre, p.documento_identidad, p.tipo_persona, p.empresa_id, 
                   p.telefono, p.email, p.foto_url, p.estado_acceso_id,
                   p.fecha_registro, p.fecha_actualizacion,
                   e.id as empresa_id, e.nombre_empresa, e.nit,
                   ea.id as estado_id, ea.nombre_estado, ea.permite_acceso
            FROM personas p
            LEFT JOIN empresas e ON p.empresa_id = e.id
            INNER JOIN persona_estados_acceso ea ON p.estado_acceso_id = ea.id
            WHERE p.documento_identidad = ?
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, documento);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPersona(rs));
                }
                return Optional.empty();
            }
        }
    }
    
    /**
     * Busca personas por tipo.
     * 
     * @param tipo Tipo de persona (TRABAJADOR, INVITADO)
     * @return Lista de personas del tipo especificado
     * @throws SQLException si hay error de BD
     */
    public List<Persona> findByTipo(TipoPersona tipo) throws SQLException {
        String sql = """
            SELECT p.id, p.nombre, p.documento_identidad, p.tipo_persona, p.empresa_id, 
                   p.telefono, p.email, p.foto_url, p.estado_acceso_id,
                   p.fecha_registro, p.fecha_actualizacion,
                   e.id as empresa_id, e.nombre_empresa, e.nit,
                   ea.id as estado_id, ea.nombre_estado, ea.permite_acceso
            FROM personas p
            LEFT JOIN empresas e ON p.empresa_id = e.id
            INNER JOIN persona_estados_acceso ea ON p.estado_acceso_id = ea.id
            WHERE p.tipo_persona = ?
            ORDER BY p.nombre
            """;
        
        List<Persona> personas = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, tipo.name());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    personas.add(mapResultSetToPersona(rs));
                }
            }
        }
        
        return personas;
    }
    
    /**
     * Busca personas por empresa.
     * 
     * @param empresaId ID de la empresa
     * @return Lista de personas de la empresa
     * @throws SQLException si hay error de BD
     */
    public List<Persona> findByEmpresa(Integer empresaId) throws SQLException {
        String sql = """
            SELECT p.id, p.nombre, p.documento_identidad, p.tipo_persona, p.empresa_id, 
                   p.telefono, p.email, p.foto_url, p.estado_acceso_id,
                   p.fecha_registro, p.fecha_actualizacion,
                   e.id as empresa_id, e.nombre_empresa, e.nit,
                   ea.id as estado_id, ea.nombre_estado, ea.permite_acceso
            FROM personas p
            LEFT JOIN empresas e ON p.empresa_id = e.id
            INNER JOIN persona_estados_acceso ea ON p.estado_acceso_id = ea.id
            WHERE p.empresa_id = ?
            ORDER BY p.nombre
            """;
        
        List<Persona> personas = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, empresaId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    personas.add(mapResultSetToPersona(rs));
                }
            }
        }
        
        return personas;
    }
    
    /**
     * Busca personas por nombre (búsqueda parcial).
     * 
     * @param nombre Nombre o parte del nombre
     * @return Lista de personas que coinciden
     * @throws SQLException si hay error de BD
     */
    public List<Persona> searchByNombre(String nombre) throws SQLException {
        String sql = """
            SELECT p.id, p.nombre, p.documento_identidad, p.tipo_persona, p.empresa_id, 
                   p.telefono, p.email, p.foto_url, p.estado_acceso_id,
                   p.fecha_registro, p.fecha_actualizacion,
                   e.id as empresa_id, e.nombre_empresa, e.nit,
                   ea.id as estado_id, ea.nombre_estado, ea.permite_acceso
            FROM personas p
            LEFT JOIN empresas e ON p.empresa_id = e.id
            INNER JOIN persona_estados_acceso ea ON p.estado_acceso_id = ea.id
            WHERE p.nombre LIKE ?
            ORDER BY p.nombre
            """;
        
        List<Persona> personas = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + nombre + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    personas.add(mapResultSetToPersona(rs));
                }
            }
        }
        
        return personas;
    }
    
    /**
     * Busca personas por estado de acceso.
     * 
     * @param estadoAccesoId ID del estado de acceso
     * @return Lista de personas con ese estado
     * @throws SQLException si hay error de BD
     */
    public List<Persona> findByEstadoAcceso(Integer estadoAccesoId) throws SQLException {
        String sql = """
            SELECT p.id, p.nombre, p.documento_identidad, p.tipo_persona, p.empresa_id, 
                   p.telefono, p.email, p.foto_url, p.estado_acceso_id,
                   p.fecha_registro, p.fecha_actualizacion,
                   e.id as empresa_id, e.nombre_empresa, e.nit,
                   ea.id as estado_id, ea.nombre_estado, ea.permite_acceso
            FROM personas p
            LEFT JOIN empresas e ON p.empresa_id = e.id
            INNER JOIN persona_estados_acceso ea ON p.estado_acceso_id = ea.id
            WHERE p.estado_acceso_id = ?
            ORDER BY p.nombre
            """;
        
        List<Persona> personas = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, estadoAccesoId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    personas.add(mapResultSetToPersona(rs));
                }
            }
        }
        
        return personas;
    }
    
    @Override
    public boolean existsById(Integer id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM personas WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        
        return false;
    }
    
    @Override
    public long count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM personas";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        
        return 0;
    }
    
    /**
     * Mapea un ResultSet a una entidad Persona.
     * 
     * @param rs ResultSet con los datos
     * @return Persona mapeada
     * @throws SQLException si hay error al leer datos
     */
    private Persona mapResultSetToPersona(ResultSet rs) throws SQLException {
        Persona persona = new Persona();
        
        persona.setId(rs.getInt("id"));
        persona.setNombre(rs.getString("nombre"));
        persona.setDocumentoIdentidad(rs.getString("documento_identidad"));
        persona.setTipoPersona(TipoPersona.valueOf(rs.getString("tipo_persona")));
        persona.setTelefono(rs.getString("telefono"));
        persona.setEmail(rs.getString("email"));
        persona.setUrlFoto(rs.getString("url_foto"));
        persona.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            persona.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        // Mapear empresa si existe
        Integer empresaId = rs.getInt("empresa_id");
        if (!rs.wasNull()) {
            Empresa empresa = new Empresa();
            empresa.setId(empresaId);
            empresa.setNombreEmpresa(rs.getString("nombre_empresa"));
            empresa.setNit(rs.getString("nit"));
            persona.setEmpresa(empresa);
        }
        
        // Mapear estado de acceso
        PersonaEstadoAcceso estadoAcceso = new PersonaEstadoAcceso();
        estadoAcceso.setId(rs.getInt("estado_id"));
        estadoAcceso.setNombreEstado(rs.getString("nombre_estado"));
        estadoAcceso.setNombreEstado(rs.getString("nombre_estado"));
        persona.setEstadoAcceso(estadoAcceso);
        
        return persona;
    }
}
