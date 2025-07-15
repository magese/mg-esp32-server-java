package com.magese.ai.communication.domain;

import com.magese.ai.enums.ListenMode;
import com.magese.ai.enums.ListenState;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public final class ListenMessage extends Message {
    public ListenMessage() {
        super("listen");
    }

    private ListenState state;
    private ListenMode mode;
    private String text;
}
