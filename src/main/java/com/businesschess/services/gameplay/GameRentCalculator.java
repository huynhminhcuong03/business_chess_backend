package com.businesschess.services.gameplay;

import com.businesschess.entities.GamePlayer;
import com.businesschess.entities.GameProperty;
import com.businesschess.entities.PropertyDetail;
import com.businesschess.enums.BoardCellType;
import com.businesschess.enums.ErrorCode;
import com.businesschess.exceptions.AppException;
import com.businesschess.repositories.GamePropertyRepository;
import org.springframework.stereotype.Component;

@Component
public class GameRentCalculator {

    // Gom công thức tính tiền thuê/net worth để các service gameplay không lẫn logic tiền bạc.
    private final GamePropertyRepository gamePropertyRepository;

    public GameRentCalculator(GamePropertyRepository gamePropertyRepository) {
        this.gamePropertyRepository = gamePropertyRepository;
    }

    public int getRent(
            PropertyDetail propertyDetail,
            GameProperty gameProperty,
            Integer diceTotal
    ) {
        if (gameProperty == null) {
            return propertyDetail.getRentLevel0();
        }

        if (Boolean.TRUE.equals(gameProperty.getMortgaged())) {
            return 0;
        }

        BoardCellType boardCellType = gameProperty.getBoardCell().getType();

        if (boardCellType == BoardCellType.STATION) {
            // Bến xe tính thuê theo số bến xe cùng chủ sở hữu trong game.
            return getStationRent(gameProperty);
        }

        if (boardCellType == BoardCellType.UTILITY) {
            // Tiện ích tính thuê theo tổng xúc xắc, nên bắt buộc cần diceTotal từ lượt vừa đi.
            return getUtilityRent(gameProperty, diceTotal);
        }

        if (Boolean.TRUE.equals(gameProperty.getHasHotel())) {
            return propertyDetail.getRentHotel();
        }

        return switch (gameProperty.getHouseCount()) {
            case 1 -> propertyDetail.getRentLevel1();
            case 2 -> propertyDetail.getRentLevel2();
            case 3 -> propertyDetail.getRentLevel3();
            case 4 -> propertyDetail.getRentLevel4();
            default -> propertyDetail.getRentLevel0();
        };
    }

    public Integer getPreviewRent(PropertyDetail propertyDetail, GameProperty gameProperty) {
        if (gameProperty != null
                && gameProperty.getBoardCell().getType() == BoardCellType.UTILITY
                && !Boolean.TRUE.equals(gameProperty.getMortgaged())) {
            return null;
        }

        return getRent(propertyDetail, gameProperty, null);
    }

    public int getNetWorth(Long gameId, GamePlayer gamePlayer) {
        // Net worth dùng cho income tax 10%: tiền mặt + giá trị tài sản/cải tiến đang sở hữu.
        int propertyWorth = gamePropertyRepository.findAllByOwnerId(gamePlayer.getId()).stream()
                .filter(gameProperty -> gameProperty.getGame().getId().equals(gameId))
                .mapToInt(this::getPropertyAssetValue)
                .sum();

        return gamePlayer.getMoney() + propertyWorth;
    }

    private int getStationRent(GameProperty gameProperty) {
        long ownedStationCount = getOwnedPropertyCountByType(
                gameProperty.getGame().getId(),
                gameProperty.getOwner().getId(),
                BoardCellType.STATION
        );

        if (ownedStationCount >= 4) {
            return 200;
        }

        return switch ((int) ownedStationCount) {
            case 3 -> 100;
            case 2 -> 50;
            default -> 25;
        };
    }

    private int getUtilityRent(GameProperty gameProperty, Integer diceTotal) {
        if (diceTotal == null) {
            throw new AppException(ErrorCode.DICE_TOTAL_REQUIRED);
        }

        long ownedUtilityCount = getOwnedPropertyCountByType(
                gameProperty.getGame().getId(),
                gameProperty.getOwner().getId(),
                BoardCellType.UTILITY
        );

        int multiplier = ownedUtilityCount >= 2 ? 10 : 4;

        return diceTotal * multiplier;
    }

    private long getOwnedPropertyCountByType(
            Long gameId,
            Long ownerId,
            BoardCellType boardCellType
    ) {
        return gamePropertyRepository.findAllByOwnerId(ownerId).stream()
                .filter(gameProperty -> gameProperty.getGame().getId().equals(gameId))
                .filter(gameProperty -> gameProperty.getBoardCell().getType() == boardCellType)
                .count();
    }

    private int getPropertyAssetValue(GameProperty gameProperty) {
        PropertyDetail propertyDetail = getPropertyDetail(gameProperty);
        int houseCount = gameProperty.getHouseCount() == null ? 0 : gameProperty.getHouseCount();
        int improvementValue = houseCount * propertyDetail.getHousePrice();

        if (Boolean.TRUE.equals(gameProperty.getHasHotel())) {
            improvementValue += propertyDetail.getHotelPrice();
        }

        return propertyDetail.getBuyPrice() + improvementValue;
    }

    private PropertyDetail getPropertyDetail(GameProperty gameProperty) {
        if (gameProperty.getBoardCell().getPropertyDetail() == null) {
            throw new AppException(ErrorCode.PROPERTY_DETAIL_NOT_FOUND);
        }

        return gameProperty.getBoardCell().getPropertyDetail();
    }
}
