package dyvil.tools.compiler.ast.type.compound;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Type;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

public class IntersectionType extends BinaryType
{
	public IntersectionType()
	{
	}

	public IntersectionType(IType left, IType right)
	{
		this.left = left;
		this.right = right;
	}

	@Override
	protected IType combine(IType left, IType right)
	{
		return combine(left, right, null);
	}

	public static IType combine(IType left, IType right, IntersectionType intersectionType)
	{
		if (Types.isSameType(left, right) || Types.isSuperType(left, right))
		{
			// same type, or left type is a super type of right type -> result left type
			return right;
		}
		if (Types.isSuperType(right, left))
		{
			// right type is a super type of left type -> result right type
			return left;
		}

		return intersectionType != null ?
			       intersectionType :
			       new IntersectionType(left.getObjectType(), right.getObjectType());
	}

	@Override
	public int typeTag()
	{
		return INTERSECTION;
	}

	@Override
	public Name getName()
	{
		return Names.Intersection;
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
		return Types.isSuperType(this.left, subType) && Types.isSuperType(this.right, subType);
	}

	@Override
	public boolean isSuperClassOf(IType subType)
	{
		return Types.isSuperClass(this.left, subType) && Types.isSuperClass(this.right, subType);
	}

	@Override
	public boolean isSubTypeOf(IType superType)
	{
		return Types.isSuperType(superType, this.left) || Types.isSuperType(superType, this.right);
	}

	@Override
	public boolean isSubClassOf(IType superType)
	{
		return Types.isSuperClass(superType, this.left) || Types.isSuperClass(superType, this.right);
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
		final IDataMember field = this.left.resolveField(name);
		if (field != null)
		{
			return field;
		}
		return this.right.resolveField(name);
	}

	@Override
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, ArgumentList arguments)
	{
		this.left.getMethodMatches(list, receiver, name, arguments);
		this.right.getMethodMatches(list, receiver, name, arguments);
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
			buffer.append('&');
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
		writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/reflect/types/IntersectionType", "apply",
		                       "(Ljava/lang/Class;Ldyvil/reflect/types/Type;Ldyvil/reflect/types/Type;)Ldyvil/reflect/types/IntersectionType;",
		                       false);
	}

	// String Conversion

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.left.toString(prefix, buffer);
		buffer.append(" & ");
		this.right.toString(prefix, buffer);
	}
}
