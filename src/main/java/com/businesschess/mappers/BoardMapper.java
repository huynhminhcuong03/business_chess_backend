package com.businesschess.mappers;

import com.businesschess.dto.response.BoardResponse;
import com.businesschess.dto.response.BoardCellResponse;
import com.businesschess.dto.response.PropertyDetailResponse;
import com.businesschess.entities.Board;
import com.businesschess.entities.BoardCell;
import com.businesschess.entities.PropertyDetail;
import org.springframework.stereotype.Component;

@Component
public class BoardMapper {

    public BoardResponse toResponse(Board board) {
        if (board == null) {
            return null;
        }

        BoardResponse response = new BoardResponse();
        response.setId(board.getId());
        response.setName(board.getName());
        response.setCreatedAt(board.getCreatedAt());
        return response;
    }

    public BoardCellResponse toCellResponse(BoardCell boardCell) {
        if (boardCell == null) {
            return null;
        }

        BoardCellResponse response = new BoardCellResponse();
        response.setId(boardCell.getId());
        response.setBoardId(boardCell.getBoard().getId());
        response.setPosition(boardCell.getPosition());
        response.setName(boardCell.getName());
        response.setType(boardCell.getType());
        response.setColor(boardCell.getColor());
        response.setImage(boardCell.getImage());
        response.setPropertyDetail(toPropertyDetailResponse(boardCell.getPropertyDetail()));
        return response;
    }

    private PropertyDetailResponse toPropertyDetailResponse(PropertyDetail propertyDetail) {
        if (propertyDetail == null) {
            return null;
        }

        PropertyDetailResponse response = new PropertyDetailResponse();
        response.setId(propertyDetail.getId());
        response.setBuyPrice(propertyDetail.getBuyPrice());
        response.setMortgagePrice(propertyDetail.getMortgagePrice());
        response.setHousePrice(propertyDetail.getHousePrice());
        response.setHotelPrice(propertyDetail.getHotelPrice());
        response.setRentLevel0(propertyDetail.getRentLevel0());
        response.setRentLevel1(propertyDetail.getRentLevel1());
        response.setRentLevel2(propertyDetail.getRentLevel2());
        response.setRentLevel3(propertyDetail.getRentLevel3());
        response.setRentLevel4(propertyDetail.getRentLevel4());
        response.setRentHotel(propertyDetail.getRentHotel());
        return response;
    }
}
