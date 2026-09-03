package com.sica.repository;

import com.sica.database.DatabaseConnection;
import com.sica.model.entity.Permiso;
import com.sica.model.entity.Rol;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Rol.
 * Implementa el patrón Repository para abstrae el acceso a datos de roles.
 * 
 * PATRÓN DE DISEÑO: REPOSITORY
 * - Separa la lógica de acceso a datos de la lógica de negocio
 * - Proporciona una interfaz orientada a objetos para la BD
 * 
 * Principios SOLID aplicados:
 * - Single Responsibility: Solo maneja persistencia de roles
 * - Dependency Inversion: Usa Connection (interfaz) no implementación
 * 
 * @author SICA Team
 * @version 1.0
 */
public class RolRepository implements BaseRepository<Rol, Integer> {
    
    private final Connection connection;
    
    /**
     * Constructor que obtiene la conexión del Singleton.
     * 
     * @throws SQLException si hay error al obtener la conexión
     */
    public RolRepository() throws SQLException {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }
    
    @Override
    public Rol save(Rol rol) throws SQLException {
        String sql = "INSERT INTO roles (nombre_rol, descripcion) VALUES (?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, rol.getNombreRol());
            pstmt.setString(2, rol.getDescripcion());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("No se pudo crear el rol");
            }
            
            // Obtener el ID generado
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    rol.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("No se pudo obtener el ID del rol creado");
                }
            }
        }
        
        return rol;
    }
    
    @Override
    public Rol update(Rol rol) throws SQLException {
        String sql = "UPDATE roles SET nombre_rol = ?, descripcion = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, rol.getNombreRol());
            pstmt.setString(2, rol.getDescripcion());
            pstmt.setInt(3, rol.getId());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("No se encontró el rol con ID: " + rol.getId());
            }
        }
        
        return rol;
    }
    
    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM roles WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    @Override
    public Optional<Rol> findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM roles WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToRol(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    @Override
    public List<Rol> findAll() throws SQLException {
        String sql = "SELECT * FROM roles ORDER BY nombre_rol";
        List<Rol> roles = new ArrayList<>();
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                roles.add(mapResultSetToRol(rs));
            }
        }
        
        return roles;
    }
    
    @Override
    public boolean existsById(Integer id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM roles WHERE id = ?";
        
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
        String sql = "SELECT COUNT(*) FROM roles";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        
        return 0;
    }
    
    /**
     * Busca un rol por su nombre.
     * 
     * @param nombreRol Nombre del rol a buscar
     * @return Optional con el rol si existe
     * @throws SQLException si hay error en la consulta
     */
    public Optional<Rol> findByNombre(String nombreRol) throws SQLException {
        String sql = "SELECT * FROM roles WHERE nombre_rol = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, nombreRol);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToRol(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Obtiene los permisos de un rol.
     * 
     * @param rolId ID del rol
     * @return Lista de permisos del rol
     * @throws SQLException si hay error en la consulta
     */
    public List<Permiso> findPermisosByRolId(Integer rolId) throws SQLException {
        String sql = """
            SELECT p.* 
            FROM permisos p
            INNER JOIN rol_permisos rp ON p.id = rp.permiso_id
            WHERE rp.rol_id = ?
            ORDER BY p.nombre_permiso
            """;
        
        List<Permiso> permisos = new ArrayList<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, rolId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    permisos.add(mapResultSetToPermiso(rs));
                }
            }
        }
        
        return permisos;
    }
    
    /**
     * Asigna un permiso a un rol.
     * 
     * @param rolId ID del rol
     * @param permisoId ID del permiso
     * @throws SQLException si hay error en la operación
     */
    public void asignarPermiso(Integer rolId, Integer permisoId) throws SQLException {
        String sql = "INSERT INTO rol_permisos (rol_id, permiso_id) VALUES (?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, rolId);
            pstmt.setInt(2, permisoId);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Remueve un permiso de un rol.
     * 
     * @param rolId ID del rol
     * @param permisoId ID del permiso
     * @return true si se removió, false si no existía la relación
     * @throws SQLException si hay error en la operación
     */
    public boolean removerPermiso(Integer rolId, Integer permisoId) throws SQLException {
        String sql = "DELETE FROM rol_permisos WHERE rol_id = ? AND permiso_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, rolId);
            pstmt.setInt(2, permisoId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Mapea un ResultSet a un objeto Rol.
     * 
     * @param rs ResultSet posicionado en el registro
     * @return Objeto Rol mapeado
     * @throws SQLException si hay error al leer datos
     */
    private Rol mapResultSetToRol(ResultSet rs) throws SQLException {
        Rol rol = new Rol();
        rol.setId(rs.getInt("id"));
        rol.setNombreRol(rs.getString("nombre_rol"));
        rol.setDescripcion(rs.getString("descripcion"));
        
        // Convertir timestamps a LocalDateTime
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            rol.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            rol.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return rol;
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
