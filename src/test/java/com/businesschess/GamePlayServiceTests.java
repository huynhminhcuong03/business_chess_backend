package com.businesschess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.businesschess.dto.response.BuyPropertyResponse;
import com.businesschess.dto.response.DrawCardResponse;
import com.businesschess.dto.response.GoToJailResponse;
import com.businesschess.dto.response.JailActionResponse;
import com.businesschess.dto.response.LandCellResponse;
import com.businesschess.dto.response.PayRentResponse;
import com.businesschess.dto.response.PayTaxResponse;
import com.businesschess.dto.response.RollDiceResponse;
import com.businesschess.entities.Board;
import com.businesschess.entities.BoardCell;
import com.businesschess.entities.ChanceCard;
import com.businesschess.entities.CommunityCard;
import com.businesschess.entities.Game;
import com.businesschess.entities.GameCardDeck;
import com.businesschess.entities.GamePlayer;
import com.businesschess.entities.GameProperty;
import com.businesschess.entities.Player;
import com.businesschess.entities.PropertyDetail;
import com.businesschess.enums.BoardCellType;
import com.businesschess.enums.CardActionType;
import com.businesschess.enums.CardType;
import com.businesschess.enums.ErrorCode;
import com.businesschess.enums.GameMode;
import com.businesschess.enums.GameStatus;
import com.businesschess.enums.IncomeTaxOption;
import com.businesschess.enums.JailActionType;
import com.businesschess.enums.LandCellAction;
import com.businesschess.enums.TokenColor;
import com.businesschess.exceptions.AppException;
import com.businesschess.mappers.CardMapper;
import com.businesschess.mappers.GamePlayMapper;
import com.businesschess.repositories.ChanceCardRepository;
import com.businesschess.repositories.CommunityCardRepository;
import com.businesschess.repositories.BoardCellRepository;
import com.businesschess.repositories.GameCardDeckRepository;
import com.businesschess.repositories.GamePlayerRepository;
import com.businesschess.repositories.GamePropertyRepository;
import com.businesschess.repositories.GameRepository;
import com.businesschess.services.DiceService;
import com.businesschess.services.gameplay.GameJailService;
import com.businesschess.services.gameplay.GameCardDrawService;
import com.businesschess.services.gameplay.GameCardEffectService;
import com.businesschess.services.gameplay.GameLandService;
import com.businesschess.services.gameplay.GameMovementService;
import com.businesschess.services.gameplay.GamePropertyPurchaseService;
import com.businesschess.services.gameplay.GameRentCalculator;
import com.businesschess.services.gameplay.GameRentService;
import com.businesschess.services.gameplay.GameTaxService;
import com.businesschess.services.gameplay.GameTurnService;
import com.businesschess.services.impl.GamePlayServiceImpl;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "spring.flyway.enabled=false")
@Transactional
class GamePlayServiceTests {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GamePlayerRepository gamePlayerRepository;

    @Autowired
    private BoardCellRepository boardCellRepository;

    @Autowired
    private GamePropertyRepository gamePropertyRepository;

    @Autowired
    private GameCardDeckRepository gameCardDeckRepository;

    @Autowired
    private ChanceCardRepository chanceCardRepository;

    @Autowired
    private CommunityCardRepository communityCardRepository;

    @Test
    void nonDoubleRollMovesPlayerAndAdvancesTurn() {
        PlayData data = createPlayingGame(1, 0);
        GamePlayServiceImpl service = gamePlayService(1, 2);

        RollDiceResponse response = service.rollDice(
                data.game().getId(),
                data.currentPlayer().getId()
        );
        entityManager.flush();
        entityManager.clear();

        GamePlayer movedPlayer = entityManager.find(GamePlayer.class, data.currentPlayer().getId());
        Game game = entityManager.find(Game.class, data.game().getId());

        assertEquals(1, response.getDice1());
        assertEquals(2, response.getDice2());
        assertEquals(3, response.getTotal());
        assertEquals(1, response.getOldPosition());
        assertEquals(0, response.getNewPosition());
        assertTrue(response.getPassedStart());
        assertEquals(200, response.getStartReward());
        assertEquals(1700, response.getCurrentPlayerMoney());
        assertEquals(data.nextPlayer().getId(), response.getNextPlayerId());
        assertEquals(0, movedPlayer.getPosition());
        assertEquals(1700, movedPlayer.getMoney());
        assertEquals(0, game.getConsecutiveDoubles());
        assertEquals(data.nextPlayer().getId(), game.getCurrentPlayer().getId());
    }

    @Test
    void doubleRollMovesPlayerAndKeepsTurn() {
        PlayData data = createPlayingGame(0, 1);
        GamePlayServiceImpl service = gamePlayService(2, 2);

        RollDiceResponse response = service.rollDice(
                data.game().getId(),
                data.currentPlayer().getId()
        );
        entityManager.flush();
        entityManager.clear();

        GamePlayer movedPlayer = entityManager.find(GamePlayer.class, data.currentPlayer().getId());
        Game game = entityManager.find(Game.class, data.game().getId());

        assertEquals(4, response.getTotal());
        assertEquals(0, response.getNewPosition());
        assertEquals(200, response.getStartReward());
        assertEquals(1700, response.getCurrentPlayerMoney());
        assertTrue(response.getIsDouble());
        assertEquals(data.currentPlayer().getId(), response.getNextPlayerId());
        assertEquals(0, movedPlayer.getPosition());
        assertEquals(1700, movedPlayer.getMoney());
        assertEquals(2, game.getConsecutiveDoubles());
        assertEquals(data.currentPlayer().getId(), game.getCurrentPlayer().getId());
    }

