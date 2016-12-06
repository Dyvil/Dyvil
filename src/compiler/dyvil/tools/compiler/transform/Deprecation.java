package dyvil.tools.compiler.transform;

import dyvil.annotation.Deprecated.Reason;
import dyvil.annotation.Experimental.Stage;
import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.AnnotationUtil;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constant.EnumValue;
import dyvil.tools.compiler.ast.expression.ArrayExpr;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.IParameterList;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.raw.ClassType;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;
import dyvil.util.MarkerLevel;

public final class Deprecation
{
	public static final Name Deprecated   = Name.fromRaw("Deprecated");
	public static final Name Experimental = Name.fromRaw("Experimental");
	public static final Name UsageInfo    = Name.fromRaw("UsageInfo");

	public static final String JAVA_INTERNAL  = "java/lang/Deprecated";
	public static final String DYVIL_INTERNAL = "dyvil/annotation/Deprecated";
	public static final String DYVIL_EXTENDED = 'L' + DYVIL_INTERNAL + ';';

	public static final IClass DEPRECATED_CLASS   = Package.dyvilAnnotation.resolveClass(Deprecated);
	public static final IClass EXPERIMENTAL_CLASS = Package.dyvilAnnotation.resolveClass(Experimental);
	public static final IClass USAGE_INFO_CLASS   = Package.dyvilAnnotation.resolveClass(UsageInfo);

	public static final IType DEPRECATED = new ClassType(DEPRECATED_CLASS);

	private static final IParameterList DEPRECATED_PARAMETERS  = DEPRECATED_CLASS.getParameterList();
	private static final IParameter     DEP_VALUE_PARAM        = DEPRECATED_PARAMETERS.get(0);
	private static final IParameter     DEP_DESCRIPTION_PARAM  = DEPRECATED_PARAMETERS.get(1);
	private static final IParameter     DEP_SINCE_PARAM        = DEPRECATED_PARAMETERS.get(2);
	private static final IParameter     DEP_REASONS_PARAM      = DEPRECATED_PARAMETERS.get(3);
	private static final IParameter     DEP_REPLACEMENTS_PARAM = DEPRECATED_PARAMETERS.get(4);
	private static final IParameter     DEP_MARKER_TYPE_PARAM  = DEPRECATED_PARAMETERS.get(5);

	private static final IParameterList EXPERIMENTAL_PARAMETERS = EXPERIMENTAL_CLASS.getParameterList();
	private static final IParameter     EXP_VALUE_PARAM         = EXPERIMENTAL_PARAMETERS.get(0);
	private static final IParameter     EXP_DESCRIPTION_PARAM   = EXPERIMENTAL_PARAMETERS.get(1);
	private static final IParameter     EXP_STAGE_PARAM         = EXPERIMENTAL_PARAMETERS.get(2);
	private static final IParameter     EXP_MARKER_TYPE_PARAM   = EXPERIMENTAL_PARAMETERS.get(3);

	private static final IParameterList USAGE_INFO_PARAMETERS = USAGE_INFO_CLASS.getParameterList();
	private static final IParameter     INF_VALUE_PARAM       = USAGE_INFO_PARAMETERS.get(0);
	private static final IParameter     INF_DESCRIPTION_PARAM = USAGE_INFO_PARAMETERS.get(1);
	private static final IParameter     INF_MARKER_TYPE_PARAM = USAGE_INFO_PARAMETERS.get(2);

	public static final String MEMBER_KIND = "{member.kind}";
	public static final String MEMBER_NAME = "{member.name}";
	public static final String STAGE       = "{stage}";
	public static final String SINCE       = "{since}";

	public static void checkAnnotations(IMember member, ICodePosition position, MarkerList markers)
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

	private static void checkDeprecation(IMember member, ICodePosition position, MarkerList markers)
	{
		IAnnotation annotation = member.getAnnotation(DEPRECATED_CLASS);
		if (annotation == null)
		{
			annotation = new Annotation(DEPRECATED);
		}

		IArguments arguments = annotation.getArguments();

		// General Description / Marker Message
		MarkerLevel markerLevel = AnnotationUtil.getEnumValue(arguments, DEP_MARKER_TYPE_PARAM, MarkerLevel.class);
		if (markerLevel == null || markerLevel == MarkerLevel.IGNORE)
		{
			return;
		}

		String value = AnnotationUtil.getStringValue(arguments, DEP_VALUE_PARAM);
		String description = AnnotationUtil.getStringValue(arguments, DEP_DESCRIPTION_PARAM);
		String since = AnnotationUtil.getStringValue(arguments, DEP_SINCE_PARAM);

		value = replaceMember(member, value).replace(SINCE, since);

		Marker marker = Markers.withText(position, markerLevel, value);
		assert marker != null;

		// Description
		if (!description.isEmpty())
		{
			marker.addInfo(Markers.getSemantic("deprecated.description", description));
		}

		// Since
		if (!since.isEmpty())
		{
			marker.addInfo(Markers.getSemantic("deprecated.since", since));
		}

		// Reasons
		Reason[] reasons = getReasons(arguments);
		if (reasons != null)
		{
			// reasonCount will be at least 1
			int reasonCount = reasons.length;

			// more than one reason
			if (reasonCount > 1)
			{
				StringBuilder builder = new StringBuilder();
				builder.append(reasons[0].name().toLowerCase());
				for (int i = 1; i < reasonCount; i++)
				{
					builder.append(", ").append(reasons[i].name().toLowerCase());
				}
				marker.addInfo(Markers.getSemantic("deprecated.reasons", builder.toString()));
			}
			// one reason that is not UNSPECIFIED
			else if (reasons[0] != Reason.UNSPECIFIED)
			{
				marker.addInfo(Markers.getSemantic("deprecated.reason", reasons[0].name().toLowerCase()));
			}
		}

		// Replacements
		String[] replacements = getReplacements(arguments);
		if (replacements != null)
		{
			final StringBuilder builder = new StringBuilder();
			for (String replacement : replacements)
			{
				builder.append("\n\t\t").append(replacement);
			}
			marker.addInfo(Markers.getSemantic("deprecated.replacements", builder.toString()));
		}

		markers.add(marker);
	}

