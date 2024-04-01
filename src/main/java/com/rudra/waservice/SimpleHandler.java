package com.rudra.waservice;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class SimpleHandler implements RequestHandler<HttpServletRequest, ResponseEntity<String>> {

    private static ApplicationContext applicationContext;

    @Override
    public ResponseEntity<String> handleRequest(HttpServletRequest request, Context context) {
        // Initialize the Spring application context if it's not already initialized
        if (applicationContext == null) {
            applicationContext = WebApplicationContextUtils.getWebApplicationContext(request.getServletContext());
        }

        // Retrieve the WebhookController bean from the Spring application context
        WebhookController webhookController = applicationContext.getBean(WebhookController.class);

        // Call the handleWebhook method of the WebhookController bean
        return webhookController.handleWebhook(request);

//        Integer s = request.size;

//        return ResponseEntity.ok("Post");
    }


}