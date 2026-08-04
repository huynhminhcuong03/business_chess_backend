package com.businesschess.repositories;

import java.util.List;
import java.util.Optional;

import com.businesschess.entities.BoardCell;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardCellRepository extends JpaRepository<BoardCell, Long> {

    @EntityGraph(attributePaths = "propertyDetail")
    List<BoardCell> findByBoardIdOrderByPositionAsc(Long boardId);

    @EntityGraph(attributePaths = "propertyDetail")
    Optional<BoardCell> findByBoardIdAndPosition(Long boardId, Integer position);
}
