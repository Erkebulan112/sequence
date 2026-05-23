package sequence.chat.rest;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import sequence.chat.dto.ChatMessage;

@Controller
public class ChatController {

    @MessageMapping("/chat/{lobbyId}")
    @SendTo("/topic/messages/{lobbyId}")
    public ChatMessage sendMessage(@DestinationVariable String lobbyId, ChatMessage message) {
        return message;
    }

}
