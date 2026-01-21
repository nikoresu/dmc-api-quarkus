package com.nikoresu.dmc.data;

import java.util.List;
import java.util.Optional;

public interface CouchbaseRepository<T, ID> {
    Optional<T> findById(ID id);

    List<T> findAll(int limit);

    void save(ID id, T entity);

    void deleteById(ID id);

    boolean exists(ID id);
}
