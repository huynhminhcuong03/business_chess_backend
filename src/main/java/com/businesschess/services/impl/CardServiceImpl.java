package com.businesschess.services.impl;

import java.util.List;
import com.businesschess.dto.response.CardResponse;
import com.businesschess.entities.ChanceCard;
import com.businesschess.entities.CommunityCard;
import com.businesschess.entities.Game;
import com.businesschess.entities.GameCardDeck;
import com.businesschess.entities.GamePlayer;
import com.businesschess.enums.CardActionType;
import com.businesschess.enums.CardType;
import com.businesschess.enums.ErrorCode;
import com.businesschess.enums.GameStatus;
import com.businesschess.exceptions.AppException;
import com.businesschess.mappers.CardMapper;
import com.businesschess.repositories.BoardRepository;
import com.businesschess.repositories.ChanceCardRepository;
import com.businesschess.repositories.CommunityCardRepository;
import com.businesschess.repositories.GameCardDeckRepository;
import com.businesschess.repositories.GamePlayerRepository;
import com.businesschess.repositories.GameRepository;
import com.businesschess.services.CardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CardServiceImpl implements CardService {

    private final BoardRepository boardRepository;
    private final GameRepository gameRepository;
    private final GamePlayerRepository gamePlayerRepository;
    private final ChanceCardRepository chanceCardRepository;
    private final CommunityCardRepository communityCardRepository;
    private final GameCardDeckRepository gameCardDeckRepository;
    private final CardMapper cardMapper;

    public CardServiceImpl(
            BoardRepository boardRepository,
            GameRepository gameRepository,
            GamePlayerRepository gamePlayerRepository,
            ChanceCardRepository chanceCardRepository,
            CommunityCardRepository communityCardRepository,
            GameCardDeckRepository gameCardDeckRepository,
            CardMapper cardMapper
    ) {
        this.boardRepository = boardRepository;
        this.gameRepository = gameRepository;
        this.gamePlayerRepository = gamePlayerRepository;
        this.chanceCardRepository = chanceCardRepository;
        this.communityCardRepository = communityCardRepository;
        this.gameCardDeckRepository = gameCardDeckRepository;
        this.cardMapper = cardMapper;
    }

    @Override
    public List<CardResponse> getChanceCards(Long boardId) {
        ensureBoardExists(boardId);

        return chanceCardRepository.findByBoardIdOrderByIdAsc(boardId).stream()
                .map(cardMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CardResponse drawChanceCard(Long gameId, Long playerId) {
        GameCardDeck deckCard = drawDeckCard(gameId, playerId, CardType.CHANCE);
        ChanceCard card = chanceCardRepository.findById(deckCard.getCardId())
                .orElseThrow(() -> new AppException(ErrorCode.CARDS_NOT_FOUND));

        return cardMapper.toResponse(card);
    }

    @Override
    public List<CardResponse> getCommunityCards(Long boardId) {
        ensureBoardExists(boardId);

        return communityCardRepository.findByBoardIdOrderByIdAsc(boardId).stream()
                .map(cardMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CardResponse drawCommunityCard(Long gameId, Long playerId) {
        GameCardDeck deckCard = drawDeckCard(gameId, playerId, CardType.COMMUNITY);
        CommunityCard card = communityCardRepository.findById(deckCard.getCardId())
                .orElseThrow(() -> new AppException(ErrorCode.CARDS_NOT_FOUND));

        return cardMapper.toResponse(card);
    }

    private void ensureBoardExists(Long boardId) {
        if (!boardRepository.existsById(boardId)) {
            throw new AppException(ErrorCode.BOARD_NOT_FOUND);
        }
    }

    private GameCardDeck drawDeckCard(Long gameId, Long playerId, CardType cardType) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new AppException(ErrorCode.GAME_NOT_FOUND));

        if (game.getStatus() != GameStatus.PLAYING) {
            throw new AppException(ErrorCode.GAME_NOT_PLAYING);
        }

        GameCardDeck deckCard = gameCardDeckRepository
                .findFirstByGameIdAndCardTypeAndUsedFalseAndHeldByPlayerIsNullOrderByDeckOrderAsc(gameId, cardType)
                .orElseThrow(() -> new AppException(ErrorCode.CARDS_NOT_FOUND));

        CardActionType actionType = getActionType(deckCard);
        if (actionType == CardActionType.GET_OUT_OF_JAIL) {
            holdJailFreeCard(deckCard, gameId, playerId);
            return deckCard;
        }

        moveCardToBottom(deckCard, gameId, cardType);
        return deckCard;
    }

    private CardActionType getActionType(GameCardDeck deckCard) {
        if (deckCard.getCardType() == CardType.CHANCE) {
            return chanceCardRepository.findById(deckCard.getCardId())
                    .map(ChanceCard::getActionType)
                    .orElseThrow(() -> new AppException(ErrorCode.CARDS_NOT_FOUND));
        }

        return communityCardRepository.findById(deckCard.getCardId())
                .map(CommunityCard::getActionType)
                .orElseThrow(() -> new AppException(ErrorCode.CARDS_NOT_FOUND));
    }

    private void holdJailFreeCard(GameCardDeck deckCard, Long gameId, Long playerId) {
        if (playerId == null) {
            moveCardToBottom(deckCard, gameId, deckCard.getCardType());
            return;
        }

        GamePlayer player = gamePlayerRepository.findById(playerId)
                .orElseThrow(() -> new AppException(ErrorCode.PLAYER_NOT_FOUND));

        if (!player.getGame().getId().equals(gameId)) {
            throw new AppException(ErrorCode.PLAYER_NOT_FOUND);
        }

        deckCard.setHeldByPlayer(player);
        deckCard.setUsed(true);
        player.setJailFreeCard(player.getJailFreeCard() + 1);
    }

    private void moveCardToBottom(GameCardDeck deckCard, Long gameId, CardType cardType) {
        int maxDeckOrder = gameCardDeckRepository.findMaxDeckOrderByGameIdAndCardType(gameId, cardType);

        deckCard.setDeckOrder(maxDeckOrder + 1);
    }
}
