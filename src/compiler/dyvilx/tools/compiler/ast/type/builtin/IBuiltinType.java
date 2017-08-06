package dyvilx.tools.compiler.ast.type.builtin;

import dyvilx.tools.asm.TypeAnnotatableVisitor;
import dyvilx.tools.asm.TypePath;
import dyvilx.tools.compiler.ast.annotation.AnnotationUtil;
import dyvilx.tools.compiler.ast.constructor.IConstructor;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.raw.IRawType;
import dyvil.lang.Name;
import dyvilx.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public interface IBuiltinType extends IRawType
{
	@Override
	default boolean isResolved()
	{
		return true;
	}

	@Override
	default IType resolveType(MarkerList markers, IContext context)
	{
		return this;
	}

	@Override
	default IDataMember resolveField(Name name)
	{
		return null;
	}

	@Override
	default void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, ArgumentList arguments)
	{
	}

	@Override
	default void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
	}

	@Override
	default void getConstructorMatches(MatchList<IConstructor> list, ArgumentList arguments)
	{
	}

	@Override
	default IMethod getFunctionalMethod()
	{
		return null;
	}

	@Override
	default void writeAnnotations(TypeAnnotatableVisitor visitor, int typeRef, String typePath)
	{
		visitor.visitTypeAnnotation(typeRef, TypePath.fromString(typePath), AnnotationUtil.PRIMITIVE,
		                            AnnotationUtil.PRIMITIVE_VISIBLE);
	}

	@Override
	default void write(DataOutput out) throws IOException
	{
	}

	@Override
	default void read(DataInput in) throws IOException
	{
	}

	@Override
	default void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.toString());
	}
}
