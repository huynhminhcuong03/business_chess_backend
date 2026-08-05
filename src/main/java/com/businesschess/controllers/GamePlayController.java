package com.businesschess.controllers;

import com.businesschess.common.ApiResponse;
import com.businesschess.common.constants.MessageConstant;
import com.businesschess.dto.request.JailActionRequest;
import com.businesschess.dto.request.PayRentRequest;
import com.businesschess.dto.request.PayTaxRequest;
import com.businesschess.dto.request.TestMoveRequest;
import com.businesschess.dto.request.TestRollRequest;
import com.businesschess.dto.response.BuyPropertyResponse;
import com.businesschess.dto.response.DrawCardResponse;
import com.businesschess.dto.response.GoToJailResponse;
import com.businesschess.dto.response.JailActionResponse;
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

    @PostMapping("/players/{gamePlayerId}/test-move")
    public ApiResponse testMove(
            @PathVariable Long gameId,
            @PathVariable Long gamePlayerId,
            @Valid @RequestBody TestMoveRequest request
    ) {
        // Endpoint dev/test để ép người chơi tới ô cần kiểm tra mà không phải chờ roll ngẫu nhiên.
        RollDiceResponse response = gamePlayService.testMove(
                gameId,
                gamePlayerId,
                request.getTargetPosition()
        );

        return ApiResponse.ok(MessageConstant.GAME_TEST_MOVE_SUCCESS, response);
    }

    @PostMapping("/players/{gamePlayerId}/test-roll")
    public ApiResponse testRoll(
            @PathVariable Long gameId,
            @PathVariable Long gamePlayerId,
            @Valid @RequestBody TestRollRequest request
    ) {
        // Endpoint dev/test để ép xúc xắc, hữu ích khi kiểm tra rule hiếm như 3 lần đôi vào tù.
        RollDiceResponse response = gamePlayService.testRoll(
                gameId,
                gamePlayerId,
                request.getDice1(),
                request.getDice2()
        );

        return ApiResponse.ok(MessageConstant.GAME_TEST_MOVE_SUCCESS, response);
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

    @PostMapping("/players/{gamePlayerId}/cards/draw")
    public ApiResponse drawCard(
            @PathVariable Long gameId,
            @PathVariable Long gamePlayerId
    ) {
        DrawCardResponse response = gamePlayService.drawCard(gameId, gamePlayerId);

        return ApiResponse.ok(MessageConstant.GAME_CARD_DRAW_SUCCESS, response);
    }

    @PostMapping("/players/{gamePlayerId}/jail/go-to-jail")
    public ApiResponse goToJail(
            @PathVariable Long gameId,
            @PathVariable Long gamePlayerId
    ) {
        GoToJailResponse response = gamePlayService.goToJail(gameId, gamePlayerId);

        return ApiResponse.ok(MessageConstant.GAME_GO_TO_JAIL_SUCCESS, response);
    }

    @PostMapping("/players/{gamePlayerId}/jail/action")
    public ApiResponse handleJailAction(
            @PathVariable Long gameId,
            @PathVariable Long gamePlayerId,
            @Valid @RequestBody JailActionRequest request
    ) {
        // FE chỉ gửi lựa chọn ra tù; backend là nơi tự kiểm tra tiền, thẻ và số lần thử.
        JailActionResponse response = gamePlayService.handleJailAction(
                gameId,
                gamePlayerId,
                request.getActionType()
        );

        return ApiResponse.ok(MessageConstant.GAME_JAIL_ACTION_SUCCESS, response);
    }
}
