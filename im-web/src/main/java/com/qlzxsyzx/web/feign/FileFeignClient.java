package com.qlzxsyzx.web.feign;

import com.qlzxsyzx.web.vo.FileDetailsVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("im-file")
public interface FileFeignClient {
    @GetMapping("/file/getFileDetails/{recordId}")
    FileDetailsVo getFileDetails(@PathVariable("recordId") Long recordId);
}
