package com.aiknow.ai.dto;

import java.util.List;

public record ChatCompletionResult(
        String answer,
        List<RawCitation> citations,
        boolean insufficientInformation
) {
}
