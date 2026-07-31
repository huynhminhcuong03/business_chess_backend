package com.businesschess.dto.response;

import com.businesschess.enums.BoardCellType;

public class BoardCellResponse {

    private Long id;

    private Long boardId;

    private int position;

    private String name;

    private BoardCellType type;

    private String color;

    private String image;

    private PropertyDetailResponse propertyDetail;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBoardId() {
        return boardId;
    }

    public void setBoardId(Long boardId) {
        this.boardId = boardId;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BoardCellType getType() {
        return type;
    }

    public void setType(BoardCellType type) {
        this.type = type;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public PropertyDetailResponse getPropertyDetail() {
        return propertyDetail;
    }

    public void setPropertyDetail(PropertyDetailResponse propertyDetail) {
        this.propertyDetail = propertyDetail;
    }
}
