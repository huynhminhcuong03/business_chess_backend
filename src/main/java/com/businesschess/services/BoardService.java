package com.businesschess.services;

import com.businesschess.dto.response.BoardResponse;

public interface BoardService {

    BoardResponse getBoardById(Long id);
}