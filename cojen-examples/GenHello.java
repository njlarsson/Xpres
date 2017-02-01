import org.cojen.classfile.*;
import java.io.*;
import java.lang.reflect.Method;

public class GenHello {
    public static void main(String[] args) throws IOException {
        ClassFile cf = createClassFile();
        cf.writeTo(new FileOutputStream("HelloGenerated.class"));
    }

    private static ClassFile createClassFile() {
        ClassFile cf = new ClassFile("HelloGenerated");
        cf.addDefaultConstructor();
        TypeDesc[] params = new TypeDesc[] { TypeDesc.STRING.toArrayType() };
        MethodInfo mi = cf.addMethod(Modifiers.PUBLIC_STATIC, "main", null, params);
        CodeBuilder b = new CodeBuilder(mi);
        TypeDesc printStream = TypeDesc.forClass("java.io.PrintStream");

        b.loadStaticField("java.lang.System", "out", printStream);
        // b.dup();
        b.loadConstant("Hello wolf!");
        params = new TypeDesc[] { TypeDesc.STRING };
        b.invokeVirtual(printStream, "println", null, params);

        b.returnVoid();

        return cf;
    }
}
