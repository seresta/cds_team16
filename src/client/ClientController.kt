package client

import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.event.ActionEvent
import javafx.event.EventHandler
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


class ClientController : Controller(), CMAppEventHandler {
    val loginView: LoginView by inject()
    val loginFieldView: LoginFieldView by inject()
    val mainView: MainView by inject()
    val friendView: FriendView by inject()

    fun showLoginView() {
        mainView.replaceWith(loginView)
    }

    fun showMainView() {
        getFriendList()
        loginView.replaceWith(mainView)
    }

    fun showChatRoomView(roomId: Int) {
        val chatRoomView = find<ChatRoomView>(mapOf(ChatRoomView::roomId to roomId))
        mainView.replaceWith(chatRoomView)
    }

    fun tryLogin(id: String, pw: String) {
        runAsync {
            clientStub.loginCM(id, pw)
        }
    }

    fun tryLogout() {
        clientStub.logoutCM()

        showLoginView()
    }

    fun tryRegister(id: String, pw: String) {
        runAsync {
            clientStub.registerUser(id, pw)
        }
    }

    fun getFriendList() = clientStub.requestFriendsList()

    fun tryNormarChatStart() {

    }

    fun trySecretChatStart() {

    }

    fun getRoomId(target: String, isSecret: Boolean): Int {
        return 0
    }

    fun createRoom(target: String, isSecret: Boolean) {
        val interInfo: CMInteractionInfo = clientStub.cmInfo.interactionInfo
        val myself = interInfo.myself

        val strSend = "1:$target:${if (isSecret) 1 else 0}"

        val due = CMDummyEvent()
        due.sender = myself.name
        due.dummyInfo = strSend

        val replyEvent = clientStub.sendrecv(due, "SERVER", CMInfo.CM_DUMMY_EVENT, 222, 1000) as CMDummyEvent?
        if (replyEvent == null) System.err.println("The reply event is null!") else {
            val dueInfo = replyEvent.dummyInfo.split(":".toRegex()).toTypedArray()
            if (dueInfo[0] == "1") {
                val roomId = dueInfo[1].toInt()
                chatRoomMap[roomId] = arrayListOf<String>()
            } else if (dueInfo[0] == "0") println("This is not valid friend name!!")


        }
    }

    fun sendChat(roomId: Int, chat: String) {
        val userEvent = CMUserEvent()

        userEvent.setEventField(CMInfo.CM_INT, "room_id", roomId.toString())
        userEvent.setEventField(CMInfo.CM_STR, "chat", chat)

        clientStub.send(userEvent, "SERVER")
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

                }
                else if (cmSessionEvent.isValidUser == 1) {
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

                friendView.friendList.setAll(friendList)
                friendView.friendNumText.text = friendList.size.toString()
            }
        }
    }

    private fun processInterestEvent(cmInterestEvent: CMInterestEvent) {

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