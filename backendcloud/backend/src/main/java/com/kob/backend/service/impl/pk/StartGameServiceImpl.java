package com.kob.backend.service.impl.pk;

import com.kob.backend.consumer.WebSocketServer;
import com.kob.backend.service.pk.StartGameService;
import org.springframework.stereotype.Service;

@Service
public class StartGameServiceImpl implements StartGameService {
    @Override
    public String startGame(Integer user1Id, Integer user1BotId, Integer user2Id, Integer user2BotId) {
        System.out.println("start game: " + user1Id + " " + user2Id);
        WebSocketServer.startGame(user1Id, user1BotId, user2Id, user2BotId);
        return "start game success";
    }
}
