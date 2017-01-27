package dyvil.tools.compiler.ast.modifiers;

import dyvil.reflect.Modifiers;
import dyvil.tools.asm.AnnotatableVisitor;
import dyvil.tools.asm.AnnotationVisitor;
import dyvil.tools.compiler.ast.annotation.AnnotationUtil;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.transform.Deprecation;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.IParserManager;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;
import dyvil.tools.parsing.token.IToken;

public final class ModifierUtil
{
	public static final int JAVA_MODIFIER_MASK = 0xFFFF;

	private static final int MODIFIERS_MASK = ~JAVA_MODIFIER_MASK // exclude java modifiers
		                                          & ~Modifiers.DEPRECATED & ~Modifiers.FUNCTIONAL
		                                          & ~Modifiers.OVERRIDE; // exclude source-only modifiers

	private static final int STATIC_ABSTRACT = (Modifiers.STATIC | Modifiers.ABSTRACT);

	private ModifierUtil()
	{
	}

	public static String accessModifiersToString(int mod)
	{
		final StringBuilder builder = new StringBuilder();
		writeAccessModifiers(mod, builder);
		return builder.toString();
	}

	public static void writeAccessModifiers(int mod, StringBuilder sb)
	{
		if ((mod & Modifiers.PUBLIC) == Modifiers.PUBLIC)
		{
			sb.append("public ");
		}
		if ((mod & Modifiers.PACKAGE) == Modifiers.PACKAGE)
		{
			sb.append("package ");
		}
		if ((mod & Modifiers.PRIVATE) == Modifiers.PRIVATE)
		{
			sb.append("private ");
		}
		if ((mod & Modifiers.PROTECTED) == Modifiers.PROTECTED)
		{
			sb.append("protected ");
		}
		if ((mod & Modifiers.INTERNAL) == Modifiers.INTERNAL)
		{
			sb.append("internal ");
		}
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
		if ((mod & Modifiers.ANNOTATION) == Modifiers.ANNOTATION)
		{
			sb.append("@interface ");
			return;
		}
		if ((mod & Modifiers.TRAIT_CLASS) == Modifiers.TRAIT_CLASS)
		{
			sb.append("trait ");
			return;
		}
		if ((mod & Modifiers.INTERFACE_CLASS) == Modifiers.INTERFACE_CLASS)
		{
			sb.append("interface ");
			return;
		}
		if ((mod & Modifiers.ENUM) == Modifiers.ENUM)
		{
			sb.append("enum ");
			return;
		}
		if ((mod & Modifiers.OBJECT_CLASS) == Modifiers.OBJECT_CLASS)
		{
			sb.append("object ");
			return;
		}

		sb.append("class ");
	}

	public static void writeClassModifiers(int mod, StringBuilder sb)
	{
		if ((mod & Modifiers.STATIC) == Modifiers.STATIC)
		{
			sb.append("static ");
		}
		if ((mod & Modifiers.ABSTRACT) == Modifiers.ABSTRACT)
		{
			sb.append("abstract ");
		}
		if ((mod & Modifiers.FINAL) == Modifiers.FINAL)
		{
			sb.append("final ");
		}
		if ((mod & Modifiers.SEALED) == Modifiers.SEALED)
		{
			sb.append("sealed ");
		}
		if ((mod & Modifiers.STRICT) == Modifiers.STRICT)
		{
			sb.append("@Strict ");
		}
		if ((mod & Modifiers.FUNCTIONAL) == Modifiers.FUNCTIONAL)
		{
			sb.append("functional ");
		}
		if ((mod & Modifiers.CASE_CLASS) == Modifiers.CASE_CLASS)
		{
			sb.append("case ");
		}
	}

	public static void writeFieldModifiers(int mod, StringBuilder sb)
	{
		if ((mod & Modifiers.LAZY) == Modifiers.LAZY)
		{
			sb.append("lazy ");
		}
		else if ((mod & Modifiers.CONST) == Modifiers.CONST)
		{
			sb.append("const ");
		}
		else
		{
			if ((mod & Modifiers.STATIC) == Modifiers.STATIC)
			{
				sb.append("static ");
			}
			if ((mod & Modifiers.FINAL) == Modifiers.FINAL)
			{
				sb.append("final ");
			}
		}

		if ((mod & Modifiers.TRANSIENT) == Modifiers.TRANSIENT)
		{
			sb.append("@Transient ");
		}
		if ((mod & Modifiers.VOLATILE) == Modifiers.VOLATILE)
		{
			sb.append("@Volatile ");
		}
	}

