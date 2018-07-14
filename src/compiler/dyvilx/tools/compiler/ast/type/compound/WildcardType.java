package dyvilx.tools.compiler.ast.type.compound;

import dyvil.lang.Name;
import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.TypeAnnotatableVisitor;
import dyvilx.tools.asm.TypePath;
import dyvilx.tools.compiler.ast.attribute.annotation.Annotation;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.generic.Variance;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.ITyped;
import dyvilx.tools.compiler.ast.type.TypeDelegate;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.ast.type.raw.IRawType;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public final class WildcardType extends TypeDelegate implements IRawType, ITyped
{
	protected SourcePosition position;
	protected Variance       variance;

	public WildcardType()
	{
		this.type = Types.NULLABLE_ANY;
	}

	public WildcardType(Variance variance)
	{
		this.variance = variance;
		this.type = Types.NULLABLE_ANY;
	}

	public WildcardType(Variance variance, IType type)
	{
		this.variance = variance;
		this.type = type;
	}

	public WildcardType(SourcePosition position, Variance variance)
	{
		this.position = position;
		this.variance = variance;
		this.type = Types.NULLABLE_ANY;
	}

	public WildcardType(SourcePosition position, IType type, Variance variance)
	{
		this.position = position;
		this.type = type;
		this.variance = variance;
	}

	public static IType unapply(IType type)
	{
		final WildcardType extracted = type.extract(WildcardType.class);
		return extracted != null ? extracted.type : type;
	}

	@Override
	protected IType wrap(IType type)
	{
		return new WildcardType(this.variance, type);
	}

	public Variance getVariance()
	{
		return this.variance;
	}

	public void setVariance(Variance variance)
	{
		this.variance = variance;
	}

	@Override
	public SourcePosition getPosition()
	{
		return this.position;
	}

	@Override
	public void setPosition(SourcePosition position)
	{
		this.position = position;
	}

	@Override
	public IType atPosition(SourcePosition position)
	{
		return new WildcardType(position, this.type, this.variance);
	}

	@Override
	public int typeTag()
	{
		return WILDCARD_TYPE;
	}

	@Override
	public boolean isGenericType()
	{
		return false;
	}

	@Override
	public boolean useNonNullAnnotation()
	{
		return false;
	}

	@Override
	public Name getName()
	{
		return null;
	}

	@Override
	public IClass getTheClass()
	{
		return this.type.getTheClass();
	}

	@Override
	public boolean hasTypeVariables()
	{
		return this.type.hasTypeVariables();
	}

	@Override
	public IType getConcreteType(ITypeContext context)
	{
		if (context == ITypeContext.COVARIANT)
		{
			return this.asParameterType();
		}

		final IType concretetype = this.type.getConcreteType(context);
		if (concretetype == this.type)
		{
			return this;
		}

		if (concretetype.typeTag() == WILDCARD_TYPE)
		{
			return concretetype;
		}

		final WildcardType copy = new WildcardType(this.position, this.variance);
		copy.type = concretetype;
		return copy;
	}

	// Phases

	@Override
	public IType resolveType(MarkerList markers, IContext context)
	{
		this.type = this.type.resolveType(markers, context);
		return this;
	}

	@Override
	public void checkType(MarkerList markers, IContext context, int position)
	{
		if ((position & TypePosition.WILDCARD_FLAG) == 0)
		{
			markers.add(Markers.semanticError(this.position, "type.wildcard.invalid"));
		}

		this.type.checkType(markers, context, TypePosition.copyReify(position, TypePosition.SUPER_TYPE_ARGUMENT));
	}

	// Compilation

	@Override
	public void appendDescriptor(StringBuilder buffer, int type)
	{
		if (type == NAME_DESCRIPTOR)
		{
			buffer.append('L').append(this.getInternalName()).append(';');
			return;
		}

		if (!Types.isSameType(this.type, Types.OBJECT))
		{
			this.variance.appendPrefix(buffer);
			this.type.appendSignature(buffer, true);
		}
		else
		{
			buffer.append('*');
		}
	}

	@Override
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException
	{
		writer.visitFieldInsn(Opcodes.GETSTATIC, "dyvil/reflect/Variance", this.variance.name(),
		                      "Ldyvil/reflect/Variance;");

		if (this.type != null)
		{
			this.type.writeTypeExpression(writer);
		}
		else
		{
			writer.visitInsn(Opcodes.ACONST_NULL);
		}

		writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/reflect/types/WildcardType", "apply",
		                       "(Ldyvil/reflect/Variance;Ldyvil/reflect/types/Type;)Ldyvil/reflect/types/WildcardType;",
		                       false);
	}

	@Override
	public IType withAnnotation(Annotation annotation)
	{
		final IType a = this.type.withAnnotation(annotation);
		if (a == null)
		{
			return null;
		}

		this.type = a;
		return this;
	}

	@Override
	public void addAnnotation(Annotation annotation, TypePath typePath, int step, int steps)
	{
		if (typePath.getStep(step) == TypePath.WILDCARD_BOUND)
		{
			this.type = IType.withAnnotation(this.type, annotation, typePath, step + 1, steps);
		}
	}

	@Override
	public void writeAnnotations(TypeAnnotatableVisitor visitor, int typeRef, String typePath)
	{
		IType.writeAnnotations(this.type, visitor, typeRef, typePath + '*');
	}

	@Override
	public void write(DataOutput out) throws IOException
	{
		Variance.write(this.variance, out);
		IType.writeType(this.type, out);
	}

	@Override
	public void read(DataInput in) throws IOException
	{
		this.variance = Variance.read(in);
		this.type = IType.readType(in);
	}

	@Override
	public String toString()
	{
		if (this.type == Types.NULLABLE_ANY)
		{
			return "_";
		}

		final StringBuilder builder = new StringBuilder();
		this.variance.appendPrefix(builder);
		builder.append(this.type);
		return builder.toString();
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.type != Types.NULLABLE_ANY)
		{
			this.variance.appendPrefix(buffer);
			this.type.toString(prefix, buffer);
		}
		else
		{
			buffer.append('_');
		}
	}
}
