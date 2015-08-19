package dyvil.tools.compiler.ast.annotation;

import java.lang.annotation.ElementType;

import dyvil.tools.asm.AnnotationVisitor;
import dyvil.tools.asm.FieldVisitor;
import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.IObjectCompilable;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public interface IAnnotation extends IASTNode, ITyped, IObjectCompilable
{
	void setArguments(IArguments arguments);
	
	IArguments getArguments();
	
	void resolveTypes(MarkerList markers, IContext context);
	
	void resolve(MarkerList markers, IContext context);
	
	void checkTypes(MarkerList markers, IContext context);
	
	void check(MarkerList markers, IContext context, ElementType target);
	
	void cleanup(IContext context, IClassCompilableList compilableList);
	
	void foldConstants();
	
	void write(ClassWriter writer);
	
	void write(MethodWriter writer);
	
	void write(MethodWriter writer, int index);
	
	void write(FieldVisitor writer);
	
	void write(AnnotationVisitor writer);
}
