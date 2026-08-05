package com.businesschess.services.impl;

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
import com.businesschess.mappers.GamePlayMapper;
import com.businesschess.services.DiceService;
import com.businesschess.services.GamePlayService;
import com.businesschess.services.gameplay.GameCardDrawService;
import com.businesschess.services.gameplay.GameJailService;
import com.businesschess.services.gameplay.GameLandService;
import com.businesschess.services.gameplay.GameMovementService;
import com.businesschess.services.gameplay.GamePlayContext;
import com.businesschess.services.gameplay.GamePropertyPurchaseService;
import com.businesschess.services.gameplay.GameRentService;
import com.businesschess.services.gameplay.GameTaxService;
import com.businesschess.services.gameplay.GameTurnService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GamePlayServiceImpl implements GamePlayService {

    // Service tổng chỉ điều phối các API gameplay sang service nhỏ tương ứng.
    // Luật chi tiết nằm ở package gameplay để file này không bị phình to.
    private final DiceService diceService;

    private final GamePlayMapper gamePlayMapper;

    private final GameTurnService gameTurnService;

    private final GameMovementService gameMovementService;

    private final GameLandService gameLandService;

    private final GamePropertyPurchaseService gamePropertyPurchaseService;

    private final GameRentService gameRentService;

    private final GameTaxService gameTaxService;

    private final GameCardDrawService gameCardDrawService;

    private final GameJailService gameJailService;

    public GamePlayServiceImpl(
            DiceService diceService,
            GamePlayMapper gamePlayMapper,
            GameTurnService gameTurnService,
            GameMovementService gameMovementService,
            GameLandService gameLandService,
            GamePropertyPurchaseService gamePropertyPurchaseService,
            GameRentService gameRentService,
            GameTaxService gameTaxService,
            GameCardDrawService gameCardDrawService,
            GameJailService gameJailService
    ) {
        this.diceService = diceService;
        this.gamePlayMapper = gamePlayMapper;
        this.gameTurnService = gameTurnService;
        this.gameMovementService = gameMovementService;
        this.gameLandService = gameLandService;
        this.gamePropertyPurchaseService = gamePropertyPurchaseService;
        this.gameRentService = gameRentService;
        this.gameTaxService = gameTaxService;
        this.gameCardDrawService = gameCardDrawService;
        this.gameJailService = gameJailService;
    }

    @Override
    @Transactional
    public RollDiceResponse rollDice(Long gameId, Long gamePlayerId) {
        GamePlayContext context = gameTurnService.getPlayingGameContext(gameId, gamePlayerId);
        return gameMovementService.rollDice(context, diceService.rollDice());
    }

    @Override
    @Transactional
    public RollDiceResponse testRoll(
            Long gameId,
            Long gamePlayerId,
            Integer dice1,
            Integer dice2
    ) {
        GamePlayContext context = gameTurnService.getPlayingGameContext(gameId, gamePlayerId);
        return gameMovementService.rollDice(context, gamePlayMapper.toTestRoll(dice1, dice2));
    }

    @Override
    @Transactional
    public RollDiceResponse testMove(Long gameId, Long gamePlayerId, Integer targetPosition) {
        GamePlayContext context = gameTurnService.getPlayingGameContext(gameId, gamePlayerId);
        return gameMovementService.testMove(context, targetPosition);
    }

    @Override
    @Transactional(readOnly = true)
    public LandCellResponse landCell(Long gameId, Long gamePlayerId) {
        GamePlayContext context = gameTurnService.getPlayingGameContext(gameId, gamePlayerId);
        return gameLandService.landCell(context);
    }

    @Override
    @Transactional
    public BuyPropertyResponse buyProperty(Long gameId, Long gamePlayerId, Long boardCellId) {
        GamePlayContext context = gameTurnService.getPlayingGameContext(gameId, gamePlayerId);
        return gamePropertyPurchaseService.buyProperty(context, boardCellId);
    }

    @Override
    @Transactional
    public PayRentResponse payRent(Long gameId, Long gamePlayerId, Integer diceTotal) {
        GamePlayContext context = gameTurnService.getPlayingGameContext(gameId, gamePlayerId);
        return gameRentService.payRent(context, diceTotal);
    }

    @Override
    @Transactional
    public PayTaxResponse payTax(
            Long gameId,
            Long gamePlayerId,
            IncomeTaxOption incomeTaxOption
    ) {
        GamePlayContext context = gameTurnService.getPlayingGameContext(gameId, gamePlayerId);
        return gameTaxService.payTax(context, incomeTaxOption);
    }

    @Override
    @Transactional
    public DrawCardResponse drawCard(Long gameId, Long gamePlayerId) {
        GamePlayContext context = gameTurnService.getPlayingGameContext(gameId, gamePlayerId);
        return gameCardDrawService.drawCard(context);
    }

    @Override
    @Transactional
    public GoToJailResponse goToJail(Long gameId, Long gamePlayerId) {
        GamePlayContext context = gameTurnService.getPlayingGameContext(gameId, gamePlayerId);
        return gameJailService.goToJail(context);
    }

    @Override
    @Transactional
    public JailActionResponse handleJailAction(
            Long gameId,
            Long gamePlayerId,
            JailActionType actionType
    ) {
        GamePlayContext context = gameTurnService.getPlayingGameContext(gameId, gamePlayerId);
        return gameJailService.handleJailAction(context, actionType);
    }
}
