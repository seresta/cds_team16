package client

import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.scene.Scene
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.util.Duration
import kr.ac.konkuk.ccslab.cm.event.*
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler
import kr.ac.konkuk.ccslab.cm.info.CMInfo
import kr.ac.konkuk.ccslab.cm.info.CMInteractionInfo
import tornadofx.*
import java.util.*
import kotlin.concurrent.thread


class ClientController : Controller(), CMAppEventHandler {
    val loginView: LoginView by inject()
    val loginFieldView: LoginFieldView by inject()
    val mainView: MainView by inject()
    val friendView: FriendView by inject()

    fun showLoginView(view: View) {
        view.replaceWith(loginView)
    }

    fun showMainView() {
        thread {
            getFriendList()
        }
        thread {
            getRoomList()
        }
        loginView.replaceWith(mainView)
    }

    fun showChatRoomView(roomId: Int) {
        val chatRoomView = find<ChatRoomView>(mapOf(ChatRoomView::roomId to roomId))
        mainView.replaceWith(chatRoomView)
    }

    fun exitChatRoomView(roomId: Int) {
        val chatRoomView = find<ChatRoomView>(mapOf(ChatRoomView::roomId to roomId))
        chatRoomView.replaceWith(mainView)
    }

    fun exitRoom(roomID: Int) {
        thread {
            val interInfo: CMInteractionInfo = clientStub.cmInfo.interactionInfo
            val myself = interInfo.myself
            val strSend = "exitRoom:$roomID"
            println("Exit the Chatting Room")
            var due = CMDummyEvent()
            due.sender = myself.name
            due.dummyInfo = strSend
            val replyEvent = clientStub.sendrecv(due, "SERVER", CMInfo.CM_DUMMY_EVENT, 111, 1000) as CMDummyEvent?
            if (replyEvent == null) {
                System.err.println("The reply event is null!")
            } else {
                println("방 나가기 정상 처리")
            }
        }
        runLater {
            val chatRoomView = find<ChatRoomView>(mapOf(ChatRoomView::roomId to roomID))
            mainView.chatList.remove(mainView.chatList.find { it.contains(roomID.toString()) })
            chatRoomView.replaceWith(mainView)
        }
    }

    fun tryLogin(id: String, pw: String) {
        runAsync {
            clientStub.loginCM(id, pw)
        }
    }

    fun tryLogout(view: View) {
        clientStub.logoutCM()

        showLoginView(view)
    }

    fun tryRegister(id: String, pw: String) {
        runAsync {
            clientStub.registerUser(id, pw)
        }
    }

    fun tryAddFriend(name: String) {
        clientStub.addNewFriend(name)
        getFriendList()
    }

    fun tryDeleteFriend(name: String) {
        clientStub.removeFriend(name)
        getFriendList()
    }

    fun getFriendList() = clientStub.requestFriendsList()

    fun sendChat(roomId: Int, msg: String) {
        val myName = clientStub.cmInfo.interactionInfo.myself.name

        val intEvent = CMInterestEvent()
        intEvent.sender = myName
        intEvent.id = CMInterestEvent.USER_TALK
        intEvent.talk = "$roomId:$msg"
        clientStub.send(intEvent, "SERVER")

        println("채팅방 roomid : $roomId")
    }

    fun getRoomList() {
        val interInfo: CMInteractionInfo = clientStub.cmInfo.interactionInfo
        val myself = interInfo.myself
        val strSend = "requestRoomList:"

        println("====== Request Room List")

        val due = CMDummyEvent()
        due.sender = myself.name
        due.dummyInfo = strSend

        val replyEvent = clientStub.sendrecv(due, "SERVER", CMInfo.CM_DUMMY_EVENT, 444, 10000) as CMDummyEvent?

        if (replyEvent == null)
            System.err.println("The reply event is null!")
        else {
            val chatView = find<ChatView>()

            val dueInfo = replyEvent.dummyInfo.split(":".toRegex()).toTypedArray()
            if (dueInfo[0] == "room") {
                val rooms = dueInfo[1]
                val roomList = rooms.split(";".toRegex()).toTypedArray()
                roomList.forEach {
                    println(it)
                    val roomInfo = it.split(",".toRegex()).toTypedArray()
                    val roomId = roomInfo[0]
                    if (roomId.isNotEmpty()) {
                        chatRoomMap[roomId.toInt()] = arrayListOf<String>()
                        requestMsgList(roomId.toInt())

                        runLater {
                            chatView.chatList.clear()
                            chatView.chatList.add("${if (roomInfo[2] == "false") "[일반]" else "[비밀]"}${roomInfo[1]}:${roomInfo[0]}")
                        }
                    }
                }
            } else if (dueInfo[0] == "noRoom") println("There is no valid room!!")
        }
    }

