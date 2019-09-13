package dyvilx.tools.compiler.ast.attribute.modifiers;

import dyvilx.tools.asm.AnnotatableVisitor;
import dyvilx.tools.asm.AnnotationVisitor;
import dyvilx.tools.compiler.ast.member.MemberKind;

import static dyvil.reflect.Modifiers.*;

public final class ModifierUtil
{
	public static final String DYVIL_MODIFIERS   = "Ldyvil/annotation/internal/DyvilModifiers;";

	public static final String NATIVE_INTERNAL    = "dyvil/annotation/native";
	public static final String STRICTFP_INTERNAL  = "dyvil/annotation/strictfp";
	public static final String TRANSIENT_INTERNAL = "dyvil/annotation/transient";
	public static final String VOLATILE_INTERNAL  = "dyvil/annotation/volatile";

	private ModifierUtil()
	{
	}

	public static String accessModifiersToString(long mod)
	{
		final StringBuilder builder = new StringBuilder();
		appendAccessModifiers(mod, builder);
		return builder.substring(0, builder.length() - 1);
	}

	public static String classTypeToString(long mod)
	{
		StringBuilder stringBuilder = new StringBuilder();
		writeClassType(mod, stringBuilder);
		return stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString();
	}

	public static void writeClassType(long mod, StringBuilder sb)
	{
		if ((mod & CASE_CLASS) != 0)
		{
			sb.append("case ");
		}

		if (mod == 0)
		{
			sb.append("class ");
			return;
		}
		if ((mod & ANNOTATION_CLASS) == ANNOTATION_CLASS)
		{
			sb.append("@interface ");
			return;
		}
		if ((mod & TRAIT_CLASS) == TRAIT_CLASS)
		{
			sb.append("trait ");
			return;
		}
		if ((mod & INTERFACE_CLASS) == INTERFACE_CLASS)
		{
			sb.append("interface ");
			return;
		}
		if ((mod & ENUM_CLASS) == ENUM_CLASS)
		{
			sb.append("enum ");
			return;
		}
		if ((mod & OBJECT_CLASS) == OBJECT_CLASS)
		{
			sb.append("object ");
			return;
		}

		sb.append("class ");
	}

	public static void appendAccessModifiers(long mod, StringBuilder builder)
	{
		// @formatter:off
		if ((mod & PUBLIC) == PUBLIC) { builder.append("public "); }
		if ((mod & PACKAGE) == PACKAGE) { builder.append("package private "); }
		if ((mod & PRIVATE) == PRIVATE) { builder.append("private "); }
		if ((mod & PROTECTED) == PROTECTED) { builder.append("protected "); }
		if ((mod & INTERNAL) == INTERNAL) { builder.append("internal "); }
		// @formatter:on
	}

	public static void appendModifiers(long mod, MemberKind memberKind, StringBuilder builder)
	{
		// @formatter:off
		if ((mod & TRANSIENT) == TRANSIENT) { builder.append("@transient "); }
		if ((mod & VOLATILE) == VOLATILE) { builder.append("@volatile "); }
		if ((mod & NATIVE) == NATIVE) { builder.append("@native "); }
		if ((mod & STRICT) == STRICT) { builder.append("@strictfp "); }
		if ((mod & MANDATED) == MANDATED) { builder.append("/*mandated*/ "); }
		if ((mod & SYNTHETIC) == SYNTHETIC) { builder.append("/*synthetic*/ "); }
		if ((mod & BRIDGE) == BRIDGE) { builder.append("/*bridge*/ "); }

		// Access Modifiers
		appendAccessModifiers(mod, builder);

		if ((mod & EXPLICIT) == EXPLICIT) { builder.append("explicit "); }

		if (memberKind == MemberKind.METHOD)
		{
			if ((mod & EXTENSION) == EXTENSION) { builder.append("extension "); }
			else if ((mod & INFIX) == INFIX) { builder.append("infix "); }
			else if ((mod & STATIC) == STATIC) { builder.append("static "); }
		}
		else if ((mod & STATIC) == STATIC) { builder.append("static "); }

		if ((mod & ABSTRACT) == ABSTRACT) { builder.append("abstract "); }
		if ((mod & FINAL) == FINAL) { builder.append("final "); }

		if (memberKind == MemberKind.CLASS)
		{
			if ((mod & CASE_CLASS) == CASE_CLASS) { builder.append("case "); }
		}
		else if (memberKind == MemberKind.FIELD)
		{
			if ((mod & LAZY) == LAZY) { builder.append("lazy "); }
		}
		else if (memberKind == MemberKind.METHOD)
		{
			if ((mod & SYNCHRONIZED) == SYNCHRONIZED) { builder.append("synchronized "); }
			if ((mod & IMPLICIT) == IMPLICIT) { builder.append("implicit "); }
			if ((mod & INLINE) == INLINE) { builder.append("inline "); }
			if ((mod & OVERRIDE) == OVERRIDE) { builder.append("override "); }
		}
		// @formatter:on
	}

	public static void writeDyvilModifiers(AnnotatableVisitor visitor, long dyvilModifiers)
	{
		if (dyvilModifiers != 0)
		{
			final AnnotationVisitor annotationVisitor = visitor.visitAnnotation(DYVIL_MODIFIERS, true);
			annotationVisitor.visit("value", dyvilModifiers);
			annotationVisitor.visitEnd();
		}
	}
}
