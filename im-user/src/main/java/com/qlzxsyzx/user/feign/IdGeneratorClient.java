package com.qlzxsyzx.user.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient("id-generator")
public interface IdGeneratorClient {
    @GetMapping("/id/generate")
    Long generate();
}
