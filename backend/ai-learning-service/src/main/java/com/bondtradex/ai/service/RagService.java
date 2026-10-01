package com.bondtradex.ai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import com.bondtradex.ai.model.IncidentRequest;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagService {

    private final VectorStore vectorStore;
    private final ChatClient chatClient;
    private final ChunkingService chunkingService;
    private final TokenChunkingService tokenChunkingService;

    public RagService(
            VectorStore vectorStore,
            ChatClient chatClient,
            ChunkingService chunkingService,
            TokenChunkingService tokenChunkingService
    ) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClient;
        this.chunkingService = chunkingService;
        this.tokenChunkingService = tokenChunkingService;
    }

    public String ask(String question) {

        SearchRequest searchRequest =
                SearchRequest.builder()
                        .query(question)
                        .topK(3)
                        .similarityThreshold(0.7)
                        .build();

        List<Document> documents = vectorStore.similaritySearch(searchRequest);

        documents.forEach(document -> {
            System.out.println(
                    "Retrieved: " + document.getText()
            );

            System.out.println(
                    "Score: " + document.getScore()
            );
        });

        String context = documents.stream().map(Document::getText).collect(Collectors.joining("\n\n"));

        String prompt = """
        You are a question-answering assistant.

        Answer the QUESTION using only information explicitly present
        in the CONTEXT.

        Important rules:
        - Use relevant information from the context even if other
          context entries are unrelated.
        - Do not add facts that are not present in the context.
        - If the context contains information that directly answers
          the question, provide that answer.
        - Only if none of the context contains information relevant
          enough to answer the question, respond exactly:
          "I don't have enough information in the provided context."

        CONTEXT:
        %s

        QUESTION:
        %s
        """.formatted(
                context,
                question
        );
        System.out.println("Final prompt is : "+prompt);
        return chatClient.prompt().user(prompt).call().content();
    }

/*    public void ingestIncident(IncidentRequest request) {

        List<String> chunks = chunkingService.chunk(request.content(), 200, 50);

        List<Document> documents = new java.util.ArrayList<>();

        for (int i = 0; i < chunks.size(); i++) {

            Document document = new Document(chunks.get(i), java.util.Map.of(
                                    "incidentId", request.incidentId(),
                                    "service", request.service(),
                                    "type", "incident",
                                    "chunkIndex", i));
            documents.add(document);
        }
        vectorStore.add(documents);
    }*/

    public void ingestIncident(IncidentRequest request) {

        Document incidentDocument =
                new Document(
                        request.content(),
                        Map.of(
                                "incidentId", request.incidentId(),
                                "service", request.service(),
                                "type", "incident"
                        )
                );

        List<Document> chunks = tokenChunkingService.chunk(incidentDocument);

        vectorStore.add(chunks);
    }
}