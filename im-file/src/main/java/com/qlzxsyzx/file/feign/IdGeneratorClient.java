package com.qlzxsyzx.file.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("id-generator")
public interface IdGeneratorClient {
    @GetMapping("/id/generate")
    Long generate();

    @GetMapping("/id/generate/batch/{count}")
    Long[] generateIdBatch(@PathVariable("count") int count);
}
