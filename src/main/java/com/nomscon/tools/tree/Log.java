package com.nomscon.tools.tree;

public final class Log {
    public final static void print(String value) {
        System.out.print(value);
    }

    public final static void print(String format, Object... args) {
        System.out.print(String.format(format, args));
    }

    public final static void println() {
        System.out.println();
    }

    public final static void println(String value) {
        System.out.println(value);
    }

    public final static void println(String format, Object... args) {
        System.out.println(String.format(format, args));
    }
}
