package com.sica.repository;

import com.sica.database.DatabaseConnection;
import com.sica.model.entity.Permiso;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Permiso.
 * Maneja todas las operaciones de acceso a datos relacionadas con permisos.
 * 
 * PATRÓN DE DISEÑO: REPOSITORY
 * 
 * Principios SOLID aplicados:
 * - Single Responsibility: Solo maneja persistencia de permisos
 * 
 * @author SICA Team
 * @version 1.0
 */
public class PermisoRepository implements BaseRepository<Permiso, Integer> {
    
    private final Connection connection;
    
    public PermisoRepository() throws SQLException {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }
    
    @Override
    public Permiso save(Permiso permiso) throws SQLException {
        String sql = "INSERT INTO permisos (nombre_permiso, descripcion) VALUES (?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, permiso.getNombrePermiso());
            pstmt.setString(2, permiso.getDescripcion());
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    permiso.setId(generatedKeys.getInt(1));
                }
            }
        }
        
        return permiso;
    }
    
    @Override
    public Permiso update(Permiso permiso) throws SQLException {
        String sql = "UPDATE permisos SET nombre_permiso = ?, descripcion = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, permiso.getNombrePermiso());
            pstmt.setString(2, permiso.getDescripcion());
            pstmt.setInt(3, permiso.getId());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("No se encontró el permiso con ID: " + permiso.getId());
            }
        }
        
        return permiso;
    }
    
    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM permisos WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    @Override
    public Optional<Permiso> findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM permisos WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPermiso(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    @Override
    public List<Permiso> findAll() throws SQLException {
        String sql = "SELECT * FROM permisos ORDER BY nombre_permiso";
        List<Permiso> permisos = new ArrayList<>();
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                permisos.add(mapResultSetToPermiso(rs));
            }
        }
        
        return permisos;
    }
    
    @Override
    public boolean existsById(Integer id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM permisos WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        
        return false;
    }
    
    @Override
    public long count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM permisos";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        
        return 0;
    }
    
    /**
     * Busca un permiso por su nombre.
     * 
     * @param nombrePermiso Nombre del permiso
     * @return Optional con el permiso si existe
     * @throws SQLException si hay error en la consulta
     */
    public Optional<Permiso> findByNombre(String nombrePermiso) throws SQLException {
        String sql = "SELECT * FROM permisos WHERE nombre_permiso = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, nombrePermiso);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPermiso(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Busca permisos que contengan el texto en su nombre o descripción.
     * 
     * @param searchTerm Texto a buscar
     * @return Lista de permisos que coinciden
     * @throws SQLException si hay error en la consulta
     */
    public List<Permiso> search(String searchTerm) throws SQLException {
        String sql = """
            SELECT * FROM permisos 
            WHERE nombre_permiso LIKE ? OR descripcion LIKE ?
            ORDER BY nombre_permiso
            """;
        
        List<Permiso> permisos = new ArrayList<>();
        String searchPattern = "%" + searchTerm + "%";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    permisos.add(mapResultSetToPermiso(rs));
                }
            }
        }
        
        return permisos;
    }
    
    /**
     * Mapea un ResultSet a un objeto Permiso.
     * 
     * @param rs ResultSet posicionado en el registro
     * @return Objeto Permiso mapeado
     * @throws SQLException si hay error al leer datos
     */
    private Permiso mapResultSetToPermiso(ResultSet rs) throws SQLException {
        Permiso permiso = new Permiso();
        permiso.setId(rs.getInt("id"));
        permiso.setNombrePermiso(rs.getString("nombre_permiso"));
        permiso.setDescripcion(rs.getString("descripcion"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            permiso.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            permiso.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return permiso;
    }
}
