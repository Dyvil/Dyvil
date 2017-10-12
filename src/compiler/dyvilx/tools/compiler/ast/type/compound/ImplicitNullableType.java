package dyvilx.tools.compiler.ast.type.compound;

import dyvil.lang.Name;
import dyvilx.tools.asm.TypeAnnotatableVisitor;
import dyvilx.tools.compiler.ast.attribute.annotation.Annotation;
import dyvilx.tools.compiler.ast.attribute.annotation.AnnotationUtil;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.optional.OptionalUnwrapOperator;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.parsing.marker.MarkerList;

public class ImplicitNullableType extends NullableType
{
	public ImplicitNullableType()
	{
	}

	public ImplicitNullableType(IType type)
	{
		this.type = type;
	}

	@Override
	public int typeTag()
	{
		return IMPLICIT_OPTIONAL;
	}

	@Override
	protected NullableType wrap(IType type)
	{
		return new ImplicitNullableType(type);
	}

	@Override
	public boolean isConvertibleTo(IType type)
	{
		return Types.isSuperType(type, this.type);
	}

	@Override
	public IValue convertValueTo(IValue value, IType targetType, ITypeContext typeContext, MarkerList markers,
		                            IContext context)
	{
		if (!this.isConvertibleTo(targetType))
		{
			return null;
		}

		return new OptionalUnwrapOperator(value);
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		return this.type.resolveField(name);
	}

	@Override
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, ArgumentList arguments)
	{
		this.type.getMethodMatches(list, receiver, name, arguments);
	}

	@Override
	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
		this.type.getImplicitMatches(list, value, targetType);
	}

	@Override
	public IMethod getFunctionalMethod()
	{
		return this.type.getFunctionalMethod();
	}

	@Override
	public IType withAnnotation(Annotation annotation)
	{
		switch (annotation.getTypeDescriptor())
		{
		case AnnotationUtil.NULLABLE_INTERNAL:
			return NullableType.apply(this.type);
		case AnnotationUtil.PRIMITIVE_INTERNAL:
			if (!this.type.getInternalName().equals("java/lang/Object"))
			{
				// @Primitive <prim>! => <prim>
				return this.type.withAnnotation(annotation);
			}
			// but @Primitive Object! => any!
		}

		return super.withAnnotation(annotation);
	}

	@Override
	public void writeAnnotations(TypeAnnotatableVisitor visitor, int typeRef, String typePath)
	{
		this.type.writeAnnotations(visitor, typeRef, typePath);
	}

	@Override
	public String toString()
	{
		return this.type.toString() + '!';
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.type.toString(prefix, buffer);
		buffer.append('!');
	}
}
