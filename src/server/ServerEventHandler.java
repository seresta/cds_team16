package server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kr.ac.konkuk.ccslab.cm.event.CMDummyEvent;
import kr.ac.konkuk.ccslab.cm.event.CMEvent;
import kr.ac.konkuk.ccslab.cm.event.CMInterestEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler;
import kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMDBManager;
import kr.ac.konkuk.ccslab.cm.stub.CMServerStub;

public class ServerEventHandler implements CMAppEventHandler {
    private CMServerStub m_serverStub;
    private Map<Integer, String[]> roomList;

    private int roomNum;

    public ServerEventHandler(CMServerStub serverStub) {
        m_serverStub = serverStub;
        roomNum = 0;
        roomList = new HashMap<Integer, String[]>();
    }

    @Override
    public void processEvent(CMEvent cme) {
        switch (cme.getType()) {
            case CMInfo.CM_SESSION_EVENT:
                processSessionEvent(cme);
                break;

            case CMInfo.CM_DUMMY_EVENT:
                processDummyEvent(cme);
                break;
		/*case CMInfo.CM_SNS_EVENT:
			processSNSEvent(cme);
			break;
			*/
            case CMInfo.CM_INTEREST_EVENT:
                processInterestEvent(cme);
                break;

            default:
                return;
        }

    }

    private void processSessionEvent(CMEvent cme) {
        CMConfigurationInfo confInfo = m_serverStub.getCMInfo().getConfigurationInfo();
        CMSessionEvent se = (CMSessionEvent) cme;
        switch (se.getID()) {
            case CMSessionEvent.LOGIN:
                System.out.println("[" + se.getUserName() + "] requests login.");
                if (confInfo.isLoginScheme()) {
                    boolean ret = CMDBManager.authenticateUser(se.getUserName(), se.getPassword(),
                            m_serverStub.getCMInfo());
                    if (!ret) {
                        System.out.println("[" + se.getUserName() + "] authentication fails!");
                        m_serverStub.replyEvent(se, 0);
                    } else {
                        System.out.println("[" + se.getUserName() + "] authentication succeeded.");
                        m_serverStub.replyEvent(se, 1);
                    }
                }
                break;
            default:
                return;
        }
    }