    @Test
    void thirdConsecutiveDoubleSendsPlayerDirectlyToJailWithoutStartReward() {
        PlayData data = createPlayingGame(3, 2);
        BoardCell jailCell = createBoardCell(
                data.game().getBoard(),
                10,
                BoardCellType.JAIL
        );
        GamePlayServiceImpl service = gamePlayService(2, 2);

        RollDiceResponse response = service.rollDice(
                data.game().getId(),
                data.currentPlayer().getId()
        );
        entityManager.flush();
        entityManager.clear();

        GamePlayer jailedPlayer = entityManager.find(GamePlayer.class, data.currentPlayer().getId());
        Game game = entityManager.find(Game.class, data.game().getId());

        assertEquals(true, response.getIsDouble());
        assertEquals(true, response.getSentToJail());
        assertEquals(3, response.getOldPosition());
        assertEquals(jailCell.getPosition(), response.getNewPosition());
        assertEquals(jailCell.getPosition(), response.getJailPosition());
        assertEquals(false, response.getPassedStart());
        assertEquals(0, response.getStartReward());
        assertEquals(1500, response.getCurrentPlayerMoney());
        assertEquals(true, response.getInJail());
        assertEquals(0, response.getJailTurn());
        assertEquals(data.nextPlayer().getId(), response.getNextPlayerId());
        assertEquals(jailCell.getPosition(), jailedPlayer.getPosition());
        assertEquals(true, jailedPlayer.getInJail());
        assertEquals(0, jailedPlayer.getJailTurn());
        assertEquals(1500, jailedPlayer.getMoney());
        assertEquals(0, game.getConsecutiveDoubles());
        assertEquals(data.nextPlayer().getId(), game.getCurrentPlayer().getId());
    }

    @Test
    void testRollUsesProvidedDiceAndCanTriggerThirdDoubleJailRule() {
        PlayData data = createPlayingGame(3, 2);
        BoardCell jailCell = createBoardCell(
                data.game().getBoard(),
                10,
                BoardCellType.JAIL
        );
        GamePlayServiceImpl service = gamePlayService(1, 2);

        RollDiceResponse response = service.testRoll(
                data.game().getId(),
                data.currentPlayer().getId(),
                4,
                4
        );

        assertEquals(4, response.getDice1());
        assertEquals(4, response.getDice2());
        assertEquals(8, response.getTotal());
        assertEquals(true, response.getIsDouble());
        assertEquals(true, response.getSentToJail());
        assertEquals(jailCell.getPosition(), response.getNewPosition());
        assertEquals(0, response.getStartReward());
    }

    @Test
    void rejectsRollWhenPlayerIsNotCurrentTurn() {
        PlayData data = createPlayingGame(0, 0);
        GamePlayServiceImpl service = gamePlayService(1, 1);

        AppException exception = assertThrows(
                AppException.class,
                () -> service.rollDice(data.game().getId(), data.nextPlayer().getId())
        );

        assertEquals(ErrorCode.PLAYER_NOT_CURRENT_TURN, exception.getErrorCode());
    }

    @Test
    void testMoveMovesPlayerToTargetPositionAndAdvancesTurn() {
        PlayData data = createPlayingGame(1, 2);
        GamePlayServiceImpl service = gamePlayService(1, 1);

        RollDiceResponse response = service.testMove(
                data.game().getId(),
                data.currentPlayer().getId(),
                3
        );
        entityManager.flush();
        entityManager.clear();

        GamePlayer movedPlayer = entityManager.find(GamePlayer.class, data.currentPlayer().getId());
        Game game = entityManager.find(Game.class, data.game().getId());

        assertEquals(0, response.getDice1());
        assertEquals(0, response.getDice2());
        assertEquals(2, response.getTotal());
        assertEquals(1, response.getOldPosition());
        assertEquals(3, response.getNewPosition());
        assertEquals(false, response.getPassedStart());
        assertEquals(0, response.getStartReward());
        assertEquals(1500, response.getCurrentPlayerMoney());
        assertEquals(false, response.getIsDouble());
        assertEquals(data.nextPlayer().getId(), response.getNextPlayerId());
        assertEquals(3, movedPlayer.getPosition());
        assertEquals(0, game.getConsecutiveDoubles());
        assertEquals(data.nextPlayer().getId(), game.getCurrentPlayer().getId());
    }

    @Test
    void testMoveMarksPassedStartWhenTargetWrapsAroundBoard() {
        PlayData data = createPlayingGame(3, 0);
        GamePlayServiceImpl service = gamePlayService(1, 1);

        RollDiceResponse response = service.testMove(
                data.game().getId(),
                data.currentPlayer().getId(),
                0
        );

        assertEquals(1, response.getTotal());
        assertEquals(3, response.getOldPosition());
        assertEquals(0, response.getNewPosition());
        assertEquals(true, response.getPassedStart());
        assertEquals(200, response.getStartReward());
        assertEquals(1700, response.getCurrentPlayerMoney());
    }

