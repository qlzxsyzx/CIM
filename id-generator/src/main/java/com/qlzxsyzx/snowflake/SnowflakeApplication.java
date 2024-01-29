package com.qlzxsyzx.snowflake;

import com.qlzxsyzx.snowflake.utils.SnowflakeUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.context.annotation.Bean;

@SpringCloudApplication
public class SnowflakeApplication {
    public static void main(String[] args) {
        SpringApplication.run(SnowflakeApplication.class, args);
    }

    @Bean
    public SnowflakeUtil snowflakeUtil() {
        return new SnowflakeUtil(0,0);
    }
}
