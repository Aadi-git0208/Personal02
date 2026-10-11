package com.medicurex.order.client;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PharmacyInternalTokenInterceptor implements RequestInterceptor {

    private final String token;

    public PharmacyInternalTokenInterceptor(@Value("${app.pharmacy.internal-token:}") String token) {
        this.token = token;
    }

    @Override
    public void apply(RequestTemplate template) {
        if (token == null || token.isBlank()) {
            return;
        }
        if (template.url() != null && template.url().contains("/internal/")) {
            template.header("X-Internal-Token", token);
        }
    }
}
