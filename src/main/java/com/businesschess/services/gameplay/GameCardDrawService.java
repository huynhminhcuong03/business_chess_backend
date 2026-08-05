package com.businesschess.services.gameplay;

import com.businesschess.dto.response.DrawCardResponse;
import com.businesschess.entities.BoardCell;
import com.businesschess.entities.ChanceCard;
import com.businesschess.entities.CommunityCard;
import com.businesschess.entities.Game;
import com.businesschess.entities.GameCardDeck;
import com.businesschess.entities.GamePlayer;
import com.businesschess.enums.BoardCellType;
import com.businesschess.enums.CardActionType;
import com.businesschess.enums.CardType;
import com.businesschess.enums.ErrorCode;
import com.businesschess.exceptions.AppException;
import com.businesschess.mappers.CardMapper;
import com.businesschess.repositories.ChanceCardRepository;
import com.businesschess.repositories.CommunityCardRepository;
import com.businesschess.repositories.GameCardDeckRepository;
import org.springframework.stereotype.Service;

@Service
public class GameCardDrawService {

    // Service này chỉ chịu trách nhiệm rút thẻ đúng deck và cập nhật vòng đời deck.
    // Phần thực thi hiệu ứng card được tách sang GameCardEffectService.
    private final GameCardDeckRepository gameCardDeckRepository;

    private final ChanceCardRepository chanceCardRepository;

    private final CommunityCardRepository communityCardRepository;

    private final GameTurnService gameTurnService;

    private final GameCardEffectService gameCardEffectService;

    private final CardMapper cardMapper;

    public GameCardDrawService(
            GameCardDeckRepository gameCardDeckRepository,
            ChanceCardRepository chanceCardRepository,
            CommunityCardRepository communityCardRepository,
            GameTurnService gameTurnService,
            GameCardEffectService gameCardEffectService,
            CardMapper cardMapper
    ) {
        this.gameCardDeckRepository = gameCardDeckRepository;
        this.chanceCardRepository = chanceCardRepository;
        this.communityCardRepository = communityCardRepository;
        this.gameTurnService = gameTurnService;
        this.gameCardEffectService = gameCardEffectService;
        this.cardMapper = cardMapper;
    }

    public DrawCardResponse drawCard(GamePlayContext context) {
        Game game = context.game();
        GamePlayer gamePlayer = context.gamePlayer();
        BoardCell boardCell = gameTurnService.getPlayerBoardCell(game, gamePlayer);
        // Loại ô hiện tại quyết định rút từ deck CHANCE hay COMMUNITY.
        CardType cardType = getCardTypeFromCell(boardCell);
        GameCardDeck deckCard = drawDeckCard(game.getId(), cardType);
        DrawnCard drawnCard = getDrawnCard(deckCard);
        DrawCardResponse response = toInitialResponse(gamePlayer, drawnCard);

        // Áp dụng effect trước rồi mới cập nhật deck để nếu effect lỗi thì transaction rollback toàn bộ.
        gameCardEffectService.applyCardEffect(game, gamePlayer, drawnCard, response);
        updateDeckAfterDraw(game.getId(), gamePlayer, deckCard, drawnCard.actionType());

        return response;
    }

    private CardType getCardTypeFromCell(BoardCell boardCell) {
        if (boardCell.getType() == BoardCellType.CHANCE) {
            return CardType.CHANCE;
        }

        if (boardCell.getType() == BoardCellType.COMMUNITY) {
            return CardType.COMMUNITY;
        }

        throw new AppException(ErrorCode.PLAYER_NOT_ON_CARD_CELL);
    }

    private GameCardDeck drawDeckCard(Long gameId, CardType cardType) {
        // Chỉ rút card chưa dùng và không bị người chơi giữ, ví dụ thẻ ra tù đang cầm sẽ bị bỏ qua.
        return gameCardDeckRepository
                .findFirstByGameIdAndCardTypeAndUsedFalseAndHeldByPlayerIsNullOrderByDeckOrderAsc(gameId, cardType)
                .orElseThrow(() -> new AppException(ErrorCode.CARDS_NOT_FOUND));
    }

    private DrawnCard getDrawnCard(GameCardDeck deckCard) {
        // Deck chỉ lưu cardId, nên cần đọc lại bảng card gốc để lấy action/amount/targetPosition.
        if (deckCard.getCardType() == CardType.CHANCE) {
            ChanceCard card = chanceCardRepository.findById(deckCard.getCardId())
                    .orElseThrow(() -> new AppException(ErrorCode.CARDS_NOT_FOUND));

            return new DrawnCard(
                    CardType.CHANCE,
                    card.getId(),
                    card.getActionType(),
                    card.getAmount(),
                    card.getTargetPosition(),
                    card.getActionData(),
                    cardMapper.toResponse(card)
            );
        }

        CommunityCard card = communityCardRepository.findById(deckCard.getCardId())
                .orElseThrow(() -> new AppException(ErrorCode.CARDS_NOT_FOUND));

        return new DrawnCard(
                CardType.COMMUNITY,
                card.getId(),
                card.getActionType(),
                card.getAmount(),
                card.getTargetPosition(),
                card.getActionData(),
                cardMapper.toResponse(card)
        );
    }

    private DrawCardResponse toInitialResponse(GamePlayer gamePlayer, DrawnCard drawnCard) {
        DrawCardResponse response = new DrawCardResponse();
        response.setCard(drawnCard.response());
        response.setCardType(drawnCard.cardType());
        response.setActionType(drawnCard.actionType());
        response.setGamePlayerId(gamePlayer.getId());
        response.setOldPosition(gamePlayer.getPosition());
        response.setNewPosition(gamePlayer.getPosition());
        response.setCurrentPlayerMoney(gamePlayer.getMoney());
        return response;
    }

    private void updateDeckAfterDraw(
            Long gameId,
            GamePlayer gamePlayer,
            GameCardDeck deckCard,
            CardActionType actionType
    ) {
        if (actionType == CardActionType.GET_OUT_OF_JAIL) {
            // Thẻ ra tù không trả về cuối deck ngay, mà được giữ trên người chơi cho tới khi sử dụng.
            deckCard.setHeldByPlayer(gamePlayer);
            deckCard.setUsed(true);
            gameCardDeckRepository.save(deckCard);
            return;
        }

        // Card thường sau khi rút xong được đưa xuống cuối deck để lần sau rút card tiếp theo.
        int maxDeckOrder = gameCardDeckRepository.findMaxDeckOrderByGameIdAndCardType(
                gameId,
                deckCard.getCardType()
        );
        deckCard.setDeckOrder(maxDeckOrder + 1);
        gameCardDeckRepository.save(deckCard);
    }
}
