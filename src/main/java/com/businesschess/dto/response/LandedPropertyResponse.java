package com.businesschess.dto.response;

public class LandedPropertyResponse {

    private Long gamePropertyId;

    private Long ownerGamePlayerId;

    private Long ownerPlayerId;

    private Integer buyPrice;

    private Integer rent;

    private Integer houseCount;

    private Boolean hasHotel;

    private Boolean mortgaged;

    public Long getGamePropertyId() {
        return gamePropertyId;
    }

    public void setGamePropertyId(Long gamePropertyId) {
        this.gamePropertyId = gamePropertyId;
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

    public Integer getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(Integer buyPrice) {
        this.buyPrice = buyPrice;
    }

    public Integer getRent() {
        return rent;
    }

    public void setRent(Integer rent) {
        this.rent = rent;
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
