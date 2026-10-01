package com.bondtradex.ai.service;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import com.bondtradex.ai.model.SearchResult;
import java.util.Comparator;
import java.util.List;

@Service
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;

    public EmbeddingService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    private static final List<String> DOCUMENTS = List.of(
            "Kafka consumer keeps rebalancing",
            "PostgreSQL connection pool is exhausted",
            "JVM heap memory is almost full",
            "Kubernetes pod keeps restarting",
            "JWT token has expired"
    );

    public float[] embed(String text) {

        return embeddingModel.embed(text);
    }


    public double cosineSimilarity(float[] vectorA, float[] vectorB) {

        if (vectorA.length != vectorB.length) {
            throw new IllegalArgumentException(
                    "Vectors must have the same dimensions"
            );
        }

        double dotProduct = 0.0;
        double magnitudeA = 0.0;
        double magnitudeB = 0.0;

        for (int i = 0; i < vectorA.length; i++) {

            dotProduct += vectorA[i] * vectorB[i];

            magnitudeA += vectorA[i] * vectorA[i];

            magnitudeB += vectorB[i] * vectorB[i];
        }

        return dotProduct /
                (Math.sqrt(magnitudeA) * Math.sqrt(magnitudeB));
    }

    public double compare(String textA, String textB) {

        float[] embeddingA = embeddingModel.embed(textA);
        float[] embeddingB = embeddingModel.embed(textB);

        return cosineSimilarity(
                embeddingA,
                embeddingB
        );
    }

    public List<SearchResult> search(String query) {

        float[] queryEmbeddings = embeddingModel.embed(query);
        return DOCUMENTS.stream().map(document->{
            float[] docEmbedding = embeddingModel.embed(document);
            double score = cosineSimilarity(queryEmbeddings,docEmbedding);
            return new SearchResult(document,score);
        }).sorted(Comparator.comparingDouble(SearchResult::score).reversed()).toList();
    }
}