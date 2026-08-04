package com.businesschess.dto.response;

import com.businesschess.enums.BoardCellType;
import com.businesschess.enums.IncomeTaxOption;

public class PayTaxResponse {

    private Long boardCellId;

    private Integer boardCellPosition;

    private String boardCellName;

    private BoardCellType taxType;

    private IncomeTaxOption incomeTaxOption;

    private Integer taxAmount;

    private Integer playerMoney;

    private Integer netWorth;

    public Long getBoardCellId() {
        return boardCellId;
    }

    public void setBoardCellId(Long boardCellId) {
        this.boardCellId = boardCellId;
    }

    public Integer getBoardCellPosition() {
        return boardCellPosition;
    }

    public void setBoardCellPosition(Integer boardCellPosition) {
        this.boardCellPosition = boardCellPosition;
    }

    public String getBoardCellName() {
        return boardCellName;
    }

    public void setBoardCellName(String boardCellName) {
        this.boardCellName = boardCellName;
    }

    public BoardCellType getTaxType() {
        return taxType;
    }

    public void setTaxType(BoardCellType taxType) {
        this.taxType = taxType;
    }

    public IncomeTaxOption getIncomeTaxOption() {
        return incomeTaxOption;
    }

    public void setIncomeTaxOption(IncomeTaxOption incomeTaxOption) {
        this.incomeTaxOption = incomeTaxOption;
    }

    public Integer getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(Integer taxAmount) {
        this.taxAmount = taxAmount;
    }

    public Integer getPlayerMoney() {
        return playerMoney;
    }

    public void setPlayerMoney(Integer playerMoney) {
        this.playerMoney = playerMoney;
    }

    public Integer getNetWorth() {
        return netWorth;
    }

    public void setNetWorth(Integer netWorth) {
        this.netWorth = netWorth;
    }
}
