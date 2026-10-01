package com.bondtradex.ai.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChunkingService {


    public List<String> chunk(
            String text,
            int chunkSize,
            int overlap
    ) {

        if (chunkSize <= 0) {
            throw new IllegalArgumentException(
                    "chunkSize must be greater than zero"
            );
        }

        if (overlap < 0 || overlap >= chunkSize) {
            throw new IllegalArgumentException(
                    "overlap must be >= 0 and < chunkSize"
            );
        }

        List<String> chunks = new ArrayList<>();

        int start = 0;

        while (start < text.length()) {

            int end = Math.min(start + chunkSize, text.length());

            String chunk = text.substring(start, end);

            chunks.add(chunk);

            if (end == text.length()) {
                break;
            }

            start = end - overlap;
        }

        return chunks;
    }
}