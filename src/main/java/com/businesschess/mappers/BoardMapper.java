package com.businesschess.mappers;

import com.businesschess.dto.response.BoardResponse;
import com.businesschess.entities.Board;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BoardMapper {

    BoardResponse toResponse(Board board);
}