    @Test
    void testMoveRejectsOutOfRangeTargetPosition() {
        PlayData data = createPlayingGame(1, 0);
        GamePlayServiceImpl service = gamePlayService(1, 1);

        AppException exception = assertThrows(
                AppException.class,
                () -> service.testMove(
                        data.game().getId(),
                        data.currentPlayer().getId(),
                        99
                )
        );

        assertEquals(ErrorCode.BOARD_CELL_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void landCellReturnsBuyPropertyForUnownedPurchasableCell() {
        PlayData data = createPlayingGame(1, 0);
        GamePlayServiceImpl service = gamePlayService(1, 1);

        LandCellResponse response = service.landCell(
                data.game().getId(),
                data.currentPlayer().getId()
        );

        assertEquals(LandCellAction.BUY_PROPERTY, response.getAction());
        assertEquals(BoardCellType.PROPERTY, response.getCellType());
        assertEquals(100, response.getProperty().getBuyPrice());
        assertEquals(10, response.getProperty().getRent());
    }

    @Test
    void landCellReturnsPayRentForPropertyOwnedByAnotherPlayer() {
        PlayData data = createPlayingGame(1, 0);
        GameProperty gameProperty = new GameProperty();
        gameProperty.setGame(data.game());
        gameProperty.setBoardCell(data.currentCell());
        gameProperty.setOwner(data.nextPlayer());
        gameProperty.setHouseCount(2);
        gamePropertyRepository.saveAndFlush(gameProperty);
        GamePlayServiceImpl service = gamePlayService(1, 1);

        LandCellResponse response = service.landCell(
                data.game().getId(),
                data.currentPlayer().getId()
        );

        assertEquals(LandCellAction.PAY_RENT, response.getAction());
        assertEquals(data.nextPlayer().getId(), response.getProperty().getOwnerGamePlayerId());
        assertEquals(30, response.getProperty().getRent());
    }

    @Test
    void buyPropertyCreatesGamePropertyAndDeductsMoney() {
        PlayData data = createPlayingGame(1, 0);
        GamePlayServiceImpl service = gamePlayService(1, 1);

        BuyPropertyResponse response = service.buyProperty(
                data.game().getId(),
                data.currentPlayer().getId(),
                data.currentCell().getId()
        );
        entityManager.flush();
        entityManager.clear();

        GamePlayer buyer = entityManager.find(GamePlayer.class, data.currentPlayer().getId());

        assertEquals(data.currentCell().getId(), response.getBoardCellId());
        assertEquals(data.currentPlayer().getId(), response.getOwnerGamePlayerId());
        assertEquals(100, response.getBuyPrice());
        assertEquals(1400, response.getOwnerMoney());
        assertEquals(1400, buyer.getMoney());
        assertTrue(
                gamePropertyRepository.existsByGameIdAndBoardCellId(
                        data.game().getId(),
                        data.currentCell().getId()
                )
        );
    }

    @Test
    void buyPropertyRejectsCellThatPlayerIsNotOn() {
        PlayData data = createPlayingGame(1, 0);
        GamePlayServiceImpl service = gamePlayService(1, 1);

        AppException exception = assertThrows(
                AppException.class,
                () -> service.buyProperty(
                        data.game().getId(),
                        data.currentPlayer().getId(),
                        data.startCell().getId()
                )
        );

        assertEquals(ErrorCode.PLAYER_NOT_ON_CELL, exception.getErrorCode());
    }

    @Test
    void payRentTransfersMoneyFromPayerToOwner() {
        PlayData data = createPlayingGame(1, 0);
        GameProperty gameProperty = new GameProperty();
        gameProperty.setGame(data.game());
        gameProperty.setBoardCell(data.currentCell());
        gameProperty.setOwner(data.nextPlayer());
        gameProperty.setHouseCount(2);
        gamePropertyRepository.saveAndFlush(gameProperty);
        GamePlayServiceImpl service = gamePlayService(1, 1);

        PayRentResponse response = service.payRent(
                data.game().getId(),
                data.currentPlayer().getId(),
                null
        );
        entityManager.flush();
        entityManager.clear();

        GamePlayer payer = entityManager.find(GamePlayer.class, data.currentPlayer().getId());
        GamePlayer owner = entityManager.find(GamePlayer.class, data.nextPlayer().getId());

        assertEquals(data.currentCell().getId(), response.getBoardCellId());
        assertEquals(data.currentPlayer().getId(), response.getPayerGamePlayerId());
        assertEquals(data.nextPlayer().getId(), response.getOwnerGamePlayerId());
        assertEquals(30, response.getRentAmount());
        assertEquals(1470, response.getPayerMoney());
        assertEquals(1530, response.getOwnerMoney());
        assertEquals(1470, payer.getMoney());
        assertEquals(1530, owner.getMoney());
    }

    @Test
    void payRentRejectsUnownedProperty() {
        PlayData data = createPlayingGame(1, 0);
        GamePlayServiceImpl service = gamePlayService(1, 1);

        AppException exception = assertThrows(
                AppException.class,
                () -> service.payRent(
                        data.game().getId(),
                        data.currentPlayer().getId(),
                        null
                )
        );

        assertEquals(ErrorCode.PROPERTY_NOT_OWNED, exception.getErrorCode());
    }

    @Test
    void payRentForStationUsesOwnedStationCount() {
        PlayData data = createPlayingGame(1, 0);
        data.currentCell().setType(BoardCellType.STATION);
        gamePropertyRepository.saveAndFlush(createOwnedGameProperty(
                data.game(),
                data.currentCell(),
                data.nextPlayer()
        ));
        BoardCell secondStation = createBoardCell(
                data.game().getBoard(),
                10,
                BoardCellType.STATION
        );
        entityManager.persist(createPropertyDetail(secondStation));
        gamePropertyRepository.saveAndFlush(createOwnedGameProperty(
                data.game(),
                secondStation,
                data.nextPlayer()
        ));
        GamePlayServiceImpl service = gamePlayService(1, 1);

        PayRentResponse response = service.payRent(
                data.game().getId(),
                data.currentPlayer().getId(),
                null
        );

        assertEquals(50, response.getRentAmount());
        assertEquals(1450, response.getPayerMoney());
        assertEquals(1550, response.getOwnerMoney());
    }

    @Test
    void payRentForUtilityUsesDiceTotalAndOwnedUtilityCount() {
        PlayData data = createPlayingGame(1, 0);
        data.currentCell().setType(BoardCellType.UTILITY);
        gamePropertyRepository.saveAndFlush(createOwnedGameProperty(
                data.game(),
                data.currentCell(),
                data.nextPlayer()
        ));
        BoardCell secondUtility = createBoardCell(
                data.game().getBoard(),
                11,
                BoardCellType.UTILITY
        );
        entityManager.persist(createPropertyDetail(secondUtility));
        gamePropertyRepository.saveAndFlush(createOwnedGameProperty(
                data.game(),
                secondUtility,
                data.nextPlayer()
        ));
        GamePlayServiceImpl service = gamePlayService(1, 1);

        PayRentResponse response = service.payRent(
                data.game().getId(),
                data.currentPlayer().getId(),
                8
        );

        assertEquals(80, response.getRentAmount());
        assertEquals(1420, response.getPayerMoney());
        assertEquals(1580, response.getOwnerMoney());
    }

    @Test
    void payIncomeTaxFixedDeductsTwoHundred() {
        PlayData data = createPlayingGame(1, 0);
        data.currentCell().setType(BoardCellType.INCOME_TAX);
        GamePlayServiceImpl service = gamePlayService(1, 1);

        PayTaxResponse response = service.payTax(
                data.game().getId(),
                data.currentPlayer().getId(),
                IncomeTaxOption.FIXED
        );
        entityManager.flush();
        entityManager.clear();

        GamePlayer player = entityManager.find(GamePlayer.class, data.currentPlayer().getId());

        assertEquals(BoardCellType.INCOME_TAX, response.getTaxType());
        assertEquals(IncomeTaxOption.FIXED, response.getIncomeTaxOption());
        assertEquals(200, response.getTaxAmount());
        assertEquals(1300, response.getPlayerMoney());
        assertEquals(1300, player.getMoney());
    }

    @Test
    void payIncomeTaxPercentUsesCashAndOwnedPropertyValue() {
        PlayData data = createPlayingGame(1, 0);
        data.currentCell().setType(BoardCellType.INCOME_TAX);
        gamePropertyRepository.saveAndFlush(createOwnedGameProperty(
                data.game(),
                data.currentCell(),
                data.currentPlayer()
        ));
        GamePlayServiceImpl service = gamePlayService(1, 1);

        PayTaxResponse response = service.payTax(
                data.game().getId(),
                data.currentPlayer().getId(),
                IncomeTaxOption.PERCENT
        );

        assertEquals(BoardCellType.INCOME_TAX, response.getTaxType());
        assertEquals(IncomeTaxOption.PERCENT, response.getIncomeTaxOption());
        assertEquals(1600, response.getNetWorth());
        assertEquals(160, response.getTaxAmount());
        assertEquals(1340, response.getPlayerMoney());
    }

    @Test
    void payLuxuryTaxDeductsOneHundred() {
        PlayData data = createPlayingGame(1, 0);
        data.currentCell().setType(BoardCellType.LUXURY_TAX);
        GamePlayServiceImpl service = gamePlayService(1, 1);

        PayTaxResponse response = service.payTax(
                data.game().getId(),
                data.currentPlayer().getId(),
                null
        );

        assertEquals(BoardCellType.LUXURY_TAX, response.getTaxType());
        assertEquals(100, response.getTaxAmount());
        assertEquals(1400, response.getPlayerMoney());
    }

    @Test
    void payTaxRejectsNonTaxCell() {
        PlayData data = createPlayingGame(1, 0);
        GamePlayServiceImpl service = gamePlayService(1, 1);

        AppException exception = assertThrows(
                AppException.class,
                () -> service.payTax(
                        data.game().getId(),
                        data.currentPlayer().getId(),
                        IncomeTaxOption.FIXED
                )
        );

        assertEquals(ErrorCode.PLAYER_NOT_ON_TAX_CELL, exception.getErrorCode());
    }

    @Test
    void goToJailMovesPlayerDirectlyToJailCell() {
        PlayData data = createPlayingGame(3, 0);
        data.currentCell().setType(BoardCellType.GO_TO_JAIL);
        BoardCell jailCell = createBoardCell(
                data.game().getBoard(),
                10,
                BoardCellType.JAIL
        );
        GamePlayServiceImpl service = gamePlayService(1, 1);

        GoToJailResponse response = service.goToJail(
                data.game().getId(),
                data.currentPlayer().getId()
        );
        entityManager.flush();
        entityManager.clear();

        GamePlayer jailedPlayer = entityManager.find(GamePlayer.class, data.currentPlayer().getId());

        assertEquals(data.currentPlayer().getId(), response.getGamePlayerId());
        assertEquals(3, response.getFromPosition());
        assertEquals(jailCell.getPosition(), response.getJailPosition());
        assertEquals(true, response.getInJail());
        assertEquals(0, response.getJailTurn());
        assertEquals(jailCell.getPosition(), jailedPlayer.getPosition());
        assertEquals(true, jailedPlayer.getInJail());
        assertEquals(0, jailedPlayer.getJailTurn());
        assertEquals(1500, jailedPlayer.getMoney());
    }

    @Test
    void goToJailRejectsWhenPlayerIsNotOnGoToJailCell() {
        PlayData data = createPlayingGame(1, 0);
        GamePlayServiceImpl service = gamePlayService(1, 1);

        AppException exception = assertThrows(
                AppException.class,
                () -> service.goToJail(
                        data.game().getId(),
                        data.currentPlayer().getId()
                )
        );

        assertEquals(ErrorCode.PLAYER_NOT_ON_GO_TO_JAIL_CELL, exception.getErrorCode());
    }

    @Test
    void rollDiceRejectsWhenCurrentPlayerIsInJail() {
        PlayData data = createPlayingGame(1, 0);
        data.currentPlayer().setInJail(true);
        GamePlayServiceImpl service = gamePlayService(1, 1);

        AppException exception = assertThrows(
                AppException.class,
                () -> service.rollDice(data.game().getId(), data.currentPlayer().getId())
        );

        assertEquals(ErrorCode.PLAYER_IN_JAIL, exception.getErrorCode());
    }

    @Test
    void jailActionRejectsUseCardWhenPlayerHasNoJailCard() {
        PlayData data = createJailedPlayingGame();
        GamePlayServiceImpl service = gamePlayService(1, 1);

        AppException exception = assertThrows(
                AppException.class,
                () -> service.handleJailAction(
                        data.game().getId(),
                        data.currentPlayer().getId(),
                        JailActionType.USE_JAIL_CARD
                )
        );

        assertEquals(ErrorCode.JAIL_CARD_NOT_AVAILABLE, exception.getErrorCode());
    }

    @Test
    void payFineReleasesPlayerRollsAndMovesNormally() {
        PlayData data = createJailedPlayingGame();
        GamePlayServiceImpl service = gamePlayService(1, 2);

        JailActionResponse response = service.handleJailAction(
                data.game().getId(),
                data.currentPlayer().getId(),
                JailActionType.PAY_FINE
        );
        entityManager.flush();
        entityManager.clear();

        GamePlayer player = entityManager.find(GamePlayer.class, data.currentPlayer().getId());

        assertEquals(JailActionType.PAY_FINE, response.getActionType());
        assertEquals(true, response.getMoved());
        assertEquals(false, response.getInJail());
        assertEquals(0, response.getJailTurn());
        assertEquals(50, response.getFinePaid());
        assertEquals(0, response.getNewPosition());
        assertEquals(1650, response.getCurrentPlayerMoney());
        assertEquals(false, player.getInJail());
        assertEquals(0, player.getJailTurn());
        assertEquals(0, player.getPosition());
        assertEquals(1650, player.getMoney());
    }

    @Test
    void rollForDoubleFailureKeepsPlayerInJailAndAdvancesTurn() {
        PlayData data = createJailedPlayingGame();
        GamePlayServiceImpl service = gamePlayService(1, 2);

        JailActionResponse response = service.handleJailAction(
                data.game().getId(),
                data.currentPlayer().getId(),
                JailActionType.ROLL_FOR_DOUBLE
        );
        entityManager.flush();
        entityManager.clear();

        GamePlayer player = entityManager.find(GamePlayer.class, data.currentPlayer().getId());
        Game game = entityManager.find(Game.class, data.game().getId());

        assertEquals(false, response.getMoved());
        assertEquals(true, response.getInJail());
        assertEquals(1, response.getJailTurn());
        assertEquals(data.nextPlayer().getId(), response.getNextPlayerId());
        assertEquals(true, player.getInJail());
        assertEquals(1, player.getJailTurn());
        assertEquals(data.nextPlayer().getId(), game.getCurrentPlayer().getId());
    }

    @Test
    void rollForDoubleSuccessReleasesAndDoesNotGrantExtraTurn() {
        PlayData data = createJailedPlayingGame();
        GamePlayServiceImpl service = gamePlayService(2, 2);

        JailActionResponse response = service.handleJailAction(
                data.game().getId(),
                data.currentPlayer().getId(),
                JailActionType.ROLL_FOR_DOUBLE
        );
        entityManager.flush();
        entityManager.clear();

        Game game = entityManager.find(Game.class, data.game().getId());

        assertEquals(true, response.getMoved());
        assertEquals(true, response.getIsDouble());
        assertEquals(false, response.getInJail());
        assertEquals(data.nextPlayer().getId(), response.getNextPlayerId());
        assertEquals(data.nextPlayer().getId(), game.getCurrentPlayer().getId());
        assertEquals(0, game.getConsecutiveDoubles());
    }

    @Test
    void thirdRollForDoubleFailurePaysFineAndMovesWithSameDice() {
        PlayData data = createJailedPlayingGame();
        data.currentPlayer().setJailTurn(2);
        GamePlayServiceImpl service = gamePlayService(1, 2);

        JailActionResponse response = service.handleJailAction(
                data.game().getId(),
                data.currentPlayer().getId(),
                JailActionType.ROLL_FOR_DOUBLE
        );
        entityManager.flush();
        entityManager.clear();

        GamePlayer player = entityManager.find(GamePlayer.class, data.currentPlayer().getId());

        assertEquals(true, response.getMoved());
        assertEquals(false, response.getInJail());
        assertEquals(50, response.getFinePaid());
        assertEquals(0, response.getNewPosition());
        assertEquals(1650, response.getCurrentPlayerMoney());
        assertEquals(false, player.getInJail());
        assertEquals(0, player.getJailTurn());
        assertEquals(0, player.getPosition());
        assertEquals(1650, player.getMoney());
    }

    @Test
    void drawChanceCardReceiveFromBankAddsMoneyAndMovesCardToBottom() {
        PlayData data = createPlayingGame(2, 0);
        data.currentCell().setType(BoardCellType.CHANCE);
        ChanceCard card = createChanceCard(
                data.game().getBoard(),
                CardActionType.RECEIVE_FROM_BANK,
                100,
                null
        );
        GameCardDeck deck = createDeck(data.game(), CardType.CHANCE, card.getId(), 0);
        GamePlayServiceImpl service = gamePlayService(1, 1);

        DrawCardResponse response = service.drawCard(
                data.game().getId(),
                data.currentPlayer().getId()
        );
        entityManager.flush();
        entityManager.clear();

        GamePlayer player = entityManager.find(GamePlayer.class, data.currentPlayer().getId());
        GameCardDeck savedDeck = entityManager.find(GameCardDeck.class, deck.getId());

        assertEquals(CardType.CHANCE, response.getCardType());
        assertEquals(CardActionType.RECEIVE_FROM_BANK, response.getActionType());
        assertEquals(1600, response.getCurrentPlayerMoney());
        assertEquals(1, response.getMoneyChanges().size());
        assertEquals(100, response.getMoneyChanges().get(0).getMoneyDelta());
        assertEquals(1600, player.getMoney());
        assertEquals(1, savedDeck.getDeckOrder());
        assertEquals(false, savedDeck.getUsed());
    }

    @Test
    void drawChanceCardReadsAmountFromActionDataWhenAmountColumnIsNull() {
        PlayData data = createPlayingGame(2, 0);
        data.currentCell().setType(BoardCellType.CHANCE);
        ChanceCard card = createChanceCard(
                data.game().getBoard(),
                CardActionType.RECEIVE_FROM_BANK,
                null,
                null
        );
        card.setActionData("{\"amount\":50}");
        chanceCardRepository.saveAndFlush(card);
        createDeck(data.game(), CardType.CHANCE, card.getId(), 0);
        GamePlayServiceImpl service = gamePlayService(1, 1);

        DrawCardResponse response = service.drawCard(
                data.game().getId(),
                data.currentPlayer().getId()
        );

        assertEquals(1550, response.getCurrentPlayerMoney());
        assertEquals(50, response.getMoneyChanges().get(0).getMoneyDelta());
    }

    @Test
    void drawChanceCardReadsTargetPositionFromActionDataWhenTargetColumnIsNull() {
        PlayData data = createPlayingGame(2, 0);
        data.currentCell().setType(BoardCellType.CHANCE);
        ChanceCard card = createChanceCard(
                data.game().getBoard(),
                CardActionType.MOVE_TO_POSITION,
                null,
                null
        );
        card.setActionData("{\"targetPosition\":0,\"collectStartSalary\":true}");
        chanceCardRepository.saveAndFlush(card);
        createDeck(data.game(), CardType.CHANCE, card.getId(), 0);
        GamePlayServiceImpl service = gamePlayService(1, 1);

        DrawCardResponse response = service.drawCard(
                data.game().getId(),
                data.currentPlayer().getId()
        );

        assertEquals(true, response.getMoved());
        assertEquals(0, response.getNewPosition());
        assertEquals(true, response.getPassedStart());
        assertEquals(200, response.getStartReward());
        assertEquals(1700, response.getCurrentPlayerMoney());
    }

    @Test
    void drawCommunityRepairCardReadsRepairAmountsFromActionData() {
        PlayData data = createPlayingGame(2, 0);
        data.currentCell().setType(BoardCellType.COMMUNITY);
        BoardCell houseCell = createBoardCell(data.game().getBoard(), 4, BoardCellType.PROPERTY);
        BoardCell hotelCell = createBoardCell(data.game().getBoard(), 5, BoardCellType.PROPERTY);
        entityManager.persist(createPropertyDetail(houseCell));
        entityManager.persist(createPropertyDetail(hotelCell));

        GameProperty houseProperty = createOwnedGameProperty(data.game(), houseCell, data.currentPlayer());
        houseProperty.setHouseCount(2);
        gamePropertyRepository.saveAndFlush(houseProperty);

        GameProperty hotelProperty = createOwnedGameProperty(data.game(), hotelCell, data.currentPlayer());
        hotelProperty.setHasHotel(true);
        gamePropertyRepository.saveAndFlush(hotelProperty);

        CommunityCard card = createCommunityCard(
                data.game().getBoard(),
                CardActionType.REPAIR_PROPERTIES,
                null,
                null
        );
        card.setActionData("{\"amountPerHouse\":25,\"amountPerHotel\":100}");
        communityCardRepository.saveAndFlush(card);
        createDeck(data.game(), CardType.COMMUNITY, card.getId(), 0);
        GamePlayServiceImpl service = gamePlayService(1, 1);

        DrawCardResponse response = service.drawCard(
                data.game().getId(),
                data.currentPlayer().getId()
        );

        assertEquals(1350, response.getCurrentPlayerMoney());
        assertEquals(-150, response.getMoneyChanges().get(0).getMoneyDelta());
    }

    @Test
    void drawCommunityJailCardGivesCardToDrawingPlayerAndKeepsDeckCardUsed() {
        PlayData data = createPlayingGame(2, 0);
        data.currentCell().setType(BoardCellType.COMMUNITY);
        CommunityCard card = createCommunityCard(
                data.game().getBoard(),
                CardActionType.GET_OUT_OF_JAIL,
                null,
                null
        );
        GameCardDeck deck = createDeck(data.game(), CardType.COMMUNITY, card.getId(), 0);
        GamePlayServiceImpl service = gamePlayService(1, 1);

        DrawCardResponse response = service.drawCard(
                data.game().getId(),
                data.currentPlayer().getId()
        );
        entityManager.flush();
        entityManager.clear();

        GamePlayer player = entityManager.find(GamePlayer.class, data.currentPlayer().getId());
        GameCardDeck savedDeck = entityManager.find(GameCardDeck.class, deck.getId());

        assertEquals(CardType.COMMUNITY, response.getCardType());
        assertEquals(CardActionType.GET_OUT_OF_JAIL, response.getActionType());
        assertEquals(1, player.getJailFreeCard());
        assertEquals(true, savedDeck.getUsed());
        assertEquals(data.currentPlayer().getId(), savedDeck.getHeldByPlayer().getId());
    }

    @Test
    void drawCardRejectsWhenPlayerIsNotOnCardCell() {
        PlayData data = createPlayingGame(1, 0);
        GamePlayServiceImpl service = gamePlayService(1, 1);

        AppException exception = assertThrows(
                AppException.class,
                () -> service.drawCard(data.game().getId(), data.currentPlayer().getId())
        );

        assertEquals(ErrorCode.PLAYER_NOT_ON_CARD_CELL, exception.getErrorCode());
    }

    private GamePlayServiceImpl gamePlayService(int dice1, int dice2) {
        FixedDiceService fixedDiceService = new FixedDiceService(dice1, dice2);
        GamePlayMapper gamePlayMapper = new GamePlayMapper();
        GameTurnService gameTurnService = new GameTurnService(
                gameRepository,
                gamePlayerRepository,
                boardCellRepository
        );
        GameRentCalculator gameRentCalculator = new GameRentCalculator(gamePropertyRepository);
        GameMovementService gameMovementService = new GameMovementService(
                gameRepository,
                gamePlayerRepository,
                boardCellRepository,
                gameTurnService,
                gamePlayMapper
        );
        GameLandService gameLandService = new GameLandService(
                gamePropertyRepository,
                gameTurnService,
                gamePlayMapper,
                gameRentCalculator
        );
        GamePropertyPurchaseService gamePropertyPurchaseService = new GamePropertyPurchaseService(
                gamePlayerRepository,
                gamePropertyRepository,
                gameTurnService,
                gamePlayMapper
        );
        GameRentService gameRentService = new GameRentService(
                gamePlayerRepository,
                gamePropertyRepository,
                gameTurnService,
                gameRentCalculator,
                gamePlayMapper
        );
        GameTaxService gameTaxService = new GameTaxService(
                gamePlayerRepository,
                gameTurnService,
                gameRentCalculator,
                gamePlayMapper
        );
        GameJailService gameJailService = new GameJailService(
                gameRepository,
                gamePlayerRepository,
                fixedDiceService,
                gameTurnService,
                gameMovementService,
                gameCardDeckRepository,
                gamePlayMapper
        );
        GameCardEffectService gameCardEffectService = new GameCardEffectService(
                gameRepository,
                gamePlayerRepository,
                gamePropertyRepository,
                gameTurnService
        );
        GameCardDrawService gameCardDrawService = new GameCardDrawService(
                gameCardDeckRepository,
                chanceCardRepository,
                communityCardRepository,
                gameTurnService,
                gameCardEffectService,
                new CardMapper()
        );

        return new GamePlayServiceImpl(
                fixedDiceService,
                gamePlayMapper,
                gameTurnService,
                gameMovementService,
                gameLandService,
                gamePropertyPurchaseService,
                gameRentService,
                gameTaxService,
                gameCardDrawService,
                gameJailService
        );
    }

    private PlayData createPlayingGame(int currentPlayerPosition, int consecutiveDoubles) {
        Board board = new Board();
        board.setName("Play Board " + System.nanoTime());
        entityManager.persist(board);

        for (int position = 0; position < 4; position++) {
            BoardCell boardCell = new BoardCell();
            boardCell.setBoard(board);
            boardCell.setPosition(position);
            boardCell.setName("Cell " + position);
            boardCell.setType(position == 0 ? BoardCellType.START : BoardCellType.PROPERTY);
            entityManager.persist(boardCell);

            if (position > 0) {
                entityManager.persist(createPropertyDetail(boardCell));
            }
        }

        Game game = new Game();
        game.setBoard(board);
        game.setStatus(GameStatus.PLAYING);
        game.setGameMode(GameMode.NORMAL);
        game.setConsecutiveDoubles(consecutiveDoubles);
        entityManager.persist(game);

        GamePlayer currentPlayer = createGamePlayer(
                game,
                "current_",
                TokenColor.RED,
                0,
                currentPlayerPosition
        );
        GamePlayer nextPlayer = createGamePlayer(
                game,
                "next_",
                TokenColor.BLUE,
                1,
                0
        );

        game.setCurrentPlayer(currentPlayer);
        entityManager.flush();

        BoardCell startCell = boardCellRepository.findByBoardIdAndPosition(board.getId(), 0)
                .orElseThrow();
        BoardCell currentCell = boardCellRepository.findByBoardIdAndPosition(
                board.getId(),
                currentPlayerPosition
        ).orElseThrow();

        return new PlayData(game, currentPlayer, nextPlayer, startCell, currentCell);
    }

    private PlayData createJailedPlayingGame() {
        PlayData data = createPlayingGame(1, 0);
        data.currentPlayer().setInJail(true);
        data.currentPlayer().setJailTurn(0);
        return data;
    }

    private PropertyDetail createPropertyDetail(BoardCell boardCell) {
        PropertyDetail propertyDetail = new PropertyDetail();
        propertyDetail.setBoardCell(boardCell);
        boardCell.setPropertyDetail(propertyDetail);
        propertyDetail.setBuyPrice(100);
        propertyDetail.setMortgagePrice(50);
        propertyDetail.setHousePrice(50);
        propertyDetail.setHotelPrice(50);
        propertyDetail.setRentLevel0(10);
        propertyDetail.setRentLevel1(20);
        propertyDetail.setRentLevel2(30);
        propertyDetail.setRentLevel3(40);
        propertyDetail.setRentLevel4(50);
        propertyDetail.setRentHotel(60);
        return propertyDetail;
    }

    private BoardCell createBoardCell(
            Board board,
            int position,
            BoardCellType boardCellType
    ) {
        BoardCell boardCell = new BoardCell();
        boardCell.setBoard(board);
        boardCell.setPosition(position);
        boardCell.setName("Cell " + position);
        boardCell.setType(boardCellType);
        entityManager.persist(boardCell);
        return boardCell;
    }

    private GameProperty createOwnedGameProperty(
            Game game,
            BoardCell boardCell,
            GamePlayer owner
    ) {
        GameProperty gameProperty = new GameProperty();
        gameProperty.setGame(game);
        gameProperty.setBoardCell(boardCell);
        gameProperty.setOwner(owner);
        gameProperty.setHouseCount(0);
        gameProperty.setHasHotel(false);
        gameProperty.setMortgaged(false);
        return gameProperty;
    }

    private GamePlayer createGamePlayer(
            Game game,
            String usernamePrefix,
            TokenColor tokenColor,
            int turnOrder,
            int position
    ) {
        Player player = new Player();
        player.setUsername(usernamePrefix + System.nanoTime());
        player.setDisplayName("Play Test Player");
        entityManager.persist(player);

        GamePlayer gamePlayer = new GamePlayer();
        gamePlayer.setGame(game);
        gamePlayer.setPlayer(player);
        gamePlayer.setTokenColor(tokenColor);
        gamePlayer.setTurnOrder(turnOrder);
        gamePlayer.setMoney(1500);
        gamePlayer.setPosition(position);
        gamePlayer.setInJail(false);
        gamePlayer.setJailTurn(0);
        gamePlayer.setBankrupt(false);
        gamePlayer.setJailFreeCard(0);
        entityManager.persist(gamePlayer);

        return gamePlayer;
    }

    private ChanceCard createChanceCard(
            Board board,
            CardActionType actionType,
            Integer amount,
            Integer targetPosition
    ) {
        ChanceCard card = new ChanceCard();
        card.setBoard(board);
        card.setTitle("Chance " + actionType);
        card.setDescription("Test chance card");
        card.setActionType(actionType);
        card.setAmount(amount);
        card.setTargetPosition(targetPosition);
        return chanceCardRepository.saveAndFlush(card);
    }

    private CommunityCard createCommunityCard(
            Board board,
            CardActionType actionType,
            Integer amount,
            Integer targetPosition
    ) {
        CommunityCard card = new CommunityCard();
        card.setBoard(board);
        card.setTitle("Community " + actionType);
        card.setDescription("Test community card");
        card.setActionType(actionType);
        card.setAmount(amount);
        card.setTargetPosition(targetPosition);
        return communityCardRepository.saveAndFlush(card);
    }

    private GameCardDeck createDeck(
            Game game,
            CardType cardType,
            Long cardId,
            Integer deckOrder
    ) {
        GameCardDeck deck = new GameCardDeck();
        deck.setGame(game);
        deck.setCardType(cardType);
        deck.setCardId(cardId);
        deck.setDeckOrder(deckOrder);
        deck.setUsed(false);
        return gameCardDeckRepository.saveAndFlush(deck);
    }

    private record PlayData(
            Game game,
            GamePlayer currentPlayer,
            GamePlayer nextPlayer,
            BoardCell startCell,
            BoardCell currentCell
    ) {
    }

    private record FixedDiceService(int dice1, int dice2) implements DiceService {

        @Override
        public RollDiceResponse rollDice() {
            RollDiceResponse response = new RollDiceResponse();
            response.setDice1(dice1);
            response.setDice2(dice2);
            response.setTotal(dice1 + dice2);
            response.setIsDouble(dice1 == dice2);
            return response;
        }
    }
}
