package com.businesschess.controllers;

import com.businesschess.common.ApiResponse;
import com.businesschess.common.constants.MessageConstant;
import com.businesschess.dto.request.PayRentRequest;
import com.businesschess.dto.request.PayTaxRequest;
import com.businesschess.dto.response.BuyPropertyResponse;
import com.businesschess.dto.response.LandCellResponse;
import com.businesschess.dto.response.PayRentResponse;
import com.businesschess.dto.response.PayTaxResponse;
import com.businesschess.dto.response.RollDiceResponse;
import jakarta.validation.Valid;
import com.businesschess.services.GamePlayService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/game/{gameId}/play")
public class GamePlayController {

    private final GamePlayService gamePlayService;

    public GamePlayController(GamePlayService gamePlayService) {
        this.gamePlayService = gamePlayService;
    }

    @PostMapping("/players/{gamePlayerId}/roll-dice")
    public ApiResponse rollDice(
            @PathVariable Long gameId,
            @PathVariable Long gamePlayerId
    ) {
        RollDiceResponse response = gamePlayService.rollDice(gameId, gamePlayerId);

        return ApiResponse.ok(MessageConstant.GAME_ROLL_DICE_SUCCESS, response);
    }

    @PostMapping("/players/{gamePlayerId}/land")
    public ApiResponse landCell(
            @PathVariable Long gameId,
            @PathVariable Long gamePlayerId
    ) {
        LandCellResponse response = gamePlayService.landCell(gameId, gamePlayerId);

        return ApiResponse.ok(MessageConstant.GAME_LAND_CELL_SUCCESS, response);
    }

    @PostMapping("/players/{gamePlayerId}/properties/{boardCellId}/buy")
    public ApiResponse buyProperty(
            @PathVariable Long gameId,
            @PathVariable Long gamePlayerId,
            @PathVariable Long boardCellId
    ) {
        BuyPropertyResponse response = gamePlayService.buyProperty(
                gameId,
                gamePlayerId,
                boardCellId
        );

        return ApiResponse.ok(MessageConstant.GAME_PROPERTY_BUY_SUCCESS, response);
    }

    @PostMapping("/players/{gamePlayerId}/rent/pay")
    public ApiResponse payRent(
            @PathVariable Long gameId,
            @PathVariable Long gamePlayerId,
            @Valid @RequestBody(required = false) PayRentRequest request
    ) {
        PayRentResponse response = gamePlayService.payRent(
                gameId,
                gamePlayerId,
                request == null ? null : request.getDiceTotal()
        );

        return ApiResponse.ok(MessageConstant.GAME_PAY_RENT_SUCCESS, response);
    }

    @PostMapping("/players/{gamePlayerId}/tax/pay")
    public ApiResponse payTax(
            @PathVariable Long gameId,
            @PathVariable Long gamePlayerId,
            @RequestBody(required = false) PayTaxRequest request
    ) {
        PayTaxResponse response = gamePlayService.payTax(
                gameId,
                gamePlayerId,
                request == null ? null : request.getIncomeTaxOption()
        );

        return ApiResponse.ok(MessageConstant.GAME_PAY_TAX_SUCCESS, response);
    }
}
