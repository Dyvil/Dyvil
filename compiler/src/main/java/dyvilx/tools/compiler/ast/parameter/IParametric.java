package dyvilx.tools.compiler.ast.parameter;

import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.type.IType;

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

	IParameter createParameter(SourcePosition position, Name name, IType type, AttributeList attributes);
}
