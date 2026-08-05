package com.businesschess.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class TestRollRequest {

    // Chỉ dùng cho endpoint test-roll; roll thật vẫn lấy giá trị từ DiceService.
    @NotNull
    @Min(1)
    @Max(6)
    private Integer dice1;

    @NotNull
    @Min(1)
    @Max(6)
    private Integer dice2;

    public Integer getDice1() {
        return dice1;
    }

    public void setDice1(Integer dice1) {
        this.dice1 = dice1;
    }

    public Integer getDice2() {
        return dice2;
    }

    public void setDice2(Integer dice2) {
        this.dice2 = dice2;
    }
}
