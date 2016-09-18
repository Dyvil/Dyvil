package dyvil.tools.compiler.ast.annotation;

import dyvil.tools.asm.AnnotatableVisitor;
import dyvil.tools.asm.AnnotationVisitor;
import dyvil.tools.asm.TypeAnnotatableVisitor;
import dyvil.tools.asm.TypePath;
import dyvil.tools.compiler.ast.consumer.IArgumentsConsumer;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.backend.IObjectCompilable;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.MarkerList;

import java.lang.annotation.ElementType;

public interface IAnnotation extends IASTNode, ITyped, IObjectCompilable, IArgumentsConsumer
{
	@Override
	void setArguments(IArguments arguments);
	
	IArguments getArguments();
	
	void resolveTypes(MarkerList markers, IContext context);
	
	void resolve(MarkerList markers, IContext context);
	
	void checkTypes(MarkerList markers, IContext context);
	
	void check(MarkerList markers, IContext context, ElementType target);
	
	void cleanup(IContext context, IClassCompilableList compilableList);
	
	void foldConstants();
	
	void write(AnnotatableVisitor writer);
	
	void write(TypeAnnotatableVisitor visitor, int typeRef, TypePath path);
	
	void write(AnnotationVisitor writer);
}
