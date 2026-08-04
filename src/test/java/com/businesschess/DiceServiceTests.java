package com.businesschess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.businesschess.dto.response.RollDiceResponse;
import com.businesschess.services.impl.DiceServiceImpl;
import org.junit.jupiter.api.Test;

class DiceServiceTests {

    private final DiceServiceImpl diceService = new DiceServiceImpl();

    @Test
    void rollDiceReturnsValidDiceValues() {
        for (int i = 0; i < 100; i++) {
            RollDiceResponse response = diceService.rollDice();

            assertTrue(response.getDice1() >= 1 && response.getDice1() <= 6);
            assertTrue(response.getDice2() >= 1 && response.getDice2() <= 6);
            assertEquals(response.getDice1() + response.getDice2(), response.getTotal());
            assertEquals(response.getDice1().equals(response.getDice2()), response.getIsDouble());
        }
    }
}
