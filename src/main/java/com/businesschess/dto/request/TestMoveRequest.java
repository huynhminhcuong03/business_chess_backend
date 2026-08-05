package com.businesschess.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class TestMoveRequest {

    // Vị trí muốn ép tới để test nhanh các ô đặc biệt trên bàn cờ.
    @NotNull
    @Min(0)
    private Integer targetPosition;

    @Min(1)
    private Integer diceTotal;

    public Integer getTargetPosition() {
        return targetPosition;
    }

    public void setTargetPosition(Integer targetPosition) {
        this.targetPosition = targetPosition;
    }

    public Integer getDiceTotal() {
        return diceTotal;
    }

    public void setDiceTotal(Integer diceTotal) {
        this.diceTotal = diceTotal;
    }
}
