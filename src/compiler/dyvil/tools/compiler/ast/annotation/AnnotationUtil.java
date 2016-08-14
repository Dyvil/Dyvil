package dyvil.tools.compiler.ast.annotation;

import dyvil.tools.compiler.ast.access.FieldAccess;
import dyvil.tools.compiler.ast.constant.EnumValue;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.type.builtin.Types;

public final class AnnotationUtil
{
	public static final String RECEIVER_TYPE = "Ldyvil/annotation/_internal/ReceiverType;";

	public static final String DYVIL_MODIFIERS = "Ldyvil/annotation/_internal/DyvilModifiers;";

	public static final String CLASS_PARAMETERS = "Ldyvil/annotation/_internal/ClassParameters;";

	public static final String IMPLICITLY_UNWRAPPED_INTERNAL = "dyvil/annotation/_internal/ImplicitlyUnwrapped";
	public static final String IMPLICITLY_UNWRAPPED          = "L" + IMPLICITLY_UNWRAPPED_INTERNAL + ";";

	public static final String PRIMITIVE_INTERNAL = "dyvil/annotation/_internal/Primitive";
	public static final String PRIMITIVE          = "L" + PRIMITIVE_INTERNAL + ";";

	private AnnotationUtil()
	{
		// no instances
	}

	public static <T extends Enum<T>> T getEnumValue(IArguments arguments, IParameter parameter, Class<T> type)
	{
		IValue value = arguments.getValue(parameter.getIndex(), parameter);
		if (value == null)
		{
			value = parameter.getValue();
		}

		switch (value.valueTag())
		{
		case IValue.ENUM_ACCESS:
			return Enum.valueOf(type, ((EnumValue) value).name.qualified);
		case IValue.FIELD_ACCESS:
			if (Types.isSameType(parameter.getInternalType(), value.getType()))
			{
				return Enum.valueOf(type, ((FieldAccess) value).getName().qualified);
			}
			break;
		}
		return null;
	}

	public static String getStringValue(IArguments arguments, IParameter parameter)
	{
		IValue value = arguments.getValue(parameter.getIndex(), parameter);
		if (value == null)
		{
			value = parameter.getValue();
		}

		if (value.valueTag() == IValue.STRING)
		{
			return value.stringValue();
		}
		return null;
	}
}
