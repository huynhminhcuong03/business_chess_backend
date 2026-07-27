package com.businesschess.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.businesschess.entities.Board;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
}