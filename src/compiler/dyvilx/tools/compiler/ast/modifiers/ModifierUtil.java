package dyvilx.tools.compiler.ast.modifiers;

import dyvil.reflect.Modifiers;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.AnnotatableVisitor;
import dyvilx.tools.asm.AnnotationVisitor;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.member.IClassMember;
import dyvilx.tools.compiler.ast.member.IMember;
import dyvilx.tools.compiler.ast.member.MemberKind;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.transform.Deprecation;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.compiler.util.Util;
import dyvilx.tools.parsing.marker.Marker;
import dyvilx.tools.parsing.marker.MarkerList;

import java.lang.annotation.ElementType;

import static dyvil.reflect.Modifiers.*;

public final class ModifierUtil
{
	public static final String DYVIL_MODIFIERS   = "Ldyvil/annotation/internal/DyvilModifiers;";
	public static final String OVERRIDE_INTERNAL = "java/lang/Override";

	public static final String NATIVE_INTERNAL    = "dyvil/annotation/native";
	public static final String STRICTFP_INTERNAL  = "dyvil/annotation/strictfp";
	public static final String TRANSIENT_INTERNAL = "dyvil/annotation/transient";
	public static final String VOLATILE_INTERNAL  = "dyvil/annotation/volatile";

	public static final int JAVA_MODIFIER_MASK = 0xFFFF;

	private static final int DYVIL_MODIFIER_MASK = ~JAVA_MODIFIER_MASK // exclude java modifiers
		                                               & ~DEPRECATED & ~FUNCTIONAL & ~OVERRIDE
		                                               & ~GENERATED; // exclude source-only modifiers

	private static final int STATIC_ABSTRACT = STATIC | ABSTRACT;

	private ModifierUtil()
	{
	}

	public static String accessModifiersToString(int mod)
	{
		final StringBuilder builder = new StringBuilder();
		appendAccessModifiers(mod, builder);
		return builder.toString();
	}

	public static String classTypeToString(int mod)
	{
		StringBuilder stringBuilder = new StringBuilder();
		writeClassType(mod, stringBuilder);
		return stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString();
	}

