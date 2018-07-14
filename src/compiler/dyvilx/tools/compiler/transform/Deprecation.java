package dyvilx.tools.compiler.transform;

import dyvil.annotation.Deprecated.Reason;
import dyvil.annotation.Experimental.Stage;
import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvil.source.position.SourcePosition;
import dyvil.util.MarkerLevel;
import dyvilx.tools.compiler.ast.attribute.annotation.Annotation;
import dyvilx.tools.compiler.ast.attribute.annotation.ExternalAnnotation;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.expression.ArrayExpr;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.constant.EnumValue;
import dyvilx.tools.compiler.ast.member.Member;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.parameter.IParameter;
import dyvilx.tools.compiler.ast.parameter.ParameterList;
import dyvilx.tools.compiler.ast.structure.Package;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.raw.ClassType;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.Marker;
import dyvilx.tools.parsing.marker.MarkerList;

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

	public static void checkAnnotations(Member member, SourcePosition position, MarkerList markers)
	{
		if (member.hasModifier(Modifiers.DEPRECATED))
		{
			checkDeprecation(member, position, markers);
		}

		Annotation annotation = member.getAnnotation(EXPERIMENTAL_CLASS);
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

	private static void checkDeprecation(Member member, SourcePosition position, MarkerList markers)
	{
		Annotation annotation = member.getAnnotation(DEPRECATED_CLASS);
		if (annotation == null)
		{
			annotation = new ExternalAnnotation(DEPRECATED);
		}

		final ArgumentList arguments = annotation.getArguments();
		final MarkerLevel markerLevel = EnumValue.eval(arguments.getOrDefault(DEP_LEVEL_PARAM), MarkerLevel.class);

		if (markerLevel == null || markerLevel == MarkerLevel.IGNORE)
		{
			return;
		}

		String value = arguments.getOrDefault(DEP_VALUE_PARAM).stringValue();
		final String description = arguments.getOrDefault(DEP_DESC_PARAM).stringValue();
		final String since = arguments.getOrDefault(DEP_SINCE_PARAM).stringValue();
		final String forRemoval = arguments.getOrDefault(DEP_UNTIL_PARAM).stringValue();

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

	private static String replaceMember(Member member, String value)
	{
		if (value == null)
		{
			return null;
		}

		return value.replace("{member.kind}", Markers.getSemantic("member." + member.getKind().getName()))
		            .replace("{member.name}", member.getName().toString());
	}

	private static void checkExperimental(Member member, SourcePosition position, MarkerList markers,
		                                     Annotation annotation)
	{
		final ArgumentList arguments = annotation.getArguments();
		final MarkerLevel markerLevel = EnumValue.eval(arguments.getOrDefault(EXP_LEVEL_PARAM), MarkerLevel.class);

		if (markerLevel == null || markerLevel == MarkerLevel.IGNORE)
		{
			return;
		}

		String value = arguments.getOrDefault(EXP_VALUE_PARAM).stringValue();
		final String description = arguments.getOrDefault(EXP_DESC_PARAM).stringValue();
		final Stage stage = EnumValue.eval(arguments.getOrDefault(EXP_STAGE_PARAM), Stage.class);
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

	private static void checkUsageInfo(Member member, SourcePosition position, MarkerList markers,
		                                  Annotation annotation)
	{
		final ArgumentList arguments = annotation.getArguments();
		final MarkerLevel markerLevel = EnumValue.eval(arguments.getOrDefault(INF_LEVEL_PARAM), MarkerLevel.class);

		if (markerLevel == null || markerLevel == MarkerLevel.IGNORE)
		{
			return;
		}

		String value = arguments.getOrDefault(INF_VALUE_PARAM).stringValue();
		final String description = arguments.getOrDefault(INF_DESC_PARAM).stringValue();

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
		final IValue value = arguments.get(DEP_REASONS_PARAM);
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
			reasons[i] = EnumValue.eval(values.get(i), Reason.class);
		}

		return reasons;
	}

	private static String[] getReplacements(ArgumentList arguments)
	{
		IValue value = arguments.get(DEP_REPLACE_PARAM);
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
