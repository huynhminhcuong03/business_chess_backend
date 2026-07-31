package com.businesschess.dto.response;

import com.businesschess.enums.CardActionType;
import com.fasterxml.jackson.databind.JsonNode;

public class CardResponse {

    private Long id;

    private Long boardId;

    private String type;

    private String title;

    private String description;

    private CardActionType actionType;

    private JsonNode actionData;

    private Integer amount;

    private Integer targetPosition;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBoardId() {
        return boardId;
    }

    public void setBoardId(Long boardId) {
        this.boardId = boardId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CardActionType getActionType() {
        return actionType;
    }

    public void setActionType(CardActionType actionType) {
        this.actionType = actionType;
    }

    public JsonNode getActionData() {
        return actionData;
    }

    public void setActionData(JsonNode actionData) {
        this.actionData = actionData;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Integer getTargetPosition() {
        return targetPosition;
    }

    public void setTargetPosition(Integer targetPosition) {
        this.targetPosition = targetPosition;
    }
}
