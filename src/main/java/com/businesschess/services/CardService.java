package com.businesschess.services;

import java.util.List;

import com.businesschess.dto.response.CardResponse;

public interface CardService {

    List<CardResponse> getChanceCards(Long boardId);

    CardResponse drawChanceCard(Long boardId);

    List<CardResponse> getCommunityCards(Long boardId);

    CardResponse drawCommunityCard(Long boardId);
}
