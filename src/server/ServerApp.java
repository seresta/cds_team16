package server;

import kr.ac.konkuk.ccslab.cm.entity.CMServer;
import kr.ac.konkuk.ccslab.cm.stub.CMServerStub;

public class ServerApp {
	private CMServerStub m_serverStub;
	private ServerEventHandler m_eventHandler;

	public ServerApp()
	{
		m_serverStub = new CMServerStub();
		m_eventHandler = new ServerEventHandler(m_serverStub);

	}

	public CMServerStub getServerStub()
	{
		return m_serverStub;
	}

	public ServerEventHandler getServerEventHandler()
	{
		return m_eventHandler;
	}

	public static void main(String[] args) {
		ServerApp server = new ServerApp();
		CMServerStub cmStub = server.getServerStub();
		cmStub.setAppEventHandler(server.getServerEventHandler());
		cmStub.startCM();
	}

}
