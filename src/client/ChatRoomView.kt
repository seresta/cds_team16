package client

import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import tornadofx.*

class ChatRoomView : View() {
    private val chatModel = object : ViewModel() {
        val chat = bind { SimpleStringProperty() }
    }

    val clientController: ClientController by inject()
    var chatList = observableListOf<String>()

    val roomId: Int by param()

    override val root = borderpane {
        setPrefSize(400.0, 600.0)
        top = hbox {

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
                        chatList.add(chatModel.chat.value)
                        clientController.sendChat(roomId, chatModel.chat.value)
                    }
                }
            }
        }
    }

}