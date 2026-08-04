package com.businesschess.dto.response;

public class GamePropertyResponse {

    private Long id;

    private Long boardCellId;

    private Integer boardCellPosition;

    private String boardCellName;

    private Long ownerGamePlayerId;

    private Long ownerPlayerId;

    private Integer houseCount;

    private Boolean hasHotel;

    private Boolean mortgaged;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Long getOwnerGamePlayerId() {
        return ownerGamePlayerId;
    }

    public void setOwnerGamePlayerId(Long ownerGamePlayerId) {
        this.ownerGamePlayerId = ownerGamePlayerId;
    }

    public Long getOwnerPlayerId() {
        return ownerPlayerId;
    }

    public void setOwnerPlayerId(Long ownerPlayerId) {
        this.ownerPlayerId = ownerPlayerId;
    }

    public Integer getHouseCount() {
        return houseCount;
    }

    public void setHouseCount(Integer houseCount) {
        this.houseCount = houseCount;
    }

    public Boolean getHasHotel() {
        return hasHotel;
    }

    public void setHasHotel(Boolean hasHotel) {
        this.hasHotel = hasHotel;
    }

    public Boolean getMortgaged() {
        return mortgaged;
    }

    public void setMortgaged(Boolean mortgaged) {
        this.mortgaged = mortgaged;
    }
}
