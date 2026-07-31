package com.businesschess.services;

import java.util.List;

import com.businesschess.dto.response.CardResponse;

public interface CardService {

    List<CardResponse> getChanceCards(Long boardId);

    CardResponse drawChanceCard(Long gameId, Long playerId);

    List<CardResponse> getCommunityCards(Long boardId);

    CardResponse drawCommunityCard(Long gameId, Long playerId);
}
