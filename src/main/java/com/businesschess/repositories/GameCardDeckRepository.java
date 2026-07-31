package com.businesschess.repositories;

import java.util.Optional;

import com.businesschess.entities.GameCardDeck;
import com.businesschess.enums.CardType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameCardDeckRepository extends JpaRepository<GameCardDeck, Long> {

    boolean existsByGameId(Long gameId);

    void deleteByGameId(Long gameId);

    long countByGameIdAndCardType(Long gameId, CardType cardType);

    Optional<GameCardDeck> findFirstByGameIdAndCardTypeAndUsedFalseAndHeldByPlayerIsNullOrderByDeckOrderAsc(
            Long gameId,
            CardType cardType
    );

    @Query("""
            select coalesce(max(deck.deckOrder), -1)
            from GameCardDeck deck
            where deck.game.id = :gameId
              and deck.cardType = :cardType
            """)
    int findMaxDeckOrderByGameIdAndCardType(
            @Param("gameId") Long gameId,
            @Param("cardType") CardType cardType
    );
}
