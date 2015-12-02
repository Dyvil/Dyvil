package dyvil.tools.compiler.backend;

import dyvil.tools.asm.*;
import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.external.ExternalClass;

import java.io.InputStream;

public class ClassReader implements ClassVisitor
{
	protected ExternalClass theClass;
	
	public ClassReader(ExternalClass theClass)
	{
		this.theClass = theClass;
	}
	
	public static IClass loadClass(ExternalClass bclass, InputStream is, boolean decompile)
	{
		try
		{
			dyvil.tools.asm.ClassReader reader = new dyvil.tools.asm.ClassReader(is);
			ClassReader visitor = new ClassReader(bclass);
			reader.accept(visitor, dyvil.tools.asm.ClassReader.SKIP_CODE | dyvil.tools.asm.ClassReader.SKIP_FRAMES);
			
			return bclass;
		}
		catch (Throwable ex)
		{
			DyvilCompiler.error("ClassReader", "loadClass", ex);
		}
		
		return null;
	}
	
	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
	{
		this.theClass.visit(version, access, name, signature, superName, interfaces);
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
	public AnnotationVisitor visitAnnotation(String type, boolean visible)
	{
		return this.theClass.visitAnnotation(type, visible);
	}
	
	@Override
	public void visitAttribute(Attribute attr)
	{
	}
	
	@Override
	public void visitInnerClass(String name, String outerName, String innerName, int access)
	{
		this.theClass.visitInnerClass(name, outerName, innerName, access);
	}
	
	@Override
	public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible)
	{
		return this.theClass.visitTypeAnnotation(typeRef, typePath, desc, visible);
	}
	
	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value)
	{
		return this.theClass.visitField(access, name, desc, signature, value);
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
	{
		return this.theClass.visitMethod(access, name, desc, signature, exceptions);
	}
	
	@Override
	public void visitEnd()
	{
		this.theClass.visitEnd();
	}
}
