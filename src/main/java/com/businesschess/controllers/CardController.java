package com.businesschess.controllers;

import java.util.List;

import com.businesschess.common.ApiResponse;
import com.businesschess.common.constants.MessageConstant;
import com.businesschess.dto.response.CardResponse;
import com.businesschess.services.CardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CardController {

    private static final Long DEFAULT_BOARD_ID = 1L;

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping("/chance_card")
    public ApiResponse getChanceCards() {
        List<CardResponse> cards = cardService.getChanceCards(DEFAULT_BOARD_ID);

        return ApiResponse.ok(MessageConstant.CHANCE_CARD_LIST_SUCCESS, cards);
    }

    @GetMapping("/community_card")
    public ApiResponse getCommunityCards() {
        List<CardResponse> cards = cardService.getCommunityCards(DEFAULT_BOARD_ID);

        return ApiResponse.ok(MessageConstant.COMMUNITY_CARD_LIST_SUCCESS, cards);
    }
}
