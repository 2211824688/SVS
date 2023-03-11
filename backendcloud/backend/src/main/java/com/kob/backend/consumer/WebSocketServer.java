package com.kob.backend.consumer;

import com.alibaba.fastjson.JSONObject;
import com.kob.backend.consumer.utils.Game;
import com.kob.backend.consumer.utils.JwtAuthentication;
import com.kob.backend.mapper.BotMapper;
import com.kob.backend.mapper.RecordMapper;
import com.kob.backend.mapper.UserMapper;
import com.kob.backend.pojo.Bot;
import com.kob.backend.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@ServerEndpoint("/websocket/{token}")
public class WebSocketServer {

    public static final ConcurrentHashMap<Integer, WebSocketServer> users = new ConcurrentHashMap<>();

    private User user;

    private Session session = null;

    public static UserMapper userMapper;
    public static RecordMapper recordMapper;

    public static BotMapper botMapper;
    public static RestTemplate restTemplate;

    public Game game = null;
    private static final String addPlayerUrl = "http://127.0.0.1:3001/player/add/";
    private static final String removePlayerUrl = "http://127.0.0.1:3001/player/remove/";

    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        WebSocketServer.userMapper = userMapper;
    }

    @Autowired
    public void setRecordMapper(RecordMapper recordMapper) {
        WebSocketServer.recordMapper = recordMapper;
    }

    @Autowired
    public void setBotMapper(BotMapper botMapper) {
        WebSocketServer.botMapper = botMapper;
    }

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        WebSocketServer.restTemplate = restTemplate;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) throws IOException {
        // 建立连接
        this.session = session;
        System.out.println("Connected!");
        Integer userId = JwtAuthentication.getUserId(token);
        this.user = userMapper.selectById(userId);

        if (user != null) {
            users.put(userId, this);
        } else {
            this.session.close();
        }
        System.out.println(users.toString());
    }

    @OnClose
    public void onClose() {
        // 关闭连接
        System.out.println("disConnected!");
        if (this.user != null) {
            users.remove(user.getId());
        }
    }

    public static void startGame(Integer user1Id, Integer user1BotId, Integer user2Id, Integer user2BotId) {
        User user1 = userMapper.selectById(user1Id), user2 = userMapper.selectById(user2Id);
        Bot bot1 = botMapper.selectById(user1BotId), bot2 = botMapper.selectById(user2BotId);

        Game game = new Game(
                13,
                14,
                10,
                user1.getId(),
                bot1,
                user2.getId(),
                bot2
        );
        game.createMap();
        if (users.get(user1.getId()) != null) {
            users.get(user1.getId()).game = game;
        }
        if (users.get(user2.getId()) != null) {
            users.get(user2.getId()).game = game;
        }

        game.start();

        JSONObject respGame = new JSONObject();
        respGame.put("user1_id", game.getPlayer1().getId());
        respGame.put("user1_sx", game.getPlayer1().getSx());
        respGame.put("user1_sy", game.getPlayer1().getSy());

        respGame.put("user2_id", game.getPlayer2().getId());
        respGame.put("user2_sx", game.getPlayer2().getSx());
        respGame.put("user2_sy", game.getPlayer2().getSy());

        respGame.put("map", game.getMap());


        JSONObject respA = new JSONObject();
        respA.put("event", "start-matching");
        respA.put("opponent_username", user2.getUsername());
        respA.put("opponent_photo", user2.getPhoto());
        respA.put("game", respGame);

        if (users.get(user1.getId()) != null)
            users.get(user1.getId()).sendMessage(respA.toJSONString());

        JSONObject respB = new JSONObject();
        respB.put("event", "start-matching");
        respB.put("opponent_username", user1.getUsername());
        respB.put("opponent_photo", user1.getPhoto());
        respB.put("game", respGame);

        if (users.get(user2.getId()) != null)
            users.get(user2.getId()).sendMessage(respB.toJSONString());
    }

    private void startMatching(Integer botId) {
        System.out.println("start matching");
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("user_id", this.user.getId().toString());
        data.add("rating", this.user.getRating().toString());
        data.add("bot_id", botId.toString());
        restTemplate.postForObject(addPlayerUrl, data, String.class);
    }

    private void stopMatching() {
        System.out.println("stop matching");
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("user_id", this.user.getId().toString());
        restTemplate.postForObject(removePlayerUrl, data, String.class);
    }

    private void move(int direction) {
        if (game.getPlayer1().getId().equals(user.getId())) {
            if (game.getPlayer1().getBotId().equals(-1))       // 玩家操作时接受前端输入，否则屏蔽玩家输入，接受ai输入
                game.setNextStepUser1(direction);
        } else if (game.getPlayer2().getId().equals(user.getId())){
            if (game.getPlayer2().getBotId().equals(-1))
                game.setNextStepUser2(direction);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {    // 当作路由
        // 从Client接收消息
        System.out.println("receive message.");
        JSONObject data = JSONObject.parseObject(message);
        String event = data.getString("event");
        if ("start-matching".equals(event)) {
            startMatching(data.getInteger("bot_id"));
        } else if ("stop-matching".equals(event)){
            stopMatching();
        } else if ("move".equals(event)) {
            move(data.getInteger("direction"));
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    public void sendMessage(String message) {
        synchronized (this.session) {
            try {
                this.session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
