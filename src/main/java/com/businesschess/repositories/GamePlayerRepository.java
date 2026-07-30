package com.businesschess.repositories;

import java.util.List;
import java.util.Optional;

import com.businesschess.entities.GamePlayer;
import com.businesschess.enums.TokenColor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GamePlayerRepository extends JpaRepository<GamePlayer, Long> {

    List<GamePlayer> findByGameIdOrderByTurnOrderAsc(Long gameId);

    Optional<GamePlayer> findFirstByGameIdOrderByTurnOrderDesc(Long gameId);

    boolean existsByGameIdAndPlayerId(Long gameId, Long playerId);

    boolean existsByGameIdAndTokenColor(Long gameId, TokenColor tokenColor);
}
