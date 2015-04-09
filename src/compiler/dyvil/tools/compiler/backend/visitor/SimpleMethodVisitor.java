package dyvil.tools.compiler.backend.visitor;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.IBaseMethod;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.backend.ClassFormat;

public final class SimpleMethodVisitor extends MethodVisitor
{
	private final IBaseMethod	method;
	
	public SimpleMethodVisitor(IBaseMethod method)
	{
		super(DyvilCompiler.asmVersion);
		this.method = method;
	}
	
	@Override
	public void visitParameter(String name, int index)
	{
		this.method.getParameter(index).setName(Name.getQualified(name));
	}
	
	@Override
	public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index)
	{
		if (this.method.hasModifier(Modifiers.STATIC))
		{
			if (index != 0 && index <= this.method.parameterCount())
			{
				this.method.getParameter(index - 1).setName(Name.getQualified(name));
			}
			return;
		}
		
		if (index < this.method.parameterCount())
		{
			this.method.getParameter(index).setName(Name.getQualified(name));
		}
	}
	
	@Override
	public AnnotationVisitor visitAnnotation(String type, boolean visible)
	{
		String internal = ClassFormat.extendedToInternal(type);
		if (this.method.addRawAnnotation(internal))
		{
			Annotation annotation = new Annotation(new Type(internal));
			return new AnnotationVisitorImpl(this.method, annotation);
		}
		return null;
	}
}
