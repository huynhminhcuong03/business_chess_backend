package com.businesschess.services.impl;

import java.util.concurrent.ThreadLocalRandom;

import com.businesschess.dto.response.RollDiceResponse;
import com.businesschess.services.DiceService;
import org.springframework.stereotype.Service;

@Service
public class DiceServiceImpl implements DiceService {

    @Override
    public RollDiceResponse rollDice() {
        int dice1 = ThreadLocalRandom.current().nextInt(1, 7);
        int dice2 = ThreadLocalRandom.current().nextInt(1, 7);

        RollDiceResponse response = new RollDiceResponse();
        response.setDice1(dice1);
        response.setDice2(dice2);
        response.setTotal(dice1 + dice2);
        response.setIsDouble(dice1 == dice2);

        return response;
    }
}
