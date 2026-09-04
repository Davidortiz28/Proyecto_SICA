package com.sica.repository;

import com.sica.database.DatabaseConnection;
import com.sica.model.entity.Empresa;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones CRUD de Empresas.
 * 
 * Responsabilidad: Acceso a datos de la tabla 'empresas'
 * 
 * Principios SOLID aplicados:
 * - Single Responsibility: Solo acceso a datos de empresas
 * - Dependency Inversion: Usa DatabaseConnection (abstracción)
 * 
 * @author SICA Team
 * @version 1.0
 */
public class EmpresaRepository implements BaseRepository<Empresa, Integer> {
    
    private final DatabaseConnection dbConnection;
    
    public EmpresaRepository() throws SQLException {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    @Override
    public Empresa save(Empresa empresa) throws SQLException {
        String sql = """
            INSERT INTO empresas (nombre_empresa, nit, telefono, email, direccion, esta_activa)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, empresa.getNombreEmpresa());
            stmt.setString(2, empresa.getNit());
            stmt.setString(3, empresa.getTelefono());
            stmt.setString(4, empresa.getEmail());
            stmt.setString(5, empresa.getDireccion());
            stmt.setBoolean(6, empresa.getEstaActiva());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Error al crear empresa, ninguna fila afectada");
            }
            
            // Obtener el ID generado
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    empresa.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Error al crear empresa, no se obtuvo el ID");
                }
            }
            
            return empresa;
        }
    }
    
    @Override
    public Empresa update(Empresa empresa) throws SQLException {
        String sql = """
            UPDATE empresas 
            SET nombre_empresa = ?, 
                nit = ?, 
                telefono = ?, 
                email = ?, 
                direccion = ?, 
                esta_activa = ?,
                fecha_actualizacion = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, empresa.getNombreEmpresa());
            stmt.setString(2, empresa.getNit());
            stmt.setString(3, empresa.getTelefono());
            stmt.setString(4, empresa.getEmail());
            stmt.setString(5, empresa.getDireccion());
            stmt.setBoolean(6, empresa.getEstaActiva());
            stmt.setInt(7, empresa.getId());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Error al actualizar empresa, empresa no encontrada");
            }
            
            return empresa;
        }
    }
    
    @Override
    public boolean delete(Integer id) throws SQLException {
        // En este sistema, eliminación es lógica (desactivar)
        String sql = "UPDATE empresas SET esta_activa = FALSE WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    @Override
    public Optional<Empresa> findById(Integer id) throws SQLException {
        String sql = """
            SELECT id, nombre_empresa, nit, telefono, email, direccion, 
                   esta_activa, fecha_registro, fecha_actualizacion
            FROM empresas
            WHERE id = ?
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToEmpresa(rs));
                }
                return Optional.empty();
            }
        }
    }
    
    @Override
    public List<Empresa> findAll() throws SQLException {
        String sql = """
            SELECT id, nombre_empresa, nit, telefono, email, direccion, 
                   esta_activa, fecha_registro, fecha_actualizacion
            FROM empresas
            ORDER BY nombre_empresa
            """;
        
        List<Empresa> empresas = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                empresas.add(mapResultSetToEmpresa(rs));
            }
        }
        
        return empresas;
    }
    
    /**
     * Busca empresas por estado (activa/inactiva).
     * 
     * @param activas true para empresas activas, false para inactivas
     * @return Lista de empresas
     * @throws SQLException si hay error de BD
     */
    public List<Empresa> findByEstaActiva(boolean activas) throws SQLException {
        String sql = """
            SELECT id, nombre_empresa, nit, telefono, email, direccion, 
                   esta_activa, fecha_registro, fecha_actualizacion
            FROM empresas
            WHERE esta_activa = ?
            ORDER BY nombre_empresa
            """;
        
        List<Empresa> empresas = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBoolean(1, activas);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    empresas.add(mapResultSetToEmpresa(rs));
                }
            }
        }
        
        return empresas;
    }
    
    /**
     * Busca una empresa por NIT.
     * 
     * @param nit NIT de la empresa
     * @return Empresa si existe
     * @throws SQLException si hay error de BD
     */
    public Optional<Empresa> findByNit(String nit) throws SQLException {
        String sql = """
            SELECT id, nombre_empresa, nit, telefono, email, direccion, 
                   esta_activa, fecha_registro, fecha_actualizacion
            FROM empresas
            WHERE nit = ?
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, nit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToEmpresa(rs));
                }
                return Optional.empty();
            }
        }
    }
    
    /**
     * Busca empresas por nombre (búsqueda parcial).
     * 
     * @param nombre Nombre o parte del nombre
     * @return Lista de empresas que coinciden
     * @throws SQLException si hay error de BD
     */
    public List<Empresa> searchByNombre(String nombre) throws SQLException {
        String sql = """
            SELECT id, nombre_empresa, nit, telefono, email, direccion, 
                   esta_activa, fecha_registro, fecha_actualizacion
            FROM empresas
            WHERE nombre_empresa LIKE ?
            ORDER BY nombre_empresa
            """;
        
        List<Empresa> empresas = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + nombre + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    empresas.add(mapResultSetToEmpresa(rs));
                }
            }
        }
        
        return empresas;
    }
    
    @Override
    public boolean existsById(Integer id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM empresas WHERE id = ?";
        
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
        String sql = "SELECT COUNT(*) FROM empresas";
        
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
     * Mapea un ResultSet a una entidad Empresa.
     * 
     * @param rs ResultSet con los datos
     * @return Empresa mapeada
     * @throws SQLException si hay error al leer datos
     */
    private Empresa mapResultSetToEmpresa(ResultSet rs) throws SQLException {
        Empresa empresa = new Empresa();
        
        empresa.setId(rs.getInt("id"));
        empresa.setNombreEmpresa(rs.getString("nombre_empresa"));
        empresa.setNit(rs.getString("nit"));
        empresa.setTelefono(rs.getString("telefono"));
        empresa.setEmail(rs.getString("email"));
        empresa.setDireccion(rs.getString("direccion"));
        empresa.setEstaActiva(rs.getBoolean("esta_activa"));
        empresa.setFechaRegistro(rs.getTimestamp("fecha_registro").toLocalDateTime());
        
        Timestamp fechaActualizacion = rs.getTimestamp("fecha_actualizacion");
        if (fechaActualizacion != null) {
            empresa.setFechaActualizacion(fechaActualizacion.toLocalDateTime());
        }
        
        return empresa;
    }
}
