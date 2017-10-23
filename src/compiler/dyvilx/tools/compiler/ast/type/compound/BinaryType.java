package dyvilx.tools.compiler.ast.type.compound;

import dyvil.collection.Set;
import dyvil.lang.Formattable;
import dyvilx.tools.asm.TypeAnnotatableVisitor;
import dyvilx.tools.asm.TypePath;
import dyvilx.tools.compiler.ast.attribute.annotation.Annotation;
import dyvilx.tools.compiler.ast.attribute.annotation.AnnotationUtil;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.constructor.IConstructor;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.generic.ITypeParameter;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.ast.type.raw.IObjectType;
import dyvilx.tools.compiler.util.MemberSorter;
import dyvilx.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

public abstract class BinaryType implements IObjectType
{
	private static final IClass[] OBJECT_CLASS_ARRAY = { Types.OBJECT_CLASS };

	protected IType left;
	protected IType right;

	// Metadata
	protected IClass[] commonClasses;

	protected abstract IType combine(IType left, IType right);

	public IType getLeft()
	{
		return this.left;
	}

	public void setLeft(IType left)
	{
		this.left = left;
	}

	public IType getRight()
	{
		return this.right;
	}

	public void setRight(IType right)
	{
		this.right = right;
	}

	@Override
	public IType asParameterType()
	{
		return this.combine(this.left.asParameterType(), this.right.asParameterType());
	}

	@Override
	public boolean isGenericType()
	{
		return this.left.isGenericType() || this.right.isGenericType();
	}

	@Override
	public boolean isResolved()
	{
		return true;
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

		if (commonClassSet.isEmpty())
		{
			this.commonClasses = OBJECT_CLASS_ARRAY;
			return Types.OBJECT_CLASS;
		}

		this.commonClasses = new IClass[commonClassSet.size()];
		commonClassSet.toArray(this.commonClasses);
		Arrays.sort(this.commonClasses, MemberSorter.CLASS_COMPARATOR);
		return this.commonClasses[0];
	}

	@Override
	public boolean hasTypeVariables()
	{
		return this.left.hasTypeVariables() || this.right.hasTypeVariables();
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

		return this.combine(left, right);
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

		return this.combine(left, right);
	}

	@Override
	public void inferTypes(IType concrete, ITypeContext typeContext)
	{
		this.left.inferTypes(concrete, typeContext);
		this.right.inferTypes(concrete, typeContext);
	}

	@Override
	public Annotation getAnnotation(IClass type)
	{
		final Annotation left = this.left.getAnnotation(type);
		return left != null ? left : this.right.getAnnotation(type);
	}

	// Resolution

	@Override
	public void getConstructorMatches(MatchList<IConstructor> list, ArgumentList arguments)
	{
	}

	@Override
	public IMethod getFunctionalMethod()
	{
		return null;
	}

	// Phases

	@Override
	public void checkType(MarkerList markers, IContext context, int position)
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
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		this.left.cleanup(compilableList, classCompilableList);
		this.right.cleanup(compilableList, classCompilableList);
	}

	// Compilation

	@Override
	public String getInternalName()
	{
		return this.getTheClass().getInternalName();
	}

	@Override
	public int getDescriptorKind()
	{
		return NAME_FULL;
	}

	@Override
	public void addAnnotation(Annotation annotation, TypePath typePath, int step, int steps)
	{
	}

	@Override
	public void writeAnnotations(TypeAnnotatableVisitor visitor, int typeRef, String typePath)
	{
		AnnotationUtil.visitDyvilName(this, visitor, typeRef, typePath);
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
		return Formattable.toString(this);
	}
}
