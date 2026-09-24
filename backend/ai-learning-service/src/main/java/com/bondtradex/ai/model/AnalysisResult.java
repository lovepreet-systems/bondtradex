package com.bondtradex.ai.model;
import java.util.List;

public record AnalysisResult(

        String summary,
        String category,
        String severity,
        List<String> suggestions

) {
}
