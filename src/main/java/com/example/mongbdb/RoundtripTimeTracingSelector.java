package com.example.mongbdb;

import com.mongodb.connection.ClusterDescription;
import com.mongodb.connection.ServerDescription;
import com.mongodb.diagnostics.logging.Logger;
import com.mongodb.diagnostics.logging.Loggers;
import com.mongodb.selector.ServerSelector;
import com.sun.security.ntlm.Server;

import java.util.List;
import java.util.stream.Collectors;

public class CustomServerSelector implements ServerSelector {

    private static final Logger LOGGER = Loggers.getLogger("histogram");

    @Override
    public List<ServerDescription> select(ClusterDescription clusterDescription) {
        LOGGER.trace("Roundtrip times (ms): " + toString(clusterDescription));
        return clusterDescription.getServerDescriptions();
    }

    private String toString(ClusterDescription clusterDescription) {
        return clusterDescription.getServerDescriptions().stream().map(serverDescription ->  toString(serverDescription)).collect(Collectors.joining(" / "));
    }

    private String toString(ServerDescription serverDescription) {
        return String.format("%s=%s", serverDescription.getAddress(), serverDescription.getRoundTripTimeNanos() / 1000 / 1000);
    }

}
