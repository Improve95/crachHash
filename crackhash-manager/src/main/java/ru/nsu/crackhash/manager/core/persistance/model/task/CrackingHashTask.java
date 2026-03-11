package ru.nsu.crackhash.manager.core.persistance.model.task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Document("cracking_hash_tasks")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CrackingHashTask {

    @Id
    private UUID id;

    private String hash;

    private int maxLength;

    private List<String> answers;

    private int currentCompletedTaskPartCount;

    private int taskPartCount;

    private CrackingHashTaskStatus status;

    private Instant startedAt;
}
