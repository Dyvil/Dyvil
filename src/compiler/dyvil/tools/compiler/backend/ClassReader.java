package dyvil.tools.compiler.backend;

import java.io.IOException;
import java.io.InputStream;

import org.objectweb.asm.*;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.external.BytecodeClass;
import dyvil.tools.compiler.ast.type.AnnotationType;
import dyvil.tools.compiler.backend.visitor.AnnotationVisitorImpl;

public class ClassReader extends ClassVisitor
{
	protected BytecodeClass	theClass;
	
	public ClassReader(BytecodeClass theClass)
	{
		super(DyvilCompiler.asmVersion);
		this.theClass = theClass;
	}
	
	public static IClass loadClass(BytecodeClass bclass, InputStream is, boolean decompile)
	{
		try
		{
			org.objectweb.asm.ClassReader reader = new org.objectweb.asm.ClassReader(is);
			ClassReader visitor = new ClassReader(bclass);
			reader.accept(visitor, 0);
			
			return bclass;
		}
		catch (IOException ex)
		{
			DyvilCompiler.logger.throwing("ClassReader", "loadClass", ex);
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
		this.theClass.visitOuterClass(owner, name, desc);
	}
	
	@Override
	public AnnotationVisitor visitAnnotation(String name, boolean visible)
	{
		AnnotationType type = new AnnotationType();
		ClassFormat.internalToType(name, type);
		Annotation annotation = new Annotation(null, type);
		return new AnnotationVisitorImpl(this.theClass, annotation);
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
	}
}
