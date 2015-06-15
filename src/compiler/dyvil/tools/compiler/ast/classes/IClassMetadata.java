package dyvil.tools.compiler.ast.classes;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import dyvil.lang.List;
import java.util.Set;

import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.IConstructor;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public interface IClassMetadata
{
	public default void setInstanceField(IField field)
	{
	}
	
	public default IField getInstanceField()
	{
		return null;
	}
	
	public default IConstructor getConstructor()
	{
		return null;
	}
	
	// Annotations
	
	public default RetentionPolicy getRetention()
	{
		return null;
	}
	
	public default boolean isTarget(ElementType target)
	{
		return false;
	}
	
	public default Set<ElementType> getTargets()
	{
		return null;
	}
	
	// Resolve
	
	public void resolve(MarkerList markers, IContext context);
	
	public void checkTypes(MarkerList markers, IContext context);
	
	public default IField resolveField(Name name)
	{
		return null;
	}
	
	public default void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
	}
	
	public default void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
	{
	}
	
	// Compilation
	
	public void write(ClassWriter writer, IValue instanceFields) throws BytecodeException;
}
