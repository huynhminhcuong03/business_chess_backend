package com.businesschess.services.gameplay;

import com.businesschess.dto.response.GoToJailResponse;
import com.businesschess.dto.response.JailActionResponse;
import com.businesschess.dto.response.RollDiceResponse;
import com.businesschess.entities.BoardCell;
import com.businesschess.entities.Game;
import com.businesschess.entities.GameCardDeck;
import com.businesschess.entities.GamePlayer;
import com.businesschess.enums.BoardCellType;
import com.businesschess.enums.ErrorCode;
import com.businesschess.enums.JailActionType;
import com.businesschess.exceptions.AppException;
import com.businesschess.mappers.GamePlayMapper;
import com.businesschess.repositories.GamePlayerRepository;
import com.businesschess.repositories.GameRepository;
import com.businesschess.repositories.GameCardDeckRepository;
import com.businesschess.services.DiceService;
import org.springframework.stereotype.Service;

@Service
public class GameJailService {

    // Xử lý toàn bộ luật nhà tù: vào tù, trả tiền, dùng thẻ và thử tung đôi.
    private static final int JAIL_FINE_AMOUNT = 50;

    private static final int MAX_JAIL_ROLL_ATTEMPTS = 3;

    private final GameRepository gameRepository;

    private final GamePlayerRepository gamePlayerRepository;

    private final DiceService diceService;

    private final GameTurnService gameTurnService;

    private final GameMovementService gameMovementService;

    private final GameCardDeckRepository gameCardDeckRepository;

    private final GamePlayMapper gamePlayMapper;

    public GameJailService(
            GameRepository gameRepository,
            GamePlayerRepository gamePlayerRepository,
            DiceService diceService,
            GameTurnService gameTurnService,
            GameMovementService gameMovementService,
            GameCardDeckRepository gameCardDeckRepository,
            GamePlayMapper gamePlayMapper
    ) {
        this.gameRepository = gameRepository;
        this.gamePlayerRepository = gamePlayerRepository;
        this.diceService = diceService;
        this.gameTurnService = gameTurnService;
        this.gameMovementService = gameMovementService;
        this.gameCardDeckRepository = gameCardDeckRepository;
        this.gamePlayMapper = gamePlayMapper;
    }

    public GoToJailResponse goToJail(GamePlayContext context) {
        Game game = context.game();
        GamePlayer gamePlayer = context.gamePlayer();
        BoardCell boardCell = gameTurnService.getPlayerBoardCell(game, gamePlayer);

        if (boardCell.getType() != BoardCellType.GO_TO_JAIL) {
            throw new AppException(ErrorCode.PLAYER_NOT_ON_GO_TO_JAIL_CELL);
        }

        return sendPlayerToJail(game, gamePlayer);
    }

    public JailActionResponse handleJailAction(
            GamePlayContext context,
            JailActionType actionType
    ) {
        Game game = context.game();
        GamePlayer gamePlayer = context.gamePlayer();

        gameTurnService.requireCurrentTurn(game, gamePlayer.getId());

        if (!Boolean.TRUE.equals(gamePlayer.getInJail())) {
            throw new AppException(ErrorCode.PLAYER_NOT_IN_JAIL);
        }

        // Người đang ở tù phải chọn một trong ba action trước khi được xử lý xúc xắc.
        return switch (actionType) {
            case PAY_FINE -> payFineAndRoll(game, gamePlayer, actionType);
            case USE_JAIL_CARD -> useJailCardAndRoll(game, gamePlayer, actionType);
            case ROLL_FOR_DOUBLE -> rollForDouble(game, gamePlayer, actionType);
        };
    }

    private GoToJailResponse sendPlayerToJail(Game game, GamePlayer gamePlayer) {
        int fromPosition = gamePlayer.getPosition();
        BoardCell jailCell = gameTurnService.getJailCell(game);

        gamePlayer.setPosition(jailCell.getPosition());
        gamePlayer.setInJail(true);
        gamePlayer.setJailTurn(0);
        gamePlayerRepository.save(gamePlayer);

        return gamePlayMapper.toGoToJailResponse(gamePlayer, fromPosition, jailCell.getPosition());
    }

    private JailActionResponse payFineAndRoll(
            Game game,
            GamePlayer gamePlayer,
            JailActionType actionType
    ) {
        // Trả phạt: ra tù ngay rồi roll và di chuyển như lượt bình thường.
        payJailFine(gamePlayer);
        releaseFromJail(gamePlayer);

        RollDiceResponse roll = diceService.rollDice();
        return moveAfterLeavingJail(game, gamePlayer, roll, actionType, roll.getIsDouble(), JAIL_FINE_AMOUNT);
    }

