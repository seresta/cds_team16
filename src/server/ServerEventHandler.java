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
    private Map<Integer, String> room;

    private int roomNum;

    public ServerEventHandler(CMServerStub serverStub) {
        m_serverStub = serverStub;
        roomNum = 0;
        room = new HashMap<Integer, String>();
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
        private void processDummyEvent (CMEvent cme){
            CMDummyEvent due = (CMDummyEvent) cme;
            String[] dueInfo = due.getDummyInfo().split(":");
            String sender = due.getSender();
            boolean isExist = true;

            // create Room
            if (dueInfo[0].equals("1")) {
                //ģ�� �˻�
                String target = dueInfo[1];
                String isSecret = dueInfo[2];
                ArrayList<String> friendsList = CMDBManager.queryGetFriendsList(sender, m_serverStub.getCMInfo());
                for (String friend : friendsList)
                    if (friend.equals(target))
                        isExist = true;

                if (isExist) {
                    //this.roomList.put(this.roomNum, sender+","+target);
                    System.out.println("roomID(" + this.roomNum + ")" + sender + "," + target + "," + isSecret);
                    //this.roomState.put(this.roomNum, "10"+isSecret);

                    this.room.put(this.roomNum, sender + "," + target + "/" + "10" + isSecret);

                    due.setDummyInfo("1:" + this.roomNum);
                    this.roomNum++;
                } else {
                    due.setDummyInfo("0:");
                }
                due.setID(222);
                m_serverStub.send(due, due.getSender());
            }

            // exit Room
            else if (dueInfo[0].equals("2")) {
                int roomID = Integer.parseInt(dueInfo[1]);

                //���ä��, ������ ������ ���� ���� ���
                //if(this.roomList.get(roomID)==null)
                if (this.room.get(roomID) == null) {
                    due.setDummyInfo("1:exit secret room");
                    due.setID(111);
                }
                //�Ϲ�ä��, ���ä�� ������ �ִ°��
                else {
                    //String[] chatName = this.roomList.get(roomID).split(",");
                    //String rState =this.roomState.get(roomID);
                    String[] roomSplit = this.room.get(roomID).split("/");
                    String[] chatName = roomSplit[0].split(",");
                    String rState = roomSplit[1];

                    String target;
                    String targetState;
                    if (chatName[0].equals(sender)) {
                        target = chatName[1];
                        targetState = rState.substring(1, 2);
                    } else {
                        target = chatName[0];
                        targetState = rState.substring(0, 1);
                    }
                    //���ä�ù�
                    if (rState.substring(2).equals("1")) {
                        //this.roomList.remove(roomID);
                        //this.roomState.remove(roomID);
                        this.room.remove(roomID);
                        due.setDummyInfo("1:remove secret room");
                        //ä�� ������� ���� ���� ����
                        m_serverStub.send(due, target);

                        due.setID(111);
                    }
                    //�Ϲ�ä�ù�
                    else {
                        String nState;
                        if (chatName[0].equals(sender)) {
                            nState = "0" + rState.substring(1);
                        } else {
                            nState = rState.substring(0, 1) + "0" + rState.substring(2);
                        }
                        //this.roomState.put(roomID, nState);
                        this.room.put(roomID, chatName[0] + "," + chatName[1] + "/" + nState);

                        due.setDummyInfo("1:exit room");
                        if (targetState.equals("1"))
                            m_serverStub.send(due, target);
                        due.setID(111);
                    }
                }
                m_serverStub.send(due, sender);
            }

            //request Room List
            else if (dueInfo[0].equals("3")) {
                String existRoom = null;
                for (int i = 0; i < this.roomNum; i++) {
                    //String Names = this.roomList.get(i);

                    String[] roomSplit = this.room.get(i).split("/");
                    String Names = roomSplit[0];

                    if (Names == null) ;
                    else {
                        String[] clientName = Names.split(",");
                        if (clientName[0].equals(sender))
                            existRoom = existRoom + i + "," + clientName[1] + ";";
                        else if (clientName[1].equals(sender))
                            existRoom = existRoom + i + "," + clientName[0] + ";";
                    }
                }
                if (existRoom == null) {
                    due.setID(444);
                    due.setDummyInfo("0:");
                } else {
                    due.setID(444);
                    due.setDummyInfo("1:" + existRoom);
                }
                m_serverStub.send(due, sender);
            }

            //enter Room  -- normal room : ���� ��ȭ���� �����ؾ��ϴ� ���� �ʿ�
            else if (dueInfo[0].equals("4")) {
                int rNum = Integer.parseInt(dueInfo[1]);
                //String clients = this.roomList.get(rNum);

                String[] roomSplit = this.room.get(rNum).split("/");
                String clients = roomSplit[0];

                if (clients == null) {
                    due.setDummyInfo("0:");
                } else {
                    String[] clientNames = clients.split(",");
                    //String rState = this.roomState.get(rNum);
                    String rState = roomSplit[1];

                    String nState;
                    if (clientNames[0].equals(due.getSender())) {
                        nState = "1" + rState.substring(1);
                        //this.roomState.put(rNum, nState);
                        this.room.put(rNum, clientNames[0] + "," + clientNames[1] + "/" + nState);
                        //test
                        System.out.println("roomId(" + rNum + ")" + "state:" + nState);
                        due.setDummyInfo("1:");
                    } else if (clientNames[1].equals(due.getSender())) {
                        nState = rState.substring(0, 1) + "1" + rState.substring(2);
                        //test
                        System.out.println("roomId(" + rNum + ")" + "state:" + nState);
                        //this.roomState.put(rNum, nState);
                        this.room.put(rNum, clientNames[0] + "," + clientNames[1] + "/" + nState);
                        due.setDummyInfo("1:");
                    } else
                        due.setDummyInfo("0:");
                }
                due.setID(333);
                m_serverStub.send(due, due.getSender());
            }
        }

        //ä�ó��� ������� �ʿ�
        private void processInterestEvent (CMEvent cme){
            CMInterestEvent ie = (CMInterestEvent) cme;
            switch (ie.getID()) {
                case CMInterestEvent.USER_TALK:
                    String[] ieInfo = ie.getTalk().split(":");
                    String sender = ie.getSender();
                    int roomID = Integer.parseInt(ieInfo[0]);

                    String[] roomSplit = this.room.get(roomID).split("/");
                    String[] clientNames = roomSplit[0].split(",");
                    String roomState = roomSplit[1];

                    //String roomState = this.roomState.get(roomID);

                    //���ä�ù� ������
                    if (roomState == null) {
                        ie.setTalk(null);
                        ie.setID(CMInterestEvent.USER_LEAVE);
                        m_serverStub.send(ie, sender);
                    } else {
                        //String[] clientNames = this.roomList.get(roomID).split(",");
                        ie.setTalk(sender + ":" + ieInfo[1]);

                        if (clientNames[0].equals(sender)) {
                            if (roomState.substring(1, 2).equals("1")) {
                                this.m_serverStub.send(ie, clientNames[1]);
                            }
                        } else if (clientNames[1].equals(sender)) {
                            if (roomState.substring(0, 1).equals("1")) {
                                this.m_serverStub.send(ie, clientNames[0]);
                            }
                        }
                        //ä�ó��� ����
                        if (roomState.substring(2) == "1") ;
                        else {
                            CMDBManager.queryInsertSNSContent(ieInfo[0], ie.getTalk(), 0, 0, 0, m_serverStub.getCMInfo());
                        }
                    }

                default:
                    return;
            }
        }


    }
