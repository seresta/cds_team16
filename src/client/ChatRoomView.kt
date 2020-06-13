package client

import javafx.beans.property.SimpleStringProperty
import tornadofx.*

class ChatRoomView : View() {
    private val chatModel = object : ViewModel() {
        val chat = bind { SimpleStringProperty() }
    }

    val clientController: ClientController by inject()
    val chatList = arrayListOf<String>().asObservable()

    val roomId: Int by param()

    override val root = borderpane {
        top = hbox {

        }
        center = listview(chatList)

        bottom = hbox {
            textfield(chatModel.chat) {

            }
            button("전송") {
                shortcut("Enter")
                action {
                    chatModel.commit {
                        clientController.sendChat(roomId, chatModel.chat.value)
                    }
                }
            }
        }
    }

}