    private JailActionResponse useJailCardAndRoll(
            Game game,
            GamePlayer gamePlayer,
            JailActionType actionType
    ) {
        if (gamePlayer.getJailFreeCard() == null || gamePlayer.getJailFreeCard() <= 0) {
            throw new AppException(ErrorCode.JAIL_CARD_NOT_AVAILABLE);
        }

        // Dùng thẻ ra tù: trừ thẻ, ra tù rồi roll và di chuyển.
        gamePlayer.setJailFreeCard(gamePlayer.getJailFreeCard() - 1);
        returnHeldJailCardToDeck(game, gamePlayer);
        releaseFromJail(gamePlayer);

        RollDiceResponse roll = diceService.rollDice();
        return moveAfterLeavingJail(game, gamePlayer, roll, actionType, roll.getIsDouble(), 0);
    }

    private JailActionResponse rollForDouble(
            Game game,
            GamePlayer gamePlayer,
            JailActionType actionType
    ) {
        RollDiceResponse roll = diceService.rollDice();

        // Nếu ra đôi thì được ra tù và đi theo xúc xắc này, nhưng không được thêm lượt vì số đôi.
        if (Boolean.TRUE.equals(roll.getIsDouble())) {
            releaseFromJail(gamePlayer);
            return moveAfterLeavingJail(game, gamePlayer, roll, actionType, false, 0);
        }

        int failedAttemptCount = (gamePlayer.getJailTurn() == null ? 0 : gamePlayer.getJailTurn()) + 1;
        gamePlayer.setJailTurn(failedAttemptCount);

        // Lần thử thứ 3 không ra đôi: bắt buộc trả $50 và đi theo chính xúc xắc vừa tung.
        if (failedAttemptCount >= MAX_JAIL_ROLL_ATTEMPTS) {
            payJailFine(gamePlayer);
            releaseFromJail(gamePlayer);
            return moveAfterLeavingJail(game, gamePlayer, roll, actionType, false, JAIL_FINE_AMOUNT);
        }

        // Chưa đủ 3 lần thất bại thì vẫn ở tù và chuyển lượt.
        GamePlayer nextPlayer = gameTurnService.resolveNextPlayer(game.getId(), gamePlayer, false);
        game.setCurrentPlayer(nextPlayer);
        game.setConsecutiveDoubles(0);
        gamePlayerRepository.save(gamePlayer);
        gameRepository.save(game);

        return gamePlayMapper.toJailActionResponse(
                actionType,
                roll,
                gamePlayer,
                nextPlayer,
                false,
                gamePlayer.getPosition(),
                gamePlayer.getPosition(),
                false,
                0,
                0
        );
    }

    private JailActionResponse moveAfterLeavingJail(
            Game game,
            GamePlayer gamePlayer,
            RollDiceResponse roll,
            JailActionType actionType,
            Boolean allowExtraTurnForDouble,
            Integer finePaid
    ) {
        GameMovementService.JailMoveResult moveResult = gameMovementService.moveAfterLeavingJail(
                game,
                gamePlayer,
                roll,
                allowExtraTurnForDouble
        );

        return gamePlayMapper.toJailActionResponse(
                actionType,
                roll,
                gamePlayer,
                moveResult.nextPlayer(),
                true,
                moveResult.oldPosition(),
                moveResult.newPosition(),
                moveResult.passedStart(),
                moveResult.startReward(),
                finePaid
        );
    }

    private void releaseFromJail(GamePlayer gamePlayer) {
        gamePlayer.setInJail(false);
        gamePlayer.setJailTurn(0);
    }

    private void payJailFine(GamePlayer gamePlayer) {
        if (gamePlayer.getMoney() < JAIL_FINE_AMOUNT) {
            throw new AppException(ErrorCode.PLAYER_NOT_ENOUGH_MONEY);
        }

        gamePlayer.setMoney(gamePlayer.getMoney() - JAIL_FINE_AMOUNT);
    }

    private void returnHeldJailCardToDeck(Game game, GamePlayer gamePlayer) {
        // Khi dùng thẻ ra tù, card đang giữ phải quay về cuối deck để có thể được rút lại sau này.
        GameCardDeck deckCard = gameCardDeckRepository
                .findFirstByGameIdAndHeldByPlayerIdAndUsedTrueOrderByIdAsc(
                        game.getId(),
                        gamePlayer.getId()
                )
                .orElseThrow(() -> new AppException(ErrorCode.JAIL_CARD_NOT_AVAILABLE));
        int maxDeckOrder = gameCardDeckRepository.findMaxDeckOrderByGameIdAndCardType(
                game.getId(),
                deckCard.getCardType()
        );

        deckCard.setHeldByPlayer(null);
        deckCard.setUsed(false);
        deckCard.setDeckOrder(maxDeckOrder + 1);
        gameCardDeckRepository.save(deckCard);
    }
}
