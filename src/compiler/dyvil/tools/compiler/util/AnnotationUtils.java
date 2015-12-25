package dyvil.tools.compiler.util;

import dyvil.tools.asm.AnnotatableVisitor;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;

public final class AnnotationUtils
{
	public static final String IMMUTABLE = "dyvil/annotation/Immutable";

	public static final String IMMUTABE_EXTENDED = "Ldyvil/annotation/Immutable;";

	public static final String DYVIL_MODIFIERS = "Ldyvil/annotation/_internal/DyvilModifiers;";

	public static final String CLASS_PARAMETERS = "Ldyvil/annotation/_internal/ClassParameters;";

	private AnnotationUtils()
	{
		// no instances
	}

	public static void writeModifiers(AnnotatableVisitor mw, ModifierSet modifiers)
	{
		if (modifiers == null)
		{
			return;
		}

		final int dyvilModifiers = modifiers.toFlags() & ~0xFFFF;
		if (dyvilModifiers != 0)
		{
			mw.visitAnnotation(DYVIL_MODIFIERS, true).visit("value", dyvilModifiers);
		}
	}
}
