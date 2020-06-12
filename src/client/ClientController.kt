package client

import javafx.scene.control.Label
import kr.ac.konkuk.ccslab.cm.event.*
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler
import kr.ac.konkuk.ccslab.cm.info.CMInfo
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub
import tornadofx.*

class ClientController : Controller(), CMAppEventHandler {
    val loginView: LoginView by inject()
    val loginFieldView: LoginFieldView by inject()
    val mainView: MainView by inject()

    fun showLoginView() {
        mainView.replaceWith(loginView)
    }

    fun showMainView() {
        loginView.replaceWith(mainView)
    }

    fun tryLogin(id: String, pw: String) {
        runAsync {
            clientStub.loginCM(id, pw)
        }
    }

    fun tryRegister(id: String, pw: String) {
        runAsync {
            clientStub.registerUser(id, pw)
        }
    }

    fun getFriendList() = clientStub.requestFriendsList()

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
                find<PopupFragment>(mapOf(PopupFragment::message to message)).openModal()
            }
        }
    }

    private fun processDummyEvent(cmDummyEvent: CMDummyEvent) {

    }

    private fun processSNSEvent(cmSNSEvent: CMSNSEvent) {
        when (cmSNSEvent.id) {
            CMSNSEvent.RESPONSE_FRIEND_LIST -> {
                val friendList = cmSNSEvent.friendList

                mainView.friendList.addAll(friendList)
            }
        }
    }

    private fun processInterestEvent(cmInterestEvent: CMInterestEvent) {

    }
}

class PopupFragment : Fragment() {
    val message: Label by param()

    override val root = message
}