package com.qlzxsyzx.snowflake.controller;

import com.qlzxsyzx.snowflake.utils.SnowflakeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/id")
public class IdGeneratorController {
    @Autowired
    private SnowflakeUtil snowflakeUtil;

    @GetMapping("/generate")
    public Long generateId() {
        return snowflakeUtil.nextId();
    }

    @GetMapping("/generate/batch/{count}")
    public Long[] generateIdBatch(@PathVariable("count") int count) {
        return snowflakeUtil.nextIdBatch(count);
    }
}
