package com.bondtradex.ai.service;

import com.bondtradex.ai.model.DocumentRequest;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SpringVectorStoreService {

    private final VectorStore vectorStore;

    public SpringVectorStoreService(
            VectorStore vectorStore
    ) {
        this.vectorStore = vectorStore;
    }

    public void addDocument(DocumentRequest request) {

        Document document = new Document(
                request.content(),
                Map.of(
                        "technology", request.technology(),
                        "service", request.service(),
                        "environment", request.environment()
                )
        );

        vectorStore.add(List.of(document));
    }

    public List<Document> search(
            String query,
            int topK
    ) {

        SearchRequest searchRequest =
                SearchRequest.builder()
                        .query(query)
                        .topK(topK)
                        .build();

        return vectorStore.similaritySearch(
                searchRequest
        );
    }

    public List<Document> searchByService(
            String query,
            String service,
            int topK
    ) {

        SearchRequest searchRequest =
                SearchRequest.builder()
                        .query(query)
                        .topK(topK)
                        .filterExpression(
                                "service == '" + service + "'"
                        )
                        .build();

        return vectorStore.similaritySearch(
                searchRequest
        );
    }
}