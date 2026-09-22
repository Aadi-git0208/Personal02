package com.apexcare.chat.realtime;

import com.apexcare.chat.dto.MessageResponse;
import org.springframework.stereotype.Component;

@Component
public class NoOpChatRealtimeNotifier implements ChatRealtimeNotifier {

    @Override
    public void messageCreated(MessageResponse message) {
        // REST-only MVP. Replace with a STOMP/WebSocket publisher later.
    }
}
