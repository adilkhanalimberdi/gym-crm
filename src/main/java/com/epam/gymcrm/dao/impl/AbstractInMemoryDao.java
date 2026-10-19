package com.epam.gymcrm.dao.impl;

import com.epam.gymcrm.dao.Dao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/** Common map-backed CRUD logic; subclasses only say how to read and set an entity's id. */
abstract class AbstractInMemoryDao<T> implements Dao<T> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private Map<Long, T> storage;
    private AtomicLong sequence;

    protected abstract T withId(T entity, Long id);

    protected abstract String entityName();

    protected void useStorage(Map<Long, T> storage) {
        this.storage = storage;
        // storage may already hold seed data, so continue after the highest existing id
        this.sequence = new AtomicLong(storage.keySet().stream().mapToLong(Long::longValue).max().orElse(0L));
    }

    @Override
    public T create(T entity) {
        long id = sequence.incrementAndGet();
        T stored = withId(entity, id);
        storage.put(id, stored);
        log.debug("Created {} with id {}", entityName(), id);
        return stored;
    }

    @Override
    public Optional<T> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<T> findAll() {
        return List.copyOf(storage.values());
    }

    protected boolean replace(Long id, T entity) {
        boolean replaced = id != null && storage.replace(id, entity) != null;
        log.debug("Update of {} with id {} {}", entityName(), id, replaced ? "applied" : "skipped: not found");
        return replaced;
    }

    protected boolean remove(Long id) {
        boolean removed = id != null && storage.remove(id) != null;
        log.debug("Delete of {} with id {} {}", entityName(), id, removed ? "applied" : "skipped: not found");
        return removed;
    }

    protected boolean anyMatch(java.util.function.Predicate<T> predicate) {
        return storage.values().stream().anyMatch(predicate);
    }
}
