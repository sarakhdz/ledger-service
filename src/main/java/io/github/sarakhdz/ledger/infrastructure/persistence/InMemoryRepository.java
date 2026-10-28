package io.github.sarakhdz.ledger.infrastructure.persistence;

import io.github.sarakhdz.ledger.domain.shared.Repository;

import java.util.*;

public abstract class InMemoryRepository<T, ID> implements Repository<T, ID> {

    protected final Map<ID, T> store = new HashMap<>();

    protected abstract ID idOf(T entity);

    @Override
    public void save(T entity) {
        Objects.requireNonNull(entity, "entity must not be null");
        store.put(idOf(entity), entity);
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<T> findAll() {
        return List.copyOf(store.values());
    }
}
