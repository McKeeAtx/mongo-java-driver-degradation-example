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

    static MongoClient createClient() {

        Configuration config = new Configuration();

        return MongoClients.create(MongoClientSettings.builder()
                .applyToConnectionPoolSettings(new Block<ConnectionPoolSettings.Builder>() {
                    @Override
                    public void apply(ConnectionPoolSettings.Builder builder) {
                        builder.applySettings(
                                ConnectionPoolSettings.builder()
                                        .addConnectionPoolListener(new ConnectionHistogramListener())
                                        .minSize(minConnections)
                                        .maxSize(maxConnections)
                                        .maxWaitQueueSize(maxWaitQueueSize)
                                        .build());
                    }
                })
                .readPreference(ReadPreference.nearest())
                .applyToSocketSettings(new Block<SocketSettings.Builder>() {
                    @Override
                    public void apply(SocketSettings.Builder builder) {
                        builder.applySettings(
                                SocketSettings.builder()
                                        // The amount of time we will wait when establishing a new connection
                                        .connectTimeout(connectionTimeoutInMs, TimeUnit.MILLISECONDS)
                                        // The amount of time we will wait when performing queries before giving up
                                        // We want this to be long enough to handle the long queries,
                                        // but short enought that if a server dies, it doesn't back up the waiting threads too much
                                        .readTimeout(readTimeoutInSeconds, TimeUnit.SECONDS)
                                        .build()
                        );
                    }
                })

                .applyToClusterSettings(new Block<ClusterSettings.Builder>() {
                    @Override
                    public void apply(ClusterSettings.Builder builder) {
                        builder.applySettings(
                                ClusterSettings.builder()
                                        .mode(mode)
                                        .hosts(servers)
                                        .maxWaitQueueSize(maxWaitQueueSize)
                                        .build());
                    }
                })
                .build());
    }
}
