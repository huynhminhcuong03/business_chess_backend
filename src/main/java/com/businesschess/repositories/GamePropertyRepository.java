package com.businesschess.repositories;

import java.util.List;
import java.util.Optional;

import com.businesschess.entities.GameProperty;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GamePropertyRepository extends JpaRepository<GameProperty, Long> {

    @EntityGraph(attributePaths = {"boardCell", "owner", "owner.player"})
    List<GameProperty> findAllByGameId(Long gameId);

    @EntityGraph(attributePaths = {"boardCell", "owner", "owner.player"})
    Optional<GameProperty> findByGameIdAndBoardCellId(Long gameId, Long boardCellId);

    boolean existsByGameIdAndBoardCellId(Long gameId, Long boardCellId);

    @EntityGraph(attributePaths = {"boardCell", "owner", "owner.player"})
    List<GameProperty> findAllByOwnerId(Long ownerId);
}
