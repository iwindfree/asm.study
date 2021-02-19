package io.windfree.asm;

import java.lang.instrument.Instrumentation;

public class JavaAgent {
    private static Instrumentation instrumentation;
    public static void premain(String options, Instrumentation instrum) {
        instrumentation = instrum;
        instrumentation.addTransformer(new MyTransformer());

    }
}
