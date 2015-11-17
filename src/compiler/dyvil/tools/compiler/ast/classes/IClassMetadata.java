package dyvil.tools.compiler.ast.classes;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.util.Set;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.method.ConstructorMatchList;
import dyvil.tools.compiler.ast.method.IConstructor;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

public interface IClassMetadata
{
	default void setInstanceField(IDataMember field)
	{
	}
	
	default IDataMember getInstanceField()
	{
		return null;
	}
	
	default IConstructor getConstructor()
	{
		return null;
	}
	
	// Annotations
	
	default RetentionPolicy getRetention()
	{
		return null;
	}
	
	default boolean isTarget(ElementType target)
	{
		return false;
	}
	
	default Set<ElementType> getTargets()
	{
		return null;
	}
	
	// Resolve
	
	/**
	 * Called before the class body goes through RESOLVE_TYPES. Super-types and
	 * -interfaces and generics have already been resolved.
	 */
	void resolveTypes(MarkerList markers, IContext context);
	
	/**
	 * Called after the class body went through RESOLVE_TYPES.
	 */
	void resolveTypesBody(MarkerList markers, IContext context);
	
	void resolve(MarkerList markers, IContext context);
	
	void checkTypes(MarkerList markers, IContext context);
	
	default IDataMember resolveField(Name name)
	{
		return null;
	}
	
	default void getMethodMatches(MethodMatchList list, IValue instance, Name name, IArguments arguments)
	{
	}
	
	default void getConstructorMatches(ConstructorMatchList list, IArguments arguments)
	{
	}
	
	// Compilation
	
	default void writeStaticInit(MethodWriter writer) throws BytecodeException
	{
	}
	
	void write(ClassWriter writer, IValue instanceFields) throws BytecodeException;
}
