package com.businesschess.dto.request;

import com.businesschess.enums.IncomeTaxOption;

public class PayTaxRequest {

    private IncomeTaxOption incomeTaxOption;

    public IncomeTaxOption getIncomeTaxOption() {
        return incomeTaxOption;
    }

    public void setIncomeTaxOption(IncomeTaxOption incomeTaxOption) {
        this.incomeTaxOption = incomeTaxOption;
    }
}
