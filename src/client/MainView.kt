package client

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.application.Platform
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.geometry.Side
import javafx.scene.Parent
import javafx.scene.image.Image
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.scene.text.Text
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub
import sun.font.FontFamily
import tornadofx.*
import tornadofx.WizardStyles.Companion.graphic
import javax.swing.text.html.StyleSheet
import kotlin.system.exitProcess

class MainView : View() {
    val chatList = observableListOf<String>()

    private val clientController: ClientController by inject()

    //val friendView = find<FriendView>(mapOf(FriendView::friendList to friendList))
    val friendView: FriendView by inject()
    val chatView = find<ChatView>(mapOf(ChatView::chatList to chatList))
    val infoView: InfoView by inject()

    override val root = borderpane {
        setPrefSize(400.0, 600.0)

        top = menubar {
            menu("앱") {
                item("로그아웃", graphic = FontAwesomeIconView(FontAwesomeIcon.ARROW_LEFT)).action {
                    clientController.tryLogout()
                }
                item("종료", graphic = FontAwesomeIconView(FontAwesomeIcon.POWER_OFF)).action {
                    Platform.exit()
                    exitProcess(0)
                }
            }
            menu("친구") {
                item("친구 추가", graphic = FontAwesomeIconView(FontAwesomeIcon.PLUS))
            }
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
    val friendList = observableListOf<String>()
    val clientController: ClientController by inject()

    var friendNumText: Text by singleAssign()

    override val root = borderpane {
        top = borderpane {
            padding = Insets(15.0, 30.0, 15.0, 30.0)
            left = label("친구") {
                style {
                    fontFamily = "나눔바른고딕"
                    fontWeight = FontWeight.BOLD
                }
            }
            right = button(graphic = FontAwesomeIconView(FontAwesomeIcon.PLUS))
            bottom = textfield {
                promptText = "이름 검색"
            }
        }
        center = vbox {
            hbox {
                label("친구 ")
                friendNumText = text()
            }
            listview(friendList) {
                cellFormat {
                    graphic = cache {
                        text(it) {
                            style {
                                fontSize = 20.px
                            }
                        }
                    }
                }
                contextmenu {
                    item("일반 채팅 하기").action {
                        selectedItem?.apply {
                            val roomId = clientController.getRoomId(selectedItem!!, false)
                            val filtered = chatRoomMap.map { it.key }.filter { it == roomId }
                            if (filtered.isEmpty()) {
                                clientController.createRoom(selectedItem!!, false)
                            }

                            clientController.showChatRoomView(roomId)
                        }

                    }
                    item("비밀 채팅 하기").action {
                        selectedItem?.apply {
                            val roomId = clientController.getRoomId(selectedItem!!, true)
                            val filtered = chatRoomMap.map { it.key }.filter { it == roomId }
                            if (filtered.isEmpty()) {
                                clientController.createRoom(selectedItem!!, true)
                            }

                            clientController.showChatRoomView(roomId)
                        }
                    }
                }
            }
        }
    }
}

class ChatView : View() {
    val chatList: ObservableList<String> by param()

    override val root = borderpane {
        top = borderpane {
            padding = Insets(15.0, 30.0, 15.0, 30.0)
            left = label("채팅") {
                style {
                    fontFamily = "나눔바른고딕"
                    fontWeight = FontWeight.BOLD
                }
            }
            right = button(graphic = FontAwesomeIconView(FontAwesomeIcon.PLUS))
            bottom = textfield {
                promptText = "채팅방 검색"
            }
        }
        center = listview(chatList) {
            cellFormat {
                graphic = cache {
                    text(it) {
                        style {
                            fontSize = 20.px
                        }
                    }
                }
            }
            contextmenu {
                item("채팅방 입장").action {
                    selectedItem?.apply {

                    }
                }
            }
        }
    }
}

class InfoView : View() {
    override val root = label("건국대학교 컴퓨터공학과 조승현")
}