package client;

import kr.ac.konkuk.ccslab.cm.event.CMEvent;
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;

public class ClientEventHandler implements CMAppEventHandler {
    private CMClientStub clientStub;
    private ClientApp client;

    @Override
    public void processEvent(CMEvent cmEvent) {

    }
}
