package com.businesschess.services.gameplay;

import com.businesschess.entities.Game;
import com.businesschess.entities.GamePlayer;

// Gom Game và GamePlayer đã được validate để truyền qua các service gameplay nhỏ.
public record GamePlayContext(Game game, GamePlayer gamePlayer) {
}
