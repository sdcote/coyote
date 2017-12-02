package coyote.commons.network.http.wsc;

import static coyote.commons.network.http.wsc.WebSocketState.CLOSING;
import static coyote.commons.network.http.wsc.WebSocketState.CREATED;


class StateManager {
  enum CloseInitiator {
    NONE, SERVER, CLIENT
  }

  private WebSocketState state;
  private CloseInitiator closeInitr = CloseInitiator.NONE;




  public StateManager() {
    state = CREATED;
  }




  public void changeToClosing(final CloseInitiator closeInitiator) {
    state = CLOSING;

    // Set the close initiator only when it has not been set yet.
    if (closeInitr == CloseInitiator.NONE) {
      this.closeInitr = closeInitiator;
    }
  }




  public boolean getClosedByServer() {
    return closeInitr == CloseInitiator.SERVER;
  }




  public WebSocketState getState() {
    return state;
  }




  public void setState(final WebSocketState state) {
    this.state = state;
  }

}
