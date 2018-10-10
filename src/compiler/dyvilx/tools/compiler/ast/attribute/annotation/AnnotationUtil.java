package dyvilx.tools.compiler.ast.attribute.annotation;

import dyvilx.tools.asm.AnnotatableVisitor;
import dyvilx.tools.asm.AnnotationVisitor;
import dyvilx.tools.asm.TypeAnnotatableVisitor;
import dyvilx.tools.asm.TypePath;
import dyvilx.tools.compiler.ast.type.IType;

public final class AnnotationUtil
{
	public static final String RECEIVER_TYPE = "Ldyvil/annotation/internal/ReceiverType;";

	public static final String JAVA_NAME_INTERNAL = "dyvil/annotation/JavaName";

	public static final String DYVIL_NAME_INTERNAL = "dyvil/annotation/internal/DyvilName";
	public static final String DYVIL_NAME          = "L" + DYVIL_NAME_INTERNAL + ";";

	public static final String AUTOMANGLED_INTERNAL = "dyvil/annotation/AutoMangled";

	public static final String DYVIL_TYPE_INTERNAL = "dyvil/annotation/internal/DyvilType";
	public static final String DYVIL_TYPE          = "L" + DYVIL_TYPE_INTERNAL + ";";

	public static final String CLASS_PARAMETERS = "Ldyvil/annotation/internal/ClassParameters;";

	public static final String IMPLICITLY_UNWRAPPED_INTERNAL = "dyvil/annotation/internal/ImplicitlyUnwrapped";
	public static final String IMPLICITLY_UNWRAPPED          = "L" + IMPLICITLY_UNWRAPPED_INTERNAL + ";";

	public static final String  PRIMITIVE_INTERNAL = "dyvil/annotation/internal/Primitive";
	public static final String  PRIMITIVE          = "L" + PRIMITIVE_INTERNAL + ";";
	public static final boolean PRIMITIVE_VISIBLE  = true;

	public static final String INRINSIC_INTERNAL = "dyvil/annotation/Intrinsic";

	public static final String NOTNULL_INTERNAL  = "dyvil/annotation/internal/NonNull";
	public static final String NOTNULL           = 'L' + NOTNULL_INTERNAL + ';';
	public static final String NULLABLE_INTERNAL = "dyvil/annotation/internal/Nullable";
	public static final String NULLABLE          = 'L' + NULLABLE_INTERNAL + ';';

	private AnnotationUtil()
	{
		// no instances
	}

	public static void writeDyvilName(AnnotatableVisitor visitor, String name)
	{
		final AnnotationVisitor annotationVisitor = visitor.visitAnnotation(AnnotationUtil.DYVIL_NAME, false);
		annotationVisitor.visit("value", name);
		annotationVisitor.visitEnd();
	}

	public static void writeDyvilType(IType type, TypeAnnotatableVisitor visitor, int typeRef, String typePath)
	{
		writeDyvilType(type, visitor, typeRef, TypePath.fromString(typePath));
	}

	public static void writeDyvilType(IType type, TypeAnnotatableVisitor visitor, int typeRef, TypePath typePath)
	{
		final AnnotationVisitor annotation = visitor.visitTypeAnnotation(typeRef, typePath, AnnotationUtil.DYVIL_TYPE,
		                                                                 true);
		annotation.visit("value", type.getDescriptor(IType.NAME_FULL));

		annotation.visitEnd();
	}
}
