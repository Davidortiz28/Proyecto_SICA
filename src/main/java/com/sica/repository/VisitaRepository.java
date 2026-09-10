package com.sica.repository;

import com.sica.database.DatabaseConnection;
import com.sica.model.entity.*;
import com.sica.model.enums.TipoPersona;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones CRUD de Visitas.
 * 
 * Responsabilidad: Acceso a datos de la tabla 'visitas'
 * 
 * Principios SOLID aplicados:
 * - Single Responsibility: Solo acceso a datos de visitas
 * - Dependency Inversion: Usa DatabaseConnection (abstracción)
 * 
 * @author SICA Team
 * @version 1.0
 */
public class VisitaRepository implements BaseRepository<Visita, Integer> {
    
    private final DatabaseConnection dbConnection;
    
    public VisitaRepository() throws SQLException {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    @Override
    public Visita save(Visita visita) throws SQLException {
        String sql = """
            INSERT INTO visitas 
            (persona_id, fecha_entrada, fecha_salida, estado_visita_id, vehiculo_placa,
             visita_aprobada_por, motivo_visita, observaciones)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, visita.getPersona().getId());
            
            if (visita.getFechaEntrada() != null) {
                stmt.setTimestamp(2, Timestamp.valueOf(visita.getFechaEntrada()));
            } else {
                stmt.setNull(2, Types.TIMESTAMP);
            }
            
            if (visita.getFechaSalida() != null) {
                stmt.setTimestamp(3, Timestamp.valueOf(visita.getFechaSalida()));
            } else {
                stmt.setNull(3, Types.TIMESTAMP);
            }
            
            stmt.setInt(4, visita.getEstado().getId());
            stmt.setString(5, visita.getVehiculoPlaca());
            
            if (visita.getVisitaAprobadaPor() != null) {
                stmt.setInt(6, visita.getVisitaAprobadaPor().getId());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }
            
            stmt.setString(7, visita.getMotivoVisita());
            stmt.setString(8, visita.getObservaciones());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Error al crear visita, ninguna fila afectada");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    visita.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Error al crear visita, no se obtuvo el ID");
                }
            }
            
            return visita;
        }
    }
    
    @Override
    public Visita update(Visita visita) throws SQLException {
        String sql = """
            UPDATE visitas 
            SET persona_id = ?,
                fecha_entrada = ?,
                fecha_salida = ?,
                estado_visita_id = ?,
                vehiculo_placa = ?,
                visita_aprobada_por = ?,
                motivo_visita = ?,
                observaciones = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, visita.getPersona().getId());
            
            if (visita.getFechaEntrada() != null) {
                stmt.setTimestamp(2, Timestamp.valueOf(visita.getFechaEntrada()));
            } else {
                stmt.setNull(2, Types.TIMESTAMP);
            }
            
            if (visita.getFechaSalida() != null) {
                stmt.setTimestamp(3, Timestamp.valueOf(visita.getFechaSalida()));
            } else {
                stmt.setNull(3, Types.TIMESTAMP);
            }
            
            stmt.setInt(4, visita.getEstado().getId());
            stmt.setString(5, visita.getVehiculoPlaca());
            
            if (visita.getVisitaAprobadaPor() != null) {
                stmt.setInt(6, visita.getVisitaAprobadaPor().getId());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }
            
            stmt.setString(7, visita.getMotivoVisita());
            stmt.setString(8, visita.getObservaciones());
            stmt.setInt(9, visita.getId());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Error al actualizar visita, visita no encontrada");
            }
            
