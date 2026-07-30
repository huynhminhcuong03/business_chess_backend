package com.businesschess.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "property_details")
public class PropertyDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cell_id", nullable = false, unique = true)
    private BoardCell boardCell;

    @Column(name = "buy_price", nullable = false)
    private Integer buyPrice;

    @Column(name = "mortgage_price", nullable = false)
    private Integer mortgagePrice;

    @Column(name = "house_price", nullable = false)
    private Integer housePrice;

    @Column(name = "hotel_price", nullable = false)
    private Integer hotelPrice;

    @Column(name = "rent_level0", nullable = false)
    private Integer rentLevel0;

    @Column(name = "rent_level1", nullable = false)
    private Integer rentLevel1;

    @Column(name = "rent_level2", nullable = false)
    private Integer rentLevel2;

    @Column(name = "rent_level3", nullable = false)
    private Integer rentLevel3;

    @Column(name = "rent_level4", nullable = false)
    private Integer rentLevel4;

    @Column(name = "rent_hotel", nullable = false)
    private Integer rentHotel;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BoardCell getBoardCell() {
        return boardCell;
    }

    public void setBoardCell(BoardCell boardCell) {
        this.boardCell = boardCell;
    }

    public Integer getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(Integer buyPrice) {
        this.buyPrice = buyPrice;
    }

    public Integer getMortgagePrice() {
        return mortgagePrice;
    }

    public void setMortgagePrice(Integer mortgagePrice) {
        this.mortgagePrice = mortgagePrice;
    }

    public Integer getHousePrice() {
        return housePrice;
    }

    public void setHousePrice(Integer housePrice) {
        this.housePrice = housePrice;
    }

    public Integer getHotelPrice() {
        return hotelPrice;
    }

    public void setHotelPrice(Integer hotelPrice) {
        this.hotelPrice = hotelPrice;
    }

    public Integer getRentLevel0() {
        return rentLevel0;
    }

    public void setRentLevel0(Integer rentLevel0) {
        this.rentLevel0 = rentLevel0;
    }

    public Integer getRentLevel1() {
        return rentLevel1;
    }

    public void setRentLevel1(Integer rentLevel1) {
        this.rentLevel1 = rentLevel1;
    }

    public Integer getRentLevel2() {
        return rentLevel2;
    }

    public void setRentLevel2(Integer rentLevel2) {
        this.rentLevel2 = rentLevel2;
    }

    public Integer getRentLevel3() {
        return rentLevel3;
    }

    public void setRentLevel3(Integer rentLevel3) {
        this.rentLevel3 = rentLevel3;
    }

    public Integer getRentLevel4() {
        return rentLevel4;
    }

    public void setRentLevel4(Integer rentLevel4) {
        this.rentLevel4 = rentLevel4;
    }

    public Integer getRentHotel() {
        return rentHotel;
    }

    public void setRentHotel(Integer rentHotel) {
        this.rentHotel = rentHotel;
    }
}
