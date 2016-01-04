package dyvil.tools.compiler.util;

import dyvil.reflect.Modifiers;
import dyvil.tools.asm.AnnotatableVisitor;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;

public final class AnnotationUtils
{
	public static final String DYVIL_MODIFIERS = "Ldyvil/annotation/_internal/DyvilModifiers;";

	public static final String CLASS_PARAMETERS = "Ldyvil/annotation/_internal/ClassParameters;";

	private static final int MODIFIERS_MASK =
			~0xFFFF & ~Modifiers.DEPRECATED & ~Modifiers.FUNCTIONAL & ~Modifiers.OVERRIDE;

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

		final int dyvilModifiers = modifiers.toFlags() & MODIFIERS_MASK;
		if (dyvilModifiers != 0)
		{
			mw.visitAnnotation(DYVIL_MODIFIERS, true).visit("value", dyvilModifiers);
		}
	}
}
