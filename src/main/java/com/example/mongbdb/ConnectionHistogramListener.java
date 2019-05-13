package com.example.mongbdb;

import com.mongodb.ServerAddress;
import com.mongodb.diagnostics.logging.Logger;
import com.mongodb.diagnostics.logging.Loggers;
import com.mongodb.event.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

class ConnectionHistogramListener implements ConnectionPoolListener {

    private static final Logger LOGGER = Loggers.getLogger("histogram");

    class ConnectionHistogram {
        Map<ServerAddress, AtomicInteger> connectedThreads = new HashMap<ServerAddress, AtomicInteger>();
        Map<ServerAddress, AtomicInteger> waitingThreads = new HashMap<ServerAddress, AtomicInteger>();

        public void connectionAdded(ConnectionAddedEvent event) {
            ServerAddress address = event.getConnectionId().getServerId().getAddress();
            if ( ! connectedThreads.containsKey(address)) {
                connectedThreads.put(address, new AtomicInteger(0));
                waitingThreads.put(address, new AtomicInteger(0));
            }
        }

        public void connectionCheckedOut(ConnectionCheckedOutEvent event) {
            ServerAddress address = event.getConnectionId().getServerId().getAddress();
            connectedThreads.get(address).getAndIncrement();
        }

        public void connectionCheckedIn(ConnectionCheckedInEvent event) {
            ServerAddress address = event.getConnectionId().getServerId().getAddress();
            connectedThreads.get(address).getAndDecrement();
        }

        public void waitQueueEntered(ConnectionPoolWaitQueueEnteredEvent event) {
            ServerAddress address = event.getServerId().getAddress();
            waitingThreads.get(address).getAndIncrement();
            logHistogram(event);
        }

        public void waitQueueExited(ConnectionPoolWaitQueueExitedEvent event) {
            ServerAddress address = event.getServerId().getAddress();
            waitingThreads.get(address).getAndDecrement();
        }

        private void logHistogram(ConnectionPoolWaitQueueEnteredEvent event) {
            LOGGER.trace(String.format("Connection attempt to server '%s'.", event.getServerId().getAddress()));
            String line = "";
            for (ServerAddress address : connectedThreads.keySet()) {
                int connected = connectedThreads.get(address).get();
                int waiting = waitingThreads.get(address).get();
                line += String.format("%s[open: %2d waiting: %2d] / ", address, connected, waiting);
            }
            LOGGER.trace("\t" + line);
        }

    }

    private ConnectionHistogram distribution = new ConnectionHistogram();

    ConnectionHistogramListener() {
        this.distribution = new ConnectionHistogram();
    }

    @Override
    public void connectionPoolOpened(ConnectionPoolOpenedEvent event) {
    }

    @Override
    public void connectionPoolClosed(ConnectionPoolClosedEvent event) {
    }

    @Override
    public void connectionCheckedOut(ConnectionCheckedOutEvent event) {
        distribution.connectionCheckedOut(event);
    }

    @Override
    public void connectionCheckedIn(ConnectionCheckedInEvent event) {
        distribution.connectionCheckedIn(event);
    }

    @Override
    public void waitQueueEntered(ConnectionPoolWaitQueueEnteredEvent event) {
        distribution.waitQueueEntered(event);
    }

    @Override
    public void waitQueueExited(ConnectionPoolWaitQueueExitedEvent event) {
        distribution.waitQueueExited(event);
    }

    @Override
    public void connectionAdded(ConnectionAddedEvent event) {
        distribution.connectionAdded(event);
    }

    @Override
    public void connectionRemoved(ConnectionRemovedEvent event) {
    }

}
