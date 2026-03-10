package ru.nsu.crackhash.worker.core.feign.manager;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.nsu.crackhash.worker.core.feign.manager.dto.SendCrackResultRequest;

@ConditionalOnProperty(name = "send-type", havingValue = "rest")
@FeignClient(value = "managerFeignClient")
public interface ManagerFeignClient {

    @RequestMapping(method = RequestMethod.POST, value = "/api/hash/task/result")
    void sendCrackResponse(@RequestBody SendCrackResultRequest request);
}
