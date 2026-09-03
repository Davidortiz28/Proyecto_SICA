package com.sica.repository;

import com.sica.database.DatabaseConnection;
import com.sica.model.entity.Incidente;
import com.sica.model.entity.Usuario;
import com.sica.model.entity.Visita;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones CRUD de Incidentes.
 * 
 * Responsabilidad: Acceso a datos de la tabla 'incidentes'
 * 
 * @author SICA Team
 * @version 1.0
 */
public class IncidenteRepository implements BaseRepository<Incidente, Integer> {
    
    private final DatabaseConnection dbConnection;
    
    public IncidenteRepository() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    @Override
    public Incidente save(Incidente incidente) throws SQLException {
        String sql = """
            INSERT INTO incidentes 
            (visita_id, descripcion, fecha_incidente, reportado_por, gravedad, estado_resolucion)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            if (incidente.getVisita() != null) {
                stmt.setInt(1, incidente.getVisita().getId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            
            stmt.setString(2, incidente.getDescripcion());
            stmt.setTimestamp(3, Timestamp.valueOf(incidente.getFechaIncidente()));
            stmt.setInt(4, incidente.getReportadoPor().getId());
            stmt.setString(5, incidente.getGravedad());
            stmt.setString(6, incidente.getEstadoResolucion());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Error al crear incidente, ninguna fila afectada");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    incidente.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Error al crear incidente, no se obtuvo el ID");
                }
            }
            
            return incidente;
        }
    }
    
    @Override
    public Incidente update(Incidente incidente) throws SQLException {
        String sql = """
            UPDATE incidentes 
            SET visita_id = ?,
                descripcion = ?,
                fecha_incidente = ?,
                reportado_por = ?,
                gravedad = ?,
                estado_resolucion = ?
            WHERE id = ?
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            if (incidente.getVisita() != null) {
                stmt.setInt(1, incidente.getVisita().getId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            
            stmt.setString(2, incidente.getDescripcion());
            stmt.setTimestamp(3, Timestamp.valueOf(incidente.getFechaIncidente()));
            stmt.setInt(4, incidente.getReportadoPor().getId());
            stmt.setString(5, incidente.getGravedad());
            stmt.setString(6, incidente.getEstadoResolucion());
            stmt.setInt(7, incidente.getId());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Error al actualizar incidente, incidente no encontrado");
            }
            
            return incidente;
        }
    }
    
    @Override
    public void delete(Integer id) throws SQLException {
        String sql = "DELETE FROM incidentes WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Error al eliminar incidente, incidente no encontrado");
            }
        }
    }
    
    @Override
    public Optional<Incidente> findById(Integer id) throws SQLException {
        String sql = """
            SELECT i.id, i.visita_id, i.descripcion, i.fecha_incidente, i.reportado_por,
                   i.gravedad, i.estado_resolucion, i.created_at,
                   u.id as usuario_id, u.nombre as usuario_nombre
            FROM incidentes i
            INNER JOIN usuarios u ON i.reportado_por = u.id
            WHERE i.id = ?
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToIncidente(rs));
                }
                return Optional.empty();
            }
        }
    }
    
    @Override
    public List<Incidente> findAll() throws SQLException {
        String sql = """
            SELECT i.id, i.visita_id, i.descripcion, i.fecha_incidente, i.reportado_por,
                   i.gravedad, i.estado_resolucion, i.created_at,
                   u.id as usuario_id, u.nombre as usuario_nombre
            FROM incidentes i
            INNER JOIN usuarios u ON i.reportado_por = u.id
            ORDER BY i.fecha_incidente DESC
            """;
        
        List<Incidente> incidentes = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                incidentes.add(mapResultSetToIncidente(rs));
            }
        }
        
        return incidentes;
    }
    
    /**
     * Busca incidentes por gravedad.
     */
    public List<Incidente> findByGravedad(String gravedad) throws SQLException {
        String sql = """
            SELECT i.id, i.visita_id, i.descripcion, i.fecha_incidente, i.reportado_por,
                   i.gravedad, i.estado_resolucion, i.created_at,
                   u.id as usuario_id, u.nombre as usuario_nombre
            FROM incidentes i
            INNER JOIN usuarios u ON i.reportado_por = u.id
            WHERE i.gravedad = ?
            ORDER BY i.fecha_incidente DESC
            """;
        
        List<Incidente> incidentes = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, gravedad);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    incidentes.add(mapResultSetToIncidente(rs));
                }
            }
        }
        
        return incidentes;
    }
    
    /**
     * Busca incidentes por estado de resolución.
     */
    public List<Incidente> findByEstado(String estado) throws SQLException {
        String sql = """
            SELECT i.id, i.visita_id, i.descripcion, i.fecha_incidente, i.reportado_por,
                   i.gravedad, i.estado_resolucion, i.created_at,
                   u.id as usuario_id, u.nombre as usuario_nombre
            FROM incidentes i
            INNER JOIN usuarios u ON i.reportado_por = u.id
            WHERE i.estado_resolucion = ?
            ORDER BY i.fecha_incidente DESC
            """;
        
        List<Incidente> incidentes = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, estado);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    incidentes.add(mapResultSetToIncidente(rs));
                }
            }
        }
        
        return incidentes;
    }
    
    /**
     * Mapea un ResultSet a una entidad Incidente.
     */
    private Incidente mapResultSetToIncidente(ResultSet rs) throws SQLException {
        Incidente incidente = new Incidente();
        
        incidente.setId(rs.getInt("id"));
        
        // Mapear visita si existe (solo ID)
        Integer visitaId = rs.getInt("visita_id");
        if (!rs.wasNull()) {
            Visita visita = new Visita();
            visita.setId(visitaId);
            incidente.setVisita(visita);
        }
        
        incidente.setDescripcion(rs.getString("descripcion"));
        incidente.setFechaIncidente(rs.getTimestamp("fecha_incidente").toLocalDateTime());
        incidente.setGravedad(rs.getString("gravedad"));
        incidente.setEstadoResolucion(rs.getString("estado_resolucion"));
        incidente.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        
        // Mapear usuario reportador
        Usuario reportador = new Usuario();
        reportador.setId(rs.getInt("usuario_id"));
        reportador.setNombre(rs.getString("usuario_nombre"));
        incidente.setReportadoPor(reportador);
        
        return incidente;
    }
}
