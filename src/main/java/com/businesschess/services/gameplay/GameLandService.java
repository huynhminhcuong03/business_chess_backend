package com.businesschess.services.gameplay;

import java.util.Optional;

import com.businesschess.dto.response.LandCellResponse;
import com.businesschess.dto.response.LandedPropertyResponse;
import com.businesschess.entities.BoardCell;
import com.businesschess.entities.Game;
import com.businesschess.entities.GamePlayer;
import com.businesschess.entities.GameProperty;
import com.businesschess.entities.PropertyDetail;
import com.businesschess.enums.LandCellAction;
import com.businesschess.mappers.GamePlayMapper;
import com.businesschess.repositories.GamePropertyRepository;
import org.springframework.stereotype.Service;

@Service
public class GameLandService {

    // Xác định ô vừa đáp xuống và action tiếp theo FE cần hiển thị cho người chơi.
    private final GamePropertyRepository gamePropertyRepository;

    private final GameTurnService gameTurnService;

    private final GamePlayMapper gamePlayMapper;

    private final GameRentCalculator gameRentCalculator;

    public GameLandService(
            GamePropertyRepository gamePropertyRepository,
            GameTurnService gameTurnService,
            GamePlayMapper gamePlayMapper,
            GameRentCalculator gameRentCalculator
    ) {
        this.gamePropertyRepository = gamePropertyRepository;
        this.gameTurnService = gameTurnService;
        this.gamePlayMapper = gamePlayMapper;
        this.gameRentCalculator = gameRentCalculator;
    }

    public LandCellResponse landCell(GamePlayContext context) {
        Game game = context.game();
        GamePlayer gamePlayer = context.gamePlayer();
        BoardCell boardCell = gameTurnService.getPlayerBoardCell(game, gamePlayer);
        Optional<GameProperty> gameProperty = gamePropertyRepository.findByGameIdAndBoardCellId(
                game.getId(),
                boardCell.getId()
        );

        GameProperty landedGameProperty = gameProperty.orElse(null);
        // Chỉ trả thông tin bằng khoán khi ô là loại có thể mua hoặc thu thuê.
        LandedPropertyResponse property = gameTurnService.isPurchasableCell(boardCell.getType())
                ? toLandedPropertyResponse(boardCell, landedGameProperty)
                : null;

        return gamePlayMapper.toLandCellResponse(
                boardCell,
                resolveLandAction(boardCell, gamePlayer, landedGameProperty),
                property
        );
    }

    private LandedPropertyResponse toLandedPropertyResponse(
            BoardCell boardCell,
            GameProperty landedGameProperty
    ) {
        PropertyDetail propertyDetail = gameTurnService.getPropertyDetail(boardCell);
        return gamePlayMapper.toLandedPropertyResponse(
                propertyDetail,
                landedGameProperty,
                gameRentCalculator.getPreviewRent(propertyDetail, landedGameProperty)
        );
    }

    private LandCellAction resolveLandAction(
            BoardCell boardCell,
            GamePlayer gamePlayer,
            GameProperty gameProperty
    ) {
        // Quy đổi loại ô thành action để FE biết mở modal/nút mua đất, trả thuê, trả thuế...
        return switch (boardCell.getType()) {
            case CHANCE -> LandCellAction.DRAW_CHANCE_CARD;
            case COMMUNITY -> LandCellAction.DRAW_COMMUNITY_CARD;
            case INCOME_TAX -> LandCellAction.PAY_INCOME_TAX;
            case LUXURY_TAX -> LandCellAction.PAY_LUXURY_TAX;
            case GO_TO_JAIL -> LandCellAction.GO_TO_JAIL;
            case PROPERTY, STATION, UTILITY -> resolvePropertyAction(gamePlayer, gameProperty);
            default -> LandCellAction.NONE;
        };
    }

    private LandCellAction resolvePropertyAction(
            GamePlayer gamePlayer,
            GameProperty gameProperty
    ) {
        if (gameProperty == null) {
            return LandCellAction.BUY_PROPERTY;
        }

        if (gameProperty.getOwner().getId().equals(gamePlayer.getId())) {
            return LandCellAction.NONE;
        }

        return LandCellAction.PAY_RENT;
    }
}
