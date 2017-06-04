package dyvil.tools.compiler.ast.parameter;

import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.parsing.Name;
import dyvil.source.position.SourcePosition;

public interface IParametric
{
	default boolean setThisType(IType type)
	{
		return false;
	}

	ParameterList getParameters();

	default boolean isVariadic()
	{
		return false;
	}

	IParameter createParameter(SourcePosition position, Name name, IType type, ModifierSet modifiers, AnnotationList annotations);
}
