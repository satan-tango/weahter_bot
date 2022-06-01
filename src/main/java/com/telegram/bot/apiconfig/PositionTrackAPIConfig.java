package com.telegram.bot.apiconfig;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class PositionTrackAPIConfig {

    @Value("${positiontrack.accessKey}")
    private String accessKey;
}
