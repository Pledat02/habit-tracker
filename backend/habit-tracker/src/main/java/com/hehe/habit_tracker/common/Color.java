package com.hehe.habit_tracker.common;

public enum Color {
    RED("#FF0000"), //
    GREEN("#00FF00"),
    BLUE("#0000FF"),
    YELLOW("#FFFF00"),
    PURPLE("#FF00FF"),
    ORANGE("#FFA500"),
    PINK("#FF1493"),
    BLACK("#000000"),
    WHITE("#FFFFFF");

    private final String hexCode;

    Color(String hexCode) {
        this.hexCode = hexCode;
    }
}
