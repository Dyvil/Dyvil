package dyvil.tools.compiler.ast.annotation;

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
}
