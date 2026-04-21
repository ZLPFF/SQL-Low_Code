package com.sunwayworld.cloud.module.lcdp;

import com.sunwayworld.GikamCoreApplication;
import com.sunwayworld.framework.spring.SunwayApplication;
import com.sunwayworld.framework.spring.annotation.SunwayBootApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jooq.JooqAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import java.util.function.Function;

/**
 * Created by wul@sunwayworld.com on 2019/11/6
 */
@SunwayBootApplication
@EnableDiscoveryClient
@EnableAutoConfiguration(exclude = JooqAutoConfiguration.class)
public class GikamLcdpDesignApplication extends GikamCoreApplication {

    public static void main(String[] args) {
        SunwayApplication.run(GikamLcdpDesignApplication.class, args);
    }

    @Override
    public Function<HttpSecurity, HttpSecurity> securityConfigureCustomizer() {
        return r -> r;
    }
}
