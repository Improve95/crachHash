package ru.nsu.crackhash.manager.core.persistance.model.queue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document("cracking_hash_tasks_queue")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CrackHashTaskQueue {

    @Id
    private int position;

    private UUID taskId;
}
