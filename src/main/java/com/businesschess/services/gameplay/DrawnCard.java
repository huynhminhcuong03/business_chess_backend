package com.businesschess.services.gameplay;

import com.businesschess.dto.response.CardResponse;
import com.businesschess.enums.CardActionType;
import com.businesschess.enums.CardType;

// Bản rút gọn của ChanceCard/CommunityCard để effect service xử lý chung cho hai loại thẻ.
public record DrawnCard(
        CardType cardType,
        Long cardId,
        CardActionType actionType,
        Integer amount,
        Integer targetPosition,
        String actionData,
        CardResponse response
) {
}
