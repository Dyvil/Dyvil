package dyvil.tools.compiler.ast.type.compound;

import dyvil.collection.Set;
import dyvil.tools.asm.TypeAnnotatableVisitor;
import dyvil.tools.asm.TypePath;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constructor.ConstructorMatchList;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.raw.IObjectType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class UnionType implements IObjectType
{
	private static final IClass[] OBJECT_CLASS_ARRAY = { Types.OBJECT_CLASS };

	private IType left;
	private IType right;

	// Metadata
	private IClass[] commonClasses;

	public UnionType(IType left, IType right)
	{
		this.left = left;
		this.right = right;
	}

	@Override
	public int typeTag()
	{
		return UNION;
	}

	@Override
	public boolean isGenericType()
	{
		return this.left.isGenericType() || this.right.isGenericType();
	}

	@Override
	public Name getName()
	{
		return Names.Union;
	}

	@Override
	public IClass getTheClass()
	{
		if (this.commonClasses != null)
		{
			return this.commonClasses[0];
		}

		if (this.left.getTheClass() == Types.OBJECT_CLASS || this.right.getTheClass() == Types.OBJECT_CLASS)
		{
			this.commonClasses = OBJECT_CLASS_ARRAY;
			return Types.OBJECT_CLASS;
		}

		final Set<IClass> commonClassSet = Types.commonClasses(this.left, this.right);
		commonClassSet.remove(Types.OBJECT_CLASS);

		this.commonClasses = new IClass[commonClassSet.size()];
		commonClassSet.toArray(this.commonClasses);

		return this.commonClasses[0];
	}

	@Override
	public boolean isSuperTypeOf(IType type)
	{
		if (type.typeTag() == UNION)
		{
			final UnionType unionType = (UnionType) type;
			return this.left.isSuperTypeOf(unionType.left) && this.right.isSuperTypeOf(unionType.right) // normal order
				       || this.left.isSuperTypeOf(unionType.right) && this.right.isSuperTypeOf(unionType.left);
			// reverse order
		}

		return this.left.isSuperTypeOf(type) || this.right.isSuperTypeOf(type);
	}

	@Override
	public int getSuperTypeDistance(IType superType)
	{
		return Math.min(this.left.getSuperTypeDistance(superType), this.right.getSuperTypeDistance(superType));
	}

	@Override
	public IType resolveType(ITypeParameter typeParameter)
	{
		final IType left = this.left.resolveType(typeParameter);
		final IType right = this.right.resolveType(typeParameter);
		if (left == null)
		{
			return right;
		}
		if (right == null)
		{
			return left;
		}

		return new UnionType(left, right);
	}

	@Override
	public boolean hasTypeVariables()
	{
		return this.left.hasTypeVariables() || this.right.hasTypeVariables();
	}

	@Override
	public IType getConcreteType(ITypeContext context)
	{
		final IType left = this.left.getConcreteType(context);
		final IType right = this.right.getConcreteType(context);
		if (left == this.left && right == this.right)
		{
			return this;
		}

		return new UnionType(left, right);
	}

	@Override
	public void inferTypes(IType concrete, ITypeContext typeContext)
	{
		this.left.inferTypes(concrete, typeContext);
		this.right.inferTypes(concrete, typeContext);
	}

	@Override
	public boolean isResolved()
	{
		return true;
	}

	@Override
	public IType resolveType(MarkerList markers, IContext context)
	{
		this.left = this.left.resolveType(markers, context);
		this.right = this.right.resolveType(markers, context);
		return combine(this.left, this.right, this);
	}

	public static IType combine(IType type1, IType type2, UnionType unionType)
	{
		if (type1 == Types.VOID || type2 == Types.VOID)
		{
			// either type is void -> result void
			return Types.VOID;
		}
		if (type1.isArrayType())
		{
			if (!type2.isArrayType())
			{
				return Types.ANY;
			}
			return arrayElementCombine(type1.getElementType(), type2.getElementType());
		}
		if (type2.isArrayType())
		{
			if (!type1.isArrayType())
			{
				return Types.ANY;
			}
			return arrayElementCombine(type1.getElementType(), type2.getElementType());
		}

		IClass class1 = type1.getTheClass();
		if (class1 == null)
		{
			// type 1 unresolved -> result type 2
			return type2;
		}
		if (class1 == Types.NULL_CLASS)
		{
			// type 1 is null -> result reference type 2
			return type2.getObjectType();
		}
		if (class1 == Types.OBJECT_CLASS)
		{
			// type 1 is Object -> result Object
			return Types.ANY;
		}

		final IClass class2 = type2.getTheClass();
		if (class2 == null)
		{
			// type 2 unresolved -> result type 1
			return type1;
		}
		if (class2 == Types.NULL_CLASS)
		{
			// type 2 is Object or null -> result reference type 1
			return type1.getObjectType();
		}
		if (class2 == Types.OBJECT_CLASS)
		{
			// type 2 is Object -> result Object
			return Types.ANY;
		}

		if (type1.isSameType(type2) || type1.isSuperTypeOf(type2))
		{
			// same type, or type 1 is a super type of type 2 -> result type 1
			return type1;
		}
		if (type2.isSuperTypeOf(type1))
		{
			// type 2 is a super type of type 1 -> result type 2
			return type2;
		}

		return unionType != null ? unionType : new UnionType(type1, type2);
	}

	private static IType arrayElementCombine(IType type1, IType type2)
	{
		if (type1.getTypecode() != type2.getTypecode())
		{
			return Types.ANY;
		}
		return new ArrayType(combine(type1, type2, null));
	}

	@Override
	public void checkType(MarkerList markers, IContext context, TypePosition position)
	{
		this.left.checkType(markers, context, position);
		this.right.checkType(markers, context, position);
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.left.check(markers, context);
		this.right.check(markers, context);
	}

	@Override
	public void foldConstants()
	{
		this.left.foldConstants();
		this.right.foldConstants();
	}

	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		this.left.cleanup(context, compilableList);
		this.right.cleanup(context, compilableList);
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		return this.getTheClass().resolveField(name);
	}

	@Override
	public void getMethodMatches(MethodMatchList list, IValue instance, Name name, IArguments arguments)
	{
		for (IClass commonClass : this.commonClasses)
		{
			commonClass.getMethodMatches(list, instance, name, arguments);
		}
	}

	@Override
	public void getConstructorMatches(ConstructorMatchList list, IArguments arguments)
	{
	}

	@Override
	public IMethod getFunctionalMethod()
	{
		return null;
	}

	@Override
	public String getInternalName()
	{
		return this.getTheClass().getInternalName();
	}

	@Override
	public String getSignature()
	{
		final StringBuilder stringBuilder = new StringBuilder();
		this.appendSignature(stringBuilder);
		return stringBuilder.toString();
	}

	@Override
	public void appendSignature(StringBuilder buffer)
	{
		buffer.append('L').append(this.getTheClass().getInternalName()).append(';');
	}

	@Override
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException
	{
		// TODO
	}

	@Override
	public void addAnnotation(IAnnotation annotation, TypePath typePath, int step, int steps)
	{
	}

	@Override
	public void writeAnnotations(TypeAnnotatableVisitor visitor, int typeRef, String typePath)
	{
	}

	@Override
	public void write(DataOutput out) throws IOException
	{
		IType.writeType(this.left, out);
		IType.writeType(this.right, out);
	}

	@Override
	public void read(DataInput in) throws IOException
	{
		this.left = IType.readType(in);
		this.right = IType.readType(in);
	}

	@Override
	public String toString()
	{
		return IASTNode.toString(this);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.left.toString(prefix, buffer);
		buffer.append(" | ");
		this.right.toString(prefix, buffer);
	}

	@Override
	public IType clone()
	{
		return new UnionType(this.left.clone(), this.right.clone());
	}
}
