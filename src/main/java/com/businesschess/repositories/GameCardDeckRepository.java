package com.businesschess.repositories;

import com.businesschess.entities.GameCardDeck;
import com.businesschess.enums.CardType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameCardDeckRepository extends JpaRepository<GameCardDeck, Long> {

    boolean existsByGameId(Long gameId);

    void deleteByGameId(Long gameId);

    long countByGameIdAndCardType(Long gameId, CardType cardType);
}
