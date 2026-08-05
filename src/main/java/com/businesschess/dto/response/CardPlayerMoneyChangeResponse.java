package com.businesschess.dto.response;

public class CardPlayerMoneyChangeResponse {

    // Một dòng thay đổi tiền do effect card gây ra cho một người chơi.
    private Long gamePlayerId;

    private Integer moneyDelta;

    private Integer moneyAfter;

    public Long getGamePlayerId() {
        return gamePlayerId;
    }

    public void setGamePlayerId(Long gamePlayerId) {
        this.gamePlayerId = gamePlayerId;
    }

    public Integer getMoneyDelta() {
        return moneyDelta;
    }

    public void setMoneyDelta(Integer moneyDelta) {
        this.moneyDelta = moneyDelta;
    }

    public Integer getMoneyAfter() {
        return moneyAfter;
    }

    public void setMoneyAfter(Integer moneyAfter) {
        this.moneyAfter = moneyAfter;
    }
}