    fun getRoomId(target: String, isSecret: Boolean): Int {
        val interInfo: CMInteractionInfo = clientStub.cmInfo.interactionInfo
        val myself = interInfo.myself
        val strSend = "getRoomID:$target:$isSecret"
        val roomID: Int

        println("Getting RoomID")

        val due = CMDummyEvent()
        due.sender = myself.name
        due.dummyInfo = strSend
        val replyEvent = clientStub.sendrecv(due, "SERVER", CMInfo.CM_DUMMY_EVENT, 555, 1000) as CMDummyEvent?

        if (replyEvent == null) {
            System.err.println("The reply event is null!")
            return -1
        }
        else {
            val dueInfo = replyEvent.dummyInfo.split(":".toRegex()).toTypedArray()
            if (dueInfo[0] == "1") {
                println("Start Chatting with$target")
                roomID = dueInfo[1].toInt()
                println("received roomid from getroomid : $roomID")
                return roomID
            } else if (dueInfo[0] == "0") {
                println("There is no valid room!!")
                return 99
            }
        }

        return 100
    }

    fun requestMsgList(roomID: Int) {
        val rID = roomID.toString()
        clientStub.requestSNSContent(rID, 0)
    }

    fun createRoom(target: String, isSecret: Boolean) {
        val interInfo: CMInteractionInfo = clientStub.cmInfo.interactionInfo
        val myself = interInfo.myself
        val strSend = "createRoom:$target:$isSecret"
        val roomID: Int
        println("====== create Chatting room")
        println("Chatting Request To :$target")
        var due = CMDummyEvent()
        due.sender = myself.name
        due.dummyInfo = strSend
        val replyEvent = clientStub.sendrecv(due, "SERVER", CMInfo.CM_DUMMY_EVENT, 222, 1000) as CMDummyEvent?
        if (replyEvent == null) System.err.println("The reply event is null!") else {
            val dueInfo = replyEvent.dummyInfo.split(":".toRegex()).toTypedArray()
            if (dueInfo[0] == "1") {
                println("Start Chatting with$target")
                roomID = dueInfo[1].toInt()

                chatRoomMap[roomID] = arrayListOf()
            } else if (dueInfo[0] == "0") println("This is not valid friend name!!")
        }
    }

    override fun processEvent(cmEvent: CMEvent) {
        when (cmEvent.type) {
            CMInfo.CM_SESSION_EVENT -> processSessionEvent(cmEvent as CMSessionEvent)
            CMInfo.CM_DUMMY_EVENT -> processDummyEvent(cmEvent as CMDummyEvent)
            CMInfo.CM_SNS_EVENT -> processSNSEvent(cmEvent as CMSNSEvent)
            CMInfo.CM_INTEREST_EVENT -> processInterestEvent(cmEvent as CMInterestEvent)
            else -> return
        }
    }

    private fun processSessionEvent(cmSessionEvent: CMSessionEvent) {
        when (cmSessionEvent.id) {
            CMSessionEvent.LOGIN_ACK -> {
                if (cmSessionEvent.isValidUser == 0) {
                    runLater {
                        loginFieldView.errorText.text = "로그인에 실패하였습니다."
                        loginFieldView.errorText.fill = c("#FF0000")
                    }

                } else if (cmSessionEvent.isValidUser == 1) {
                    runLater {
                        showMainView()
                    }

                }
            }
            CMSessionEvent.REGISTER_USER_ACK -> {
                val result = cmSessionEvent.returnCode
                val message = if (result == 1) "등록에 성공하였습니다" else "등록에 실패하였습니다"
                runLater {
                    //find<PopupFragment>(mapOf(PopupFragment::message to message)).openModal()
                    Toast.makeText(primaryStage, message, 500, 0, 0)
                }

            }
        }
    }

