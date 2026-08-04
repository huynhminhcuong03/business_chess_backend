package com.businesschess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import com.businesschess.dto.response.GameResponse;
import com.businesschess.entities.Board;
import com.businesschess.entities.BoardCell;
import com.businesschess.entities.Game;
import com.businesschess.entities.GamePlayer;
import com.businesschess.entities.GameProperty;
import com.businesschess.entities.Player;
import com.businesschess.enums.BoardCellType;
import com.businesschess.enums.GameMode;
import com.businesschess.enums.GameStatus;
import com.businesschess.enums.TokenColor;
import com.businesschess.repositories.GamePropertyRepository;
import com.businesschess.services.GameService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "spring.flyway.enabled=false")
@Transactional
class GamePropertyTests {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private GamePropertyRepository gamePropertyRepository;

    @Autowired
    private GameService gameService;

    @Test
    void savesValidGamePropertyWithGamePlayerOwnerAndFindsByGameAndCell() {
        TestGameData data = createGameData(0, TokenColor.RED);

        GameProperty gameProperty = new GameProperty();
        gameProperty.setGame(data.game());
        gameProperty.setBoardCell(data.boardCell());
        gameProperty.setOwner(data.gamePlayer());

        GameProperty saved = gamePropertyRepository.saveAndFlush(gameProperty);
        entityManager.clear();

        GameProperty found = gamePropertyRepository
                .findByGameIdAndBoardCellId(data.game().getId(), data.boardCell().getId())
                .orElseThrow();

        assertEquals(saved.getId(), found.getId());
        assertEquals(data.gamePlayer().getId(), found.getOwner().getId());
        assertEquals(data.player().getId(), found.getOwner().getPlayer().getId());
    }

    @Test
    void doesNotAllowDuplicateGameAndBoardCell() {
        TestGameData data = createGameData(1, TokenColor.BLUE);

        gamePropertyRepository.saveAndFlush(
                createGameProperty(data.game(), data.boardCell(), data.gamePlayer())
        );

        assertThrows(
                DataIntegrityViolationException.class,
                () -> gamePropertyRepository.saveAndFlush(
                        createGameProperty(data.game(), data.boardCell(), data.gamePlayer())
                )
        );
    }

    @Test
    void allowsSameBoardCellAcrossDifferentGames() {
        Board board = createBoard("Shared board");
        BoardCell boardCell = createBoardCell(board, 2);
        Game firstGame = createGame(board);
        Game secondGame = createGame(board);
        GamePlayer firstOwner = createGamePlayer(firstGame, TokenColor.GREEN);
        GamePlayer secondOwner = createGamePlayer(secondGame, TokenColor.YELLOW);

        gamePropertyRepository.save(createGameProperty(firstGame, boardCell, firstOwner));
        gamePropertyRepository.save(createGameProperty(secondGame, boardCell, secondOwner));
        entityManager.flush();

        assertTrue(
                gamePropertyRepository.existsByGameIdAndBoardCellId(
                        firstGame.getId(),
                        boardCell.getId()
                )
        );
        assertTrue(
                gamePropertyRepository.existsByGameIdAndBoardCellId(
                        secondGame.getId(),
                        boardCell.getId()
                )
        );
    }

    @Test
    void defaultPropertyStateMatchesDatabaseDefaults() {
        TestGameData data = createGameData(3, TokenColor.RED);

        GameProperty saved = gamePropertyRepository.saveAndFlush(
                createGameProperty(data.game(), data.boardCell(), data.gamePlayer())
        );
        entityManager.clear();

        GameProperty found = entityManager.find(GameProperty.class, saved.getId());

        assertEquals(0, found.getHouseCount());
        assertFalse(found.getHasHotel());
        assertFalse(found.getMortgaged());
    }

    @Test
    void findsPropertiesByGameAndOwner() {
        TestGameData data = createGameData(4, TokenColor.BLUE);
        gamePropertyRepository.saveAndFlush(
                createGameProperty(data.game(), data.boardCell(), data.gamePlayer())
        );
        entityManager.clear();

        List<GameProperty> byGame = gamePropertyRepository.findAllByGameId(data.game().getId());
        List<GameProperty> byOwner = gamePropertyRepository.findAllByOwnerId(data.gamePlayer().getId());

        assertEquals(1, byGame.size());
        assertEquals(1, byOwner.size());
    }

