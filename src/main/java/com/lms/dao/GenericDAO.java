package com.lms.dao;

import java.util.List;

/**
 * GenericDAO — Generic DAO Interface (OOP: Abstraction + Generics)
 *
 * Generics: <T> is the entity type (User, Course, etc.)
 * <ID> is the primary key type (Integer)
 *
 * Abstraction: Declares a contract for CRUD operations that each DAO must
 * implement.
 * All four DAOs implement this interface, ensuring a uniform API surface.
 * The service layer can type-check DAOs using this interface.
 */
public interface GenericDAO<T, ID> {

    /**
     * Persists a new entity to the database.
     */
    void save(T entity);

    /**
     * Retrieves an entity by its primary key. Returns null if not found.
     */
    T findById(ID id);

    /**
     * Retrieves all entities of this type from the database.
     */
    List<T> findAll();

    /**
     * Updates an existing entity in the database.
     */
    void update(T entity);

    /**
     * Deletes an entity by its primary key.
     */
    void delete(ID id);
}
