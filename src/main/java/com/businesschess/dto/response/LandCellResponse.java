package com.businesschess.dto.response;

import com.businesschess.enums.BoardCellType;
import com.businesschess.enums.LandCellAction;

public class LandCellResponse {

    private Long cellId;

    private Integer cellPosition;

    private String cellName;

    private BoardCellType cellType;

    private LandCellAction action;

    private LandedPropertyResponse property;

    public Long getCellId() {
        return cellId;
    }

    public void setCellId(Long cellId) {
        this.cellId = cellId;
    }

    public Integer getCellPosition() {
        return cellPosition;
    }

    public void setCellPosition(Integer cellPosition) {
        this.cellPosition = cellPosition;
    }

    public String getCellName() {
        return cellName;
    }

    public void setCellName(String cellName) {
        this.cellName = cellName;
    }

    public BoardCellType getCellType() {
        return cellType;
    }

    public void setCellType(BoardCellType cellType) {
        this.cellType = cellType;
    }

    public LandCellAction getAction() {
        return action;
    }

    public void setAction(LandCellAction action) {
        this.action = action;
    }

    public LandedPropertyResponse getProperty() {
        return property;
    }

    public void setProperty(LandedPropertyResponse property) {
        this.property = property;
    }
}
