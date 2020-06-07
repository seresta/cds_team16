package client

import tornadofx.*

class LoginController : Controller() {
    val loginView: LoginView by inject()
    val mainView: MainView by inject()

    fun showLoginView() {
        mainView.replaceWith(loginView)
    }

    fun showMainView() {
        loginView.replaceWith(mainView)
    }

    fun tryLogin(id: String, pw: String) {
        //CM Login Api

        val loginResult: Int = 0
        if (loginResult != 0) {
            showMainView()
        } else {
            //loginView.errorText.text = "로그인에 실패하였습니다."
            //loginView.errorText.fill = c("#FF0000")
        }
    }
}