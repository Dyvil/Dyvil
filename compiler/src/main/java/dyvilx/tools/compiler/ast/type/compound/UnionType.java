package dyvilx.tools.compiler.ast.type.compound;

import dyvil.reflect.Opcodes;
import dyvilx.tools.asm.Type;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.NullType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.transform.Names;
import dyvil.lang.Name;
import dyvilx.tools.parsing.marker.MarkerList;

public class UnionType extends BinaryType
{
	public UnionType()
	{
	}

	public UnionType(IType left, IType right)
	{
		this.left = left;
		this.right = right;
	}

	@Override
	protected IType combine(IType left, IType right)
	{
		return combine(left, right, null);
	}

	public static IType combine(IType left, IType right, UnionType unionType)
	{
		if (left.canExtract(NullType.class))
		{
			// left type is null -> result reference right type
			return NullableType.apply(right);
		}

		if (right.canExtract(NullType.class))
		{
			// right type is null -> result reference left type
			return NullableType.apply(left);
		}

		if (Types.isSameType(left, right) || Types.isSuperType(left, right))
		{
			// same type, or left type is a super type of right type -> result left type
			return left;
		}
		if (Types.isSuperType(right, left))
		{
			// right type is a super type of left type -> result right type
			return right;
		}

		return unionType != null ? unionType : new UnionType(left.getObjectType(), right.getObjectType());
	}

	@Override
	public int typeTag()
	{
		return UNION;
	}

	@Override
	public Name getName()
	{
		return Names.Union;
	}

	// Subtyping

	@Override
	public int subTypeCheckLevel()
	{
		return SUBTYPE_UNION_INTERSECTION;
	}

	@Override
	public boolean isSuperTypeOf(IType subType)
	{
		if (subType.typeTag() == IType.UNION)
		{
			return subType.isSubTypeOf(this);
		}
		return Types.isSuperType(this.left, subType) || Types.isSuperType(this.right, subType);
	}

	@Override
	public boolean isSuperClassOf(IType subType)
	{
		return Types.isSuperClass(this.left, subType) || Types.isSuperClass(this.right, subType);
	}

	@Override
	public boolean isSubTypeOf(IType superType)
	{
		return Types.isSuperType(superType, this.left) && Types.isSuperType(superType, this.right);
	}

	@Override
	public boolean isSubClassOf(IType superType)
	{
		return Types.isSuperClass(superType, this.left) && Types.isSuperClass(superType, this.right);
	}

	// Phases

	@Override
	public IType resolveType(MarkerList markers, IContext context)
	{
		this.left = this.left.resolveType(markers, context);
		this.right = this.right.resolveType(markers, context);
		return combine(this.left, this.right, this);
	}

	// Resolution

	@Override
	public IDataMember resolveField(Name name)
	{
		return this.getTheClass().resolveField(name);
	}

	@Override
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, ArgumentList arguments)
	{
		if (receiver == null || receiver.isIgnoredClassAccess())
		{
			// Static Call
			this.left.getMethodMatches(list, receiver, name, arguments);
			this.right.getMethodMatches(list, receiver, name, arguments);
			return;
		}

		this.getTheClass();
		for (IClass commonClass : this.commonClasses)
		{
			commonClass.getMethodMatches(list, receiver, name, arguments);
		}
	}

	@Override
	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
		this.left.getImplicitMatches(list, value, targetType);
		this.right.getImplicitMatches(list, value, targetType);
	}

	// Compilation

	@Override
	public void appendDescriptor(StringBuilder buffer, int type)
	{
		if (type == NAME_FULL)
		{
			buffer.append('|');
			this.left.appendDescriptor(buffer, NAME_FULL);
			this.right.appendDescriptor(buffer, NAME_FULL);
			return;
		}

		buffer.append('L').append(this.getInternalName()).append(';');
	}

	@Override
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException
	{
		writer.visitLdcInsn(Type.getObjectType(this.getTheClass().getInternalName()));
		this.left.writeTypeExpression(writer);
		this.right.writeTypeExpression(writer);
		writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/reflect/types/UnionType", "apply",
		                       "(Ljava/lang/Class;Ldyvil/reflect/types/Type;Ldyvil/reflect/types/Type;)Ldyvil/reflect/types/UnionType;",
		                       false);
	}

	// String Conversion

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.left.toString(prefix, buffer);
		buffer.append(" | ");
		this.right.toString(prefix, buffer);
	}
}
