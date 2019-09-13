package dyvilx.tools.compiler.ast.member;

import dyvil.reflect.Modifiers;
import dyvilx.tools.compiler.ast.attribute.Attributable;
import dyvilx.tools.compiler.ast.type.Typed;
import dyvilx.tools.compiler.phase.Resolvable;
import dyvilx.tools.parsing.ASTNode;

import static dyvil.reflect.Modifiers.PRIVATE;
import static dyvil.reflect.Modifiers.PRIVATE_PROTECTED;

public interface Member extends ASTNode, Resolvable, Named, Typed, Attributable
{
	MemberKind getKind();

	String getInternalName();

	@Override
	default int getJavaFlags()
	{
		int javaFlags = Attributable.super.getJavaFlags();
		if ((javaFlags & PRIVATE_PROTECTED) == PRIVATE_PROTECTED)
		{
			javaFlags &= ~PRIVATE;
		}
		return javaFlags;
	}

	@Override
	default void setJavaFlags(int javaFlags)
	{
		if ((javaFlags & Modifiers.VISIBILITY_MODIFIERS) == 0)
		{
			javaFlags |= Modifiers.PACKAGE;
		}
		Attributable.super.setJavaFlags(javaFlags);
	}

	@Override
	default long getDyvilFlags()
	{
		long dyvilFlags = Attributable.super.getDyvilFlags();
		if ((dyvilFlags & PRIVATE_PROTECTED) == PRIVATE_PROTECTED)
		{
			dyvilFlags |= PRIVATE;
		}
		return dyvilFlags;
	}
}
