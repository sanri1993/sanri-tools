package systest;

import java.io.IOException;

import com.sanri.tools.modules.classloader.controller.ClassloaderController;
import org.junit.Test;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

public class VisitClassMain {

	/**
	 * Visit fields and methods of Application class by core API.
	 * 
	 * @throws IOException
	 */
	public void visitByCoreAPI() throws IOException {
		ClassReader cr = new ClassReader(ClassloaderController.class.getName());
		ClassWriter cw = new ClassWriter(0);

		ClassVisitor cv = new ClassVisitor(Opcodes.ASM5, cw) {

			@Override
			public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
				System.out.println("field in visitor: " + name);
				return super.visitField(access, name, descriptor, signature, value);
			}

			@Override
			public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
					String[] exceptions) {
				System.out.println("method in visitor: " + name);
				return super.visitMethod(access, name, descriptor, signature, exceptions);
			}
		};

		cr.accept(cv, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG);
	}

	/**
	 * Visit method instructions of Application class by core API.
	 * 
	 * @throws IOException
	 */
	public void visitInstByCoreAPI() throws IOException {
		ClassReader cr = new ClassReader(ClassloaderController.class.getName());
		ClassWriter cw = new ClassWriter(0);

		ClassVisitor cv = new ClassVisitor(Opcodes.ASM5, cw) {

			@Override
			public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
					String[] exceptions) {
				System.out.println("method in visitor: " + name);
				MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

				return new MethodVisitor(Opcodes.ASM5, mv) {

					@Override
					public void visitFieldInsn(final int opcode, final String owner, final String name,
							final String descriptor) {
						System.out.println("\tfield instruction in visitor: " + name);
						super.visitFieldInsn(opcode, owner, name, descriptor);
					}

					@Override
					public void visitMethodInsn(final int opcode, final String owner, final String name,
							final String descriptor, final boolean isInterface) {
						System.out.println("\tmethod instruction in visitor: " + name);
						super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
					}
				};
			}
		};

		cr.accept(cv, ClassReader.SKIP_DEBUG);
	}

	/**
	 * Visit fields and methods of Application class by tree API.
	 * 
	 * @throws IOException
	 */
	public void visitByTreeAPI() throws IOException {
		ClassReader cr = new ClassReader(ClassloaderController.class.getName());
		ClassNode cn = new ClassNode();
		cr.accept(cn, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG);

		for (FieldNode fn : cn.fields) {
			System.out.println("field in visitor: " + fn.name);
		}

		for (MethodNode mn : cn.methods) {
			System.out.println("method in visitor: " + mn.name);
		}
	}

	/**
	 * Visit method instructions of Application class by tree API.
	 * 
	 * @throws IOException
	 */
	public void visitInstByTreeAPI() throws IOException {
		ClassReader cr = new ClassReader(ClassloaderController.class.getName());
		ClassNode cn = new ClassNode();
		cr.accept(cn, ClassReader.SKIP_DEBUG);

		for (MethodNode mn : cn.methods) {
			System.out.printf("method=%s, inst_count=%d\n", mn.name, mn.instructions.size());
			for (AbstractInsnNode ins : mn.instructions) {
				System.out.println("\t" + ins.toString());
			}
		}
	}

	public static void main(String[] args) throws Exception {

		VisitClassMain visit = new VisitClassMain();
		System.out.println("visit class by core API:");
		visit.visitByCoreAPI();

		System.out.println("\nvisit method instructions by core API:");
		visit.visitInstByCoreAPI();

		System.out.println("\n\nvisit class by tree API:");
		visit.visitByTreeAPI();

		System.out.println("\nvisit method instructions by tree API:");
		visit.visitInstByTreeAPI();
	}

	@Test
	public void test() throws IOException {
		ClassReader cr = new ClassReader(ClassloaderController.class.getName());
		ClassNode classNode = new ClassNode();
		cr.accept(classNode,0);
		System.out.println(classNode.name);
	}
}
