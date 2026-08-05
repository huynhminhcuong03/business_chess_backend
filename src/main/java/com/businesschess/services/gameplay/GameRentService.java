package com.businesschess.services.gameplay;

import com.businesschess.dto.response.PayRentResponse;
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
public class GameRentService {

    // Xử lý trả tiền thuê từ người đáp vào tài sản sang chủ sở hữu.
    private final GamePlayerRepository gamePlayerRepository;

    private final GamePropertyRepository gamePropertyRepository;

    private final GameTurnService gameTurnService;

    private final GameRentCalculator gameRentCalculator;

    private final GamePlayMapper gamePlayMapper;

    public GameRentService(
            GamePlayerRepository gamePlayerRepository,
            GamePropertyRepository gamePropertyRepository,
            GameTurnService gameTurnService,
            GameRentCalculator gameRentCalculator,
            GamePlayMapper gamePlayMapper
    ) {
        this.gamePlayerRepository = gamePlayerRepository;
        this.gamePropertyRepository = gamePropertyRepository;
        this.gameTurnService = gameTurnService;
        this.gameRentCalculator = gameRentCalculator;
        this.gamePlayMapper = gamePlayMapper;
    }

    public PayRentResponse payRent(GamePlayContext context, Integer diceTotal) {
        Game game = context.game();
        GamePlayer payer = context.gamePlayer();
        BoardCell boardCell = gameTurnService.getPlayerBoardCell(game, payer);

        if (!gameTurnService.isPurchasableCell(boardCell.getType())) {
            throw new AppException(ErrorCode.PROPERTY_NOT_PURCHASABLE);
        }

        PropertyDetail propertyDetail = gameTurnService.getPropertyDetail(boardCell);
        GameProperty gameProperty = gamePropertyRepository.findByGameIdAndBoardCellId(
                game.getId(),
                boardCell.getId()
        ).orElseThrow(() -> new AppException(ErrorCode.PROPERTY_NOT_OWNED));
        GamePlayer owner = gameProperty.getOwner();

        // Chủ sở hữu đáp vào tài sản của mình thì không phải trả thuê.
        if (owner.getId().equals(payer.getId())) {
            throw new AppException(ErrorCode.PROPERTY_OWNED_BY_PLAYER);
        }

        int rentAmount = gameRentCalculator.getRent(propertyDetail, gameProperty, diceTotal);

        // Chưa xử lý phá sản ở bước này, nên thiếu tiền sẽ trả lỗi cho flow thiếu tiền sau.
        if (payer.getMoney() < rentAmount) {
            throw new AppException(ErrorCode.PLAYER_NOT_ENOUGH_MONEY);
        }

        payer.setMoney(payer.getMoney() - rentAmount);
        owner.setMoney(owner.getMoney() + rentAmount);

        gamePlayerRepository.save(payer);
        gamePlayerRepository.save(owner);

        return gamePlayMapper.toPayRentResponse(boardCell, payer, owner, rentAmount);
    }
}
