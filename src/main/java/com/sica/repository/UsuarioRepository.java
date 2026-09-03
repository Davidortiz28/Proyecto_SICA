package com.sica.repository;

import com.sica.database.DatabaseConnection;
import com.sica.model.entity.Rol;
import com.sica.model.entity.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Usuario.
 * Maneja todas las operaciones de acceso a datos relacionadas con usuarios,
 * incluyendo la carga de roles y permisos asociados.
 * 
 * PATRÓN DE DISEÑO: REPOSITORY
 * 
 * Principios SOLID aplicados:
 * - Single Responsibility: Solo maneja persistencia de usuarios
 * - Dependency Inversion: Usa Connection e interfaces de repositorio
 * 
 * @author SICA Team
 * @version 1.0
 */
public class UsuarioRepository implements BaseRepository<Usuario, Integer> {
    
    private final Connection connection;
    private final RolRepository rolRepository;
    
    public UsuarioRepository() throws SQLException {
        this.connection = DatabaseConnection.getInstance().getConnection();
        this.rolRepository = new RolRepository();
    }
    
    @Override
    public Usuario save(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuarios (nombre, email, password, rol_id, esta_activo) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, usuario.getNombre());
            pstmt.setString(2, usuario.getEmail());
            pstmt.setString(3, usuario.getPassword());
            pstmt.setInt(4, usuario.getRol().getId());
            pstmt.setBoolean(5, usuario.isEstaActivo());
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    usuario.setId(generatedKeys.getInt(1));
                }
            }
        }
        
        return usuario;
    }
    
    @Override
    public Usuario update(Usuario usuario) throws SQLException {
        String sql = "UPDATE usuarios SET nombre = ?, email = ?, password = ?, rol_id = ?, esta_activo = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, usuario.getNombre());
            pstmt.setString(2, usuario.getEmail());
            pstmt.setString(3, usuario.getPassword());
            pstmt.setInt(4, usuario.getRol().getId());
            pstmt.setBoolean(5, usuario.isEstaActivo());
            pstmt.setInt(6, usuario.getId());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("No se encontró el usuario con ID: " + usuario.getId());
            }
        }
        
        return usuario;
    }
    
    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM usuarios WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    @Override
    public Optional<Usuario> findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUsuario(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    @Override
    public List<Usuario> findAll() throws SQLException {
        String sql = "SELECT * FROM usuarios ORDER BY nombre";
        List<Usuario> usuarios = new ArrayList<>();
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                usuarios.add(mapResultSetToUsuario(rs));
            }
        }
        
        return usuarios;
    }
    
    @Override
    public boolean existsById(Integer id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE id = ?";
        
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
        String sql = "SELECT COUNT(*) FROM usuarios";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        
        return 0;
    }
    
    /**
     * Busca un usuario por su email.
     * Este método es crucial para el proceso de login.
     * 
     * @param email Email del usuario
     * @return Optional con el usuario si existe
     * @throws SQLException si hay error en la consulta
     */
    public Optional<Usuario> findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE email = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUsuario(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Busca usuarios activos.
     * 
     * @return Lista de usuarios activos
     * @throws SQLException si hay error en la consulta
     */
    public List<Usuario> findByEstaActivo(boolean activo) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE esta_activo = ? ORDER BY nombre";
        List<Usuario> usuarios = new ArrayList<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setBoolean(1, activo);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    usuarios.add(mapResultSetToUsuario(rs));
                }
            }
        }
        
        return usuarios;
    }
    
    /**
     * Busca usuarios por rol.
     * 
     * @param rolId ID del rol
     * @return Lista de usuarios con ese rol
     * @throws SQLException si hay error en la consulta
     */
    public List<Usuario> findByRolId(Integer rolId) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE rol_id = ? ORDER BY nombre";
        List<Usuario> usuarios = new ArrayList<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, rolId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    usuarios.add(mapResultSetToUsuario(rs));
                }
            }
        }
        
        return usuarios;
    }
    
    /**
     * Actualiza el estado activo de un usuario.
     * 
     * @param id ID del usuario
     * @param activo true para activar, false para desactivar
     * @return true si se actualizó correctamente
     * @throws SQLException si hay error en la operación
     */
    public boolean updateEstaActivo(Integer id, boolean activo) throws SQLException {
        String sql = "UPDATE usuarios SET esta_activo = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setBoolean(1, activo);
            pstmt.setInt(2, id);
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Actualiza solo la contraseña de un usuario.
     * 
     * @param id ID del usuario
     * @param nuevaPassword Nueva contraseña (debe estar hasheada)
     * @return true si se actualizó correctamente
     * @throws SQLException si hay error en la operación
     */
    public boolean updatePassword(Integer id, String nuevaPassword) throws SQLException {
        String sql = "UPDATE usuarios SET password = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, nuevaPassword);
            pstmt.setInt(2, id);
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Verifica si existe un usuario con el email dado.
     * Útil para validar unicidad antes de crear un usuario.
     * 
     * @param email Email a verificar
     * @return true si existe, false si no existe
     * @throws SQLException si hay error en la consulta
     */
    public boolean existsByEmail(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE email = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Mapea un ResultSet a un objeto Usuario.
     * Incluye la carga del Rol con sus permisos.
     * 
     * @param rs ResultSet posicionado en el registro
     * @return Objeto Usuario mapeado
     * @throws SQLException si hay error al leer datos
     */
    private Usuario mapResultSetToUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getInt("id"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setEmail(rs.getString("email"));
        usuario.setPassword(rs.getString("password"));
        usuario.setEstaActivo(rs.getBoolean("esta_activo"));
        
        // Cargar el rol completo
        int rolId = rs.getInt("rol_id");
        Optional<Rol> rolOpt = rolRepository.findById(rolId);
        if (rolOpt.isPresent()) {
            Rol rol = rolOpt.get();
            // Cargar permisos del rol
            rol.setPermisos(rolRepository.findPermisosByRolId(rolId));
            usuario.setRol(rol);
        }
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            usuario.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            usuario.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return usuario;
    }
}