	public static void writeClassType(int mod, StringBuilder sb)
	{
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

	public static void appendAccessModifiers(int mod, StringBuilder builder)
	{
		// @formatter:off
		if ((mod & PUBLIC) == PUBLIC) { builder.append("public "); }
		if ((mod & PACKAGE) == PACKAGE) { builder.append("package private "); }
		if ((mod & PRIVATE) == PRIVATE) { builder.append("private "); }
		if ((mod & PROTECTED) == PROTECTED) { builder.append("protected "); }
		if ((mod & INTERNAL) == INTERNAL) { builder.append("internal "); }
		// @formatter:on
	}

	public static void appendModifiers(int mod, MemberKind memberKind, StringBuilder builder)
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
			if ((mod & FUNCTIONAL) == FUNCTIONAL) { builder.append("functional "); }
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

	public static void checkVisibility(IMember member, SourcePosition position, MarkerList markers, IContext context)
	{
		Deprecation.checkAnnotations(member, position, markers);

		if (!(member instanceof IClassMember))
		{
			return;
		}

		switch (IContext.getVisibility(context, (IClassMember) member))
		{
		case IContext.INTERNAL:
			markers.add(Markers.semanticError(position, "access.internal", Util.memberNamed(member)));
			break;
		case IContext.INVISIBLE:
			markers.add(Markers.semanticError(position, "access.invisible", Util.memberNamed(member),
			                                  accessModifiersToString(member.getAccessLevel())));
			break;
		}
	}

	public static void checkOverride(IMethod member, IMethod overriden, MarkerList markers)
	{
		final int accessLevel = member.getAccessLevel() & ~Modifiers.INTERNAL;
		final int overrideFlags = overriden.getModifiers().toFlags();

		// Final Modifier Check
		if ((overrideFlags & Modifiers.FINAL) != 0)
		{
			markers.add(Markers.semanticError(member.getPosition(), "method.override.final", member.getName()));
		}

		// Visibility Check

		switch (overrideFlags & Modifiers.VISIBILITY_MODIFIERS)
		{
		case Modifiers.PRIVATE:
			markers.add(Markers.semanticError(member.getPosition(), "method.override.private", member.getName()));
			break;
		case Modifiers.PRIVATE_PROTECTED:
			if (accessLevel == Modifiers.PRIVATE_PROTECTED)
			{
				return;
			}
			// Fallthrough
		case Modifiers.PROTECTED:
			if (accessLevel == Modifiers.PROTECTED)
			{
				return;
			}
			// Fallthrough
		case Modifiers.PUBLIC:
			if (accessLevel == Modifiers.PUBLIC)
			{
				return;
			}
		}

		final Marker marker = Markers.semanticError(member.getPosition(), "method.override.visibility.mismatch",
		                                            member.getName());
		marker.addInfo(Markers.getSemantic("method.override.visibility", accessModifiersToString(overrideFlags)));
		markers.add(marker);
	}

	public static void checkMethodModifiers(MarkerList markers, IMethod member)
	{
		final ModifierSet modifiers = member.getModifiers();
		final int flags = modifiers.toFlags();

		final boolean hasValue = member.getValue() != null;
		final boolean isAbstract = (flags & ABSTRACT) != 0;
		final boolean isNative = (flags & NATIVE) != 0;

		// If the method does not have an implementation and is static
		if (isAbstract)
		{
			if (hasValue)
			{
				markers.add(Markers.semanticError(member.getPosition(), "modifiers.abstract.implemented",
				                                  Util.memberNamed(member)));
			}
			if (isNative)
			{
				markers.add(
					Markers.semanticError(member.getPosition(), "modifiers.native.abstract", Util.memberNamed(member)));
			}

			final IClass enclosingClass = member.getEnclosingClass();
			if (!enclosingClass.isAbstract())
			{
				markers.add(Markers.semanticError(member.getPosition(), "modifiers.abstract.concrete_class",
				                                  Util.memberNamed(member), enclosingClass.getName()));
			}

			return;
		}
		if (hasValue)
		{
			if (isNative)
			{
				markers.add(Markers.semanticError(member.getPosition(), "modifiers.native.implemented",
				                                  Util.memberNamed(member)));
			}

			return;
		}

		if (!isNative)
		{
			markers
				.add(Markers.semanticError(member.getPosition(), "modifiers.unimplemented", Util.memberNamed(member)));
		}
	}

	public static long getFlags(IClassMember member)
	{
		final int flags = member.getModifiers().toFlags();
		int javaModifiers = flags & JAVA_MODIFIER_MASK;
		int dyvilModifiers = flags & DYVIL_MODIFIER_MASK;

		if ((flags & PRIVATE_PROTECTED) == PRIVATE_PROTECTED)
		{
			// for private protected members, move the private modifier

			javaModifiers &= ~PRIVATE;
			dyvilModifiers |= PRIVATE;
		}
		if (member.getElementType() == ElementType.METHOD)
		{
			if ((flags & STATIC_ABSTRACT) == STATIC_ABSTRACT)
			{
				// for static abstract methods, move the abstract modifier

				javaModifiers &= ~ABSTRACT;
				dyvilModifiers |= ABSTRACT;
			}
			if ((flags & FINAL) != 0)
			{
				final IClass enclosingClass = member.getEnclosingClass();
				if (enclosingClass != null && enclosingClass.isInterface())
				{
					// for final interface methods, move the final modifier

					javaModifiers &= ~FINAL;
					dyvilModifiers |= FINAL;
				}
			}
		}
		return (long) dyvilModifiers << 32 | javaModifiers;
	}

	public static int getJavaModifiers(long flags)
	{
		return (int) flags;
	}

	public static void writeModifiers(AnnotatableVisitor mw, IClassMember member, long flags)
	{
		final ModifierSet modifiers = member.getModifiers();
		if (modifiers == null)
		{
			return;
		}

		final int dyvilModifiers = (int) (flags >> 32);
		if (dyvilModifiers != 0)
		{
			final AnnotationVisitor annotationVisitor = mw.visitAnnotation(DYVIL_MODIFIERS, true);
			annotationVisitor.visit("value", dyvilModifiers);
			annotationVisitor.visitEnd();
		}
	}
}
