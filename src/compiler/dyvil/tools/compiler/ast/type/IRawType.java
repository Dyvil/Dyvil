package dyvil.tools.compiler.ast.type;

import dyvil.tools.asm.TypeAnnotatableVisitor;
import dyvil.tools.asm.TypePath;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.parsing.marker.MarkerList;

public interface IRawType extends IObjectType
{
	@Override
	default IType getConcreteType(ITypeContext context)
	{
		return this;
	}
	
	@Override
	default boolean isGenericType()
	{
		return false;
	}

	@Override
	default boolean hasTypeVariables()
	{
		return false;
	}
	
	@Override
	default void inferTypes(IType concrete, ITypeContext typeContext)
	{
	}
	
	@Override
	default IType resolveType(ITypeParameter typeParameter)
	{
		final IClass theClass = this.getTheClass();
		return theClass == null ? null : theClass.resolveType(typeParameter, this);
	}
	
	@Override
	default void checkType(MarkerList markers, IContext context, TypePosition position)
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
	default void cleanup(IContext context, IClassCompilableList compilableList)
	{
	}
	
	@Override
	default String getSignature()
	{
		return null;
	}

	@Override
	default void appendExtendedName(StringBuilder buffer)
	{
		buffer.append('L').append(this.getInternalName()).append(';');
	}

	@Override
	default void appendSignature(StringBuilder buffer)
	{
		this.appendExtendedName(buffer);
	}

	@Override
	default void addAnnotation(IAnnotation annotation, TypePath typePath, int step, int steps)
	{
	}
	
	@Override
	default void writeAnnotations(TypeAnnotatableVisitor visitor, int typeRef, String typePath)
	{
	}
}
