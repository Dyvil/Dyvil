package dyvil.tools.compiler.ast.annotation;

import dyvil.tools.asm.Handle;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.consumer.IAnnotationConsumer;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.IParameterList;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class AnnotationValue implements IValue, IAnnotationConsumer
{
	private static final Handle ANNOTATION_METAFACTORY = new Handle(ClassFormat.H_INVOKESTATIC,
	                                                                "dyvil/runtime/AnnotationMetafactory",
	                                                                "metafactory",
	                                                                ClassFormat.BSM_HEAD + "[Ljava/lang/Object;"
		                                                                + ClassFormat.BSM_TAIL);

	protected IAnnotation annotation;

	public AnnotationValue()
	{
	}

	public AnnotationValue(IAnnotation annotation)
	{
		this.annotation = annotation;
	}

	@Override
	public void setPosition(ICodePosition position)
	{
	}

	@Override
	public ICodePosition getPosition()
	{
		return this.annotation.getPosition();
	}

	@Override
	public void setAnnotation(IAnnotation annotation)
	{
		this.annotation = annotation;
	}

	public IAnnotation getAnnotation()
	{
		return this.annotation;
	}

	@Override
	public int valueTag()
	{
		return ANNOTATION;
	}

	@Override
	public boolean isResolved()
	{
		return this.annotation.getType().isResolved();
	}

	@Override
	public IType getType()
	{
		return this.annotation.getType();
	}

	@Override
	public IValue toAnnotationConstant(MarkerList markers, IContext context, int depth)
	{
		return this;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.annotation.resolveTypes(markers, context);
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.annotation.resolve(markers, context);
		return this;
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.annotation.checkTypes(markers, context);
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.annotation.check(markers, context, null);
	}

	@Override
	public IValue foldConstants()
	{
		this.annotation.foldConstants();
		return this;
	}

	@Override
	public IValue cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		this.annotation.cleanup(compilableList, classCompilableList);
		return this;
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		final StringBuilder descBuilder = new StringBuilder().append('(');

		final IArguments arguments = this.annotation.getArguments();
		final IClass iclass = this.annotation.getType().getTheClass();
		final IParameterList parameterList = iclass.getParameterList();
		final int count = parameterList.size();

		String[] parameterNames = new String[count];
		for (int i = 0; i < count; i++)
		{
			final IParameter parameter = parameterList.get(i);
			final IType parameterType = parameter.getType();

			parameterNames[i] = parameter.getInternalName();
			parameterType.appendExtendedName(descBuilder);

			arguments.writeValue(i, parameter, writer);
		}

		descBuilder.append(')');
		descBuilder.append('L').append(iclass.getInternalName()).append(';');

		writer.visitInvokeDynamicInsn("_", descBuilder.toString(), ANNOTATION_METAFACTORY, (Object[]) parameterNames);

		if (type != null)
		{
			this.annotation.getType().writeCast(writer, type, this.getLineNumber());
		}
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.annotation.toString(prefix, buffer);
	}
}