	public static void writeMethodModifiers(int mod, StringBuilder sb)
	{
		if ((mod & Modifiers.EXTENSION) == Modifiers.EXTENSION)
		{
			sb.append("extension ");
		}
		else if ((mod & Modifiers.INFIX) != 0 && (mod & Modifiers.INFIX) != Modifiers.STATIC)
		{
			sb.append("infix ");
		}
		else if ((mod & Modifiers.STATIC) == Modifiers.STATIC)
		{
			sb.append("static ");
		}

		if ((mod & Modifiers.FINAL) == Modifiers.FINAL)
		{
			sb.append("final ");
		}
		if ((mod & Modifiers.SEALED) == Modifiers.SEALED)
		{
			sb.append("sealed ");
		}

		if ((mod & Modifiers.SYNCHRONIZED) == Modifiers.SYNCHRONIZED)
		{
			sb.append("synchronized ");
		}
		if ((mod & Modifiers.NATIVE) == Modifiers.NATIVE)
		{
			sb.append("@Native ");
		}
		if ((mod & Modifiers.ABSTRACT) == Modifiers.ABSTRACT)
		{
			sb.append("abstract ");
		}
		if ((mod & Modifiers.STRICT) == Modifiers.STRICT)
		{
			sb.append("@Strict ");
		}
		if ((mod & Modifiers.INLINE) == Modifiers.INLINE)
		{
			sb.append("inline ");
		}
		if ((mod & Modifiers.OVERRIDE) == Modifiers.OVERRIDE)
		{
			sb.append("override ");
		}
	}

	public static void writeParameterModifier(int mod, StringBuilder sb)
	{
		if ((mod & Modifiers.LAZY) == Modifiers.LAZY)
		{
			sb.append("lazy ");
		}
		if ((mod & Modifiers.FINAL) == Modifiers.FINAL)
		{
			sb.append("final ");
		}
	}

	public static void checkVisibility(IMember member, ICodePosition position, MarkerList markers, IContext context)
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

	public static void checkMethodModifiers(MarkerList markers, IMethod member)
	{
		final ModifierSet modifiers = member.getModifiers();
		final int flags = modifiers.toFlags();

		final boolean hasValue = member.getValue() != null;
		final boolean isAbstract = (flags & Modifiers.ABSTRACT) != 0;
		final boolean isNative = (flags & Modifiers.NATIVE) != 0;

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

	public static int getFlags(IClassMember method)
	{
		int flags = method.getModifiers().toFlags();
		if ((flags & STATIC_ABSTRACT) == STATIC_ABSTRACT)
		{
			flags &= ~Modifiers.ABSTRACT;
		}
		if ((flags & Modifiers.FINAL) != 0)
		{
			final IClass enclosingClass = method.getEnclosingClass();
			if (enclosingClass != null && enclosingClass.isInterface())
			{
				flags &= ~Modifiers.FINAL;
			}
		}
		return flags;
	}

	public static void writeModifiers(AnnotatableVisitor mw, IClassMember member)
	{
		final ModifierSet modifiers = member.getModifiers();
		if (modifiers == null)
		{
			return;
		}

		final int flags = modifiers.toFlags();
		int dyvilModifiers = flags & MODIFIERS_MASK;

		if ((flags & STATIC_ABSTRACT) == STATIC_ABSTRACT)
		{
			dyvilModifiers |= Modifiers.ABSTRACT;
		}
		if ((flags & Modifiers.FINAL) != 0)
		{
			final IClass enclosingClass = member.getEnclosingClass();
			if (enclosingClass != null && enclosingClass.isInterface())
			{
				dyvilModifiers |= Modifiers.FINAL;
			}
		}

		if (dyvilModifiers != 0)
		{
			final AnnotationVisitor annotationVisitor = mw.visitAnnotation(AnnotationUtil.DYVIL_MODIFIERS, true);
			annotationVisitor.visit("value", dyvilModifiers);
			annotationVisitor.visitEnd();
		}
	}
}
