package dyvilx.tools.compiler.ast.expression;

import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.AnnotationVisitor;
import dyvilx.tools.asm.Handle;
import dyvilx.tools.compiler.ast.attribute.annotation.Annotation;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.consumer.IAnnotationConsumer;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.parameter.IParameter;
import dyvilx.tools.compiler.ast.parameter.ParameterList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.backend.ClassFormat;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.parsing.marker.MarkerList;

public class AnnotationExpr implements IValue, IAnnotationConsumer
{
	private static final Handle ANNOTATION_METAFACTORY = new Handle(ClassFormat.H_INVOKESTATIC,
	                                                                "dyvil/runtime/AnnotationMetafactory",
	                                                                "metafactory",
	                                                                ClassFormat.BSM_HEAD + "[Ljava/lang/Object;"
	                                                                + ClassFormat.BSM_TAIL);

	protected Annotation annotation;

	public AnnotationExpr()
	{
	}

	public AnnotationExpr(Annotation annotation)
	{
		this.annotation = annotation;
	}

	@Override
	public void setPosition(SourcePosition position)
	{
	}

	@Override
	public SourcePosition getPosition()
	{
		return this.annotation.getPosition();
	}

	@Override
	public void setAnnotation(Annotation annotation)
	{
		this.annotation = annotation;
	}

	public Annotation getAnnotation()
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

		final ArgumentList arguments = this.annotation.getArguments();
		final IClass iclass = this.annotation.getType().getTheClass();
		final ParameterList parameterList = iclass.getParameters();
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
			this.annotation.getType().writeCast(writer, type, this.lineNumber());
		}
	}

	@Override
	public void writeAnnotationValue(AnnotationVisitor visitor, String key)
	{
		final AnnotationVisitor av = visitor.visitAnnotation(key, this.annotation.getType().getExtendedName());
		this.annotation.write(av);
		av.visitEnd();
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.annotation.toString(prefix, buffer);
	}
}
