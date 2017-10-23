package dyvilx.tools.compiler.ast.type;

import dyvilx.tools.asm.TypeAnnotatableVisitor;
import dyvilx.tools.asm.TypePath;
import dyvilx.tools.compiler.ast.attribute.annotation.Annotation;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public enum Mutability
{
	UNDEFINED(null),
	IMMUTABLE("dyvil/annotation/Immutable", "Ldyvil/annotation/Immutable;"),
	MUTABLE("dyvil/annotation/Mutable", "Ldyvil/annotation/Mutable;");

	private final String annotationType;
	private final String extendedAnnotationType;

	Mutability(String annotationType)
	{
		this.annotationType = annotationType;

		if (annotationType == null)
		{
			this.extendedAnnotationType = null;
		}
		else
		{
			this.extendedAnnotationType = 'L' + annotationType + ';';
		}
	}

	Mutability(String annotationType, String extendedAnnotationType)
	{
		this.annotationType = annotationType;
		this.extendedAnnotationType = extendedAnnotationType;
	}

	public String getAnnotationType()
	{
		return this.annotationType;
	}

	public String getExtendedAnnotationType()
	{
		return this.extendedAnnotationType;
	}

	public void writeAnnotation(TypeAnnotatableVisitor visitor, int typeRef, String typePath)
	{
		if (this != UNDEFINED)
		{
			visitor.visitTypeAnnotation(typeRef, TypePath.fromString(typePath), this.extendedAnnotationType, true)
			       .visitEnd();
		}
	}

	public static Mutability readAnnotation(Annotation annotation)
	{
		final String annotationType = annotation.getTypeDescriptor();
		if (IMMUTABLE.annotationType.equals(annotationType))
		{
			return IMMUTABLE;
		}
		if (MUTABLE.annotationType.equals(annotationType))
		{
			return MUTABLE;
		}
		return UNDEFINED;
	}

	public void write(DataOutput output) throws IOException
	{
		output.writeByte(this.ordinal());
	}

	public static Mutability read(DataInput input) throws IOException
	{
		switch (input.readByte())
		{
		case 1:
			return IMMUTABLE;
		case 2:
			return MUTABLE;
		default:
		case 0:
			return UNDEFINED;
		}
	}

	public void appendKeyword(StringBuilder builder)
	{
		switch (this)
		{
		case MUTABLE:
			builder.append("var ");
			return;
		case IMMUTABLE:
			builder.append("final ");
		}
	}
}
