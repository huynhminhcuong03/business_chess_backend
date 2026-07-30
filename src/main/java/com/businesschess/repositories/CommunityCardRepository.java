package com.businesschess.repositories;

import java.util.List;

import com.businesschess.entities.CommunityCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunityCardRepository extends JpaRepository<CommunityCard, Long> {

    List<CommunityCard> findByBoardIdOrderByIdAsc(Long boardId);
}
