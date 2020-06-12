package client

import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.text.Font
import javafx.scene.text.Text
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub
import tornadofx.*
import javax.swing.text.html.StyleSheet

private val clientStub = CMClientStub()
private val clientEventHandler = ClientEventHandler()

class LoginView : View() {

    private val loginFieldView: LoginFieldView by inject()
    private val registerFieldView: RegisterFieldView by inject()

    init {
        clientStub.appEventHandler = clientEventHandler
    }

    override val root = borderpane {
        setPrefSize(400.0, 600.0)
        style {
            backgroundColor += c("#455a64")
        }

        top = hbox {
            padding = Insets(80.0, 50.0, 50.0, 50.0)
            alignment = Pos.CENTER
            imageview("/assets/UI_Mark_2011.png") {
                fitHeight = 140.0
                fitWidth = 140.0
            }
        }

        center = loginFieldView.root

        bottom = vbox {
            paddingAll = 20.0
            alignment = Pos.CENTER
            hyperlink("회원 가입") {
                action {
                    openInternalWindow(registerFieldView)
                }
            }
            label("건국대학교 2020 협동분산시스템 | 컴퓨터공학과 조승현")
        }
    }
}

class LoginFieldView : View() {
    private val loginModel = object : ViewModel() {
        val id = bind { SimpleStringProperty() }
        val password = bind { SimpleStringProperty() }
    }

    var errorText: Text by singleAssign()

    private val loginController = find<LoginController>(mapOf(LoginController::clientStub to clientStub))

    override val root = hbox {
        padding = Insets(0.0, 30.0, 0.0, 30.0)
        alignment = Pos.CENTER
        form {
            fieldset {
                field {
                    textfield(loginModel.id) {
                        maxWidth = 300.0
                        maxHeight = 25.0
                        required()
                        whenDocked { requestFocus() }
                        promptText = "아이디"
                        validator {
                            if ((it?.length ?: 0) > 10) error("ID 길이는 10자 이하여야 합니다") else null
                        }
                    }
                }
                field {
                    passwordfield(loginModel.password) {
                        maxWidth = 300.0
                        maxHeight = 25.0
                        required()
                        promptText = "비밀번호"
                        validator {
                            if ((it?.length ?: 0) > 10) error("비밀번호 길이는 10자 이하여야 합니다") else null
                        }
                    }
                }
                field {
                    button("로그인") {
                        shortcut("Enter")
                        style {
                            fontSize = 12.px
                        }
                        prefWidth = 300.0
                        alignment = Pos.CENTER
                        action {
                            loginModel.commit {
                                loginController.tryLogin(
                                        loginModel.id.value,
                                        loginModel.password.value
                                )
                            }
                        }
                    }
                }
                field {
                    errorText = text()
                }
            }
        }
    }

    fun clear() {
        loginModel.id.value = ""
        loginModel.password.value = ""
    }
}

class RegisterFieldView : View() {
    override val root = borderpane { }
}

