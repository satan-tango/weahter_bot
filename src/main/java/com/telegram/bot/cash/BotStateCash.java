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
            botStateMap.put(chatId, new ArrayList<>(Arrays.asList(botState)));
        }
    }

    public BotState getPreviousBotState(long chatId) {
        if (botStateMap.get(chatId) != null) {
            List<BotState> list = botStateMap.get(chatId);
            if (list.get(list.size() - 1) == BotState.LOCATION_BY_CHAT ||
                    list.get(list.size() - 1) == BotState.DELETE_LOCATION) {
                list.remove(list.size() - 1);
            }
            list.remove(list.size() - 1);
            if (list.isEmpty()) {
                list.add(BotState.START);
                botStateMap.put(chatId, list);
                return BotState.START;
            }
            botStateMap.put(chatId, list);
            return list.get(list.size() - 1);
        } else {
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
