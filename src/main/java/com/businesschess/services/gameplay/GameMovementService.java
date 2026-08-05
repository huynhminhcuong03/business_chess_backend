package com.businesschess.services.gameplay;

import com.businesschess.dto.response.RollDiceResponse;
import com.businesschess.entities.BoardCell;
import com.businesschess.entities.Game;
import com.businesschess.entities.GamePlayer;
import com.businesschess.enums.ErrorCode;
import com.businesschess.exceptions.AppException;
import com.businesschess.mappers.GamePlayMapper;
import com.businesschess.repositories.BoardCellRepository;
import com.businesschess.repositories.GamePlayerRepository;
import com.businesschess.repositories.GameRepository;
import org.springframework.stereotype.Service;

@Service
public class GameMovementService {

    // Xử lý thay đổi vị trí do xúc xắc/test move, bao gồm qua Start và 3 lần đôi vào tù.
    private static final int START_REWARD_AMOUNT = 200;

    private final GameRepository gameRepository;

    private final GamePlayerRepository gamePlayerRepository;

    private final BoardCellRepository boardCellRepository;

    private final GameTurnService gameTurnService;

    private final GamePlayMapper gamePlayMapper;

    public GameMovementService(
            GameRepository gameRepository,
            GamePlayerRepository gamePlayerRepository,
            BoardCellRepository boardCellRepository,
            GameTurnService gameTurnService,
            GamePlayMapper gamePlayMapper
    ) {
        this.gameRepository = gameRepository;
        this.gamePlayerRepository = gamePlayerRepository;
        this.boardCellRepository = boardCellRepository;
        this.gameTurnService = gameTurnService;
        this.gamePlayMapper = gamePlayMapper;
    }

    public RollDiceResponse rollDice(GamePlayContext context, RollDiceResponse response) {
        Game game = context.game();
        GamePlayer gamePlayer = context.gamePlayer();

        gameTurnService.requireCurrentTurn(game, gamePlayer.getId());
        gameTurnService.requireNotInJail(gamePlayer);

        int oldPosition = gamePlayer.getPosition();
        int consecutiveDoubles = Boolean.TRUE.equals(response.getIsDouble())
                ? getConsecutiveDoubles(game) + 1
                : 0;

        // Đổ đôi lần thứ 3: đi thẳng vào tù, không đi theo xúc xắc và không nhận $200.
        if (consecutiveDoubles >= 3) {
            BoardCell jailCell = gameTurnService.getJailCell(game);
            GamePlayer nextPlayer = gameTurnService.resolveNextPlayer(game.getId(), gamePlayer, false);

            gamePlayer.setPosition(jailCell.getPosition());
            gamePlayer.setInJail(true);
            gamePlayer.setJailTurn(0);
            game.setCurrentPlayer(nextPlayer);
            game.setConsecutiveDoubles(0);

            gamePlayerRepository.save(gamePlayer);
            gameRepository.save(game);

            gamePlayMapper.fillSentToJailResult(
                    response,
                    gamePlayer,
                    nextPlayer,
                    oldPosition,
                    jailCell.getPosition()
            );

            return response;
        }

        int boardSize = gameTurnService.getBoardSize(game);
        int rawPosition = oldPosition + response.getTotal();
        int newPosition = rawPosition % boardSize;
        boolean passedStart = rawPosition >= boardSize;
        // Di chuyển thường theo vòng bàn cờ; nếu vượt qua Start thì cộng tiền thưởng.
        GamePlayer nextPlayer = gameTurnService.resolveNextPlayer(
                game.getId(),
                gamePlayer,
                response.getIsDouble()
        );

        gamePlayer.setPosition(newPosition);
        applyStartReward(gamePlayer, passedStart);
        game.setCurrentPlayer(nextPlayer);
        game.setConsecutiveDoubles(consecutiveDoubles);

        gamePlayerRepository.save(gamePlayer);
        gameRepository.save(game);

        gamePlayMapper.fillMoveResult(
                response,
                gamePlayer,
                nextPlayer,
                oldPosition,
                newPosition,
                passedStart,
                passedStart ? START_REWARD_AMOUNT : 0
        );

        return response;
    }

