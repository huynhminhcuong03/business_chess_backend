package com.businesschess.entities;

import java.time.LocalDateTime;

import com.businesschess.enums.TokenColor;
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
        name = "game_players",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_game_players_player", columnNames = {"game_id", "player_id"}),
                @UniqueConstraint(name = "uk_game_players_turn_order", columnNames = {"game_id", "turn_order"}),
                @UniqueConstraint(name = "uk_game_players_token_color", columnNames = {"game_id", "token_color"})
        }
)
public class GamePlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(name = "turn_order", nullable = false)
    private Integer turnOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "token_color", nullable = false, length = 20)
    private TokenColor tokenColor;

    @Column(nullable = false)
    private Integer money = 1500;

    @Column(nullable = false)
    private Integer position = 0;

    @Column(name = "in_jail", nullable = false)
    private Boolean inJail = false;

    @Column(name = "jail_turn", nullable = false)
    private Integer jailTurn = 0;

    @Column(nullable = false)
    private Boolean bankrupt = false;

    @Column(name = "jail_free_card", nullable = false)
    private Integer jailFreeCard = 0;

    @Column(name = "joined_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime joinedAt;

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

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Integer getTurnOrder() {
        return turnOrder;
    }

    public void setTurnOrder(Integer turnOrder) {
        this.turnOrder = turnOrder;
    }

    public TokenColor getTokenColor() {
        return tokenColor;
    }

    public void setTokenColor(TokenColor tokenColor) {
        this.tokenColor = tokenColor;
    }

    public Integer getMoney() {
        return money;
    }

    public void setMoney(Integer money) {
        this.money = money;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Boolean getInJail() {
        return inJail;
    }

    public void setInJail(Boolean inJail) {
        this.inJail = inJail;
    }

    public Integer getJailTurn() {
        return jailTurn;
    }

    public void setJailTurn(Integer jailTurn) {
        this.jailTurn = jailTurn;
    }

    public Boolean getBankrupt() {
        return bankrupt;
    }

    public void setBankrupt(Boolean bankrupt) {
        this.bankrupt = bankrupt;
    }

    public Integer getJailFreeCard() {
        return jailFreeCard;
    }

    public void setJailFreeCard(Integer jailFreeCard) {
        this.jailFreeCard = jailFreeCard;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
}
