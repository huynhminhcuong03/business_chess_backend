package com.businesschess.services;

import java.util.List;

import com.businesschess.dto.response.CardResponse;

public interface CardService {

    List<CardResponse> getChanceCards(Long boardId);

    List<CardResponse> getCommunityCards(Long boardId);
}
