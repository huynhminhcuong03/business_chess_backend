package com.businesschess.dto.request;

import jakarta.validation.constraints.Min;

public class PayRentRequest {

    @Min(1)
    private Integer diceTotal;

    public Integer getDiceTotal() {
        return diceTotal;
    }

    public void setDiceTotal(Integer diceTotal) {
        this.diceTotal = diceTotal;
    }
}
