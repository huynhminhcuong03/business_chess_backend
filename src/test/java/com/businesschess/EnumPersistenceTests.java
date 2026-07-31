package com.businesschess;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.businesschess.entities.Board;
import com.businesschess.entities.BoardCell;
import com.businesschess.entities.ChanceCard;
import com.businesschess.entities.CommunityCard;
import com.businesschess.enums.BoardCellType;
import com.businesschess.enums.CardActionType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "spring.flyway.enabled=false")
@Transactional
class EnumPersistenceTests {

    @Autowired
    private EntityManager entityManager;

    @Test
    void readsBoardCellTypeAndCardActionTypeFromDatabase() {
        Board board = new Board();
        board.setName("Enum persistence test board");
        entityManager.persist(board);

        BoardCell boardCell = new BoardCell();
        boardCell.setBoard(board);
        boardCell.setPosition(0);
        boardCell.setName("Test Utility");
        boardCell.setType(BoardCellType.UTILITY);
        entityManager.persist(boardCell);

        ChanceCard chanceCard = new ChanceCard();
        chanceCard.setBoard(board);
        chanceCard.setTitle("Chance jail card");
        chanceCard.setDescription("Keep this card until needed.");
        chanceCard.setActionType(CardActionType.GET_OUT_OF_JAIL);
        entityManager.persist(chanceCard);

        CommunityCard communityCard = new CommunityCard();
        communityCard.setBoard(board);
        communityCard.setTitle("Community bank card");
        communityCard.setDescription("Collect from the bank.");
        communityCard.setActionType(CardActionType.RECEIVE_FROM_BANK);
        communityCard.setAmount(100);
        entityManager.persist(communityCard);

        entityManager.flush();
        entityManager.clear();

        BoardCell savedBoardCell = entityManager.find(
                BoardCell.class,
                boardCell.getId()
        );
        ChanceCard savedChanceCard = entityManager.find(
                ChanceCard.class,
                chanceCard.getId()
        );
        CommunityCard savedCommunityCard = entityManager.find(
                CommunityCard.class,
                communityCard.getId()
        );

        assertEquals(BoardCellType.UTILITY, savedBoardCell.getType());
        assertEquals(CardActionType.GET_OUT_OF_JAIL, savedChanceCard.getActionType());
        assertEquals(CardActionType.RECEIVE_FROM_BANK, savedCommunityCard.getActionType());
    }
}