	private static String replaceMember(IMember member, String value)
	{
		return value.replace(MEMBER_KIND, Markers.getSemantic("member." + member.getKind().getName()))
		            .replace(MEMBER_NAME, member.getName().toString());
	}

	private static void checkExperimental(IMember member, ICodePosition position, MarkerList markers, IAnnotation annotation)
	{
		IArguments arguments = annotation.getArguments();

		MarkerLevel markerLevel = AnnotationUtil.getEnumValue(arguments, EXP_MARKER_TYPE_PARAM, MarkerLevel.class);
		if (markerLevel == null || markerLevel == MarkerLevel.IGNORE)
		{
			return;
		}

		String value = AnnotationUtil.getStringValue(arguments, EXP_VALUE_PARAM);
		String description = AnnotationUtil.getStringValue(arguments, EXP_DESCRIPTION_PARAM);
		Stage stage = AnnotationUtil.getEnumValue(arguments, EXP_STAGE_PARAM, Stage.class);
		assert stage != null;

		value = replaceMember(member, value).replace(STAGE, stage.toString());

		Marker marker = Markers.withText(position, markerLevel, value);
		assert marker != null;

		// Description
		if (!description.isEmpty())
		{
			marker.addInfo(Markers.getSemantic("experimental.description", description));
		}

		// Stage
		marker.addInfo(Markers.getSemantic("experimental.stage", stage.name().toLowerCase()));

		markers.add(marker);
	}

	private static void checkUsageInfo(IMember member, ICodePosition position, MarkerList markers, IAnnotation annotation)
	{
		IArguments arguments = annotation.getArguments();

		MarkerLevel markerLevel = AnnotationUtil.getEnumValue(arguments, INF_MARKER_TYPE_PARAM, MarkerLevel.class);
		if (markerLevel == null || markerLevel == MarkerLevel.IGNORE)
		{
			return;
		}

		String value = AnnotationUtil.getStringValue(arguments, INF_VALUE_PARAM);
		String description = AnnotationUtil.getStringValue(arguments, INF_DESCRIPTION_PARAM);

		value = replaceMember(member, value);

		Marker marker = Markers.withText(position, markerLevel, value);
		assert marker != null;

		// Description
		if (!description.isEmpty())
		{
			marker.addInfo(Markers.getSemantic("experimental.description", description));
		}

		markers.add(marker);
	}

	private static Reason[] getReasons(IArguments arguments)
	{
		IValue value = arguments.getValue(DEP_REASONS_PARAM.getIndex(), DEP_REASONS_PARAM);
		if (value == null)
		{
			return null;
		}

		assert value.valueTag() == IValue.ARRAY;
		ArrayExpr array = (ArrayExpr) value;
		int size = array.valueCount();

		if (size <= 0)
		{
			return null;
		}

		Reason[] reasons = new Reason[size];
		for (int i = 0; i < size; i++)
		{
			IValue element = array.getValue(i);
			assert element.valueTag() == IValue.ENUM_ACCESS;
			EnumValue enumValue = (EnumValue) element;
			reasons[i] = Reason.valueOf(enumValue.name.qualified);
		}

		return reasons;
	}

	private static String[] getReplacements(IArguments arguments)
	{
		IValue value = arguments.getValue(DEP_REPLACEMENTS_PARAM.getIndex(), DEP_REPLACEMENTS_PARAM);
		if (value == null)
		{
			return null;
		}

		assert value.valueTag() == IValue.ARRAY;
		ArrayExpr array = (ArrayExpr) value;
		int size = array.valueCount();

		if (size <= 0)
		{
			return null;
		}

		String[] replacements = new String[size];
		for (int i = 0; i < size; i++)
		{
			IValue element = array.getValue(i);
			assert element.valueTag() == IValue.STRING;
			replacements[i] = element.stringValue();
		}

		return replacements;
	}
}
