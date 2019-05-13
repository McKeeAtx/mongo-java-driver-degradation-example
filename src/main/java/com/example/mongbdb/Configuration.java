package com.example.mongbdb;

import com.mongodb.Block;
import com.mongodb.MongoClientSettings;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.connection.ClusterConnectionMode;
import com.mongodb.connection.ClusterSettings;
import com.mongodb.connection.ConnectionPoolSettings;
import com.mongodb.connection.SocketSettings;
import com.mongodb.selector.LatencyMinimizingServerSelector;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;

public class Configuration {


    private static ClusterConnectionMode mode = ClusterConnectionMode.MULTIPLE;

    private static int minConnections = 2;

    private static int maxConnections = 2;

    private static int maxWaitQueueSize = 6;

    private static int connectionTimeoutInMs =  500;

    private static int readTimeoutInSeconds = 45;


    static List<ServerAddress> servers = asList(
            new ServerAddress("127.0.0.1", 27017),
            new ServerAddress("127.0.0.1", 27018),
            new ServerAddress("127.0.0.1", 27019)
    );

    static MongoClient createClient(boolean withHistogramListener) {
        return MongoClients.create(MongoClientSettings.builder()
                .applyToConnectionPoolSettings(builder -> builder.applySettings(
                        ConnectionPoolSettings.builder()
                                .addConnectionPoolListener(withHistogramListener ? new ConnectionHistogramListener() : new NoOpListener())
                                .minSize(minConnections)
                                .maxSize(maxConnections)
                                .maxWaitQueueSize(maxWaitQueueSize)
                                .build()))
                .readPreference(ReadPreference.nearest())
                .applyToSocketSettings(builder -> builder.applySettings(
                        SocketSettings.builder()
                                .connectTimeout(connectionTimeoutInMs, TimeUnit.MILLISECONDS)
                                .readTimeout(readTimeoutInSeconds, TimeUnit.SECONDS)
                                .build()
                ))

                .applyToClusterSettings(builder -> builder.applySettings(
                        ClusterSettings.builder()
                                .mode(mode)
                                .hosts(servers)
                                .maxWaitQueueSize(maxWaitQueueSize)
                                .build()))
                .build());
    }
}
