package dyvilx.tools.compiler.ast.classes.metadata;

import dyvilx.tools.compiler.ast.constructor.IConstructor;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.field.IField;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.header.IClassCompilable;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.backend.ClassWriter;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.phase.IResolvable;
import dyvil.lang.Name;
import dyvilx.tools.parsing.marker.MarkerList;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.util.Set;

public interface IClassMetadata extends IClassCompilable, IResolvable
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

	default IMethod getFunctionalMethod()
	{
		return null;
	}

	default void setFunctionalMethod(IMethod method)
	{
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

	@Override
	default void resolveTypes(MarkerList markers, IContext context)
	{
		throw new UnsupportedOperationException();
	}

	default void resolveTypesPre(MarkerList markers, IContext context)
	{
	}

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

	@Override
	default void resolve(MarkerList markers, IContext context)
	{
	}

	@Override
	default void checkTypes(MarkerList markers, IContext context)
	{
	}

	@Override
	default void check(MarkerList markers, IContext context)
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

	default boolean checkImplements(IMethod candidate, ITypeContext typeContext)
	{
		return false;
	}

	default IDataMember resolveField(Name name)
	{
		return null;
	}

	default void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, ArgumentList arguments)
	{
	}

	default void getConstructorMatches(MatchList<IConstructor> list, ArgumentList arguments)
	{
	}

	default void writePost(ClassWriter writer)
	{
	}

	default void writeStaticInitPost(MethodWriter writer) throws BytecodeException
	{
	}

	default void writeClassInitPost(MethodWriter writer) throws BytecodeException
	{
	}
}
