package ru.nsu.crackhash.manager.core.persistance.repository;


import org.springframework.data.mongodb.repository.MongoRepository;
import ru.nsu.crackhash.manager.core.persistance.model.CrackingHashWorkerTask;

import java.util.UUID;

public interface CrackHashWorkerTaskRepository
    extends MongoRepository<CrackingHashWorkerTask, UUID> {


}
