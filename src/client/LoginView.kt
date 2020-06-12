package client

import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.PasswordField
import javafx.scene.text.Text
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub
import tornadofx.*

class LoginView : View() {
    private val clientController: ClientController by inject()
    val loginFieldView: LoginFieldView by inject()
    val registerFieldView: RegisterFieldView by inject()

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
    private val clientController: ClientController by inject()
    private val loginModel = object : ViewModel() {
        val id = bind { SimpleStringProperty() }
        val password = bind { SimpleStringProperty() }
    }

    var errorText: Text by singleAssign()

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
                                clientController.tryLogin(
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
    private val clientController: ClientController by inject()

    private val registerModel = object : ViewModel() {
        val id = bind { SimpleStringProperty() }
        val password = bind { SimpleStringProperty() }
    }

    var repasswd: PasswordField by singleAssign()

    override val root = hbox {
        padding = Insets(0.0, 30.0, 0.0, 30.0)
        alignment = Pos.CENTER
        form {
            fieldset {
                field {
                    textfield(registerModel.id) {
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
                    passwordfield(registerModel.password) {
                        maxWidth = 300.0
                        maxHeight = 25.0
                        required()
                        promptText = "비밀번호"
                        validator {
                            if ((it?.length ?: 0) > 10) error("비밀번호 길이는 10자 이하여야 합니다") else null
                        }
                    }
                    repasswd = passwordfield {
                        promptText = "비밀번호 재입력"
                    }
                }
                field {
                    button("회원가입") {
                        shortcut("Enter")
                        style {
                            fontSize = 12.px
                        }
                        prefWidth = 300.0
                        alignment = Pos.CENTER
                        action {
                            registerModel.commit {
                                clientController.tryRegister(
                                        registerModel.id.value,
                                        registerModel.password.value
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

