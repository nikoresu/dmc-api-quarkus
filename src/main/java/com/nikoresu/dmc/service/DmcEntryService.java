package com.nikoresu.dmc.service;

import com.nikoresu.dmc.data.DmcEntryRepository;
import com.nikoresu.dmc.domain.DmcEntry;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Slf4j
@ApplicationScoped
public class DmcEntryService {

    @Inject
    DmcEntryRepository repository;

    public Optional<DmcEntry> getById(String id) {
        return repository.findById(id);
    }

    public DmcEntry create(DmcEntry entry) {
        entry.setCreatedAt(System.currentTimeMillis());
        entry.setUpdatedAt(System.currentTimeMillis());
        repository.save(entry.getNumber(), entry);
        return entry;
    }

    public Optional<DmcEntry> update(String id, DmcEntry entry) {
        Optional<DmcEntry> existing = repository.findById(id);

        if (existing.isPresent()) {
            entry.setNumber(id);
            entry.setCreatedAt(existing.get().getCreatedAt());
            entry.setUpdatedAt(System.currentTimeMillis());
            repository.save(id, entry);
            return Optional.of(entry);
        }

        return Optional.empty();
    }

    public boolean delete(String id) {
        if (repository.exists(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<DmcEntry> getAllEntries(int limit) {
        return repository.findAll(limit);
    }

}
