package ru.nsu.crackhash.worker.core.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.nsu.crackhash.worker.api.dto.CreateCrackHashTaskRequest;
import ru.nsu.crackhash.worker.core.kafka.dto.CrackHashTaskRequestKafkaMessage;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CrackHashRequestDtoMapper {

    CreateCrackHashTaskRequest toCreateCrackHashTaskRequest(CrackHashTaskRequestKafkaMessage crackHashTaskRequestKafkaMessage);
}
