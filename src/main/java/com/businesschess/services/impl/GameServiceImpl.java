package com.businesschess.services.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.businesschess.dto.request.CreateGamePlayerRequest;
import com.businesschess.dto.request.CreateGameRequest;
import com.businesschess.dto.response.GamePlayerResponse;
import com.businesschess.dto.response.GameResponse;
import com.businesschess.entities.Board;
import com.businesschess.entities.ChanceCard;
import com.businesschess.entities.CommunityCard;
import com.businesschess.entities.Game;
import com.businesschess.entities.GameCardDeck;
import com.businesschess.entities.GamePlayer;
import com.businesschess.entities.Player;
import com.businesschess.enums.CardType;
import com.businesschess.enums.ErrorCode;
import com.businesschess.enums.GameMode;
import com.businesschess.enums.GameStatus;
import com.businesschess.exceptions.AppException;
import com.businesschess.mappers.GameMapper;
import com.businesschess.repositories.BoardRepository;
import com.businesschess.repositories.ChanceCardRepository;
import com.businesschess.repositories.CommunityCardRepository;
import com.businesschess.repositories.GameCardDeckRepository;
import com.businesschess.repositories.GamePlayerRepository;
import com.businesschess.repositories.GameRepository;
import com.businesschess.repositories.PlayerRepository;
import com.businesschess.services.GameService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GameServiceImpl implements GameService {

    private static final Long DEFAULT_BOARD_ID = 1L;

    private final BoardRepository boardRepository;
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final GamePlayerRepository gamePlayerRepository;
    private final ChanceCardRepository chanceCardRepository;
    private final CommunityCardRepository communityCardRepository;
    private final GameCardDeckRepository gameCardDeckRepository;
    private final GameMapper gameMapper;

    public GameServiceImpl(
            BoardRepository boardRepository,
            GameRepository gameRepository,
            PlayerRepository playerRepository,
            GamePlayerRepository gamePlayerRepository,
            ChanceCardRepository chanceCardRepository,
            CommunityCardRepository communityCardRepository,
            GameCardDeckRepository gameCardDeckRepository,
            GameMapper gameMapper
    ) {
        this.boardRepository = boardRepository;
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
        this.gamePlayerRepository = gamePlayerRepository;
        this.chanceCardRepository = chanceCardRepository;
        this.communityCardRepository = communityCardRepository;
        this.gameCardDeckRepository = gameCardDeckRepository;
        this.gameMapper = gameMapper;
    }

    @Override
    @Transactional
    public GameResponse createGame(CreateGameRequest request) {
        Board board = boardRepository.findById(DEFAULT_BOARD_ID)
                .orElseThrow(() -> new AppException(ErrorCode.BOARD_NOT_FOUND));

        Game game = new Game();
        game.setBoard(board);
        game.setStatus(GameStatus.WAITING);
        game.setGameMode(request == null || request.getGameMode() == null
                ? GameMode.NORMAL
                : request.getGameMode());
        game.setConsecutiveDoubles(0);

        Game savedGame = gameRepository.save(game);
        return toGameResponse(savedGame);
    }

    @Override
    @Transactional(readOnly = true)
    public GameResponse getGame(Long id) {
        return toGameResponse(findGame(id));
    }

    @Override
    @Transactional
    public GameResponse startGame(Long id) {
        Game game = findGame(id);

        if (game.getStatus() != GameStatus.WAITING) {
            throw new AppException(ErrorCode.GAME_NOT_WAITING);
        }

        List<GamePlayer> players = gamePlayerRepository.findByGameIdOrderByTurnOrderAsc(id);
        if (players.isEmpty()) {
            throw new AppException(ErrorCode.GAME_PLAYERS_NOT_FOUND);
        }

        initializeDecks(game);
        game.setStatus(GameStatus.PLAYING);
        game.setCurrentPlayer(players.get(0));
        game.setStartedAt(LocalDateTime.now());

        return toGameResponse(gameRepository.save(game));
    }

    @Override
    @Transactional
    public GamePlayerResponse createGamePlayer(CreateGamePlayerRequest request) {
        Game game = findGame(request.getGameId());

        if (game.getStatus() != GameStatus.WAITING) {
            throw new AppException(ErrorCode.GAME_NOT_WAITING);
        }

        Player player = playerRepository.findByUsername(request.getUsername())
                .orElseGet(() -> createPlayer(request));

        if (gamePlayerRepository.existsByGameIdAndPlayerId(game.getId(), player.getId())) {
            throw new AppException(ErrorCode.PLAYER_ALREADY_JOINED);
        }

        if (gamePlayerRepository.existsByGameIdAndTokenColor(game.getId(), request.getTokenColor())) {
            throw new AppException(ErrorCode.TOKEN_COLOR_ALREADY_USED);
        }

        GamePlayer gamePlayer = new GamePlayer();
        gamePlayer.setGame(game);
        gamePlayer.setPlayer(player);
        gamePlayer.setTokenColor(request.getTokenColor());
        gamePlayer.setTurnOrder(nextTurnOrder(game.getId()));
        gamePlayer.setMoney(1500);
        gamePlayer.setPosition(0);
        gamePlayer.setInJail(false);
        gamePlayer.setJailTurn(0);
        gamePlayer.setBankrupt(false);
        gamePlayer.setJailFreeCard(0);

        return gameMapper.toResponse(gamePlayerRepository.save(gamePlayer));
    }

    @Override
    @Transactional(readOnly = true)
    public List<GamePlayerResponse> getGamePlayers(Long gameId) {
        ensureGameExists(gameId);

        return gamePlayerRepository.findByGameIdOrderByTurnOrderAsc(gameId).stream()
                .map(gameMapper::toResponse)
                .toList();
    }

    private Game findGame(Long id) {
        return gameRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.GAME_NOT_FOUND));
    }

    private void ensureGameExists(Long id) {
        if (!gameRepository.existsById(id)) {
            throw new AppException(ErrorCode.GAME_NOT_FOUND);
        }
    }

    private Player createPlayer(CreateGamePlayerRequest request) {
        Player player = new Player();
        player.setUsername(request.getUsername());
        player.setDisplayName(request.getDisplayName());
        return playerRepository.save(player);
    }

    private Integer nextTurnOrder(Long gameId) {
        return gamePlayerRepository.findFirstByGameIdOrderByTurnOrderDesc(gameId)
                .map(gamePlayer -> gamePlayer.getTurnOrder() + 1)
                .orElse(0);
    }

    private GameResponse toGameResponse(Game game) {
        return gameMapper.toResponse(game, gamePlayerRepository.findByGameIdOrderByTurnOrderAsc(game.getId()));
    }

    private void initializeDecks(Game game) {
        if (gameCardDeckRepository.existsByGameId(game.getId())) {
            return;
        }

        List<GameCardDeck> decks = new ArrayList<>();
        decks.addAll(buildChanceDeck(game));
        decks.addAll(buildCommunityDeck(game));

        if (decks.isEmpty()) {
            throw new AppException(ErrorCode.CARDS_NOT_FOUND);
        }

        gameCardDeckRepository.saveAll(decks);
    }

    private List<GameCardDeck> buildChanceDeck(Game game) {
        List<ChanceCard> cards = new ArrayList<>(
                chanceCardRepository.findByBoardIdOrderByIdAsc(game.getBoard().getId())
        );
        Collections.shuffle(cards);

        List<GameCardDeck> decks = new ArrayList<>();
        for (int i = 0; i < cards.size(); i++) {
            decks.add(toDeck(game, CardType.CHANCE, cards.get(i).getId(), i));
        }
        return decks;
    }

    private List<GameCardDeck> buildCommunityDeck(Game game) {
        List<CommunityCard> cards = new ArrayList<>(
                communityCardRepository.findByBoardIdOrderByIdAsc(game.getBoard().getId())
        );
        Collections.shuffle(cards);

        List<GameCardDeck> decks = new ArrayList<>();
        for (int i = 0; i < cards.size(); i++) {
            decks.add(toDeck(game, CardType.COMMUNITY, cards.get(i).getId(), i));
        }
        return decks;
    }

    private GameCardDeck toDeck(Game game, CardType cardType, Long cardId, int deckOrder) {
        GameCardDeck deck = new GameCardDeck();
        deck.setGame(game);
        deck.setCardType(cardType);
        deck.setCardId(cardId);
        deck.setDeckOrder(deckOrder);
        deck.setUsed(false);
        return deck;
    }
}
