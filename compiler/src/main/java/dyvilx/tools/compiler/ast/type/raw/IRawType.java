package dyvilx.tools.compiler.ast.type.raw;

import dyvilx.tools.asm.TypeAnnotatableVisitor;
import dyvilx.tools.asm.TypePath;
import dyvilx.tools.compiler.ast.attribute.annotation.Annotation;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.generic.ITypeParameter;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.parsing.marker.MarkerList;

public interface IRawType extends IObjectType
{
	@Override
	default IType asParameterType()
	{
		return this;
	}

	@Override
	default boolean hasTypeVariables()
	{
		return false;
	}

	@Override
	default boolean isGenericType()
	{
		return false;
	}

	@Override
	default IType getConcreteType(ITypeContext context)
	{
		return this;
	}

	@Override
	default void inferTypes(IType concrete, ITypeContext typeContext)
	{
	}

	@Override
	default IType resolveType(ITypeParameter typeParameter)
	{
		final IClass theClass = this.getTheClass();
		if (theClass == null || typeParameter.getGeneric() == theClass)
		{
			return null;
		}

		return theClass.resolveType(typeParameter, this);
	}

	@Override
	default void checkType(MarkerList markers, IContext context, int position)
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

	@Override
	default void addAnnotation(Annotation annotation, TypePath typePath, int step, int steps)
	{
	}

	@Override
	default void writeAnnotations(TypeAnnotatableVisitor visitor, int typeRef, String typePath)
	{
	}
}
