package ru.nsu.crackhash.manager.core.persistance.repository;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ru.nsu.crackhash.manager.core.persistance.model.CrackingHashTask;

import java.util.UUID;

@Repository
public interface CrackHashTaskRepository extends MongoRepository<CrackingHashTask, UUID> {

}
