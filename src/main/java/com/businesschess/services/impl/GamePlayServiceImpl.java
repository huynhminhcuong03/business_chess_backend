package com.businesschess.services.impl;

import java.util.List;
import java.util.Optional;

import com.businesschess.dto.response.BuyPropertyResponse;
import com.businesschess.dto.response.LandCellResponse;
import com.businesschess.dto.response.LandedPropertyResponse;
import com.businesschess.dto.response.PayRentResponse;
import com.businesschess.dto.response.PayTaxResponse;
import com.businesschess.dto.response.RollDiceResponse;
import com.businesschess.entities.BoardCell;
import com.businesschess.entities.Game;
import com.businesschess.entities.GamePlayer;
import com.businesschess.entities.GameProperty;
import com.businesschess.entities.PropertyDetail;
import com.businesschess.enums.BoardCellType;
import com.businesschess.enums.ErrorCode;
import com.businesschess.enums.GameStatus;
import com.businesschess.enums.IncomeTaxOption;
import com.businesschess.enums.LandCellAction;
import com.businesschess.exceptions.AppException;
import com.businesschess.repositories.BoardCellRepository;
import com.businesschess.repositories.GamePlayerRepository;
import com.businesschess.repositories.GamePropertyRepository;
import com.businesschess.repositories.GameRepository;
import com.businesschess.services.DiceService;
import com.businesschess.services.GamePlayService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GamePlayServiceImpl implements GamePlayService {

    private static final int FIXED_INCOME_TAX_AMOUNT = 200;

    private static final int LUXURY_TAX_AMOUNT = 100;

    private final GameRepository gameRepository;

    private final GamePlayerRepository gamePlayerRepository;

    private final BoardCellRepository boardCellRepository;

    private final GamePropertyRepository gamePropertyRepository;

    private final DiceService diceService;

    public GamePlayServiceImpl(
            GameRepository gameRepository,
            GamePlayerRepository gamePlayerRepository,
            BoardCellRepository boardCellRepository,
            GamePropertyRepository gamePropertyRepository,
            DiceService diceService
    ) {
        this.gameRepository = gameRepository;
        this.gamePlayerRepository = gamePlayerRepository;
        this.boardCellRepository = boardCellRepository;
        this.gamePropertyRepository = gamePropertyRepository;
        this.diceService = diceService;
    }

    @Override
    @Transactional
    public RollDiceResponse rollDice(Long gameId, Long gamePlayerId) {
        GamePlayContext context = getPlayingGameContext(gameId, gamePlayerId);
        Game game = context.game();
        GamePlayer gamePlayer = context.gamePlayer();

        if (game.getCurrentPlayer() == null
                || !game.getCurrentPlayer().getId().equals(gamePlayerId)) {
            throw new AppException(ErrorCode.PLAYER_NOT_CURRENT_TURN);
        }

        RollDiceResponse response = diceService.rollDice();
        int oldPosition = gamePlayer.getPosition();
        int boardSize = getBoardSize(game);
        int rawPosition = oldPosition + response.getTotal();
        int newPosition = rawPosition % boardSize;
        boolean passedStart = rawPosition >= boardSize;
        GamePlayer nextPlayer = resolveNextPlayer(gameId, gamePlayer, response.getIsDouble());

        gamePlayer.setPosition(newPosition);
        game.setCurrentPlayer(nextPlayer);

        if (Boolean.TRUE.equals(response.getIsDouble())) {
            game.setConsecutiveDoubles(getConsecutiveDoubles(game) + 1);
        } else {
            game.setConsecutiveDoubles(0);
        }

        gamePlayerRepository.save(gamePlayer);
        gameRepository.save(game);

        response.setOldPosition(oldPosition);
        response.setNewPosition(newPosition);
        response.setPassedStart(passedStart);
        response.setCurrentPlayerId(gamePlayerId);
        response.setNextPlayerId(nextPlayer.getId());

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public LandCellResponse landCell(Long gameId, Long gamePlayerId) {
        GamePlayContext context = getPlayingGameContext(gameId, gamePlayerId);
        Game game = context.game();
        GamePlayer gamePlayer = context.gamePlayer();
        BoardCell boardCell = getPlayerBoardCell(game, gamePlayer);
        Optional<GameProperty> gameProperty = gamePropertyRepository.findByGameIdAndBoardCellId(
                gameId,
                boardCell.getId()
        );

        return toLandCellResponse(boardCell, gamePlayer, gameProperty.orElse(null));
    }

    @Override
    @Transactional
    public BuyPropertyResponse buyProperty(Long gameId, Long gamePlayerId, Long boardCellId) {
        GamePlayContext context = getPlayingGameContext(gameId, gamePlayerId);
        Game game = context.game();
        GamePlayer gamePlayer = context.gamePlayer();
        BoardCell boardCell = getPlayerBoardCell(game, gamePlayer);

        if (!boardCell.getId().equals(boardCellId)) {
            throw new AppException(ErrorCode.PLAYER_NOT_ON_CELL);
        }

        if (!isPurchasableCell(boardCell.getType())) {
            throw new AppException(ErrorCode.PROPERTY_NOT_PURCHASABLE);
        }

        PropertyDetail propertyDetail = getPropertyDetail(boardCell);

        if (gamePropertyRepository.existsByGameIdAndBoardCellId(gameId, boardCellId)) {
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

        gamePlayer.setMoney(gamePlayer.getMoney() - propertyDetail.getBuyPrice());

        GameProperty savedGameProperty = gamePropertyRepository.save(gameProperty);
        gamePlayerRepository.save(gamePlayer);

        return toBuyPropertyResponse(savedGameProperty, propertyDetail);
    }

    @Override
    @Transactional
    public PayRentResponse payRent(Long gameId, Long gamePlayerId, Integer diceTotal) {
        GamePlayContext context = getPlayingGameContext(gameId, gamePlayerId);
        Game game = context.game();
        GamePlayer payer = context.gamePlayer();
        BoardCell boardCell = getPlayerBoardCell(game, payer);

        if (!isPurchasableCell(boardCell.getType())) {
            throw new AppException(ErrorCode.PROPERTY_NOT_PURCHASABLE);
        }

        PropertyDetail propertyDetail = getPropertyDetail(boardCell);
        GameProperty gameProperty = gamePropertyRepository.findByGameIdAndBoardCellId(
                gameId,
                boardCell.getId()
        ).orElseThrow(() -> new AppException(ErrorCode.PROPERTY_NOT_OWNED));
        GamePlayer owner = gameProperty.getOwner();

        if (owner.getId().equals(payer.getId())) {
            throw new AppException(ErrorCode.PROPERTY_OWNED_BY_PLAYER);
        }

        int rentAmount = getRent(propertyDetail, gameProperty, diceTotal);

        if (payer.getMoney() < rentAmount) {
            throw new AppException(ErrorCode.PLAYER_NOT_ENOUGH_MONEY);
        }

        payer.setMoney(payer.getMoney() - rentAmount);
        owner.setMoney(owner.getMoney() + rentAmount);

        gamePlayerRepository.save(payer);
        gamePlayerRepository.save(owner);

        return toPayRentResponse(boardCell, payer, owner, rentAmount);
    }

    @Override
    @Transactional
    public PayTaxResponse payTax(
            Long gameId,
            Long gamePlayerId,
            IncomeTaxOption incomeTaxOption
    ) {
        GamePlayContext context = getPlayingGameContext(gameId, gamePlayerId);
        Game game = context.game();
        GamePlayer gamePlayer = context.gamePlayer();
        BoardCell boardCell = getPlayerBoardCell(game, gamePlayer);

        if (boardCell.getType() != BoardCellType.INCOME_TAX
                && boardCell.getType() != BoardCellType.LUXURY_TAX) {
            throw new AppException(ErrorCode.PLAYER_NOT_ON_TAX_CELL);
        }

        Integer netWorth = null;
        int taxAmount;

        if (boardCell.getType() == BoardCellType.INCOME_TAX) {
            if (incomeTaxOption == null) {
                throw new AppException(ErrorCode.TAX_OPTION_REQUIRED);
            }

            if (incomeTaxOption == IncomeTaxOption.FIXED) {
                taxAmount = FIXED_INCOME_TAX_AMOUNT;
            } else {
                netWorth = getNetWorth(gameId, gamePlayer);
                taxAmount = netWorth / 10;
            }
        } else {
            taxAmount = LUXURY_TAX_AMOUNT;
        }

        if (gamePlayer.getMoney() < taxAmount) {
            throw new AppException(ErrorCode.PLAYER_NOT_ENOUGH_MONEY);
        }

        gamePlayer.setMoney(gamePlayer.getMoney() - taxAmount);
        gamePlayerRepository.save(gamePlayer);

        return toPayTaxResponse(
                boardCell,
                gamePlayer,
                incomeTaxOption,
                taxAmount,
                netWorth
        );
    }

    private GamePlayContext getPlayingGameContext(Long gameId, Long gamePlayerId) {
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

    private BoardCell getPlayerBoardCell(Game game, GamePlayer gamePlayer) {
        return boardCellRepository.findByBoardIdAndPosition(
                game.getBoard().getId(),
                gamePlayer.getPosition()
        ).orElseThrow(() -> new AppException(ErrorCode.BOARD_CELL_NOT_FOUND));
    }

    private LandCellResponse toLandCellResponse(
            BoardCell boardCell,
            GamePlayer gamePlayer,
            GameProperty gameProperty
    ) {
        LandCellResponse response = new LandCellResponse();
        response.setCellId(boardCell.getId());
        response.setCellPosition(boardCell.getPosition());
        response.setCellName(boardCell.getName());
        response.setCellType(boardCell.getType());
        response.setAction(resolveLandAction(boardCell, gamePlayer, gameProperty));

        if (isPurchasableCell(boardCell.getType())) {
            response.setProperty(toLandedPropertyResponse(boardCell, gameProperty));
        }

        return response;
    }

    private LandCellAction resolveLandAction(
            BoardCell boardCell,
            GamePlayer gamePlayer,
            GameProperty gameProperty
    ) {
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

    private LandedPropertyResponse toLandedPropertyResponse(
            BoardCell boardCell,
            GameProperty gameProperty
    ) {
        PropertyDetail propertyDetail = getPropertyDetail(boardCell);
        LandedPropertyResponse response = new LandedPropertyResponse();
        response.setBuyPrice(propertyDetail.getBuyPrice());
        response.setRent(getPreviewRent(propertyDetail, gameProperty));
        response.setHouseCount(gameProperty == null ? 0 : gameProperty.getHouseCount());
        response.setHasHotel(gameProperty != null && Boolean.TRUE.equals(gameProperty.getHasHotel()));
        response.setMortgaged(gameProperty != null && Boolean.TRUE.equals(gameProperty.getMortgaged()));

        if (gameProperty != null) {
            response.setGamePropertyId(gameProperty.getId());
            response.setOwnerGamePlayerId(gameProperty.getOwner().getId());
            response.setOwnerPlayerId(gameProperty.getOwner().getPlayer().getId());
        }

        return response;
    }

    private BuyPropertyResponse toBuyPropertyResponse(
            GameProperty gameProperty,
            PropertyDetail propertyDetail
    ) {
        BuyPropertyResponse response = new BuyPropertyResponse();
        response.setGamePropertyId(gameProperty.getId());
        response.setBoardCellId(gameProperty.getBoardCell().getId());
        response.setBoardCellPosition(gameProperty.getBoardCell().getPosition());
        response.setBoardCellName(gameProperty.getBoardCell().getName());
        response.setOwnerGamePlayerId(gameProperty.getOwner().getId());
        response.setOwnerMoney(gameProperty.getOwner().getMoney());
        response.setBuyPrice(propertyDetail.getBuyPrice());
        return response;
    }

    private PayRentResponse toPayRentResponse(
            BoardCell boardCell,
            GamePlayer payer,
            GamePlayer owner,
            Integer rentAmount
    ) {
        PayRentResponse response = new PayRentResponse();
        response.setBoardCellId(boardCell.getId());
        response.setBoardCellPosition(boardCell.getPosition());
        response.setBoardCellName(boardCell.getName());
        response.setPayerGamePlayerId(payer.getId());
        response.setOwnerGamePlayerId(owner.getId());
        response.setRentAmount(rentAmount);
        response.setPayerMoney(payer.getMoney());
        response.setOwnerMoney(owner.getMoney());
        return response;
    }

    private PayTaxResponse toPayTaxResponse(
            BoardCell boardCell,
            GamePlayer gamePlayer,
            IncomeTaxOption incomeTaxOption,
            Integer taxAmount,
            Integer netWorth
    ) {
        PayTaxResponse response = new PayTaxResponse();
        response.setBoardCellId(boardCell.getId());
        response.setBoardCellPosition(boardCell.getPosition());
        response.setBoardCellName(boardCell.getName());
        response.setTaxType(boardCell.getType());
        response.setIncomeTaxOption(
                boardCell.getType() == BoardCellType.INCOME_TAX
                        ? incomeTaxOption
                        : null
        );
        response.setTaxAmount(taxAmount);
        response.setPlayerMoney(gamePlayer.getMoney());
        response.setNetWorth(netWorth);
        return response;
    }

    private boolean isPurchasableCell(BoardCellType boardCellType) {
        return boardCellType == BoardCellType.PROPERTY
                || boardCellType == BoardCellType.STATION
                || boardCellType == BoardCellType.UTILITY;
    }

    private PropertyDetail getPropertyDetail(BoardCell boardCell) {
        if (boardCell.getPropertyDetail() == null) {
            throw new AppException(ErrorCode.PROPERTY_DETAIL_NOT_FOUND);
        }

        return boardCell.getPropertyDetail();
    }

    private int getRent(
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
            return getStationRent(gameProperty);
        }

        if (boardCellType == BoardCellType.UTILITY) {
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

    private Integer getPreviewRent(PropertyDetail propertyDetail, GameProperty gameProperty) {
        if (gameProperty != null
                && gameProperty.getBoardCell().getType() == BoardCellType.UTILITY
                && !Boolean.TRUE.equals(gameProperty.getMortgaged())) {
            return null;
        }

        return getRent(propertyDetail, gameProperty, null);
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

    private int getNetWorth(Long gameId, GamePlayer gamePlayer) {
        int propertyWorth = gamePropertyRepository.findAllByOwnerId(gamePlayer.getId()).stream()
                .filter(gameProperty -> gameProperty.getGame().getId().equals(gameId))
                .mapToInt(this::getPropertyAssetValue)
                .sum();

        return gamePlayer.getMoney() + propertyWorth;
    }

    private int getPropertyAssetValue(GameProperty gameProperty) {
        PropertyDetail propertyDetail = getPropertyDetail(gameProperty.getBoardCell());
        int houseCount = gameProperty.getHouseCount() == null ? 0 : gameProperty.getHouseCount();
        int improvementValue = houseCount * propertyDetail.getHousePrice();

        if (Boolean.TRUE.equals(gameProperty.getHasHotel())) {
            improvementValue += propertyDetail.getHotelPrice();
        }

        return propertyDetail.getBuyPrice() + improvementValue;
    }

    private int getBoardSize(Game game) {
        List<BoardCell> boardCells = boardCellRepository.findByBoardIdOrderByPositionAsc(
                game.getBoard().getId()
        );

        if (boardCells.isEmpty()) {
            throw new AppException(ErrorCode.BOARD_CELLS_NOT_FOUND);
        }

        return boardCells.size();
    }

    private int getConsecutiveDoubles(Game game) {
        return game.getConsecutiveDoubles() == null ? 0 : game.getConsecutiveDoubles();
    }

    private GamePlayer resolveNextPlayer(
            Long gameId,
            GamePlayer currentPlayer,
            Boolean isDouble
    ) {
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

    private record GamePlayContext(Game game, GamePlayer gamePlayer) {
    }
}
