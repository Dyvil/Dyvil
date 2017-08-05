package dyvilx.tools.compiler.backend;

import dyvilx.tools.asm.*;
import dyvilx.tools.compiler.DyvilCompiler;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.external.ExternalClass;

import java.io.InputStream;

public class ClassReader implements ClassVisitor
{
	protected ExternalClass theClass;
	
	public ClassReader(ExternalClass theClass)
	{
		this.theClass = theClass;
	}
	
	public static IClass loadClass(DyvilCompiler compiler, ExternalClass externalClass, InputStream inputStream)
	{
		try
		{
			final dyvilx.tools.asm.ClassReader reader = new dyvilx.tools.asm.ClassReader(inputStream);
			final ClassReader visitor = new ClassReader(externalClass);

			reader.accept(visitor, dyvilx.tools.asm.ClassReader.SKIP_FRAMES);
			
			return externalClass;
		}
		catch (Throwable ex)
		{
			compiler.error("ClassReader", "loadClass", ex);
		}
		
		return null;
	}
	
	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
	{
		this.theClass.visit(access, name, signature, superName, interfaces);
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
		return this.theClass.visitAnnotation(type);
	}
	
	@Override
	public void visitAttribute(Attribute attr)
	{
	}
	
	@Override
	public void visitInnerClass(String name, String outerName, String innerName, int access)
	{
		this.theClass.visitInnerClass(name, outerName, innerName);
	}
	
	@Override
	public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible)
	{
		return this.theClass.visitTypeAnnotation(typeRef, typePath, desc);
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
