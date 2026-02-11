package ru.nsu.crackhash.manager.core.persistance.repository;


import org.springframework.data.mongodb.repository.MongoRepository;
import ru.nsu.crackhash.manager.core.persistance.model.CrackingHashTask;

import java.util.UUID;

public interface CrackHashTaskRepository extends MongoRepository<CrackingHashTask, UUID> {


}
