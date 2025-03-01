package io.quarkus.websockets.next.test.telemetry.endpoints.ontextmessage;

import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocket;
import io.smallrye.mutiny.Uni;

@WebSocket(path = "/received-single-text-response-uni-text")
public class SingleTextReceived_UniTextResponse_Endpoint {

    @OnTextMessage
    public Uni<String> onMessage(String message) {
        return Uni.createFrom().item("echo 0: " + message);
    }

}
