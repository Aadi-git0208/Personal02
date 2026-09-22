package com.apexcare.chat.realtime;

import com.apexcare.chat.dto.MessageResponse;

/**
 * Hook for a future WebSocket/STOMP layer. REST remains the source of truth;
 * a later broker can implement this and push the same {@link MessageResponse}.
 */
public interface ChatRealtimeNotifier {

    void messageCreated(MessageResponse message);
}
