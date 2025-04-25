package com.host.SpringBootAutomationProduction.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LuMove {

    private int fId;

    private String msgId;

    private String orderNo;

    private int movementId;

    private String sscc;

    private String fromLoc;

    private String toLoc;

    private String reason;

    private String userCode;

    private String dateTime;


    public void trimStringFields() {
        if (msgId != null) {
            msgId = msgId.trim();
        }
        if (orderNo != null) {
            orderNo = orderNo.trim();
        }
        if (sscc != null) {
            sscc = sscc.trim();
        }
        if (fromLoc != null) {
            fromLoc = fromLoc.trim();
        }
        if (toLoc != null) {
            toLoc = toLoc.trim();
        }
        if (reason != null) {
            reason = reason.trim();
        }
        if (userCode != null) {
            userCode = userCode.trim();
        }
        if (dateTime != null) {
            dateTime = dateTime.trim();
        }
    }

}