    public RollDiceResponse testMove(GamePlayContext context, Integer targetPosition) {
        // API dev/test để ép tới một ô cụ thể nhưng vẫn giữ rule qua Start và chuyển lượt.
        Game game = context.game();
        GamePlayer gamePlayer = context.gamePlayer();

        gameTurnService.requireCurrentTurn(game, gamePlayer.getId());
        gameTurnService.requireNotInJail(gamePlayer);

        int oldPosition = gamePlayer.getPosition();
        int boardSize = gameTurnService.getBoardSize(game);

        if (targetPosition < 0 || targetPosition >= boardSize) {
            throw new AppException(ErrorCode.BOARD_CELL_NOT_FOUND);
        }

        boardCellRepository.findByBoardIdAndPosition(
                game.getBoard().getId(),
                targetPosition
        ).orElseThrow(() -> new AppException(ErrorCode.BOARD_CELL_NOT_FOUND));

        int stepCount = getForwardStepCount(oldPosition, targetPosition, boardSize);
        boolean passedStart = targetPosition < oldPosition;
        GamePlayer nextPlayer = gameTurnService.resolveNextPlayer(game.getId(), gamePlayer, false);

        gamePlayer.setPosition(targetPosition);
        applyStartReward(gamePlayer, passedStart);
        game.setCurrentPlayer(nextPlayer);
        game.setConsecutiveDoubles(0);

        gamePlayerRepository.save(gamePlayer);
        gameRepository.save(game);

        RollDiceResponse response = new RollDiceResponse();
        response.setDice1(0);
        response.setDice2(0);
        response.setTotal(stepCount);
        response.setIsDouble(false);
        gamePlayMapper.fillMoveResult(
                response,
                gamePlayer,
                nextPlayer,
                oldPosition,
                targetPosition,
                passedStart,
                passedStart ? START_REWARD_AMOUNT : 0
        );

        return response;
    }

    public JailMoveResult moveAfterLeavingJail(
            Game game,
            GamePlayer gamePlayer,
            RollDiceResponse roll,
            Boolean allowExtraTurnForDouble
    ) {
        // Dùng cho luồng ra tù: người chơi đã được rời tù rồi mới di chuyển theo xúc xắc.
        int oldPosition = gamePlayer.getPosition();
        int boardSize = gameTurnService.getBoardSize(game);
        int rawPosition = oldPosition + roll.getTotal();
        int newPosition = rawPosition % boardSize;
        boolean passedStart = rawPosition >= boardSize;
        GamePlayer nextPlayer = gameTurnService.resolveNextPlayer(
                game.getId(),
                gamePlayer,
                allowExtraTurnForDouble
        );

        gamePlayer.setPosition(newPosition);
        applyStartReward(gamePlayer, passedStart);
        game.setCurrentPlayer(nextPlayer);

        if (Boolean.TRUE.equals(allowExtraTurnForDouble)) {
            game.setConsecutiveDoubles(getConsecutiveDoubles(game) + 1);
        } else {
            game.setConsecutiveDoubles(0);
        }

        gamePlayerRepository.save(gamePlayer);
        gameRepository.save(game);

        return new JailMoveResult(
                nextPlayer,
                oldPosition,
                newPosition,
                passedStart,
                passedStart ? START_REWARD_AMOUNT : 0
        );
    }

    private void applyStartReward(GamePlayer gamePlayer, boolean passedStart) {
        if (passedStart) {
            gamePlayer.setMoney(gamePlayer.getMoney() + START_REWARD_AMOUNT);
        }
    }

    private int getForwardStepCount(int oldPosition, int targetPosition, int boardSize) {
        if (targetPosition >= oldPosition) {
            return targetPosition - oldPosition;
        }

        return boardSize - oldPosition + targetPosition;
    }

    private int getConsecutiveDoubles(Game game) {
        return game.getConsecutiveDoubles() == null ? 0 : game.getConsecutiveDoubles();
    }

    public record JailMoveResult(
            GamePlayer nextPlayer,
            Integer oldPosition,
            Integer newPosition,
            Boolean passedStart,
            Integer startReward
    ) {
    }
}
