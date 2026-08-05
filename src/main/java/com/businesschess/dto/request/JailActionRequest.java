package com.businesschess.dto.request;

import com.businesschess.enums.JailActionType;
import jakarta.validation.constraints.NotNull;

public class JailActionRequest {

    // Lựa chọn của người chơi khi đang ở tù: trả phạt, thử tung đôi hoặc dùng thẻ.
    @NotNull
    private JailActionType actionType;

    public JailActionType getActionType() {
        return actionType;
    }

    public void setActionType(JailActionType actionType) {
        this.actionType = actionType;
    }
}
