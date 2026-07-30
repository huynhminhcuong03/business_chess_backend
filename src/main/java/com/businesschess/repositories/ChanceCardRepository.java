package com.businesschess.repositories;

import java.util.List;

import com.businesschess.entities.ChanceCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChanceCardRepository extends JpaRepository<ChanceCard, Long> {

    List<ChanceCard> findByBoardIdOrderByIdAsc(Long boardId);
}
