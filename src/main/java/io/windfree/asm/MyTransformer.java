package io.windfree.asm;


import io.windfree.asm.cv.MyClassVisitor1;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class MyTransformer implements ClassFileTransformer {
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        //System.out.println(className);
        if (className == null)
            return null;
        /*
        if (className.startsWith("io/windfree/asm")) {
            return null;
        }
        */

        if(className.startsWith("io/windfree/asm/TestApp1")) {
            ClassReader cr = new ClassReader(classfileBuffer);
            ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
            ClassVisitor cv = new MyClassVisitor1(Opcodes.ASM7,cw);
            cr.accept(cv, ClassReader.EXPAND_FRAMES);
            debugPrintClass(cw.toByteArray());
            return cw.toByteArray();
        } else {
            return null;
        }

    }

    private static void debugPrintClass(byte[] classFile) {
        ClassReader cr = new ClassReader(classFile);
        StringWriter sw = new StringWriter();
        cr.accept(new TraceClassVisitor(new PrintWriter(sw)), 0);
        System.out.println(sw.toString());

    }
}
