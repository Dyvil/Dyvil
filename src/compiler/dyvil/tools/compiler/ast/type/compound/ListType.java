package dyvil.tools.compiler.ast.type.compound;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.TypeAnnotatableVisitor;
import dyvil.tools.asm.TypePath;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constructor.IConstructor;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Mutability;
import dyvil.tools.compiler.ast.type.raw.IObjectType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ListType implements IObjectType
{
	public static final class ListTypes
	{
		public static final IClass LIST_CLASS           = Package.dyvilCollection.resolveClass("List");
		public static final IClass MUTABLE_LIST_CLASS   = Package.dyvilCollection.resolveClass("MutableList");
		public static final IClass IMMUTABLE_LIST_CLASS = Package.dyvilCollection.resolveClass("ImmutableList");

		private ListTypes()
		{
			// no instances
		}
	}

	protected IType elementType;
	protected Mutability mutability = Mutability.UNDEFINED;

	// Metadata
	private IClass theClass;

	public ListType()
	{
	}

	public ListType(IType elementType)
	{
		this.elementType = elementType;
	}

	public ListType(IType elementType, Mutability mutability)
	{
		this.elementType = elementType;
		this.mutability = mutability;
	}

	public ListType(IType elementType, Mutability mutability, IClass theClass)
	{
		this.elementType = elementType;
		this.mutability = mutability;
		this.theClass = theClass;
	}

	@Override
	public int typeTag()
	{
		return LIST;
	}

	@Override
	public boolean isGenericType()
	{
		return true;
	}

	@Override
	public Name getName()
	{
		return this.theClass.getName();
	}

	@Override
	public IClass getTheClass()
	{
		return this.theClass;
	}

	@Override
	public IType asParameterType()
	{
		final IType type = this.elementType.asParameterType();
		return type == this.elementType ? this : new ListType(type, this.mutability, this.theClass);
	}

	@Override
	public IType resolveType(ITypeParameter typeParameter)
	{
		if (typeParameter.getGeneric() == this.theClass)
		{
			return this.elementType;
		}
		return this.theClass.resolveType(typeParameter, this);
	}

	@Override
	public boolean hasTypeVariables()
	{
		return this.elementType.hasTypeVariables();
	}

	@Override
	public IType getConcreteType(ITypeContext context)
	{
		final IType concreteElementType = this.elementType.getConcreteType(context);
		if (concreteElementType != this.elementType)
		{
			return new ListType(concreteElementType, this.mutability, this.theClass);
		}
		return this;
	}

	@Override
	public void inferTypes(IType concrete, ITypeContext typeContext)
	{
		this.elementType.inferTypes(concrete.resolveType(this.theClass.getTypeParameter(0)), typeContext);
	}

	@Override
	public boolean isResolved()
	{
		return this.theClass != null;
	}

	@Override
	public IType resolveType(MarkerList markers, IContext context)
	{
		this.theClass = getClass(this.mutability);

		this.elementType = this.elementType.resolveType(markers, context);
		return this;
	}
	
	private static IClass getClass(Mutability mutability)
	{
		if (mutability == Mutability.IMMUTABLE)
		{
			return ListTypes.IMMUTABLE_LIST_CLASS;
		}
		if (mutability == Mutability.MUTABLE)
		{
			return ListTypes.MUTABLE_LIST_CLASS;
		}
		return ListTypes.LIST_CLASS;
	}
	
	@Override
	public void checkType(MarkerList markers, IContext context, TypePosition position)
	{
		this.elementType.checkType(markers, context, position);
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.elementType.check(markers, context);
	}

	@Override
	public void foldConstants()
	{
		this.elementType.foldConstants();
	}

	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		this.elementType.cleanup(context, compilableList);
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		return null;
	}

	@Override
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, IArguments arguments)
	{
		this.theClass.getMethodMatches(list, receiver, name, arguments);
	}

	@Override
	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
		this.theClass.getImplicitMatches(list, value, targetType);
	}

	@Override
	public void getConstructorMatches(MatchList<IConstructor> list, IArguments arguments)
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
		return this.theClass.getInternalName();
	}

	@Override
	public String getSignature()
	{
		return IType.getSignature(this);
	}

	@Override
	public void appendSignature(StringBuilder buffer)
	{
		buffer.append('L').append(this.theClass.getInternalName()).append('<');
		this.elementType.appendSignature(buffer);
		buffer.append('>').append(';');
	}

	@Override
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException
	{
		this.elementType.writeTypeExpression(writer);
		writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvilx/lang/model/type/ListType", "apply",
		                       "(Ldyvilx/lang/model/type/Type;)Ldyvilx/lang/model/type/ListType;", false);
	}

	@Override
	public void addAnnotation(IAnnotation annotation, TypePath typePath, int step, int steps)
	{
		if (step >= steps || typePath.getStep(step) != TypePath.TYPE_ARGUMENT)
		{
			return;
		}

		this.elementType = IType.withAnnotation(this.elementType, annotation, typePath, step + 1, steps);
	}

	@Override
	public void writeAnnotations(TypeAnnotatableVisitor visitor, int typeRef, String typePath)
	{
		this.elementType.writeAnnotations(visitor, typeRef, typePath + "0;");
	}

	@Override
	public void write(DataOutput out) throws IOException
	{
		IType.writeType(this.elementType, out);
		this.mutability.write(out);
	}

	@Override
	public void read(DataInput in) throws IOException
	{
		this.elementType = IType.readType(in);
		this.mutability = Mutability.read(in);
	}

	@Override
	public String toString()
	{
		return "[" + this.elementType.toString() + "...]";
	}

	@Override
	public void toString(String prefix, StringBuilder builder)
	{
		builder.append('[');
		this.mutability.appendKeyword(builder);
		this.elementType.toString(prefix, builder);
		builder.append("...]");
	}

	@Override
	public IType clone()
	{
		return new ListType(this.elementType, this.mutability, this.theClass);
	}
}
