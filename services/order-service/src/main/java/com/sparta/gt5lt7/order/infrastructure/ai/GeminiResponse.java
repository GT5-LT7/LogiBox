package com.sparta.gt5lt7.order.infrastructure.ai;

import java.util.List;

public record GeminiResponse(
        List<Candidate> candidates
) {
    public String extractText() {
        if (candidates == null || candidates.isEmpty()) {
            return "";
        }

        Content content = candidates.get(0).content();

        if (content == null || content.parts() == null || content.parts().isEmpty()) {
            return "";
        }

        return content.parts().get(0).text();
    }

    public record Candidate(Content content) {
    }

    public record Content(List<Part> parts) {
    }

    public record Part(String text) {
    }
}