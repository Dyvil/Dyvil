package dyvil.tools.compiler.ast.parameter;

import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.modifiers.IModified;
import dyvil.tools.compiler.ast.type.IType;

public interface IParameterized extends INamed, IModified, IParameterList
{
	default boolean setReceiverType(IType type)
	{
		return false;
	}
}
