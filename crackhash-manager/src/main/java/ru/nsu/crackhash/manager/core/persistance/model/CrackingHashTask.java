package ru.nsu.crackhash.manager.core.persistance.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.UUID;

@Document
public class CrackingHashTask {

    @Id
    private UUID id;

    private String hash;

    private int length;

    private List<String> matches;
}
