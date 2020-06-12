package client

import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.geometry.Side
import javafx.scene.image.Image
import javafx.scene.text.Font
import javafx.scene.text.Text
import tornadofx.*
import tornadofx.WizardStyles.Companion.graphic
import javax.swing.text.html.StyleSheet

class MainView : View() {
    val friendList = observableListOf<String>("친구1", "친구2", "친구3")

    override val root = borderpane {
        setPrefSize(400.0, 600.0)
        style {
            backgroundColor += c("#455a64")
        }

        center = drawer(side = Side.BOTTOM) {

            item("친구", expanded = true) {
                //icon = Image("/assets/UI_Mark_2011.png")
                listview(friendList) {

                    contextmenu {
                        item("채팅 시작").action {

                        }
                    }
                }
            }
            item("채팅") {
            }
            item("정보") {

            }

        }
    }
}