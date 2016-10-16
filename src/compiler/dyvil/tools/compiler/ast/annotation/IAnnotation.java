package dyvil.tools.compiler.ast.annotation;

import dyvil.tools.asm.AnnotatableVisitor;
import dyvil.tools.asm.AnnotationVisitor;
import dyvil.tools.asm.TypeAnnotatableVisitor;
import dyvil.tools.asm.TypePath;
import dyvil.tools.compiler.phase.IResolvable;
import dyvil.tools.compiler.ast.consumer.IArgumentsConsumer;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.ast.header.IObjectCompilable;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.MarkerList;

import java.lang.annotation.ElementType;

public interface IAnnotation extends IASTNode, IResolvable, ITyped, IObjectCompilable, IArgumentsConsumer
{
	@Override
	void setArguments(IArguments arguments);
	
	IArguments getArguments();
	
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
