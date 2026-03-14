package ru.nsu.crackhash.manager.core.feign.worker;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.nsu.crackhash.manager.core.kafka.dto.CrackHashTaskWorkerRequest;

//@ConditionalOnProperty(name = "send-type", havingValue = "rest")
//@FeignClient(value = "workerFeignClient-3")
public interface WorkerFeignClient3 {

    @RequestMapping(method = RequestMethod.POST, value = "/internal/api/worker/hash/crack/task")
    void createCrackHashTask(@RequestBody CrackHashTaskWorkerRequest crackHashTaskWorkerRequest);
}
