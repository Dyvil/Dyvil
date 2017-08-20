package dyvilx.tools.compiler.ast.annotation;

import dyvilx.tools.asm.AnnotatableVisitor;
import dyvilx.tools.asm.AnnotationVisitor;
import dyvilx.tools.asm.TypeAnnotatableVisitor;
import dyvilx.tools.asm.TypePath;
import dyvilx.tools.compiler.phase.IResolvable;
import dyvilx.tools.compiler.ast.consumer.IArgumentsConsumer;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.type.ITyped;
import dyvilx.tools.compiler.ast.header.IObjectCompilable;
import dyvilx.tools.parsing.ASTNode;
import dyvilx.tools.parsing.marker.MarkerList;

import java.lang.annotation.ElementType;

public interface IAnnotation extends ASTNode, IResolvable, ITyped, IObjectCompilable, IArgumentsConsumer
{
	@Override
	void setArguments(ArgumentList arguments);
	
	ArgumentList getArguments();
	
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
	
	void write(AnnotationVisitor writer);
}
