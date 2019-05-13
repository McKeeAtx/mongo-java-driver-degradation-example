package com.example.mongbdb;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Consumer {

    public static void main( String[] args ) throws ExecutionException, InterruptedException, IOException {
        MongoClient client = Configuration.createClient(true);
        MongoDatabase db = client.getDatabase("test");
        final MongoCollection<Document> customers = db.getCollection("test");
        ExecutorService executorService = Executors.newFixedThreadPool(6);

        for (;;) {
            executorService.submit(() -> {
                FindIterable<Document> documents = customers.find();
                documents.into(new LinkedList<>());
            });
            Thread.sleep(1000);
        }
    }

}
