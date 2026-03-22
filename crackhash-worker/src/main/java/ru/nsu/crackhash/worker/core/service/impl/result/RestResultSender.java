package ru.nsu.crackhash.worker.core.service.impl.result;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import ru.nsu.crackhash.worker.core.feign.manager.ManagerFeignClient;
import ru.nsu.crackhash.worker.core.feign.manager.dto.SendCrackResultRequest;
import ru.nsu.crackhash.worker.core.service.ResultSender;

@RequiredArgsConstructor
@ConditionalOnProperty(name = "send-type", havingValue = "rest")
@Service
public class RestResultSender implements ResultSender {

    private final ManagerFeignClient managerFeignClient;

    @Override
    public void sendResultToManager(SendCrackResultRequest sendCrackResultRequest) {
        managerFeignClient.sendCrackResponse(sendCrackResultRequest);
    }
}
