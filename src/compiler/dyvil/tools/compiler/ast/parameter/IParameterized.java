package dyvil.tools.compiler.ast.parameter;

import dyvil.tools.compiler.ast.member.IModified;
import dyvil.tools.compiler.ast.member.INamed;

public interface IParameterized extends INamed, IModified, IParameterList
{
	public default boolean isClass()
	{
		return false;
	}
}
