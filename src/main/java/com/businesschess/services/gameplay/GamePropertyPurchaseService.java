package com.businesschess.services.gameplay;

import com.businesschess.dto.response.BuyPropertyResponse;
import com.businesschess.entities.BoardCell;
import com.businesschess.entities.Game;
import com.businesschess.entities.GamePlayer;
import com.businesschess.entities.GameProperty;
import com.businesschess.entities.PropertyDetail;
import com.businesschess.enums.ErrorCode;
import com.businesschess.exceptions.AppException;
import com.businesschess.mappers.GamePlayMapper;
import com.businesschess.repositories.GamePlayerRepository;
import com.businesschess.repositories.GamePropertyRepository;
import org.springframework.stereotype.Service;

@Service
public class GamePropertyPurchaseService {

    // Xử lý mua tài sản khi người chơi đang đứng đúng ô và ô đó chưa có chủ.
    private final GamePlayerRepository gamePlayerRepository;

    private final GamePropertyRepository gamePropertyRepository;

    private final GameTurnService gameTurnService;

    private final GamePlayMapper gamePlayMapper;

    public GamePropertyPurchaseService(
            GamePlayerRepository gamePlayerRepository,
            GamePropertyRepository gamePropertyRepository,
            GameTurnService gameTurnService,
            GamePlayMapper gamePlayMapper
    ) {
        this.gamePlayerRepository = gamePlayerRepository;
        this.gamePropertyRepository = gamePropertyRepository;
        this.gameTurnService = gameTurnService;
        this.gamePlayMapper = gamePlayMapper;
    }

    public BuyPropertyResponse buyProperty(GamePlayContext context, Long boardCellId) {
        Game game = context.game();
        GamePlayer gamePlayer = context.gamePlayer();
        BoardCell boardCell = gameTurnService.getPlayerBoardCell(game, gamePlayer);

        if (!boardCell.getId().equals(boardCellId)) {
            throw new AppException(ErrorCode.PLAYER_NOT_ON_CELL);
        }

        if (!gameTurnService.isPurchasableCell(boardCell.getType())) {
            throw new AppException(ErrorCode.PROPERTY_NOT_PURCHASABLE);
        }

        PropertyDetail propertyDetail = gameTurnService.getPropertyDetail(boardCell);

        // Một ô trong một game chỉ được có một GameProperty để tránh nhiều chủ sở hữu.
        if (gamePropertyRepository.existsByGameIdAndBoardCellId(game.getId(), boardCellId)) {
            throw new AppException(ErrorCode.PROPERTY_ALREADY_OWNED);
        }

        if (gamePlayer.getMoney() < propertyDetail.getBuyPrice()) {
            throw new AppException(ErrorCode.PLAYER_NOT_ENOUGH_MONEY);
        }

        GameProperty gameProperty = new GameProperty();
        gameProperty.setGame(game);
        gameProperty.setBoardCell(boardCell);
        gameProperty.setOwner(gamePlayer);
        gameProperty.setHouseCount(0);
        gameProperty.setHasHotel(false);
        gameProperty.setMortgaged(false);

        // Trừ tiền người mua trước khi lưu bằng khoán mới.
        gamePlayer.setMoney(gamePlayer.getMoney() - propertyDetail.getBuyPrice());

        GameProperty savedGameProperty = gamePropertyRepository.save(gameProperty);
        gamePlayerRepository.save(gamePlayer);

        return gamePlayMapper.toBuyPropertyResponse(savedGameProperty, propertyDetail);
    }
}
