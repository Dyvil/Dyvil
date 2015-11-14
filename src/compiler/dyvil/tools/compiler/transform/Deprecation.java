package dyvil.tools.compiler.transform;

import dyvil.annotation.Deprecated.Reason;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constant.EnumValue;
import dyvil.tools.compiler.ast.expression.Array;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.ClassType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.util.I18n;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;
import dyvil.util.MarkerLevel;

public class Deprecation
{
	public static final Name Deprecated = Name.getQualified("Deprecated");
	
	public static final String	JAVA_INTERNAL	= "java/lang/Deprecated";
	public static final String	DYVIL_INTERNAL	= "dyvil/annotation/Deprecated";
	public static final String	DYVIL_EXTENDED	= 'L' + DYVIL_INTERNAL + ';';
	
	public static final IClass	DEPRECATED_CLASS	= Package.dyvilAnnotation.resolveClass(Deprecated);
	public static final IType	DEPRECATED			= new ClassType(DEPRECATED_CLASS);
	
	private static final IParameter	VALUE_PARAM			= DEPRECATED_CLASS.getParameter(0);
	private static final IParameter	MARKER_PARAM		= DEPRECATED_CLASS.getParameter(1);
	private static final IParameter	SINCE_PARAM			= DEPRECATED_CLASS.getParameter(2);
	private static final IParameter	REASONS_PARAM		= DEPRECATED_CLASS.getParameter(3);
	private static final IParameter	REPLACEMENTS_PARAM	= DEPRECATED_CLASS.getParameter(4);
	private static final IParameter	MARKER_TYPE_PARAM	= DEPRECATED_CLASS.getParameter(5);
	
	public static void checkDeprecation(MarkerList markers, ICodePosition position, IMember member, String memberType)
	{
		IAnnotation annotation = member.getAnnotation(DEPRECATED_CLASS);
		if (annotation == null)
		{
			annotation = new Annotation(DEPRECATED);
		}
		
		IArguments arguments = annotation.getArguments();
		
		// General Description / Marker Message
		MarkerLevel markerLevel = getMarkerLevel(arguments);
		String value = getStringValue(arguments, VALUE_PARAM);
		String markerString = getStringValue(arguments, MARKER_PARAM);
		String since = getStringValue(arguments, SINCE_PARAM);
		
		markerString = markerString.replace("{value}", value) // description
				.replace("{since}", since) // since
				.replace("{member}", I18n.getString("member." + memberType, member.getName()));
				
		Marker marker = I18n.createTextMarker(position, markerLevel, markerString);
		
		// Since
		if (!since.isEmpty())
		{
			marker.addInfo(I18n.getString("deprecated.since", since));
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
				marker.addInfo(I18n.getString("deprecated.reasons", builder.toString()));
			}
			// one reason that is not UNSPECIFIED
			else if (reasons[0] != Reason.UNSPECIFIED)
			{
				marker.addInfo(I18n.getString("deprecated.reason", reasons[0].name().toLowerCase()));
			}
		}
		
		// Replacements
		String[] replacements = getReplacements(arguments);
		if (replacements != null)
		{
			StringBuilder builder = new StringBuilder("Replacements:");
			int replacementCount = replacements.length;
			for (int i = 0; i < replacementCount; i++)
			{
				builder.append("\n\t\t").append(replacements[i]);
			}
			marker.addInfo(builder.toString());
		}
		
		markers.add(marker);
	}
	
	private static MarkerLevel getMarkerLevel(IArguments arguments)
	{
		IValue value = arguments.getValue(5, MARKER_TYPE_PARAM);
		if (value == null)
		{
			value = MARKER_TYPE_PARAM.getValue();
		}
		
		assert value.valueTag() == IValue.ENUM_ACCESS;
		EnumValue enumValue = (EnumValue) value;
		return MarkerLevel.valueOf(enumValue.name.qualified);
	}
	
	private static String getStringValue(IArguments arguments, IParameter parameter)
	{
		IValue value = arguments.getValue(parameter.getIndex(), parameter);
		if (value == null)
		{
			value = parameter.getValue();
		}
		
		assert value.valueTag() == IValue.STRING;
		return value.stringValue();
	}
	
	private static Reason[] getReasons(IArguments arguments)
	{
		IValue value = arguments.getValue(REASONS_PARAM.getIndex(), REASONS_PARAM);
		if (value == null)
		{
			return null;
		}
		
		assert value.valueTag() == IValue.ARRAY;
		Array array = (Array) value;
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
		IValue value = arguments.getValue(REPLACEMENTS_PARAM.getIndex(), REPLACEMENTS_PARAM);
		if (value == null)
		{
			return null;
		}
		
		assert value.valueTag() == IValue.ARRAY;
		Array array = (Array) value;
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
