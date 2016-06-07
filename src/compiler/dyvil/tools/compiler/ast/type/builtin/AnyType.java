package dyvil.tools.compiler.ast.type.builtin;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constructor.ConstructorMatchList;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.raw.IRawType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class AnyType implements IRawType
{
	@Override
	public int typeTag()
	{
		return ANY;
	}
	
	@Override
	public Name getName()
	{
		return Names.any;
	}
	
	@Override
	public IClass getTheClass()
	{
		return Types.OBJECT_CLASS;
	}
	
	@Override
	public boolean isSuperTypeOf(IType type)
	{
		return true;
	}
	
	@Override
	public boolean isSuperClassOf(IType subType)
	{
		return true;
	}
	
	@Override
	public boolean isResolved()
	{
		return true;
	}
	
	@Override
	public IType resolveType(MarkerList markers, IContext context)
	{
		return this;
	}
	
	@Override
	public void checkType(MarkerList markers, IContext context, IType.TypePosition position)
	{
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		return null;
	}
	
	@Override
	public void getMethodMatches(MethodMatchList list, IValue receiver, Name name, IArguments arguments)
	{
		Types.OBJECT_CLASS.getMethodMatches(list, receiver, name, arguments);
	}

	@Override
	public void getImplicitMatches(MethodMatchList list, IValue value, IType targetType)
	{
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
		return "java/lang/Object";
	}
	
	@Override
	public void appendExtendedName(StringBuilder buffer)
	{
		buffer.append("Ljava/lang/Object;");
	}
	
	@Override
	public void appendSignature(StringBuilder buffer)
	{
		buffer.append("Ljava/lang/Object;");
	}
	
	@Override
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException
	{
		writer.visitFieldInsn(Opcodes.GETSTATIC, "dyvilx/lang/model/type/AnyType", "instance",
		                      "Ldyvilx/lang/model/type/AnyType;");
	}
	
	@Override
	public void write(DataOutput out) throws IOException
	{
	}
	
	@Override
	public void read(DataInput in) throws IOException
	{
	}
	
	@Override
	public String toString()
	{
		return "any";
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("any");
	}
	
	@Override
	public IType clone()
	{
		return this;
	}
}
