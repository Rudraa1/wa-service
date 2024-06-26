package com.rudra.waservice;//package com.rudra.wamsgapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;

@SpringBootApplication
public class WhatsAppEchoBotApplication {

    private final String token = System.getenv("WHATSAPP_TOKEN");
    private final String verifyToken = System.getenv("VERIFY_TOKEN");

    public static void main(String[] args) {
        SpringApplication.run(WhatsAppEchoBotApplication.class, args);
    }
}

@RestController
class WebhookController {

    private final String token;
    private final String verifyToken;

    public WebhookController() {
        this.token = System.getenv("WHATSAPP_TOKEN");
        this.verifyToken = System.getenv("VERIFY_TOKEN");
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(HttpServletRequest request) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode requestBody = objectMapper.readTree(request.getInputStream());

            // Check if it's a WhatsApp text message payload
            if (requestBody.has("object") && requestBody.get("object").asText().equals("page")) {
                JsonNode entry = requestBody.get("entry").get(0);
                JsonNode changes = entry.get("changes").get(0);
                JsonNode value = changes.get("value");
                String phoneNumberId = value.get("metadata").get("phone_number_id").asText();
                JsonNode message = value.get("messages").get(0);
                String from = message.get("from").asText();
                String msgBody = message.get("text").get("body").asText();

                // Acknowledge the message
                sendMessage(phoneNumberId, from, "Ack: " + msgBody);
            }

//            String requestBodyString = requestBody.toString();
//            return ResponseEntity.ok(requestBodyString);
            return ResponseEntity.ok().build();


        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }



//    public ResponseEntity<String> verifyWebhook( String hubMode,
//                                                 String hubVerifyToken,
//                                                 String hubChallenge) {
//        if ("subscribe".equals(hubMode) && verifyToken.equals(hubVerifyToken)) {
//            System.out.println("WEBHOOK_VERIFIED" );
//            return ResponseEntity.ok(hubChallenge);
//        } else {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
//        }
//    }

    @GetMapping("/webhook")
    public void handleWebhook(Map<String, String> queryParams, HttpServletResponse response) {
        String mode = queryParams.get("hub.mode");
        String challenge = queryParams.get("hub.challenge");
        String token = queryParams.get("hub.verify_token");

        if (mode != null && token != null) {
            if (mode.equals("subscribe") && token.equals(verifyToken)) {
                response.setStatus(200);
                try {
                    response.getWriter().print(challenge);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                response.setStatus(403);
            }
        }
    }

    @GetMapping("/{name}")
    public String getName(@PathVariable("name") String name){
        return  "Hello there " + name + token;
    }

    private void sendMessage(String phoneNumberId, String to, String text) {
        RestTemplate restTemplate = new RestTemplate();
        String url = String.format("https://graph.facebook.com/v12.0/%s/messages?access_token=%s", phoneNumberId, token);

        restTemplate.postForEntity(url, new MessageRequest("whatsapp", to, new MessageText(text)),
                Void.class);
    }
}

class MessageRequest {

    private final String messagingProduct;
    private final String to;
    private final MessageText text;

    public MessageRequest(String messagingProduct, String to, MessageText text) {
        this.messagingProduct = messagingProduct;
        this.to = to;
        this.text = text;
    }

    public String getMessagingProduct() {
        return messagingProduct;
    }

    public String getTo() {
        return to;
    }

    public MessageText getText() {
        return text;
    }
}

class MessageText {

    private final String body;

    public MessageText(String body) {
        this.body = body;
    }

    public String getBody() {
        return body;
    }
}

