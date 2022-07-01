package com.telegram.bot.model;

import lombok.*;
import org.springframework.stereotype.Component;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserCurrentWeather {

    private String briefCurrentWeather;

    private String detailCurrentWeather;

    private long hash;
}
