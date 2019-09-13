package dyvilx.tools.compiler.ast.attribute.modifiers;

import dyvil.annotation.internal.Nullable;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.AnnotatableVisitor;
import dyvilx.tools.asm.TypeAnnotatableVisitor;
import dyvilx.tools.asm.TypePath;
import dyvilx.tools.compiler.ast.attribute.Attribute;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.parsing.marker.MarkerList;

import java.io.DataOutput;
import java.lang.annotation.ElementType;

public interface Modifier extends Attribute
{
	@Override
	@Nullable
	default IType getType()
	{
		return null;
	}

	@Override
	default SourcePosition getPosition()
	{
		return null;
	}

	@Override
	default void setPosition(SourcePosition position)
	{
	}

	@Override
	default void resolveTypes(MarkerList markers, IContext context)
	{
	}

	@Override
	default void resolve(MarkerList markers, IContext context)
	{
	}

	@Override
	default void checkTypes(MarkerList markers, IContext context)
	{
	}

	@Override
	default void check(MarkerList markers, IContext context, ElementType target)
	{
	}

	@Override
	default void foldConstants()
	{
	}

	@Override
	default void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
	}

	@Override
	default void write(DataOutput out)
	{
	}

	@Override
	default void write(AnnotatableVisitor writer)
	{
	}

	@Override
	default void write(TypeAnnotatableVisitor writer, int typeRef, TypePath path)
	{
	}
}
