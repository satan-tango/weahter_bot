package com.telegram.bot.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserForecastWeather {
    private String briefCurrentWeather;

    private String detailCurrentWeather;

    private long hash;
}
