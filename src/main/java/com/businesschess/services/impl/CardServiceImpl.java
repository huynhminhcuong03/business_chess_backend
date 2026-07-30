package com.businesschess.services.impl;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.businesschess.dto.response.CardResponse;
import com.businesschess.entities.ChanceCard;
import com.businesschess.entities.CommunityCard;
import com.businesschess.enums.ErrorCode;
import com.businesschess.exceptions.AppException;
import com.businesschess.mappers.CardMapper;
import com.businesschess.repositories.BoardRepository;
import com.businesschess.repositories.ChanceCardRepository;
import com.businesschess.repositories.CommunityCardRepository;
import com.businesschess.services.CardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CardServiceImpl implements CardService {

    private final BoardRepository boardRepository;
    private final ChanceCardRepository chanceCardRepository;
    private final CommunityCardRepository communityCardRepository;
    private final CardMapper cardMapper;

    public CardServiceImpl(
            BoardRepository boardRepository,
            ChanceCardRepository chanceCardRepository,
            CommunityCardRepository communityCardRepository,
            CardMapper cardMapper
    ) {
        this.boardRepository = boardRepository;
        this.chanceCardRepository = chanceCardRepository;
        this.communityCardRepository = communityCardRepository;
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
    public CardResponse drawChanceCard(Long boardId) {
        List<ChanceCard> cards = chanceCardRepository.findByBoardIdOrderByIdAsc(boardId);
        ensureCardsAvailable(boardId, cards);

        return cardMapper.toResponse(cards.get(randomIndex(cards.size())));
    }

    @Override
    public List<CardResponse> getCommunityCards(Long boardId) {
        ensureBoardExists(boardId);

        return communityCardRepository.findByBoardIdOrderByIdAsc(boardId).stream()
                .map(cardMapper::toResponse)
                .toList();
    }

    @Override
    public CardResponse drawCommunityCard(Long boardId) {
        List<CommunityCard> cards = communityCardRepository.findByBoardIdOrderByIdAsc(boardId);
        ensureCardsAvailable(boardId, cards);

        return cardMapper.toResponse(cards.get(randomIndex(cards.size())));
    }

    private void ensureBoardExists(Long boardId) {
        if (!boardRepository.existsById(boardId)) {
            throw new AppException(ErrorCode.BOARD_NOT_FOUND);
        }
    }

    private void ensureCardsAvailable(Long boardId, List<?> cards) {
        ensureBoardExists(boardId);

        if (cards.isEmpty()) {
            throw new AppException(ErrorCode.CARDS_NOT_FOUND);
        }
    }

    private int randomIndex(int size) {
        return ThreadLocalRandom.current().nextInt(size);
    }
}
