package client

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import tornadofx.*
import kotlin.system.exitProcess

class ChatRoomView : View() {
    private val chatModel = object : ViewModel() {
        val chat = bind { SimpleStringProperty() }
    }

    val clientController: ClientController by inject()
    var chatList = observableListOf<String>()

    val roomId: Int by param()

    override val root = borderpane {
        setPrefSize(400.0, 600.0)
        top = menubar {
            menu("앱") {
                item("로그아웃", graphic = FontAwesomeIconView(FontAwesomeIcon.ARROW_LEFT)).action {
                    clientController.tryLogout(this@ChatRoomView)
                }
                item("종료", graphic = FontAwesomeIconView(FontAwesomeIcon.POWER_OFF)).action {
                    Platform.exit()
                    exitProcess(0)
                }
            }
            menu("채팅방") {
                item("뒤로 가기", graphic = FontAwesomeIconView(FontAwesomeIcon.BACKWARD)).action {
                    clientController.exitChatRoomView(roomId)
                }
                item("방 나가기", graphic = FontAwesomeIconView(FontAwesomeIcon.BACKWARD)).action {
                    clientController.exitRoom(roomId)
                }
            }
        }
        center = listview(chatList) {

        }

        bottom = hbox {
            prefHeight = 50.0
            textfield(chatModel.chat) {
                useMaxHeight = true
                prefWidth = 300.0
            }
            button("전송") {
                alignment = Pos.CENTER_RIGHT
                shortcut("Enter")
                action {
                    chatModel.commit {
                        //chatList.add(chatModel.chat.value)
                        clientController.sendChat(roomId, chatModel.chat.value)
                    }
                }
            }
        }
    }

}