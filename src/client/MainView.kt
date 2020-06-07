package client

import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.text.Font
import javafx.scene.text.Text
import tornadofx.*
import javax.swing.text.html.StyleSheet

class MainView : View() {
    override val root = borderpane {
        setPrefSize(400.0, 600.0)
        style {
            backgroundColor += c("#455a64")
        }

        center

        bottom = drawer {

            item("친구") {

            }
            item("채팅") {
            }
            item("정보") {

            }

        }
    }
}