package dyvil.tools.compiler.transform;

import dyvil.annotation.Deprecated.Reason;
import dyvil.annotation.Experimental.Stage;
import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.AnnotationUtil;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.ArrayExpr;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.constant.EnumValue;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.ParameterList;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.raw.ClassType;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.source.position.SourcePosition;
import dyvil.util.MarkerLevel;

public final class Deprecation
{
	public static final Name Deprecated   = Name.fromRaw("Deprecated");
	public static final Name Experimental = Name.fromRaw("Experimental");
	public static final Name UsageInfo    = Name.fromRaw("UsageInfo");
	public static final Name description  = Name.fromRaw("description");
	public static final Name level        = Name.fromRaw("level");

	public static final String JAVA_INTERNAL  = "java/lang/Deprecated";
	public static final String DYVIL_INTERNAL = "dyvil/annotation/Deprecated";
	public static final String DYVIL_EXTENDED = 'L' + DYVIL_INTERNAL + ';';

	public static final IClass DEPRECATED_CLASS   = Package.dyvilAnnotation.resolveClass(Deprecated);
	public static final IClass EXPERIMENTAL_CLASS = Package.dyvilAnnotation.resolveClass(Experimental);
	public static final IClass USAGE_INFO_CLASS   = Package.dyvilAnnotation.resolveClass(UsageInfo);

	public static final IType DEPRECATED = new ClassType(DEPRECATED_CLASS);

	// Deprecated Parameters

	private static final ParameterList DEP_PARAMS = DEPRECATED_CLASS.getParameters();

	private static final IParameter DEP_VALUE_PARAM   = DEP_PARAMS.get(Names.value);
	private static final IParameter DEP_DESC_PARAM    = DEP_PARAMS.get(description);
	private static final IParameter DEP_SINCE_PARAM   = DEP_PARAMS.get(Name.fromRaw("since"));
	private static final IParameter DEP_UNTIL_PARAM   = DEP_PARAMS.get(Name.fromRaw("forRemoval"));
	private static final IParameter DEP_REASONS_PARAM = DEP_PARAMS.get(Name.fromRaw("reasons"));
	private static final IParameter DEP_REPLACE_PARAM = DEP_PARAMS.get(Name.fromRaw("replacements"));
	private static final IParameter DEP_LEVEL_PARAM   = DEP_PARAMS.get(level);

	// Experimental Parameters

	private static final ParameterList EXP_PARAMS = EXPERIMENTAL_CLASS.getParameters();

	private static final IParameter EXP_VALUE_PARAM = EXP_PARAMS.get(Names.value);
	private static final IParameter EXP_DESC_PARAM  = EXP_PARAMS.get(description);
	private static final IParameter EXP_STAGE_PARAM = EXP_PARAMS.get(Name.fromRaw("stage"));
	private static final IParameter EXP_LEVEL_PARAM = EXP_PARAMS.get(level);

	// UsageInfo Parameters

	private static final ParameterList INF_PARAMS = USAGE_INFO_CLASS.getParameters();

	private static final IParameter INF_VALUE_PARAM = INF_PARAMS.get(Names.value);
	private static final IParameter INF_DESC_PARAM  = INF_PARAMS.get(description);
	private static final IParameter INF_LEVEL_PARAM = INF_PARAMS.get(level);

	public static void checkAnnotations(IMember member, SourcePosition position, MarkerList markers)
	{
		if (member.hasModifier(Modifiers.DEPRECATED))
		{
			checkDeprecation(member, position, markers);
		}

		IAnnotation annotation = member.getAnnotation(EXPERIMENTAL_CLASS);
		if (annotation != null)
		{
			checkExperimental(member, position, markers, annotation);
		}

		annotation = member.getAnnotation(USAGE_INFO_CLASS);
		if (annotation != null)
		{
			checkUsageInfo(member, position, markers, annotation);
		}
	}

	private static void checkDeprecation(IMember member, SourcePosition position, MarkerList markers)
	{
		IAnnotation annotation = member.getAnnotation(DEPRECATED_CLASS);
		if (annotation == null)
		{
			annotation = new Annotation(DEPRECATED);
		}

		final ArgumentList arguments = annotation.getArguments();
		final MarkerLevel markerLevel = AnnotationUtil.getEnumValue(arguments, DEP_LEVEL_PARAM, MarkerLevel.class);

		if (markerLevel == null || markerLevel == MarkerLevel.IGNORE)
		{
			return;
		}

		String value = AnnotationUtil.getStringValue(arguments, DEP_VALUE_PARAM);
		final String description = AnnotationUtil.getStringValue(arguments, DEP_DESC_PARAM);
		final String since = AnnotationUtil.getStringValue(arguments, DEP_SINCE_PARAM);
		final String forRemoval = AnnotationUtil.getStringValue(arguments, DEP_UNTIL_PARAM);

		value = replaceMember(member, value);
		if (since != null)
		{
			value = value.replace("{since}", since);
		}
		if (forRemoval != null)
		{
			value = value.replace("{forRemoval}", forRemoval);
		}

		final Marker marker = Markers.withText(position, markerLevel, value);
		assert marker != null;

		// Description
		if (description != null && !description.isEmpty())
		{
			marker.addInfo(Markers.getSemantic("deprecated.description", description));
		}

		// Since
		if (since != null && !since.isEmpty())
		{
			marker.addInfo(Markers.getSemantic("deprecated.since", since));
		}

		if (forRemoval != null && !forRemoval.isEmpty())
		{
			marker.addInfo(Markers.getSemantic("deprecated.forRemoval", forRemoval));
		}

		// Until

		// Reasons
		final Reason[] reasons = getReasons(arguments);
		if (reasons != null)
		{
			final int reasonCount = reasons.length;

			// more than one reason
			if (reasonCount == 1)
			{
				marker.addInfo(Markers.getSemantic("deprecated.reason", reasonName(reasons[0])));
			}
			else if (reasonCount > 0)
			{
				final StringBuilder builder = new StringBuilder(reasonName(reasons[0]));
				for (int i = 1; i < reasonCount; i++)
				{
					builder.append(", ").append(reasonName(reasons[i]));
				}
				marker.addInfo(Markers.getSemantic("deprecated.reasons", builder.toString()));
			}
		}

		// Replacements
		final String[] replacements = getReplacements(arguments);
		if (replacements != null)
		{
			for (String replacement : replacements)
			{
				marker.addInfo("\t\t" + replacement);
			}
			marker.addInfo(Markers.getSemantic("deprecated.replacements"));
		}

		markers.add(marker);
	}

