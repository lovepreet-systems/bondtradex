package com.bondtradex.ai.service;

import com.bondtradex.ai.model.DocumentRequest;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.bondtradex.ai.model.VectorSearchResult;
import java.util.List;

@Service
public class VectorDocumentService {

    private final EmbeddingModel embeddingModel;
    private final JdbcTemplate jdbcTemplate;

    public VectorDocumentService(
            EmbeddingModel embeddingModel,
            JdbcTemplate jdbcTemplate
    ) {
        this.embeddingModel = embeddingModel;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addDocument(DocumentRequest request) {

        float[] embedding =
                embeddingModel.embed(request.content());

        String vector =
                toPgVector(embedding);

        String metadata = """
            {
              "technology": "%s",
              "service": "%s",
              "environment": "%s"
            }
            """.formatted(
                request.technology(),
                request.service(),
                request.environment()
        );

        jdbcTemplate.update(
                """
                INSERT INTO ai_document (
                    content,
                    metadata,
                    embedding
                )
                VALUES (?, ?::jsonb, ?::vector)
                """,
                request.content(),
                metadata,
                vector
        );
    }

    private String toPgVector(float[] embedding) {

        StringBuilder builder = new StringBuilder("[");

        for (int i = 0; i < embedding.length; i++) {

            if (i > 0) {
                builder.append(",");
            }

            builder.append(embedding[i]);
        }

        builder.append("]");

        System.out.println("PG vector is  : "+builder);
        return builder.toString();
    }

    public List<VectorSearchResult> search(
            String query,
            int topK
    ) {

        float[] queryEmbedding =
                embeddingModel.embed(query);

        String queryVector =
                toPgVector(queryEmbedding);

        return jdbcTemplate.query(
                """
                SELECT
                    id,
                    content,
                    1 - (embedding <=> ?::vector) AS similarity
                FROM ai_document
                ORDER BY embedding <=> ?::vector
                LIMIT ?
                """,
                (rs, rowNum) -> new VectorSearchResult(
                        rs.getLong("id"),
                        rs.getString("content"),
                        rs.getDouble("similarity")
                ),
                queryVector,
                queryVector,
                topK
        );
    }

    public List<VectorSearchResult> searchByService(
            String query,
            String service,
            int topK
    ) {

        float[] queryEmbedding =
                embeddingModel.embed(query);

        String queryVector =
                toPgVector(queryEmbedding);

        return jdbcTemplate.query(
                """
                SELECT
                    id,
                    content,
                    1 - (embedding <=> ?::vector) AS similarity
                FROM ai_document
                WHERE metadata ->> 'service' = ?
                ORDER BY embedding <=> ?::vector
                LIMIT ?
                """,
                (rs, rowNum) -> new VectorSearchResult(
                        rs.getLong("id"),
                        rs.getString("content"),
                        rs.getDouble("similarity")
                ),
                queryVector,
                service,
                queryVector,
                topK
        );
    }
}