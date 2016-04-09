package dyvil.tools.compiler.ast.classes;

import dyvil.tools.compiler.ast.constructor.ConstructorMatchList;
import dyvil.tools.compiler.ast.constructor.IConstructor;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.backend.IClassCompilable;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.util.Set;

public interface IClassMetadata extends IClassCompilable
{
	default void setInstanceField(IField field)
	{
	}
	
	default IField getInstanceField()
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
	 * Called before the class body goes through RESOLVE_TYPES. Super-types and -interfaces and type parameters have
	 * already been resolved.
	 */
	default void resolveTypesHeader(MarkerList markers, IContext context)
	{
	}
	
	/**
	 * Called after the class body went through RESOLVE_TYPES.<p/> Checks which synthetic members have to be generated
	 */
	default void resolveTypesBody(MarkerList markers, IContext context)
	{
	}

	/**
	 * Generates the signatures of the synthetic members. Concrete implementations should be added in {@link
	 * #resolve(MarkerList, IContext)}.
	 */
	default void resolveTypesGenerate(MarkerList markers, IContext context)
	{
	}
	
	default void resolve(MarkerList markers, IContext context)
	{
	}
	
	default void checkTypes(MarkerList markers, IContext context)
	{
	}

	default void check(MarkerList markers, IContext context)
	{
	}

	default void foldConstants()
	{
	}

	default void cleanup(IContext context, IClassCompilableList compilableList)
	{
	}
	
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
}
