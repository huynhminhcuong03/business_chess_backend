package com.businesschess.services.gameplay;

import java.util.List;

import com.businesschess.entities.BoardCell;
import com.businesschess.entities.Game;
import com.businesschess.entities.GamePlayer;
import com.businesschess.entities.PropertyDetail;
import com.businesschess.enums.BoardCellType;
import com.businesschess.enums.ErrorCode;
import com.businesschess.enums.GameStatus;
import com.businesschess.exceptions.AppException;
import com.businesschess.repositories.BoardCellRepository;
import com.businesschess.repositories.GamePlayerRepository;
import com.businesschess.repositories.GameRepository;
import org.springframework.stereotype.Service;

@Service
public class GameTurnService {

    // Gom các kiểm tra dùng chung cho gameplay: trạng thái game, đúng lượt và chuyển lượt.
    private final GameRepository gameRepository;

    private final GamePlayerRepository gamePlayerRepository;

    private final BoardCellRepository boardCellRepository;

    public GameTurnService(
            GameRepository gameRepository,
            GamePlayerRepository gamePlayerRepository,
            BoardCellRepository boardCellRepository
    ) {
        this.gameRepository = gameRepository;
        this.gamePlayerRepository = gamePlayerRepository;
        this.boardCellRepository = boardCellRepository;
    }

    public GamePlayContext getPlayingGameContext(Long gameId, Long gamePlayerId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new AppException(ErrorCode.GAME_NOT_FOUND));

        if (game.getStatus() != GameStatus.PLAYING) {
            throw new AppException(ErrorCode.GAME_NOT_PLAYING);
        }

        GamePlayer gamePlayer = gamePlayerRepository.findById(gamePlayerId)
                .orElseThrow(() -> new AppException(ErrorCode.PLAYER_NOT_FOUND));

        if (!gamePlayer.getGame().getId().equals(gameId)) {
            throw new AppException(ErrorCode.PLAYER_NOT_IN_GAME);
        }

        return new GamePlayContext(game, gamePlayer);
    }

    public void requireCurrentTurn(Game game, Long gamePlayerId) {
        // Mọi action gameplay chỉ hợp lệ khi người gọi đang là currentPlayer.
        if (game.getCurrentPlayer() == null
                || !game.getCurrentPlayer().getId().equals(gamePlayerId)) {
            throw new AppException(ErrorCode.PLAYER_NOT_CURRENT_TURN);
        }
    }

    public void requireNotInJail(GamePlayer gamePlayer) {
        if (Boolean.TRUE.equals(gamePlayer.getInJail())) {
            throw new AppException(ErrorCode.PLAYER_IN_JAIL);
        }
    }

    public BoardCell getPlayerBoardCell(Game game, GamePlayer gamePlayer) {
        return boardCellRepository.findByBoardIdAndPosition(
                game.getBoard().getId(),
                gamePlayer.getPosition()
        ).orElseThrow(() -> new AppException(ErrorCode.BOARD_CELL_NOT_FOUND));
    }

    public BoardCell getJailCell(Game game) {
        return boardCellRepository.findByBoardIdOrderByPositionAsc(game.getBoard().getId()).stream()
                .filter(boardCell -> boardCell.getType() == BoardCellType.JAIL)
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.BOARD_CELL_NOT_FOUND));
    }

    public BoardCell getNextBoardCellByType(
            Game game,
            Integer currentPosition,
            BoardCellType boardCellType
    ) {
        List<BoardCell> boardCells = boardCellRepository.findByBoardIdOrderByPositionAsc(game.getBoard().getId());

        if (boardCells.isEmpty()) {
            throw new AppException(ErrorCode.BOARD_CELLS_NOT_FOUND);
        }

        // Tìm ô tiếp theo theo chiều đi của bàn cờ; nếu cuối bàn thì vòng về đầu.
        return boardCells.stream()
                .filter(boardCell -> boardCell.getPosition() > currentPosition)
                .filter(boardCell -> boardCell.getType() == boardCellType)
                .findFirst()
                .or(() -> boardCells.stream()
                        .filter(boardCell -> boardCell.getType() == boardCellType)
                        .findFirst())
                .orElseThrow(() -> new AppException(ErrorCode.BOARD_CELL_NOT_FOUND));
    }

    public int getBoardSize(Game game) {
        List<BoardCell> boardCells = boardCellRepository.findByBoardIdOrderByPositionAsc(
                game.getBoard().getId()
        );

        if (boardCells.isEmpty()) {
            throw new AppException(ErrorCode.BOARD_CELLS_NOT_FOUND);
        }

        return boardCells.size();
    }

    public GamePlayer resolveNextPlayer(
            Long gameId,
            GamePlayer currentPlayer,
            Boolean isDouble
    ) {
        // Đổ đôi thì giữ lượt, các trường hợp còn lại chuyển sang người kế tiếp.
        if (Boolean.TRUE.equals(isDouble)) {
            return currentPlayer;
        }

        List<GamePlayer> gamePlayers = gamePlayerRepository.findByGameIdOrderByTurnOrderAsc(gameId);

        if (gamePlayers.isEmpty()) {
            throw new AppException(ErrorCode.GAME_PLAYERS_NOT_FOUND);
        }

        for (int i = 0; i < gamePlayers.size(); i++) {
            if (gamePlayers.get(i).getId().equals(currentPlayer.getId())) {
                return gamePlayers.get((i + 1) % gamePlayers.size());
            }
        }

        throw new AppException(ErrorCode.PLAYER_NOT_IN_GAME);
    }

    public boolean isPurchasableCell(BoardCellType boardCellType) {
        // Chỉ đất thường, bến xe và tiện ích mới có bằng khoán để mua/thu thuê.
        return boardCellType == BoardCellType.PROPERTY
                || boardCellType == BoardCellType.STATION
                || boardCellType == BoardCellType.UTILITY;
    }

    public PropertyDetail getPropertyDetail(BoardCell boardCell) {
        if (boardCell.getPropertyDetail() == null) {
            throw new AppException(ErrorCode.PROPERTY_DETAIL_NOT_FOUND);
        }

        return boardCell.getPropertyDetail();
    }
}
