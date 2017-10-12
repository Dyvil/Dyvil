package dyvilx.tools.compiler.ast.attribute;

import dyvil.annotation.internal.Nullable;
import dyvilx.tools.asm.AnnotatableVisitor;
import dyvilx.tools.asm.TypeAnnotatableVisitor;
import dyvilx.tools.asm.TypePath;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.phase.IResolvable;
import dyvilx.tools.parsing.ASTNode;
import dyvilx.tools.parsing.marker.MarkerList;

import java.io.DataOutput;
import java.io.IOException;
import java.lang.annotation.ElementType;

public interface Attribute extends ASTNode, IResolvable
{
	@Nullable IType getType();

	int flags();

	@Override
	void resolveTypes(MarkerList markers, IContext context);

	@Override
	void resolve(MarkerList markers, IContext context);

	@Override
	void checkTypes(MarkerList markers, IContext context);

	@Override
	default void check(MarkerList markers, IContext context)
	{
		this.check(markers, context, null);
	}

	void check(MarkerList markers, IContext context, ElementType target);

	@Override
	void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList);

	@Override
	void foldConstants();

	void write(AnnotatableVisitor writer);

	void write(TypeAnnotatableVisitor writer, int typeRef, TypePath path);

	void write(DataOutput out) throws IOException;
}