    @Test
    void gameResponseReturnsEmptyPropertiesWhenGameHasNoProperties() {
        TestGameData data = createGameData(5, TokenColor.GREEN);

        GameResponse response = gameService.getGame(data.game().getId());

        assertNotNull(response.getProperties());
        assertTrue(response.getProperties().isEmpty());
    }

    @Test
    void gameResponseReturnsPropertyOwnerAndBoardCellDetails() {
        TestGameData data = createGameData(6, TokenColor.YELLOW);
        GameProperty saved = gamePropertyRepository.saveAndFlush(
                createGameProperty(data.game(), data.boardCell(), data.gamePlayer())
        );
        entityManager.clear();

        GameResponse response = gameService.getGame(data.game().getId());

        assertEquals(1, response.getProperties().size());
        assertEquals(saved.getId(), response.getProperties().get(0).getId());
        assertEquals(data.boardCell().getId(), response.getProperties().get(0).getBoardCellId());
        assertEquals(data.boardCell().getPosition(), response.getProperties().get(0).getBoardCellPosition());
        assertEquals(data.boardCell().getName(), response.getProperties().get(0).getBoardCellName());
        assertEquals(data.gamePlayer().getId(), response.getProperties().get(0).getOwnerGamePlayerId());
        assertEquals(data.player().getId(), response.getProperties().get(0).getOwnerPlayerId());
    }

    private TestGameData createGameData(int position, TokenColor tokenColor) {
        Board board = createBoard("Board " + position);
        BoardCell boardCell = createBoardCell(board, position);
        Game game = createGame(board);
        GamePlayer gamePlayer = createGamePlayer(game, tokenColor);

        return new TestGameData(boardCell, game, gamePlayer, gamePlayer.getPlayer());
    }

    private Board createBoard(String name) {
        Board board = new Board();
        board.setName(name + " " + System.nanoTime());
        entityManager.persist(board);
        return board;
    }

    private BoardCell createBoardCell(Board board, int position) {
        BoardCell boardCell = new BoardCell();
        boardCell.setBoard(board);
        boardCell.setPosition(position);
        boardCell.setName("Test Property " + position);
        boardCell.setType(BoardCellType.PROPERTY);
        entityManager.persist(boardCell);
        return boardCell;
    }

    private Game createGame(Board board) {
        Game game = new Game();
        game.setBoard(board);
        game.setStatus(GameStatus.WAITING);
        game.setGameMode(GameMode.NORMAL);
        game.setConsecutiveDoubles(0);
        entityManager.persist(game);
        return game;
    }

    private GamePlayer createGamePlayer(Game game, TokenColor tokenColor) {
        Player player = new Player();
        player.setUsername("player_" + System.nanoTime());
        player.setDisplayName("Test Player");
        entityManager.persist(player);

        GamePlayer gamePlayer = new GamePlayer();
        gamePlayer.setGame(game);
        gamePlayer.setPlayer(player);
        gamePlayer.setTurnOrder(0);
        gamePlayer.setTokenColor(tokenColor);
        gamePlayer.setMoney(1500);
        gamePlayer.setPosition(0);
        gamePlayer.setInJail(false);
        gamePlayer.setJailTurn(0);
        gamePlayer.setBankrupt(false);
        gamePlayer.setJailFreeCard(0);
        entityManager.persist(gamePlayer);
        return gamePlayer;
    }

    private GameProperty createGameProperty(
            Game game,
            BoardCell boardCell,
            GamePlayer owner
    ) {
        GameProperty gameProperty = new GameProperty();
        gameProperty.setGame(game);
        gameProperty.setBoardCell(boardCell);
        gameProperty.setOwner(owner);
        return gameProperty;
    }

    private record TestGameData(
            BoardCell boardCell,
            Game game,
            GamePlayer gamePlayer,
            Player player
    ) {
    }
}
