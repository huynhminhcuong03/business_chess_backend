package com.businesschess.repositories;

import java.util.List;

import com.businesschess.entities.BoardCell;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardCellRepository extends JpaRepository<BoardCell, Long> {

    @EntityGraph(attributePaths = "propertyDetail")
    List<BoardCell> findByBoardIdOrderByPositionAsc(Long boardId);
}
