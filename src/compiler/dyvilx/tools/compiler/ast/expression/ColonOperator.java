package dyvilx.tools.compiler.ast.expression;

import dyvil.lang.Formattable;
import dyvil.lang.Name;
import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.attribute.annotation.Annotation;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IImplicitContext;
import dyvilx.tools.compiler.ast.expression.constant.WildcardValue;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.generic.ITypeParameter;
import dyvilx.tools.compiler.ast.generic.TypeParameterList;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.ast.type.compound.TupleType;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.config.Formatting;
import dyvilx.tools.compiler.transform.TypeChecker;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.MarkerList;

public class ColonOperator implements IValue
{
	public static final class LazyFields
	{
		public static final IClass COLON_CONVERTIBLE = Types.LITERALCONVERTIBLE_CLASS
			                                               .resolveClass(Name.fromRaw("FromColonOperator"));

		public static final ITypeParameter KEY_PARAMETER;
		public static final ITypeParameter VALUE_PARAMETER;

		static
		{
			final TypeParameterList typeParameters = TupleExpr.LazyFields.ENTRY.getTypeParameters();
			KEY_PARAMETER = typeParameters.get(0);
			VALUE_PARAMETER = typeParameters.get(1);
		}

		private LazyFields()
		{
			// no instances
		}
	}

	private IValue left;
	private IValue right;

	// Metadata
	private SourcePosition position;
	private IType          type;

	public ColonOperator(IValue left, IValue right)
	{
		this.left = left;
		this.right = right;
		this.position = SourcePosition.between(left.getPosition(), right.getPosition());
	}

	public ColonOperator(SourcePosition position, IValue left, IValue right)
	{
		this.position = position;
		this.left = left;
		this.right = right;
	}

	@Override
	public int valueTag()
	{
		return COLON;
	}

	public IValue getLeft()
	{
		return this.left;
	}

	public void setLeft(IValue left)
	{
		this.left = left;
	}

	public IValue getRight()
	{
		return this.right;
	}

	public void setRight(IValue right)
	{
		this.right = right;
	}

	@Override
	public void setPosition(SourcePosition position)
	{
		this.position = position;
	}

	@Override
	public SourcePosition getPosition()
	{
		return this.position;
	}

	@Override
	public boolean isResolved()
	{
		return this.left.isResolved() && this.right.isResolved();
	}

	@Override
	public IType getType()
	{
		if (this.type == null)
		{
			return this.type = new TupleType(this.left.getType(), this.right.getType());
		}
		return this.type;
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		final Annotation annotation = type.getAnnotation(LazyFields.COLON_CONVERTIBLE);
		if (annotation != null)
		{
			return new LiteralConversion(this, annotation, new ArgumentList(this.left, this.right))
				       .withType(type, typeContext, markers, context);
		}

		if (!Types.isSuperClass(type, TupleType.getTupleClass(2).getClassType()))
		{
			return null;
		}

		this.type = null; // reset type

		final IType leftType = Types.resolveTypeSafely(type, LazyFields.KEY_PARAMETER);
		final IType rightType = Types.resolveTypeSafely(type, LazyFields.VALUE_PARAMETER);

		this.left = TypeChecker.convertValue(this.left, leftType, typeContext, markers, context,
		                                     TypeChecker.markerSupplier("colon_operator.left.type"));
		this.right = TypeChecker.convertValue(this.right, rightType, typeContext, markers, context,
		                                      TypeChecker.markerSupplier("colon_operator.right.type"));

		return this;
	}

	@Override
	public boolean isType(IType type)
	{
		return Types.isSuperType(type, this.getType()) || type.getAnnotation(LazyFields.COLON_CONVERTIBLE) != null;
	}

	@Override
	public int getTypeMatch(IType type, IImplicitContext implicitContext)
	{
		if (!Types.isSuperClass(type, TupleType.getTupleClass(2).getClassType()))
		{
			if (type.getAnnotation(LazyFields.COLON_CONVERTIBLE) != null)
			{
				return CONVERSION_MATCH;
			}
			return MISMATCH;
		}

		final IType leftType = Types.resolveTypeSafely(type, LazyFields.KEY_PARAMETER);
		final int leftMatch = TypeChecker.getTypeMatch(this.left, leftType, implicitContext);
		if (leftMatch == MISMATCH)
		{
			return MISMATCH;
		}

		final IType rightType = Types.resolveTypeSafely(type, LazyFields.VALUE_PARAMETER);
		final int rightMatch = TypeChecker.getTypeMatch(this.right, rightType, implicitContext);
		if (rightMatch == MISMATCH)
		{
			return MISMATCH;
		}
		return Math.min(leftMatch, rightMatch);
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.left != null)
		{
			this.left.resolveTypes(markers, context);
		}
		if (this.right != null)
		{
			this.right.resolveTypes(markers, context);
		}
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		if (this.left != null)
		{
			this.left = this.left.resolve(markers, context);
		}
		else
		{
			markers.add(Markers.semanticError(this.position, "colon_operator.left.invalid"));
			this.left = new WildcardValue(this.position);
		}

		if (this.right != null)
		{
			this.right = this.right.resolve(markers, context);
		}
		else
		{
			markers.add(Markers.semanticError(this.position, "colon_operator.right.invalid"));
			this.right = new WildcardValue(this.position);
		}
		return this;
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.left.checkTypes(markers, context);
		this.right.checkTypes(markers, context);
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.left.check(markers, context);
		this.right.check(markers, context);
	}

	@Override
	public IValue foldConstants()
	{
		this.left = this.left.foldConstants();
		this.right = this.right.foldConstants();
		return this;
	}

	@Override
	public IValue cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		this.left.cleanup(compilableList, classCompilableList);
		this.right.cleanup(compilableList, classCompilableList);
		return this;
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		this.left.writeExpression(writer, Types.OBJECT);
		this.right.writeExpression(writer, Types.OBJECT);

		final int lineNumber = this.lineNumber();

		writer.visitLineNumber(lineNumber);
		writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/tuple/Tuple$Of2", "apply",
		                       "(Ljava/lang/Object;Ljava/lang/Object;)Ldyvil/tuple/Tuple$Of2;", false);

		if (type != null)
		{
			TupleType.getTupleClass(2).getClassType().writeCast(writer, type, lineNumber);
		}
	}

	@Override
	public String toString()
	{
		return Formattable.toString(this);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.left.toString(prefix, buffer);
		Formatting.appendSeparator(buffer, "colonoperator", ':');
		this.right.toString(prefix, buffer);
	}
}
