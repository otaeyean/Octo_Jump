package game;

import java.awt.*;

public class Item {
    Rectangle bounds; // 아이템의 좌표와 크기
    int type;         // 아이템 유형

    public Item(Rectangle bounds, int type) {
        this.bounds = bounds;
        this.type = type;
    }
}