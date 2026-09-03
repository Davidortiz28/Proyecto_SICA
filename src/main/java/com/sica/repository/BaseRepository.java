package com.sica.repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz base genérica para repositorios.
 * Define las operaciones CRUD estándar que deben implementar todos los repositorios.
 * 
 * PATRÓN DE DISEÑO: REPOSITORY
 * - Abstrae el acceso a datos de la lógica de negocio
 * - Proporciona una colección de objetos en memoria
 * - Encapsula las queries SQL
 * 
 * Principios SOLID aplicados:
 * - Interface Segregation: Interfaz específica para operaciones CRUD
 * - Dependency Inversion: Servicios dependen de esta interfaz, no de implementación
 * 
 * @param <T> Tipo de entidad que maneja el repositorio
 * @param <ID> Tipo de dato del identificador
 * 
 * @author SICA Team
 * @version 1.0
 */
public interface BaseRepository<T, ID> {
    
    /**
     * Guarda una nueva entidad en la base de datos.
     * 
     * @param entity Entidad a guardar
     * @return Entidad guardada con ID asignado
     * @throws SQLException si hay error en la operación
     */
    T save(T entity) throws SQLException;
    
    /**
     * Actualiza una entidad existente en la base de datos.
     * 
     * @param entity Entidad a actualizar
     * @return Entidad actualizada
     * @throws SQLException si hay error en la operación
     */
    T update(T entity) throws SQLException;
    
    /**
     * Elimina una entidad por su ID.
     * 
     * @param id ID de la entidad a eliminar
     * @return true si se eliminó, false si no existía
     * @throws SQLException si hay error en la operación
     */
    boolean delete(ID id) throws SQLException;
    
    /**
     * Busca una entidad por su ID.
     * 
     * @param id ID de la entidad a buscar
     * @return Optional con la entidad si existe, Optional.empty() si no existe
     * @throws SQLException si hay error en la operación
     */
    Optional<T> findById(ID id) throws SQLException;
    
    /**
     * Obtiene todas las entidades.
     * 
     * @return Lista de todas las entidades
     * @throws SQLException si hay error en la operación
     */
    List<T> findAll() throws SQLException;
    
    /**
     * Verifica si existe una entidad con el ID dado.
     * 
     * @param id ID a verificar
     * @return true si existe, false si no existe
     * @throws SQLException si hay error en la operación
     */
    boolean existsById(ID id) throws SQLException;
    
    /**
     * Cuenta el total de entidades.
     * 
     * @return Cantidad total de entidades
     * @throws SQLException si hay error en la operación
     */
    long count() throws SQLException;
}
