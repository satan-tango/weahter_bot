package com.telegram.bot.apiconfig;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class WeatherAPIConfig {

    @Value("${weather.accessKey}")
    private String accessKey;
}
