package com.example.mongbdb;

import com.mongodb.event.*;

public class NoOpListener implements ConnectionPoolListener {
    @Override
    public void connectionPoolOpened(ConnectionPoolOpenedEvent connectionPoolOpenedEvent) {

    }

    @Override
    public void connectionPoolClosed(ConnectionPoolClosedEvent connectionPoolClosedEvent) {

    }

    @Override
    public void connectionCheckedOut(ConnectionCheckedOutEvent connectionCheckedOutEvent) {

    }

    @Override
    public void connectionCheckedIn(ConnectionCheckedInEvent connectionCheckedInEvent) {

    }

    @Override
    public void waitQueueEntered(ConnectionPoolWaitQueueEnteredEvent connectionPoolWaitQueueEnteredEvent) {

    }

    @Override
    public void waitQueueExited(ConnectionPoolWaitQueueExitedEvent connectionPoolWaitQueueExitedEvent) {

    }

    @Override
    public void connectionAdded(ConnectionAddedEvent connectionAddedEvent) {

    }

    @Override
    public void connectionRemoved(ConnectionRemovedEvent connectionRemovedEvent) {

    }
}
