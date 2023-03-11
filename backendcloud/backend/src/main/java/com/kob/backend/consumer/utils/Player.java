package com.kob.backend.consumer.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Player {
    private Integer id;
    private Integer botId;      // -1表示玩家操作，否则表示ai操作
    private String botCode;

    private Integer sx;

    private Integer sy;
    private List<Integer> steps;

    public boolean snakeIsIncreasing(int step) {    //检查当前回合蛇的长度是否会增加
        if (step <= 10)
            return true;
        return step%3 == 1;
    }

    public List<Cell> getCells() {      // 返回蛇的身体
        List<Cell> cells = new ArrayList<>();

        int[] dx = { -1, 0, 1, 0 }, dy = { 0, 1, 0, -1 };

        int x = sx, y = sy;
        int step = 0;   //记录当前回合数,初始为0
        cells.add(new Cell(sx, sy));
        for (int d : steps) {
            x += dx[d];
            y += dy[d];
            cells.add(new Cell(x, y));
            step++;

            if (!snakeIsIncreasing(step)) {
                cells.remove(0);
            }
        }
        return cells;
    }

    public String getStepsString() {
        StringBuilder res = new StringBuilder();
        for (int step : steps) {
            res.append(step);
        }
        return res.toString();
    }
}
