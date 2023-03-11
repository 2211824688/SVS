package com.kob.backend.service.impl.pk;

import com.kob.backend.consumer.WebSocketServer;
import com.kob.backend.consumer.utils.Game;
import com.kob.backend.service.pk.ReceiveBotMoveService;
import org.springframework.stereotype.Service;

@Service
public class ReceiveBotMoveServiceImpl implements ReceiveBotMoveService {
    @Override
    public String receiveBotMove(Integer userId, Integer direction) {
        System.out.println("receive bot move: " + userId + " " + direction);
        if (WebSocketServer.users.get(userId) != null) {
            Game game = WebSocketServer.users.get(userId).game;

            if (game != null) {
                if (game.getPlayer1().getId().equals(userId)) {
                    game.setNextStepUser1(direction);
                } else if (game.getPlayer2().getId().equals(userId)) {
                    game.setNextStepUser2(direction);
                }
            }
        }
        return "receive bot move success";
    }
}
