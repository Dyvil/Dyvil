package dyvil.tools.compiler.bytecode;

import java.io.IOException;
import java.io.InputStream;

import jdk.internal.org.objectweb.asm.*;
import dyvil.tools.compiler.ast.classes.BytecodeClass;
import dyvil.tools.compiler.ast.classes.IClass;

public class ClassReader extends ClassVisitor
{
	protected BytecodeClass	bclass;
	
	public ClassReader(BytecodeClass bclass)
	{
		super(Opcodes.ASM5);
		this.bclass = bclass;
	}
	
	public static IClass loadClass(InputStream is, boolean decompile)
	{
		try
		{
			BytecodeClass bclass = new BytecodeClass();
			jdk.internal.org.objectweb.asm.ClassReader reader = new jdk.internal.org.objectweb.asm.ClassReader(is);
			ClassReader visitor = new ClassReader(bclass);
			reader.accept(visitor, 0);
			
			return bclass;
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		
		return null;
	}
	
	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
	{
		this.bclass.visit(version, access, name, signature, superName, interfaces);
	}
	
	@Override
	public void visitSource(String source, String debug)
	{
	}
	
	@Override
	public void visitOuterClass(String owner, String name, String desc)
	{
	}
	
	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible)
	{
		return null;
	}
	
	@Override
	public void visitAttribute(Attribute attr)
	{
	}
	
	@Override
	public void visitInnerClass(String name, String outerName, String innerName, int access)
	{
	}
	
	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value)
	{
		return this.bclass.visitField(access, name, desc, signature, value);
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
	{
		return this.bclass.visitMethod(access, name, desc, signature, exceptions);
	}
	
	@Override
	public void visitEnd()
	{
	}
}
