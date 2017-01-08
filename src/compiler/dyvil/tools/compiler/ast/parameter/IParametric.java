package dyvil.tools.compiler.ast.parameter;

import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.position.ICodePosition;

public interface IParametric
{
	default boolean setReceiverType(IType type)
	{
		return false;
	}

	IParameterList getParameterList();

	default boolean isVariadic()
	{
		return false;
	}

	default IParameter createParameter(ICodePosition position, Name name, IType type, ModifierSet modifiers, AnnotationList annotations)
	{
		return new CodeParameter(position, name, type, modifiers, annotations);
	}
}
