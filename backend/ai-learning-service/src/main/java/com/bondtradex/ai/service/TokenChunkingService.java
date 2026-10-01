package com.bondtradex.ai.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TokenChunkingService {

    public List<Document> chunk(String text) {

        Document sourceDocument = new Document(text, Map.of("source", "chunking-experiment"));

        TokenTextSplitter splitter =
                TokenTextSplitter.builder()
                        .withChunkSize(100)
                        .withMinChunkSizeChars(50)
                        .withMinChunkLengthToEmbed(10)
                        .withMaxNumChunks(100)
                        .withKeepSeparator(true)
                        .build();

        return splitter.apply(
                List.of(sourceDocument)
        );
    }

    public List<Document> chunk(Document document) {

        TokenTextSplitter splitter =
                TokenTextSplitter.builder()
                        .withChunkSize(100)
                        .withMinChunkSizeChars(50)
                        .withMinChunkLengthToEmbed(10)
                        .withMaxNumChunks(100)
                        .withKeepSeparator(true)
                        .build();

        return splitter.apply(
                List.of(document)
        );
    }
}