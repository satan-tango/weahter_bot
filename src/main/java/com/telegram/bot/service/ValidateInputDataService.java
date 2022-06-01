package com.telegram.bot.service;

import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ValidateInputDataService {

    public boolean validateInputLocation(String location) {
        if (location.length() > 15) {
            return false;
        }

        Pattern pattern = Pattern.compile("[a-zA-Zа-яА-Я ]*");
        Matcher matcher = pattern.matcher(location);
        if (!matcher.matches()) {
            return false;
        }
        return true;
    }
}
