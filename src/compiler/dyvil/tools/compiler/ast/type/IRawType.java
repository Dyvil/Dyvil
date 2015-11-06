package dyvil.tools.compiler.ast.type;

import dyvil.tools.asm.TypeAnnotatableVisitor;
import dyvil.tools.asm.TypePath;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.parsing.marker.MarkerList;

public interface IRawType extends IObjectType
{
	@Override
	public default IType getConcreteType(ITypeContext context)
	{
		return this;
	}
	
	@Override
	public default boolean isGenericType()
	{
		return false;
	}
	
	@Override
	public default boolean hasTypeVariables()
	{
		return false;
	}
	
	@Override
	public default void inferTypes(IType concrete, ITypeContext typeContext)
	{
	}
	
	@Override
	public default IType resolveType(ITypeVariable typeVar)
	{
		return null;
	}
	
	@Override
	default IType resolveType(MarkerList markers, IContext context)
	{
		return null;
	}
	
	@Override
	default void resolve(MarkerList markers, IContext context)
	{
		IObjectType.super.resolve(markers, context);
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
	public default String getSignature()
	{
		return null;
	}
	
	@Override
	public default void addAnnotation(IAnnotation annotation, TypePath typePath, int step, int steps)
	{
	}
	
	@Override
	public default void writeAnnotations(TypeAnnotatableVisitor visitor, int typeRef, String typePath)
	{
	}
}
