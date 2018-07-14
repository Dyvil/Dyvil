package dyvilx.tools.compiler.check;

import dyvil.reflect.Modifiers;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.attribute.Attribute;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.attribute.modifiers.ModifierUtil;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.member.IClassMember;
import dyvilx.tools.compiler.ast.member.Member;
import dyvilx.tools.compiler.ast.member.MemberKind;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.transform.Deprecation;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.compiler.util.Util;
import dyvilx.tools.parsing.marker.Marker;
import dyvilx.tools.parsing.marker.MarkerList;

import static dyvil.reflect.Modifiers.ABSTRACT;
import static dyvil.reflect.Modifiers.NATIVE;

public class ModifierChecks
{
	public static void checkModifiers(Member member, MarkerList markers)
	{
		final AttributeList attributes = member.getAttributes();
		final MemberKind memberKind = member.getKind();
		final int defaultAccess = memberKind.getDefaultAccess(member);
		StringBuilder errorBuilder = null;

		for (Attribute modifier : attributes)
		{
			if (!memberKind.isAttributeAllowed(modifier))
			{
				if (errorBuilder == null)
				{
					errorBuilder = new StringBuilder();
				}
				else
				{
					errorBuilder.append(", ");
				}
				modifier.toString(errorBuilder);
			}

			final int visibility = modifier.flags() & Modifiers.VISIBILITY_MODIFIERS;
			if (visibility != 0 && visibility == defaultAccess)
			{
				markers.add(Markers.semantic(member.getPosition(), "modifiers.visibility.default",
				                             Util.memberNamed(member),
				                             ModifierUtil.accessModifiersToString(visibility)));
			}
		}

		if (errorBuilder != null)
		{
			markers.add(Markers.semanticError(member.getPosition(), "modifiers.illegal", Util.memberNamed(member),
			                                  errorBuilder.toString()));
		}
		if (!attributes.hasAnyFlag(Modifiers.VISIBILITY_MODIFIERS))
		{
			// If there is no explicit or implicit visibility modifier already, add the default one
			attributes.addFlag(defaultAccess);
		}
	}

	public static void checkVisibility(Member member, SourcePosition position, MarkerList markers, IContext context)
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
			                                  ModifierUtil.accessModifiersToString(member.getAccessLevel())));
			break;
		}
	}

	public static void checkOverride(IMethod member, IMethod overriden, MarkerList markers)
	{
		final int accessLevel = member.getAccessLevel() & ~Modifiers.INTERNAL;
		final int overrideFlags = overriden.getAttributes().flags();

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
		marker.addInfo(Markers.getSemantic("method.override.visibility", ModifierUtil
			                                                                 .accessModifiersToString(overrideFlags)));
		markers.add(marker);
	}

	public static void checkMethodModifiers(MarkerList markers, IMethod member)
	{
		final int flags = member.getAttributes().flags();

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
}
