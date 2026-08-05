package com.businesschess.services;

import com.businesschess.dto.response.BuyPropertyResponse;
import com.businesschess.dto.response.DrawCardResponse;
import com.businesschess.dto.response.GoToJailResponse;
import com.businesschess.dto.response.JailActionResponse;
import com.businesschess.dto.response.LandCellResponse;
import com.businesschess.dto.response.PayRentResponse;
import com.businesschess.dto.response.PayTaxResponse;
import com.businesschess.dto.response.RollDiceResponse;
import com.businesschess.enums.IncomeTaxOption;
import com.businesschess.enums.JailActionType;

public interface GamePlayService {

    RollDiceResponse rollDice(Long gameId, Long gamePlayerId);

    RollDiceResponse testRoll(Long gameId, Long gamePlayerId, Integer dice1, Integer dice2);

    RollDiceResponse testMove(Long gameId, Long gamePlayerId, Integer targetPosition);

    LandCellResponse landCell(Long gameId, Long gamePlayerId);

    BuyPropertyResponse buyProperty(Long gameId, Long gamePlayerId, Long boardCellId);

    PayRentResponse payRent(Long gameId, Long gamePlayerId, Integer diceTotal);

    PayTaxResponse payTax(Long gameId, Long gamePlayerId, IncomeTaxOption incomeTaxOption);

    DrawCardResponse drawCard(Long gameId, Long gamePlayerId);

    GoToJailResponse goToJail(Long gameId, Long gamePlayerId);

    JailActionResponse handleJailAction(
            Long gameId,
            Long gamePlayerId,
            JailActionType actionType
    );
}
