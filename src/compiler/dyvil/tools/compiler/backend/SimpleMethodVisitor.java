package dyvil.tools.compiler.backend;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.type.AnnotationType;

public final class SimpleMethodVisitor extends MethodVisitor
{
	private final IMethod	method;
	
	public SimpleMethodVisitor(int api, IMethod method)
	{
		super(api);
		this.method = method;
	}
	
	@Override
	public void visitParameter(String name, int index)
	{
		method.getParameter(index).setName(name);
	}
	
	@Override
	public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index)
	{
		if (this.method.hasModifier(Modifiers.STATIC))
		{
			if (index != 0 && index <= this.method.parameterCount())
			{
				method.getParameter(index - 1).setName(name);
			}
			return;
		}
		
		if (index < this.method.parameterCount())
		{
			method.getParameter(index).setName(name);
		}
	}
	
	@Override
	public AnnotationVisitor visitAnnotation(String name, boolean visible)
	{
		AnnotationType type = new AnnotationType();
		ClassFormat.internalToType(name, type);
		Annotation annotation = new Annotation(null, type);
		
		return new AnnotationVisitorImpl(Opcodes.ASM5, method, annotation);
	}
}