    private fun processDummyEvent(cmDummyEvent: CMDummyEvent) {

    }

    private fun processSNSEvent(cmSNSEvent: CMSNSEvent) {
        when (cmSNSEvent.id) {
            CMSNSEvent.RESPONSE_FRIEND_LIST -> {
                val friendList = cmSNSEvent.friendList

                runLater {
                    friendView.friendList.setAll(friendList)
                    friendView.friendNumText.text = friendList.size.toString()
                }
            }
            CMSNSEvent.CONTENT_DOWNLOAD -> {
                val seInfo = cmSNSEvent.message.split(":")
                val roomID = seInfo[0].toInt()
                chatRoomMap[roomID] = arrayListOf(seInfo[1] + ":" + seInfo[2])
                val chatRoomView = find<ChatRoomView>(mapOf(ChatRoomView::roomId to roomID))
                chatRoomView.chatList.add(seInfo[1] + ":" + seInfo[2])
            }
            CMSNSEvent.CONTENT_DOWNLOAD_END -> {
                println("download end")
            }
        }
    }

    private fun processInterestEvent(cmInterestEvent: CMInterestEvent) {
        val ie = cmInterestEvent
        val ieInfo = ie.talk.split(":".toRegex()).toTypedArray()
        val interInfo: CMInteractionInfo = clientStub.cmInfo.interactionInfo
        val myself = interInfo.myself
        println("Process InterestEvent")
        println(ie.talk)
        var roomID: Int
        when (ie.id) {
            CMInterestEvent.USER_TALK -> {
                roomID = ieInfo[0].toInt()
                val sender = ieInfo[1]
                val message = ieInfo[2]


                val chatRoomView = find<ChatRoomView>(mapOf(ChatRoomView::roomId to roomID))
                runLater {
                    chatRoomView.chatList.add("$sender:$message")
                }
            }
            CMInterestEvent.USER_LEAVE -> {
                roomID = ieInfo[0].toInt()
                println("$roomID : None User")
            }
            else -> return
        }
    }
}

object Toast {
    fun makeText(stage: Stage, message: String, displayTime: Int = 3000, fadeInDelay: Int = 500, fadeOutDelay: Int = 500, size: Double = 15.0, opacity: Double = 5.0) {
        val toastStage = Stage()
        toastStage.initOwner(stage)
        toastStage.isResizable = false
        toastStage.initStyle(StageStyle.TRANSPARENT)

        val text = Text(message)
        text.font = Font.font("Verdana", size)
        text.fill = Color.RED

        val root = StackPane(text)
        root.style = "-fx-background-radius: 20; -fx-background-color: rgba(0, 0, 0, 0.2); -fx-padding: 50px;"
        root.opacity = opacity

        val scene = Scene(root)
        scene.fill = Color.TRANSPARENT
        toastStage.scene = scene
        toastStage.show()

        val fadeInTimeline = Timeline()
        val fadeInKey1 =
                KeyFrame(Duration.millis(fadeInDelay.toDouble()), KeyValue(toastStage.scene.root.opacityProperty(), 1))
        fadeInTimeline.keyFrames.add(fadeInKey1)
        fadeInTimeline.setOnFinished {
            Thread {
                try {
                    Thread.sleep(displayTime.toLong())
                } catch (e: InterruptedException) {
                    // TODO Auto-generated catch block
                    e.printStackTrace()
                }

                val fadeOutTimeline = Timeline()
                val fadeOutKey1 =
                        KeyFrame(
                                Duration.millis(fadeOutDelay.toDouble()),
                                KeyValue(toastStage.scene.root.opacityProperty(), 0)
                        )
                fadeOutTimeline.keyFrames.add(fadeOutKey1)
                fadeOutTimeline.setOnFinished { toastStage.close() }
                runLater {
                    fadeOutTimeline.play()
                }

            }.start()
        }
        fadeInTimeline.play()
    }
}