	private static String reasonName(Reason reason)
	{
		return Markers.getSemantic("deprecated.reason." + reason.name());
	}

	private static String replaceMember(IMember member, String value)
	{
		return value.replace("{member.kind}", Markers.getSemantic("member." + member.getKind().getName()))
		            .replace("{member.name}", member.getName().toString());
	}

	private static void checkExperimental(IMember member, SourcePosition position, MarkerList markers,
		                                     IAnnotation annotation)
	{
		final ArgumentList arguments = annotation.getArguments();
		final MarkerLevel markerLevel = AnnotationUtil.getEnumValue(arguments, EXP_LEVEL_PARAM, MarkerLevel.class);

		if (markerLevel == null || markerLevel == MarkerLevel.IGNORE)
		{
			return;
		}

		String value = AnnotationUtil.getStringValue(arguments, EXP_VALUE_PARAM);
		final String description = AnnotationUtil.getStringValue(arguments, EXP_DESC_PARAM);
		final Stage stage = AnnotationUtil.getEnumValue(arguments, EXP_STAGE_PARAM, Stage.class);
		assert stage != null;

		final String stageName = Markers.getSemantic("experimental.stage." + stage.name());

		value = replaceMember(member, value).replace("{stage}", stageName);

		final Marker marker = Markers.withText(position, markerLevel, value);
		assert marker != null;

		// Description
		if (description != null && !description.isEmpty())
		{
			marker.addInfo(Markers.getSemantic("experimental.description", description));
		}

		// Stage
		marker.addInfo(Markers.getSemantic("experimental.stage", stageName));

		markers.add(marker);
	}

	private static void checkUsageInfo(IMember member, SourcePosition position, MarkerList markers,
		                                  IAnnotation annotation)
	{
		final ArgumentList arguments = annotation.getArguments();
		final MarkerLevel markerLevel = AnnotationUtil.getEnumValue(arguments, INF_LEVEL_PARAM, MarkerLevel.class);

		if (markerLevel == null || markerLevel == MarkerLevel.IGNORE)
		{
			return;
		}

		String value = AnnotationUtil.getStringValue(arguments, INF_VALUE_PARAM);
		final String description = AnnotationUtil.getStringValue(arguments, INF_DESC_PARAM);

		value = replaceMember(member, value);

		final Marker marker = Markers.withText(position, markerLevel, value);
		assert marker != null;

		// Description
		if (description != null && !description.isEmpty())
		{
			marker.addInfo(Markers.getSemantic("experimental.description", description));
		}

		markers.add(marker);
	}

	private static Reason[] getReasons(ArgumentList arguments)
	{
		final IValue value = arguments.get(DEP_REASONS_PARAM.getIndex(), DEP_REASONS_PARAM);
		if (value == null)
		{
			return null;
		}

		assert value.valueTag() == IValue.ARRAY;

		final ArrayExpr array = (ArrayExpr) value;
		final ArgumentList values = array.getValues();
		final int size = values.size();

		if (size <= 0)
		{
			return null;
		}

		final Reason[] reasons = new Reason[size];
		for (int i = 0; i < size; i++)
		{
			final IValue element = values.get(i);
			assert element.valueTag() == IValue.ENUM_ACCESS;

			final EnumValue enumValue = (EnumValue) element;
			reasons[i] = Reason.valueOf(enumValue.getInternalName());
		}

		return reasons;
	}

	private static String[] getReplacements(ArgumentList arguments)
	{
		IValue value = arguments.get(DEP_REPLACE_PARAM.getIndex(), DEP_REPLACE_PARAM);
		if (value == null)
		{
			return null;
		}

		assert value.valueTag() == IValue.ARRAY;

		final ArrayExpr array = (ArrayExpr) value;
		final ArgumentList values = array.getValues();
		final int size = values.size();

		if (size == 0)
		{
			return null;
		}

		String[] replacements = new String[size];
		for (int i = 0; i < size; i++)
		{
			IValue element = values.get(i);
			assert element.valueTag() == IValue.STRING;
			replacements[i] = element.stringValue();
		}

		return replacements;
	}
}
