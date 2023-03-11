package com.kob.botrunningsystem.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Bot implements java.util.function.Supplier<Integer> {

    static class Cell {
        public int x, y;
        public Cell(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public boolean snakeIsIncreasing(int step) {    //检查当前回合蛇的长度是否会增加
        if (step <= 10)
            return true;
        return step%3 == 1;
    }

    public List<Cell> getCells(int sx, int sy, String steps) {      // 返回蛇的身体
        steps = steps.substring(1, steps.length()-1);
        List<Cell> cells = new ArrayList<>();

        int[] dx = { -1, 0, 1, 0 }, dy = { 0, 1, 0, -1 };

        int x = sx, y = sy;
        int step = 0;   //记录当前回合数,初始为0
        cells.add(new Cell(sx, sy));
        for (int i = 0; i <steps.length(); i++) {
            int d = steps.charAt(i) - '0';
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

    public Integer nextMove(String input) {
        String[] strs = input.split("#");
        int[][] g = new int[13][14];
        for (int i = 0, k = 0; i < 13; i++) {
            for (int j = 0; j < 14; j++, k++) {
                if (strs[0].charAt(k) == '1') {
                    g[i][j] = 1;
                }
            }
        }

        int user1Sx = Integer.parseInt(strs[1]), user1Sy = Integer.parseInt(strs[2]);
        int user2Sx = Integer.parseInt(strs[4]), user2Sy = Integer.parseInt(strs[5]);

        List<Cell> user1Cells = getCells(user1Sx, user1Sy, strs[3]);
        List<Cell> user2Cells = getCells(user2Sx, user2Sy, strs[6]);

        for (Cell cell : user1Cells)
            g[cell.x][cell.y] = 1;
        for (Cell cell : user2Cells)
            g[cell.x][cell.y] = 1;

        int[] dx = { -1, 0, 1, 0 }, dy = { 0, 1, 0, -1 };

        for (int i = 0; i < 4; i++) {
            int x = user1Cells.get(user1Cells.size()-1).x + dx[i];
            int y = user1Cells.get(user1Cells.size()-1).y + dy[i];
            if (x >= 0 && x < 13 && y >= 0 && y < 14 && g[x][y] == 0)
                return i;
        }

        return 0;
    }

    @Override
    public Integer get() {
        File file = new File("input.txt");
        try {
            Scanner sc = new Scanner(file);
            return nextMove(sc.next());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
