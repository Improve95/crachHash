package ru.nsu.crackhash.worker.core.service.impl.result;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import ru.nsu.crackhash.worker.core.feign.manager.ManagerFeignClient;
import ru.nsu.crackhash.worker.core.feign.manager.dto.SendCrackResultRequest;
import ru.nsu.crackhash.worker.core.service.ResultSender;

@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "send-type", havingValue = "rest")
@Service
public class RestResultSender implements ResultSender {

    private final ManagerFeignClient managerFeignClient;

    @Value("${spring.cloud.openfeign.client.config.managerFeignClient.url}")
    private String managerUrl;

    @Override
    public void sendResultToManager(SendCrackResultRequest sendCrackResultRequest) {
        log.info("Send answers to manager, url={}", managerUrl);
        managerFeignClient.sendCrackResponse(sendCrackResultRequest);
    }
}
