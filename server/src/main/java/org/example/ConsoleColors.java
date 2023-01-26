package org.example;

public enum ConsoleColors {
    YELLOW("\u001B[33m"),
    WHITE("\u001B[0m");
    private String escape;

    ConsoleColors(String s) {
        this.escape = s;
    }

    @Override
    public String toString() {
        return this.escape;
    }
}
