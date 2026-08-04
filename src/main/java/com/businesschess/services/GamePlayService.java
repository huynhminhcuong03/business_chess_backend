package com.businesschess.services;

import com.businesschess.dto.response.BuyPropertyResponse;
import com.businesschess.dto.response.LandCellResponse;
import com.businesschess.dto.response.PayRentResponse;
import com.businesschess.dto.response.PayTaxResponse;
import com.businesschess.dto.response.RollDiceResponse;
import com.businesschess.enums.IncomeTaxOption;

public interface GamePlayService {

    RollDiceResponse rollDice(Long gameId, Long gamePlayerId);

    LandCellResponse landCell(Long gameId, Long gamePlayerId);

    BuyPropertyResponse buyProperty(Long gameId, Long gamePlayerId, Long boardCellId);

    PayRentResponse payRent(Long gameId, Long gamePlayerId, Integer diceTotal);

    PayTaxResponse payTax(Long gameId, Long gamePlayerId, IncomeTaxOption incomeTaxOption);
}
