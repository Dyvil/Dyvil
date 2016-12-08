package dyvil.tools.compiler.ast.type.compound;

import dyvil.tools.asm.TypeAnnotatableVisitor;
import dyvil.tools.compiler.ast.annotation.AnnotationUtil;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.parsing.Name;

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
	public boolean isSameType(IType type)
	{
		final NullableType nullable = type.extract(NullableType.class);
		return Types.isSameType(this.type, nullable != null ? nullable.getElementType() : type);
	}

	@Override
	public boolean isSubTypeOf(IType superType)
	{
		final NullableType nullable = superType.extract(NullableType.class);
		return Types.isSuperType(nullable != null ? nullable.getElementType() : superType, this.type);
	}

	@Override
	public boolean isSuperTypeOf(IType subType)
	{
		final NullableType nullable = subType.extract(NullableType.class);
		return Types.isSuperType(this.type, nullable != null ? nullable.getElementType() : subType);
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		return this.type.resolveField(name);
	}

	@Override
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, IArguments arguments)
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
	public IType withAnnotation(IAnnotation annotation)
	{
		switch (annotation.getType().getInternalName())
		{
		case AnnotationUtil.NOTNULL_INTERNAL:
			return this.type;
		case AnnotationUtil.NULLABLE_INTERNAL:
			return new NullableType(this.type);
		}

		return null;
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
