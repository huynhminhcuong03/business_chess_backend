package com.businesschess;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.businesschess.dto.response.BoardCellResponse;
import com.businesschess.dto.response.CardResponse;
import com.businesschess.enums.BoardCellType;
import com.businesschess.enums.CardActionType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.junit.jupiter.api.Test;

class EnumJsonTests {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void serializesEnumsAsUppercaseJsonValues() throws Exception {
        BoardCellResponse boardCellResponse = new BoardCellResponse();
        boardCellResponse.setType(BoardCellType.GO_TO_JAIL);

        CardResponse cardResponse = new CardResponse();
        cardResponse.setActionType(CardActionType.RECEIVE_FROM_BANK);

        String boardCellJson = objectMapper.writeValueAsString(boardCellResponse);
        String cardJson = objectMapper.writeValueAsString(cardResponse);

        assertTrue(boardCellJson.contains("\"type\":\"GO_TO_JAIL\""));
        assertTrue(cardJson.contains("\"actionType\":\"RECEIVE_FROM_BANK\""));
    }

    @Test
    void rejectsInvalidEnumValuesClearly() {
        assertThrows(
                InvalidFormatException.class,
                () -> objectMapper.readValue(
                        "{\"type\":\"NOT_A_CELL\"}",
                        BoardCellResponse.class
                )
        );

        assertThrows(
                InvalidFormatException.class,
                () -> objectMapper.readValue(
                        "{\"actionType\":\"NOT_AN_ACTION\"}",
                        CardResponse.class
                )
        );
    }
}