    //ä�ù� ����, ä�ù� ����
    private void processDummyEvent(CMEvent cme) {
        CMDummyEvent due = (CMDummyEvent) cme;
        String[] dueInfo = due.getDummyInfo().split(":");
        String msgInfo = dueInfo[0];
        String sender = due.getSender();


        boolean isExist = true;

        // create Room
        if (msgInfo.equals("createRoom")) {

            String target = dueInfo[1];
            boolean isSecret = Boolean.parseBoolean(dueInfo[2]);


            //this.roomList.put(this.roomNum, sender + target + isSecret);
            System.out.println("roomID(" + this.roomNum + ")" + sender + "," + target + "," + isSecret);

            String[] putInfo = {sender, target, Boolean.toString(isSecret)};
            this.roomList.put(this.roomNum, putInfo);
            due.setDummyInfo("1:" + this.roomNum);

            if (!isSecret) {
                CMDummyEvent due2 = new CMDummyEvent();
                due2.setSender("SERVER");
                due2.setDummyInfo("enterRoom" + ":" + this.roomNum);
                m_serverStub.send(due2, sender);
            }
            this.roomNum++;
            due.setID(222);
            m_serverStub.send(due, sender);
        }

        // exit Room
        else if (msgInfo.equals("exitRoom")) {
            int roomID = Integer.parseInt(dueInfo[1]);

            //비밀채팅, 상대방이 나가서 방이 없는 경우
            if (this.roomList.get(roomID) == null)
                due.setDummyInfo("exitSecret");

                //일반채팅, 비밀채팅 상대방이 있는경우
            else {

                String[] roomInfo = this.roomList.get(roomID);
                String[] chatName = {roomInfo[0], roomInfo[1]};
                boolean isSecret = Boolean.parseBoolean(roomInfo[2]);

                String target;
                if (chatName[0].equals(sender))
                    target = chatName[1];
                else
                    target = chatName[0];


                //비밀채팅방
                if (isSecret) {
                    this.roomList.remove(roomID);
                    due.setDummyInfo("removeSecret");

                    m_serverStub.send(due, target);
                }
                //일반채팅방
                else {
                    due.setDummyInfo("exitRoom");
                }
            }
            due.setID(111);
            m_serverStub.send(due, sender);
        }

        //request Room List
        else if (msgInfo.equals("requestRoomList")) {
            String existRoom = "";
            for (int i = 0; i < this.roomNum; i++) {

                String[] roomInfo = this.roomList.get(i);

                if (roomInfo == null) ;
                else {

                    if (roomInfo[0].equals(sender))
                        existRoom = existRoom + i + "," + roomInfo[1] + "," + roomInfo[2] + ";";
                    else if (roomInfo[1].equals(sender))
                        existRoom = existRoom + i + "," + roomInfo[0] + "," + roomInfo[2] + ";";
                }
            }
            if (existRoom.equals("")) {
                due.setID(444);
                due.setDummyInfo("noRoom:");
            } else {
                due.setID(444);
                due.setDummyInfo("room:" + existRoom);
            }
            m_serverStub.send(due, sender);
        }

        //enter Room
        else if (msgInfo.equals("enterRoom")) {
            int roomID = Integer.parseInt(dueInfo[1]);

            String[] roomInfo = this.roomList.get(roomID);

            if (roomInfo == null) {
                due.setDummyInfo("noRoom:");
            } else {
                String[] clientNames = {roomInfo[0], roomInfo[1]};
                boolean isSecret = Boolean.parseBoolean(roomInfo[2]);

                if (clientNames[0].equals(due.getSender())) {
                    due.setDummyInfo("room:" + isSecret);
                } else if (clientNames[1].equals(due.getSender())) {
                    due.setDummyInfo("room:" + isSecret);
                } else
                    due.setDummyInfo("noRoom:");

                if (!isSecret) {
                    CMDummyEvent due2 = new CMDummyEvent();
                    due2.setSender("SERVER");
                    due2.setDummyInfo("enterRoom" + ":" + roomID);
                    m_serverStub.send(due2, sender);
                }
            }
            due.setID(333);
            m_serverStub.send(due, sender);
        } else if (msgInfo.equals("getRoomID")) {
            String target = dueInfo[1];
            String isSecret = dueInfo[2];
            boolean Exist = false;
            int findRoomID;
            for (int i = 0; i < this.roomNum; i++) {

                String[] roomInfo = this.roomList.get(i);
                if (roomInfo == null) ;
                else {
                    if (isSecret.equals(roomInfo[2]) && (target.equals(roomInfo[0]) || target.equals(roomInfo[1]))) {
                        Exist = true;
                        findRoomID = i;
                        due.setDummyInfo("1:" + findRoomID);
                        break;
                    }
                }
            }
            if (!Exist)
                due.setDummyInfo("0:");

            due.setID(555);
            m_serverStub.send(due, sender);
        }
    }

    //ä�ó��� ������� �ʿ�
    private void processInterestEvent(CMEvent cme) {
        CMInterestEvent ie = (CMInterestEvent) cme;
        switch (ie.getID()) {
            case CMInterestEvent.USER_TALK:
                String[] ieInfo = ie.getTalk().split(":");
                String sender = ie.getSender();
                int roomID = Integer.parseInt(ieInfo[0]);
                String message = ieInfo[1];

                System.out.println("interest event room id : " + roomID);
                String[] roomInfo = this.roomList.get(roomID);

                //비밀방 없어지고 혼자남으면
                if (roomInfo == null) {
                    ie.setID(CMInterestEvent.USER_LEAVE);
                    m_serverStub.send(ie, sender);
                } else {
                    String[] clientNames = {roomInfo[0], roomInfo[1]};
                    boolean isSecret = Boolean.parseBoolean(roomInfo[2]);
                    ie.setTalk(roomID + ":" + sender + ":" + message);

                    if (clientNames[0].equals(sender)) {
                        this.m_serverStub.send(ie, clientNames[1]);
                    } else {
                        this.m_serverStub.send(ie, clientNames[0]);
                    }
                    this.m_serverStub.send(ie, sender);

                    if (isSecret) ;
                    else {
                        // roomID(String) , sender:msg
                        CMDBManager.queryInsertSNSContent(ieInfo[0], ie.getTalk(), 0, 0, 0, m_serverStub.getCMInfo());
                    }

                }
            default:
                return;
        }
    }


}