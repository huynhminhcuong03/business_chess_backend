package com.businesschess.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.businesschess.dto.response.CardResponse;
import com.businesschess.entities.ChanceCard;
import com.businesschess.entities.CommunityCard;
import org.springframework.stereotype.Component;

@Component
public class CardMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();


    public CardResponse toResponse(ChanceCard card) {
        if (card == null) {
            return null;
        }

        CardResponse response = new CardResponse();
        response.setId(card.getId());
        response.setBoardId(card.getBoard().getId());
        response.setType("CHANCE");
        response.setTitle(card.getTitle());
        response.setDescription(card.getDescription());
        response.setActionType(card.getActionType());
        response.setActionData(toActionData(card.getActionData()));
        response.setAmount(card.getAmount());
        response.setTargetPosition(card.getTargetPosition());
        return response;
    }

    public CardResponse toResponse(CommunityCard card) {
        if (card == null) {
            return null;
        }

        CardResponse response = new CardResponse();
        response.setId(card.getId());
        response.setBoardId(card.getBoard().getId());
        response.setType("COMMUNITY");
        response.setTitle(card.getTitle());
        response.setDescription(card.getDescription());
        response.setActionType(card.getActionType());
        response.setActionData(toActionData(card.getActionData()));
        response.setAmount(card.getAmount());
        response.setTargetPosition(card.getTargetPosition());
        return response;
    }

    private JsonNode toActionData(String actionData) {
        if (actionData == null || actionData.isBlank()) {
            return null;
        }

        try {
            return objectMapper.readTree(actionData);
        } catch (JsonProcessingException exception) {
            return TextNode.valueOf(actionData);
        }
    }
}
