// Cojen example that generates a program to sum the integers from 1
// to 100, and prints the sum (5050).
//
// Jesper Larsson, Feb 2016.

import org.cojen.classfile.*;
import java.io.*;

public class GenSum100 {
    static String generatedClassName = "Sum100Generated";
    
    public static void main(String[] args) throws IOException {
        ClassFile cf = createClassFile();
        cf.writeTo(new FileOutputStream(generatedClassName + ".class"));
    }

    private static ClassFile createClassFile() {
        ClassFile cf = new ClassFile(generatedClassName);
        cf.addDefaultConstructor();
        TypeDesc[] params = new TypeDesc[] { TypeDesc.STRING.toArrayType() };
        MethodInfo mi = cf.addMethod(Modifiers.PUBLIC_STATIC, "main", null, params);
        CodeBuilder b = new CodeBuilder(mi);
        TypeDesc printStream = TypeDesc.forClass("java.io.PrintStream");
        TypeDesc integer = TypeDesc.forClass("java.lang.Integer");

        LocalVariable i = b.createLocalVariable("i", TypeDesc.INT);
        LocalVariable sum = b.createLocalVariable("sum", TypeDesc.INT);
        Label end = b.createLabel();
        Label loop = b.createLabel();
        b.loadConstant(0);
        b.storeLocal(sum);
        b.loadConstant(1);
        b.storeLocal(i);
        loop.setLocation();
        b.loadLocal(i);
        b.loadConstant(100);
        b.ifComparisonBranch(end, ">");
        b.loadLocal(i);
        b.dup();
        b.loadLocal(sum);
        b.math(Opcode.IADD);
        b.storeLocal(sum);
        b.loadConstant(1);
        b.math(Opcode.IADD);
        b.storeLocal(i);
        b.branch(loop);
        end.setLocation();
        b.loadStaticField("java.lang.System", "out", printStream);
        b.loadLocal(sum);
        params = new TypeDesc[] { TypeDesc.INT };
        b.invokeStatic(integer, "toString", TypeDesc.STRING, params);
        params = new TypeDesc[] { TypeDesc.STRING };
        b.invokeVirtual(printStream, "println", null, params);

        b.returnVoid();

        return cf;
    }
}
