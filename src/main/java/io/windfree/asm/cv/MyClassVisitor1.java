package io.windfree.asm.cv;


import io.windfree.asm.TraceMain;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.LocalVariablesSorter;

public class MyClassVisitor1 extends ClassVisitor {
    String className ;
    String[] interfaces;
    public MyClassVisitor1(int api, ClassVisitor classVisitor) {
        super(api, classVisitor);

    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        if(mv == null) {  // 해당 클래스가 변조를 하지 않을 클래스인 경우.
            return mv;
        }
        if (name.equals("print1")) {
            return new MyMethodVisitor1(api, access,descriptor,mv);
        }
        return mv;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.className = name;
        this.interfaces = interfaces;
        super.visit(version, access, name, signature, superName, interfaces);
    }
}

/* LOAD : local variable -> stack
   STORE : stack -> local variable
 */
class MyMethodVisitor1 extends LocalVariablesSorter implements Opcodes {
    private int statIdx;
    private Type returnType;
    private static final String TRACEMAIN = TraceMain.class.getName().replace('.', '/');
    private Label startFinally = new Label();
    private final static String START_SERVICE = "start";
    private static final String START_SIGNATURE = "(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;";
    private final static String END_METHOD = "end";
    private static final String END_SIGNATURE = "(Ljava/lang/Object;Ljava/lang/Object;)V";

    protected MyMethodVisitor1(int api, int access, String descriptor, MethodVisitor methodVisitor) {
        super(api, access, descriptor, methodVisitor);
        this.returnType = Type.getReturnType(descriptor);
    }
    @Override
    public void visitCode() {
        //mv.visitVarInsn(Opcodes.ALOAD, 0);  --> this
        /*
         method 의 argument 를 load 함. print1(String args) 의 args
         static method 인 경우는 0번부터 메서드의 첫번째 argument 가 저장됨
        */

        // stack 에 1번 변수 (method 의 첫번째 argument) push
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        // stack 에 2번 변수 (method 의 두번째 argument) push
        mv.visitVarInsn(Opcodes.ALOAD,2);
        // stack 에 null push
        mv.visitInsn(Opcodes.ACONST_NULL);

        mv.visitMethodInsn(Opcodes.INVOKESTATIC, TRACEMAIN, START_SERVICE, START_SIGNATURE, false);

        // TraceMain의 start() 의 return value 인 localContext 를 statIdx 에 저장된 변수에 저장하기 위한 로직
        // return value 가 stack 의 top 에 존재하기 때문에 AStore 로 변수에 저
        statIdx = newLocal(Type.getType(Object.class));
        mv.visitVarInsn(Opcodes.ASTORE, statIdx);

        mv.visitLabel(startFinally);
        mv.visitCode();
    }

    @Override
    public void visitInsn(int opcode) {
        if ((opcode >= IRETURN && opcode <= RETURN)) {
            mv.visitVarInsn(Opcodes.ALOAD, statIdx);// stat
            mv.visitInsn(Opcodes.ACONST_NULL);// throwable
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, TRACEMAIN, END_METHOD, END_SIGNATURE, false);
        }
        mv.visitInsn(opcode);
    }



    @Override
    public void visitMaxs(int maxStack, int maxLocals) {

        Label endFinally = new Label();
        mv.visitTryCatchBlock(startFinally, endFinally, endFinally, null);
        mv.visitLabel(endFinally);
        mv.visitInsn(DUP);

        int errIdx = newLocal(Type.getType(Throwable.class));
        mv.visitVarInsn(Opcodes.ASTORE, errIdx);

        mv.visitVarInsn(Opcodes.ALOAD, statIdx);
        mv.visitVarInsn(Opcodes.ALOAD, errIdx);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, TRACEMAIN, END_METHOD, END_SIGNATURE,false);
        mv.visitInsn(ATHROW);

        mv.visitMaxs(maxStack + 8, maxLocals + 2);

    }



    private void capReturn() {
        Type tp = returnType;

        if (tp == null || tp.equals(Type.VOID_TYPE)) {
            mv.visitInsn(Opcodes.ACONST_NULL);
            //mv.visitVarInsn(Opcodes.ALOAD, statIdx);
            mv.visitInsn(Opcodes.ACONST_NULL);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, TRACEMAIN, END_METHOD, END_SIGNATURE, false);
            return;
        }

        switch (tp.getSort()) {
            case Type.DOUBLE:
            case Type.LONG:
                mv.visitInsn(Opcodes.DUP2);
                break;
            default:
                mv.visitInsn(Opcodes.DUP);
        }
        // TODO method return test dup and store
//		int rtnIdx = newLocal(tp);
//		mv.visitVarInsn(Opcodes.ASTORE, rtnIdx);
//		mv.visitVarInsn(Opcodes.ALOAD, rtnIdx);

        mv.visitVarInsn(Opcodes.ALOAD, statIdx);// stat
        mv.visitInsn(Opcodes.ACONST_NULL);// throwable
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, TRACEMAIN, END_METHOD, END_SIGNATURE, false);
    }
}