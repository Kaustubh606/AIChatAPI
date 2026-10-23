package com.aichatapp.rag;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HRPolicyLoader {
    private final VectorStore vectorStore;
    @Value("classpath:/rag-documents/Eazybytes_HR_Policies.pdf")
    private Resource policiyPDF;


    public HRPolicyLoader(VectorStore vectorStore){
        this.vectorStore = vectorStore;
    }

    @PostConstruct
    public void loadHrPolicies(){
        TikaDocumentReader tikaReader = new TikaDocumentReader(policiyPDF);
        List<Document> docs = tikaReader.get();

        TextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(200)
                .withMaxNumChunks(500)
                .build();

        vectorStore.add(splitter.split(docs));
    }
}
