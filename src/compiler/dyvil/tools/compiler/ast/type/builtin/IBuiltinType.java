package dyvil.tools.compiler.ast.type.builtin;

import dyvil.tools.asm.TypeAnnotatableVisitor;
import dyvil.tools.asm.TypePath;
import dyvil.tools.compiler.ast.annotation.AnnotationUtil;
import dyvil.tools.compiler.ast.constructor.IConstructor;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.raw.IRawType;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

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
	default void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, IArguments arguments)
	{
	}

	@Override
	default void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
	}

	@Override
	default void getConstructorMatches(MatchList<IConstructor> list, IArguments arguments)
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

	@Override
	default IType clone()
	{
		return this;
	}
}