            return visita;
        }
    }
    
    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM visitas WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    @Override
    public Optional<Visita> findById(Integer id) throws SQLException {
        String sql = """
            SELECT v.id, v.persona_id, v.fecha_entrada, v.fecha_salida, v.estado_visita_id,
                   v.vehiculo_placa, v.visita_aprobada_por, v.motivo_visita, v.observaciones,
                   v.created_at, v.updated_at,
                   p.nombre as persona_nombre, p.documento_identidad, p.tipo_persona,
                   ve.id as estado_id, ve.nombre_estado, ve.descripcion as estado_desc,
                   u.id as aprobador_id, u.nombre as aprobador_nombre
            FROM visitas v
            INNER JOIN personas p ON v.persona_id = p.id
            INNER JOIN visita_estados ve ON v.estado_visita_id = ve.id
            LEFT JOIN usuarios u ON v.visita_aprobada_por = u.id
            WHERE v.id = ?
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToVisita(rs));
                }
                return Optional.empty();
            }
        }
    }
    
    @Override
    public List<Visita> findAll() throws SQLException {
        String sql = """
            SELECT v.id, v.persona_id, v.fecha_entrada, v.fecha_salida, v.estado_visita_id,
                   v.vehiculo_placa, v.visita_aprobada_por, v.motivo_visita, v.observaciones,
                   v.created_at, v.updated_at,
                   p.nombre as persona_nombre, p.documento_identidad, p.tipo_persona,
                   ve.id as estado_id, ve.nombre_estado, ve.descripcion as estado_desc,
                   u.id as aprobador_id, u.nombre as aprobador_nombre
            FROM visitas v
            INNER JOIN personas p ON v.persona_id = p.id
            INNER JOIN visita_estados ve ON v.estado_visita_id = ve.id
            LEFT JOIN usuarios u ON v.visita_aprobada_por = u.id
            ORDER BY v.created_at DESC
            """;
        
        List<Visita> visitas = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                visitas.add(mapResultSetToVisita(rs));
            }
        }
        
        return visitas;
    }
    
    /**
     * Busca visitas por persona.
     */
    public List<Visita> findByPersona(Integer personaId) throws SQLException {
        String sql = """
            SELECT v.id, v.persona_id, v.fecha_entrada, v.fecha_salida, v.estado_visita_id,
                   v.vehiculo_placa, v.visita_aprobada_por, v.motivo_visita, v.observaciones,
                   v.created_at, v.updated_at,
                   p.nombre as persona_nombre, p.documento_identidad, p.tipo_persona,
                   ve.id as estado_id, ve.nombre_estado, ve.descripcion as estado_desc,
                   u.id as aprobador_id, u.nombre as aprobador_nombre
            FROM visitas v
            INNER JOIN personas p ON v.persona_id = p.id
            INNER JOIN visita_estados ve ON v.estado_visita_id = ve.id
            LEFT JOIN usuarios u ON v.visita_aprobada_por = u.id
            WHERE v.persona_id = ?
            ORDER BY v.created_at DESC
            """;
        
        List<Visita> visitas = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, personaId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    visitas.add(mapResultSetToVisita(rs));
                }
            }
        }
        
        return visitas;
    }
    
    /**
     * Busca la última visita abierta de una persona (sin fecha de salida).
     */
    public Optional<Visita> findUltimaVisitaAbiertaPorPersona(Integer personaId) throws SQLException {
        String sql = """
            SELECT v.id, v.persona_id, v.fecha_entrada, v.fecha_salida, v.estado_visita_id,
                   v.vehiculo_placa, v.visita_aprobada_por, v.motivo_visita, v.observaciones,
                   v.created_at, v.updated_at,
                   p.nombre as persona_nombre, p.documento_identidad, p.tipo_persona,
                   ve.id as estado_id, ve.nombre_estado, ve.descripcion as estado_desc,
                   u.id as aprobador_id, u.nombre as aprobador_nombre
            FROM visitas v
            INNER JOIN personas p ON v.persona_id = p.id
            INNER JOIN visita_estados ve ON v.estado_visita_id = ve.id
            LEFT JOIN usuarios u ON v.visita_aprobada_por = u.id
            WHERE v.persona_id = ? AND v.fecha_salida IS NULL
            ORDER BY v.fecha_entrada DESC
            LIMIT 1
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, personaId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToVisita(rs));
                }
                return Optional.empty();
            }
        }
    }
    
    /**
     * Busca visitas por estado.
     */
    public List<Visita> findByEstado(Integer estadoId) throws SQLException {
        String sql = """
            SELECT v.id, v.persona_id, v.fecha_entrada, v.fecha_salida, v.estado_visita_id,
                   v.vehiculo_placa, v.visita_aprobada_por, v.motivo_visita, v.observaciones,
                   v.created_at, v.updated_at,
                   p.nombre as persona_nombre, p.documento_identidad, p.tipo_persona,
                   ve.id as estado_id, ve.nombre_estado, ve.descripcion as estado_desc,
                   u.id as aprobador_id, u.nombre as aprobador_nombre
            FROM visitas v
            INNER JOIN personas p ON v.persona_id = p.id
            INNER JOIN visita_estados ve ON v.estado_visita_id = ve.id
            LEFT JOIN usuarios u ON v.visita_aprobada_por = u.id
            WHERE v.estado_visita_id = ?
            ORDER BY v.created_at DESC
            """;
        
        List<Visita> visitas = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, estadoId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    visitas.add(mapResultSetToVisita(rs));
                }
            }
        }
        
        return visitas;
    }
    
    /**
     * Busca personas actualmente dentro del complejo.
     */
    public List<Visita> findPersonasDentro() throws SQLException {
        String sql = """
            SELECT v.id, v.persona_id, v.fecha_entrada, v.fecha_salida, v.estado_visita_id,
                   v.vehiculo_placa, v.visita_aprobada_por, v.motivo_visita, v.observaciones,
                   v.created_at, v.updated_at,
                   p.nombre as persona_nombre, p.documento_identidad, p.tipo_persona,
                   ve.id as estado_id, ve.nombre_estado, ve.descripcion as estado_desc,
                   u.id as aprobador_id, u.nombre as aprobador_nombre
            FROM visitas v
            INNER JOIN personas p ON v.persona_id = p.id
            INNER JOIN visita_estados ve ON v.estado_visita_id = ve.id
            LEFT JOIN usuarios u ON v.visita_aprobada_por = u.id
            WHERE ve.nombre_estado = 'Dentro'
            ORDER BY v.fecha_entrada DESC
            """;
        
        List<Visita> visitas = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                visitas.add(mapResultSetToVisita(rs));
            }
        }
        
        return visitas;
    }
    
    /**
     * Busca visitas pendientes de aprobación.
     */
    public List<Visita> findPendientesAprobacion() throws SQLException {
        String sql = """
            SELECT v.id, v.persona_id, v.fecha_entrada, v.fecha_salida, v.estado_visita_id,
                   v.vehiculo_placa, v.visita_aprobada_por, v.motivo_visita, v.observaciones,
                   v.created_at, v.updated_at,
                   p.nombre as persona_nombre, p.documento_identidad, p.tipo_persona,
                   ve.id as estado_id, ve.nombre_estado, ve.descripcion as estado_desc,
                   u.id as aprobador_id, u.nombre as aprobador_nombre
            FROM visitas v
            INNER JOIN personas p ON v.persona_id = p.id
            INNER JOIN visita_estados ve ON v.estado_visita_id = ve.id
            LEFT JOIN usuarios u ON v.visita_aprobada_por = u.id
            WHERE ve.nombre_estado LIKE '%Pendiente%'
            ORDER BY v.created_at ASC
            """;
        
        List<Visita> visitas = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                visitas.add(mapResultSetToVisita(rs));
            }
        }
        
        return visitas;
    }
    
    /**
     * Mapea un ResultSet a una entidad Visita.
     */
    private Visita mapResultSetToVisita(ResultSet rs) throws SQLException {
        Visita visita = new Visita();
        
        visita.setId(rs.getInt("id"));
        
        // Mapear persona (básico)
        Persona persona = new Persona();
        persona.setId(rs.getInt("persona_id"));
        persona.setNombre(rs.getString("persona_nombre"));
        persona.setDocumentoIdentidad(rs.getString("documento_identidad"));
        persona.setTipoPersona(TipoPersona.valueOf(rs.getString("tipo_persona")));
        visita.setPersona(persona);
        
        Timestamp fechaEntrada = rs.getTimestamp("fecha_entrada");
        if (fechaEntrada != null) {
            visita.setFechaEntrada(fechaEntrada.toLocalDateTime());
        }
        
        Timestamp fechaSalida = rs.getTimestamp("fecha_salida");
        if (fechaSalida != null) {
            visita.setFechaSalida(fechaSalida.toLocalDateTime());
        }
        
        // Mapear estado
        VisitaEstado estado = new VisitaEstado();
        estado.setId(rs.getInt("estado_id"));
        estado.setNombreEstado(rs.getString("nombre_estado"));
        estado.setDescripcion(rs.getString("estado_desc"));
        visita.setEstado(estado);
        
        visita.setVehiculoPlaca(rs.getString("vehiculo_placa"));
        
        // Mapear usuario aprobador si existe
        Integer aprobadorId = rs.getInt("aprobador_id");
        if (!rs.wasNull()) {
            Usuario aprobador = new Usuario();
            aprobador.setId(aprobadorId);
            aprobador.setNombre(rs.getString("aprobador_nombre"));
            visita.setVisitaAprobadaPor(aprobador);
        }
        
        visita.setMotivoVisita(rs.getString("motivo_visita"));
        visita.setObservaciones(rs.getString("observaciones"));
        visita.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            visita.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return visita;
    }
    
    @Override
    public boolean existsById(Integer id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM visitas WHERE id = ?";
        
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
        String sql = "SELECT COUNT(*) FROM visitas";
        
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
     * Busca todas las visitas que se superponen con un día específico.
     * Incluye visitas que:
     * - Comenzaron y terminaron en el día
     * - Comenzaron antes pero continuaron en el día
     * - Comenzaron en el día pero no han finalizado (fecha_salida null)
     * - Comenzaron en el día pero finalizaron después
     * 
     * @param fecha Fecha del día a analizar
     * @return Lista de visitas relevantes para el análisis de ocupación
     * @throws SQLException si hay error en la consulta
     */
    public List<Visita> findVisitasParaDia(java.time.LocalDate fecha) throws SQLException {
        String sql = """
            SELECT v.id, v.persona_id, v.fecha_entrada, v.fecha_salida, v.estado_visita_id,
                   v.vehiculo_placa, v.visita_aprobada_por, v.motivo_visita, v.observaciones,
                   v.created_at, v.updated_at,
                   p.nombre as persona_nombre, p.documento_identidad, p.tipo_persona,
                   ve.id as estado_id, ve.nombre_estado, ve.descripcion as estado_desc,
                   u.id as aprobador_id, u.nombre as aprobador_nombre
            FROM visitas v
            INNER JOIN personas p ON v.persona_id = p.id
            INNER JOIN visita_estados ve ON v.estado_visita_id = ve.id
            LEFT JOIN usuarios u ON v.visita_aprobada_por = u.id
            WHERE v.fecha_entrada IS NOT NULL
              AND (
                  -- Entrada en el día
                  DATE(v.fecha_entrada) = ?
                  -- O entrada antes del día pero sin salida (aún dentro)
                  OR (DATE(v.fecha_entrada) < ? AND v.fecha_salida IS NULL)
                  -- O entrada antes del día pero salida en o después del día
                  OR (DATE(v.fecha_entrada) < ? AND DATE(v.fecha_salida) >= ?)
              )
            ORDER BY v.fecha_entrada ASC
            """;
        
        List<Visita> visitas = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            java.sql.Date sqlDate = java.sql.Date.valueOf(fecha);
            stmt.setDate(1, sqlDate);
            stmt.setDate(2, sqlDate);
            stmt.setDate(3, sqlDate);
            stmt.setDate(4, sqlDate);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    visitas.add(mapResultSetToVisita(rs));
                }
            }
        }
        
        return visitas;
    }
}
