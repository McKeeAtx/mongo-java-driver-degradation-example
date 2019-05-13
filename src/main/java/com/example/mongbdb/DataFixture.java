package com.example.mongbdb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class DataFixture {

    public static void main( String[] args ) {
        MongoClient client = Configuration.createClient(false);
        MongoDatabase db = client.getDatabase("test");

        db.createCollection("test");
        MongoCollection<Document> collection = db.getCollection("test");

        for (int i = 0; i < 10000; i++) {
            Document doc = new Document("name" + i, "MongoDB")
                    .append("type", "database")
                    .append("index", i);
            collection.insertOne(doc);
        }
    }

}