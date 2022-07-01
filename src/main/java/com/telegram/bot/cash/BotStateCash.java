package com.telegram.bot.cash;

import com.telegram.bot.model.BotState;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Setter
@Getter
public class BotStateCash {

    private final Map<Long, List<BotState>> botStateMap = new HashMap<>();


    public void saveBotState(long chatId, BotState botState) {
        if (botStateMap.get(chatId) != null) {
            List<BotState> list = botStateMap.get(chatId);
            if (list.get(list.size() - 1) == botState) {
                return;
            }
            list.add(botState);
            botStateMap.put(chatId, list);
        } else {
            botStateMap.put(chatId, new ArrayList<>(Arrays.asList(BotState.START, botState)));
        }
    }

    public BotState getPreviousBotState(long chatId) {
        if (botStateMap.get(chatId) != null) {
            List<BotState> list = botStateMap.get(chatId);
            if (list.isEmpty() || list.size() == 1) {
                botStateMap.put(chatId, new ArrayList<>(Arrays.asList(BotState.START)));
                return BotState.START;
            }
            if (list.get(list.size() - 1) == BotState.LOCATION_BY_CHAT ||
                    list.get(list.size() - 1) == BotState.DELETE_LOCATION) {
                list.remove(list.size() - 1);
            }
            if (list.get(list.size() - 1) == BotState.ADD_LOCATION &&
                    list.get(list.size() - 2) == BotState.WEATHER) {
                botStateMap.put(chatId, new ArrayList<>(Arrays.asList(BotState.START)));
                return BotState.START;
            }
            if (list.get(list.size() - 1) == BotState.ADD_LOCATION &&
                    list.get(list.size() - 2) == BotState.FORECAST) {
                botStateMap.put(chatId, new ArrayList<>(Arrays.asList(BotState.START)));
                return BotState.START;
            }
            if (list.get(list.size() - 1) == BotState.SELECT_LOCATION &&
                    list.get(list.size() - 2) == BotState.WEATHER) {
                botStateMap.put(chatId, new ArrayList<>(Arrays.asList(BotState.START)));
                return BotState.START;
            }
            if (list.get(list.size() - 1) == BotState.SELECT_LOCATION &&
                    list.get(list.size() - 2) == BotState.FORECAST) {
                botStateMap.put(chatId, new ArrayList<>(Arrays.asList(BotState.START)));
                return BotState.START;
            }

            list.remove(list.size() - 1);
            botStateMap.put(chatId, list);
            return list.get(list.size() - 1);
        } else {
            botStateMap.put(chatId, new ArrayList<>(Arrays.asList(BotState.START)));
            return BotState.START;
        }
    }

    public void cleaningBotSateCash(long chatId) {
        botStateMap.put(chatId, new ArrayList<>(Arrays.asList(BotState.START)));
    }

    public BotState getCurrentBotState(long chatId) {
        return botStateMap.get(chatId).get(botStateMap.get(chatId).size() - 1);
    }
}
