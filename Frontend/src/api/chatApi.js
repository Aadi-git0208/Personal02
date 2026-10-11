import { apiRequest } from "./client";

export function createConversation(payload) {
    return apiRequest("/api/chat/conversations", {
        method: "POST",
        body: payload
    });
}

export function listConversations(query) {
    return apiRequest("/api/chat/conversations", {
        method: "GET",
        query
    });
}

export function getConversation(conversationId) {
    return apiRequest(`/api/chat/conversations/${conversationId}`, {
        method: "GET"
    });
}

export function closeConversation(conversationId) {
    return apiRequest(`/api/chat/conversations/${conversationId}/close`, {
        method: "PATCH"
    });
}

export function reopenConversation(conversationId) {
    return apiRequest(`/api/chat/conversations/${conversationId}/reopen`, {
        method: "PATCH"
    });
}

export function listMessages(conversationId, query) {
    return apiRequest(`/api/chat/conversations/${conversationId}/messages`, {
        method: "GET",
        query
    });
}

export function sendMessage(conversationId, body) {
    return apiRequest(`/api/chat/conversations/${conversationId}/messages`, {
        method: "POST",
        body: { body }
    });
}

export function markConversationRead(conversationId) {
    return apiRequest(`/api/chat/conversations/${conversationId}/read`, {
        method: "PATCH"
    });
}
