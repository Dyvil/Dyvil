package dyvil.tools.compiler.backend.visitor;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.type.AnnotationType;
import dyvil.tools.compiler.backend.ClassFormat;

public final class SimpleMethodVisitor extends MethodVisitor
{
	private final IMethod	method;
	
	public SimpleMethodVisitor(IMethod method)
	{
		super(DyvilCompiler.asmVersion);
		this.method = method;
	}
	
	@Override
	public void visitParameter(String name, int index)
	{
		this.method.getParameter(index).setName(name);
	}
	
	@Override
	public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index)
	{
		if (this.method.hasModifier(Modifiers.STATIC))
		{
			if (index != 0 && index <= this.method.parameterCount())
			{
				this.method.getParameter(index - 1).setName(name);
			}
			return;
		}
		
		if (index < this.method.parameterCount())
		{
			this.method.getParameter(index).setName(name);
		}
	}
	
	@Override
	public AnnotationVisitor visitAnnotation(String type, boolean visible)
	{
		String packName = ClassFormat.internalToPackage2(type);
		if (this.method.addRawAnnotation(packName))
		{
			AnnotationType atype = new AnnotationType(packName);
			Annotation annotation = new Annotation(atype);
			return new AnnotationVisitorImpl(this.method, annotation);
		}
		return null;
	}
}
