package com.kob.backend.consumer.utils;

import com.alibaba.fastjson.JSONObject;
import com.kob.backend.consumer.WebSocketServer;
import com.kob.backend.pojo.Bot;
import com.kob.backend.pojo.Record;
import com.kob.backend.pojo.User;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class Game extends Thread {
    private final Integer rows;
    private final Integer cols;
    private final Integer innerWallsCount;

    private final int[][] g;        // map
    private static final int[] dx = { -1, 0, 1, 0, }, dy = { 0, 1, 0, -1 };
    private final Player player1, player2;

    private ReentrantLock lock = new ReentrantLock();
    private String status = "playing";  // playing -> finished
    private String loser = "";  // "all":平局, "user1": user1输, "user2": user2输

    private Integer nextStepUser1 = null;   // 玩家下一步操作, 0,1,2,3表示上下左右
    private Integer nextStepUser2 = null;

    private static final String addBotUrl = "http://127.0.0.1:3002/bot/add/";

    public Game(Integer rows,
                Integer cols,
                Integer innerWallsCount,
                Integer user1Id,
                Bot bot1,
                Integer user2Id,
                Bot bot2) {
        this.rows = rows;
        this.cols = cols;
        this.innerWallsCount = innerWallsCount;
        this.g = new int[rows][cols];

        Integer botId1 = -1, botId2 = -1;
        String botCode1 = "", botCode2 = "";
        if (bot1 != null) {
            botId1 = bot1.getId();
            botCode1 = bot1.getContent();
        }

        if (bot2 != null) {
            botId2 = bot2.getId();
            botCode2 = bot2.getContent();
        }


        player1 = new Player(user1Id, botId1, botCode1,rows-2, 1, new ArrayList<>());
        player2 = new Player(user2Id, botId2, botCode2,1, cols-2, new ArrayList<>());


    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public void setNextStepUser1(Integer nextStepUser1) {
        lock.lock();
        try {
            this.nextStepUser1 = nextStepUser1;
        } finally {
            lock.unlock();
        }
    }

    public void setNextStepUser2(Integer nextStepUser2) {
        lock.lock();
        try {
            this.nextStepUser2 = nextStepUser2;
        } finally {
            lock.unlock();
        }
    }

    public int[][] getMap() {
        return g;
    }

    private boolean checkConnectivity(int sx, int sy, int tx, int ty) {    //检查两个点是否连通，用来检查两条蛇的起点是否连通
        if (sx == tx && sy == ty) {
            return true;
        }
        g[sx][sy] = 1;

        for (int i = 0; i < 4; i++) {
            int x = sx + dx[i], y = sy + dy[i];
            if (x >= 0 && x < this.rows && y >= 0 && y < this.cols) {
                if (g[x][y] == 0 && checkConnectivity(x, y, tx, ty)) {
                    g[sx][sy] = 0;
                    return true;
                }
            }
        }

        g[sx][sy] = 0;
        return false;
    }

    private boolean draw() {   //  画地图
        //清空地图
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                g[i][j] = 0;
            }
        }

        for (int r = 0; r < rows; r++) {
            g[r][0] = g[r][cols-1] = 1;
        }

        for (int c = 0; c < cols; c++) {
            g[0][c] = g[rows-1][c] = 1;
        }

        //随机生成障碍物
        Random random = new Random();
        for (int i = 0; i < this.innerWallsCount; i++) {
            for (int j = 0; j < 1000; j++) {
                int r = random.nextInt(this.rows);
                int c = random.nextInt(this.cols);

                if (g[r][c] == 1 || g[this.rows-1-r][this.cols-1-c] == 1) {
                    continue;
                }

                if (r == this.rows-2 && c == 1 || r  == 1 && c == this.cols-2) {
                    continue;
                }

                g[r][c] = g[this.rows-1-r][this.cols-1-c] = 1;
                break;
            }
        }
        return checkConnectivity(this.rows-2, 1, 1, this.cols-2);
    }

    public void createMap() {
        for (int i = 0; i < 1000; i++) {
            if (draw())
                break;
        }
    }

    private String getInput(Player player) {    // 将当前局面信息，编码成字符串
        Player me, you;     //  地图#me.sx#me.sy#(我的操作序列)#you.sx#you.sy#(对手的操作序列)
        if (player1.getId().equals(player.getId())) {
            me = player1;
            you = player2;
        } else {
            me = player2;
            you = player1;
        }
        return getMapString() + "#" +
                me.getSx() + "#" +
                me.getSy() + "#(" +
                me.getStepsString() + ")#" +
                you.getSx() + "#" +
                you.getSy() + "#(" +
                you.getStepsString() + ")";
    }

    private void sendBotCode(Player player) {
        if (player.getBotId().equals(-1))   // 表示玩家操作，不需要代码
            return;

        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("user_id", player.getId().toString());
        data.add("bot_code", player.getBotCode());
        data.add("input", getInput(player));

        WebSocketServer.restTemplate.postForObject(addBotUrl, data, String.class);
    }

    private boolean nextStep() { //等待两名玩家的下一步操作
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        sendBotCode(player1);
        sendBotCode(player2);

        for (int i = 0; i < 50; i++) {
            try {
                Thread.sleep(100);
                lock.lock();
                try {
                    if (nextStepUser1 != null && nextStepUser2 != null) {
                        player1.getSteps().add(nextStepUser1);
                        player2.getSteps().add(nextStepUser2);
                        return true;
                    }
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private boolean isValid(List<Cell> cells1, List<Cell> cells2) {
        int n = cells1.size();
        Cell cell = cells1.get(n-1);
        if (g[cell.x][cell.y] == 1) {
            return false;
        }

        for (int i = 0; i < n-1; i++) {
            if (cell.x == cells1.get(i).x && cell.y == cells1.get(i).y) {
                return false;
            }
        }

        for (int i = 0; i < n-1; i++) {
            if (cell.x == cells2.get(i).x && cell.y == cells2.get(i).y) {
                return false;
            }
        }

        return true;
    }

    private void judge() { // 判断两名玩家下一步操作是否合法
        List<Cell> cells1 = player1.getCells();
        List<Cell> cells2 = player2.getCells();

        boolean player1IsValid = isValid(cells1, cells2);
        boolean player2IsValid = isValid(cells2, cells1);

        if (!player1IsValid || !player2IsValid) {
            status = "finished";

            if (!player1IsValid && !player2IsValid) {
                loser = "all";
            } else if (!player1IsValid) {
                loser = "user1";
            } else {
                loser = "user2";
            }
        }
    }

    private void sendAllMessage(String message) {
        if (WebSocketServer.users.get(player1.getId()) != null)
            WebSocketServer.users.get(player1.getId()).sendMessage(message);
        if (WebSocketServer.users.get(player2.getId()) != null)
            WebSocketServer.users.get(player2.getId()).sendMessage(message);
    }

    private void sendMove() {   //  向两个client传递移动信息
        lock.lock();
        try {
            JSONObject resp = new JSONObject();
            resp.put("event", "move");
            resp.put("user1_direction", nextStepUser1);
            resp.put("user2_direction", nextStepUser2);
            sendAllMessage(resp.toJSONString());
            nextStepUser1 = nextStepUser2 = null;      // 执行下一步操作前，将两名玩家的操作清空
        } finally {
            lock.unlock();
        }
    }

    private String getMapString() {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                res.append(g[i][j]);
            }
        }
        return res.toString();
    }

    private void updateUserRating(Player player, Integer rating) {
        User user = WebSocketServer.userMapper.selectById(player.getId());
        user.setRating(rating);
        WebSocketServer.userMapper.updateById(user);
    }

    private void saveToDatabase() {
        Integer rating1 = WebSocketServer.userMapper.selectById(player1.getId()).getRating();
        Integer rating2 = WebSocketServer.userMapper.selectById(player2.getId()).getRating();

        if ("user1".equals(loser)) {
            rating1 -= 2;
            rating2 += 5;
        } else if ("user2".equals(loser)) {
            rating2 -= 2;
            rating1 += 5;
        }

        updateUserRating(player1, rating1);
        updateUserRating(player2, rating2);

        Record record = new Record(
                null,
                player1.getId(),
                player1.getSx(),
                player1.getSy(),
                player2.getId(),
                player2.getSx(),
                player2.getSy(),
                player1.getStepsString(),
                player2.getStepsString(),
                getMapString(),
                loser,
                new Date()
        );
        WebSocketServer.recordMapper.insert(record);
    }

    private void sendResult() {     //  向两个client公布结果
        JSONObject resp = new JSONObject();
        resp.put("event", "result");
        resp.put("loser", loser);
        saveToDatabase();
        sendAllMessage(resp.toJSONString());
    }

    @Override
    public void run() {
        for (int i = 0; i < 1000; i++) {
            if (nextStep()) {   // 是否获取到两条蛇下一步操作
                judge();

                if (status.equals("playing")) {
                    sendMove();
                } else {
                    sendResult();
                    break;
                }
            } else {
                status = "finished";
                lock.lock();
                try {
                    if (nextStepUser1 == null && nextStepUser2 == null) {
                        loser = "all";
                    } else if (nextStepUser1 == null) {
                        loser = "user1";
                    } else {
                        loser = "user2";
                    }
                } finally {
                    lock.unlock();
                }
                sendResult();
                break;
            }
        }
    }
}
