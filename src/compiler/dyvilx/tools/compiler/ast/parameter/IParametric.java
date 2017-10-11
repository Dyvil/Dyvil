package dyvilx.tools.compiler.ast.parameter;

import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.modifiers.ModifierSet;
import dyvilx.tools.compiler.ast.type.IType;
import dyvil.lang.Name;
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

	IParameter createParameter(SourcePosition position, Name name, IType type, ModifierSet modifiers, AttributeList annotations);
}
