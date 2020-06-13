package client

import kr.ac.konkuk.ccslab.cm.stub.CMClientStub
import tornadofx.*

val clientStub = CMClientStub()
val chatRoomMap = hashMapOf<Int, ArrayList<String>>()

class ClientApp : App(LoginView::class) {
    val clientController: ClientController by inject()

    init {
        reloadStylesheetsOnFocus()
        clientStub.appEventHandler = clientController
        clientStub.startCM()
    }
}


