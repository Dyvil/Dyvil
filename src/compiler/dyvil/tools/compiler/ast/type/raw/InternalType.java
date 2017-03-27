package dyvil.tools.compiler.ast.type.raw;

import dyvil.tools.compiler.ast.annotation.AnnotationUtil;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constructor.IConstructor;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.reference.ReferenceType;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.PrimitiveType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class InternalType implements IRawType, IUnresolvedType
{
	protected String internalName;

	public InternalType(String internalName)
	{
		this.internalName = internalName;
	}

	@Override
	public int typeTag()
	{
		return INTERNAL;
	}

	@Override
	public Name getName()
	{
		return Name.fromRaw(this.internalName.substring(this.internalName.lastIndexOf('/') + 1));
	}

	@Override
	public IClass getTheClass()
	{
		return null;
	}

	@Override
	public boolean isResolved()
	{
		return false;
	}

	@Override
	public IType resolveType(MarkerList markers, IContext context)
	{
		switch (this.internalName)
		{
		case "dyvil/ref/BooleanRef":
			return ReferenceType.apply(Types.BOOLEAN);
		case "dyvil/ref/ByteRef":
			return ReferenceType.apply(Types.BYTE);
		case "dyvil/ref/ShortRef":
			return ReferenceType.apply(Types.SHORT);
		case "dyvil/ref/CharRef":
			return ReferenceType.apply(Types.CHAR);
		case "dyvil/ref/IntRef":
			return ReferenceType.apply(Types.INT);
		case "dyvil/ref/LongRef":
			return ReferenceType.apply(Types.LONG);
		case "dyvil/ref/FloatRef":
			return ReferenceType.apply(Types.FLOAT);
		case "dyvil/ref/DoubleRef":
			return ReferenceType.apply(Types.DOUBLE);
		case "dyvil/ref/StringRef":
			return ReferenceType.apply(Types.STRING);
		}

		final IClass resolvedClass = Package.rootPackage.resolveInternalClass(this.internalName);
		if (resolvedClass == null)
		{
			return Types.UNKNOWN;
		}
		return resolvedClass.getClassType();
	}

	@Override
	public void checkType(MarkerList markers, IContext context, int position)
	{
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		return null;
	}

	@Override
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, ArgumentList arguments)
	{
	}

	@Override
	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
	}

	@Override
	public void getConstructorMatches(MatchList<IConstructor> list, ArgumentList arguments)
	{
	}

	@Override
	public IMethod getFunctionalMethod()
	{
		return null;
	}

	@Override
	public IType withAnnotation(IAnnotation annotation)
	{
		if (AnnotationUtil.PRIMITIVE_INTERNAL.equals(annotation.getType().getInternalName()))
		{
			return PrimitiveType.getPrimitiveType(this.internalName);
		}
		return null;
	}

	@Override
	public String getInternalName()
	{
		return this.internalName;
	}

	@Override
	public void write(DataOutput out) throws IOException
	{
		out.writeUTF(this.internalName);
	}

	@Override
	public void read(DataInput in) throws IOException
	{
		this.internalName = in.readUTF();
	}

	@Override
	public String toString()
	{
		return ClassFormat.internalToPackage(this.internalName);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(ClassFormat.internalToPackage(this.internalName));
	}
}
