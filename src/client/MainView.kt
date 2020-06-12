package client

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.geometry.Side
import javafx.scene.Parent
import javafx.scene.image.Image
import javafx.scene.text.Font
import javafx.scene.text.Text
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub
import tornadofx.*
import tornadofx.WizardStyles.Companion.graphic
import javax.swing.text.html.StyleSheet

class MainView : View() {
    val friendList = observableListOf<String>()
    val chatList = observableListOf<String>()

    private val clientController: ClientController by inject()
    val friendView = find<FriendView>(mapOf(FriendView::friendList to friendList))
    val chatView = find<ChatView>(mapOf(ChatView::chatList to chatList))
    val infoView: InfoView by inject()

    override val root = borderpane {
        setPrefSize(400.0, 600.0)
        style {
            backgroundColor += c("#455a64")
        }

        center = friendView.root

        bottom = listmenu {
            orientation = Orientation.HORIZONTAL
            prefHeight = 50.0
            prefWidth = 400.0

            item("친구", graphic = FontAwesomeIconView(FontAwesomeIcon.USERS)) {
                activeItem = this
                prefWidth = 133.3
                whenSelected {
                    clientController.getFriendList()
                    center.replaceWith(friendView.root)
                }

                contextmenu {
                    item("채팅 시작").action {

                    }
                }
            }
            item("채팅", graphic = FontAwesomeIconView(FontAwesomeIcon.WECHAT)) {
                prefWidth = 133.3

                whenSelected {
                    center.replaceWith(chatView.root)
                }
            }
            item("정보", graphic = FontAwesomeIconView(FontAwesomeIcon.INFO)) {
                prefWidth = 133.3

                whenSelected {
                    center.replaceWith(infoView.root)
                }

            }

        }
    }
}

class FriendView : View() {
    val friendList: ObservableList<String> by param()

    override val root = borderpane {
        top = vbox {
            label("친구")
            textfield { 
                promptText = "이름 검색"
            }
        }
        center = listview(friendList) {  }
    }
}

class ChatView : View() {
    val chatList: ObservableList<String> by param()

    override val root = borderpane {
        top = vbox {
            label("친구")
            textfield {
                promptText = "채팅방 검색"
            }
        }
        center = listview(chatList) {  }
    }
}

class InfoView : View() {
    override val root = label("건국대학교 컴퓨터공학과 조승현")
}

