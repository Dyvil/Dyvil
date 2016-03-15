package dyvil.tools.compiler.ast.operator;

import dyvil.tools.asm.Opcodes;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.LiteralConversion;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.compound.TupleType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.transform.TypeChecker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class ColonOperator implements IValue
{
	public static final class LazyFields
	{
		public static final IClass COLON_CONVERTIBLE = Package.dyvilLangLiteral.resolveClass("ColonConvertible");

		private LazyFields()
		{
			// no instances
		}
	}

	private IValue left;
	private IValue right;

	// Metadata
	private ICodePosition position;
	private IType         type;

	public ColonOperator(ICodePosition position, IValue left, IValue right)
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
	public void setPosition(ICodePosition position)
	{
		this.position = position;
	}

	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}

	@Override
	public boolean isResolved()
	{
		return this.type != null;
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
		final IAnnotation annotation = type.getAnnotation(LazyFields.COLON_CONVERTIBLE);
		if (annotation != null)
		{
			return new LiteralConversion(this, annotation, new ArgumentList(this.left, this.right))
					       .withType(type, typeContext, markers, context);
		}

		if (!type.isSuperClassOf(TupleType.getTupleClass(2).getClassType()))
		{
			return null;
		}

		final IClass iclass = type.getTheClass();

		final IType leftType;
		final IType rightType;

		if (iclass == Types.OBJECT_CLASS)
		{
			leftType = rightType = Types.ANY;
		}
		else
		{
			leftType = type.resolveTypeSafely(iclass.getTypeParameter(0));
			rightType = type.resolveTypeSafely(iclass.getTypeParameter(1));
		}

		this.left = TypeChecker.convertValue(this.left, leftType, typeContext, markers, context,
		                                     TypeChecker.markerSupplier("colon.type.left"));
		this.right = TypeChecker.convertValue(this.right, rightType, typeContext, markers, context,
		                                      TypeChecker.markerSupplier("colon.type.right"));

		return this;
	}

	@Override
	public boolean isType(IType type)
	{
		return type.getAnnotation(LazyFields.COLON_CONVERTIBLE) != null || type.isSuperTypeOf(this.getType());
	}

	@Override
	public float getTypeMatch(IType type)
	{
		if (type.getAnnotation(LazyFields.COLON_CONVERTIBLE) != null)
		{
			return IValue.CONVERSION_MATCH;
		}

		return type.getSubClassDistance(this.getType());
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.left.resolveTypes(markers, context);
		this.right.resolveTypes(markers, context);
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.left = this.left.resolve(markers, context);
		this.right = this.right.resolve(markers, context);
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
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		this.left.cleanup(context, compilableList);
		this.right.cleanup(context, compilableList);
		return this;
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		this.left.writeExpression(writer, Types.OBJECT);
		this.right.writeExpression(writer, Types.OBJECT);

		final int lineNumber = this.getLineNumber();

		writer.writeLineNumber(lineNumber);
		writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/tuple/Tuple2", "apply",
		                       "(Ljava/lang/Object;Ljava/lang/Object;)Ldyvil/tuple/Tuple2;", false);

		if (type != null)
		{
			TupleType.getTupleClass(2).getClassType().writeCast(writer, type, lineNumber);
		}
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.left.toString(prefix, buffer);
		Formatting.appendSeparator(buffer, "colonoperator", ':');
		this.right.toString(prefix, buffer);
	}
}
