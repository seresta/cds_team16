package client

import kr.ac.konkuk.ccslab.cm.stub.CMClientStub
import tornadofx.*

class LoginController : Controller() {
    val loginView: LoginView by inject()
    val loginFieldView: LoginFieldView by inject()
    val mainView: MainView by inject()

    val clientStub: CMClientStub by param()

    fun showLoginView() {
        mainView.replaceWith(loginView)
    }

    fun showMainView() {
        loginView.replaceWith(mainView)
    }

    fun tryLogin(id: String, pw: String) {
        runAsync {
            clientStub.loginCM(id, pw)
        } ui {
            loginResult ->
                if (loginResult)  {
                    showMainView()
                } else {
                    loginFieldView.errorText.text = "로그인에 실패하였습니다."
                    loginFieldView.errorText.fill = c("#FF0000")
                }
        }
    }
}