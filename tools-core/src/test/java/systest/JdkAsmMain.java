//package systest;
//
//import com.sanri.tools.modules.core.controller.ClassloaderController;
//import jdk.internal.org.objectweb.asm.ClassReader;
//import jdk.internal.org.objectweb.asm.tree.ClassNode;
//import org.junit.Test;
//
//import java.io.IOException;
//
//public class JdkAsmMain {
//    @Test
//    public void test1() throws IOException {
//        ClassReader cr = new ClassReader(ClassloaderController.class.getName());
//        ClassNode classNode = new ClassNode();
//        cr.accept(classNode,0);
//        System.out.println(classNode.name);
//    }
//}
