package com.businesschess.entities;

import com.businesschess.enums.CardType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "game_card_decks",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_game_card_decks_order", columnNames = {"game_id", "card_type", "deck_order"}),
                @UniqueConstraint(name = "uk_game_card_decks_card", columnNames = {"game_id", "card_type", "card_id"})
        }
)
public class GameCardDeck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_type", nullable = false, length = 20)
    private CardType cardType;

    @Column(name = "card_id", nullable = false)
    private Long cardId;

    @Column(name = "deck_order", nullable = false)
    private Integer deckOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "held_by_player_id")
    private GamePlayer heldByPlayer;

    @Column(name = "is_used", nullable = false)
    private Boolean used = false;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public CardType getCardType() {
        return cardType;
    }

    public void setCardType(CardType cardType) {
        this.cardType = cardType;
    }

    public Long getCardId() {
        return cardId;
    }

    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }

    public Integer getDeckOrder() {
        return deckOrder;
    }

    public void setDeckOrder(Integer deckOrder) {
        this.deckOrder = deckOrder;
    }

    public GamePlayer getHeldByPlayer() {
        return heldByPlayer;
    }

    public void setHeldByPlayer(GamePlayer heldByPlayer) {
        this.heldByPlayer = heldByPlayer;
    }

    public Boolean getUsed() {
        return used;
    }

    public void setUsed(Boolean used) {
        this.used = used;
    }
}
