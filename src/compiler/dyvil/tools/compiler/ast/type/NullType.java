package dyvil.tools.compiler.ast.type;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.method.ConstructorMatchList;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

public class NullType implements IRawType
{
	@Override
	public int typeTag()
	{
		return NULL;
	}
	
	@Override
	public Name getName()
	{
		return Names._null;
	}
	
	@Override
	public IClass getTheClass()
	{
		return Types.NULL_CLASS;
	}
	
	@Override
	public IType getSuperType()
	{
		return null;
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
	public void checkType(MarkerList markers, IContext context, TypePosition position)
	{
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		return null;
	}
	
	@Override
	public void getMethodMatches(MethodMatchList list, IValue instance, Name name, IArguments arguments)
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
		return "dyvil/lang/Null";
	}
	
	@Override
	public void appendExtendedName(StringBuilder buffer)
	{
		buffer.append("Ldyvil/lang/Null;");
	}
	
	@Override
	public void appendSignature(StringBuilder buffer)
	{
		buffer.append("Ldyvil/lang/Null;");
	}
	
	@Override
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException
	{
		writer.writeFieldInsn(Opcodes.GETSTATIC, "dyvil/reflect/types/NullType", "instance", "Ldyvil/reflect/types/NullType;");
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
		return "null";
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("null");
	}
	
	@Override
	public IType clone()
	{
		return this;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return this.equals((IType) obj);
	}
	
	@Override
	public int hashCode()
	{
		return NULL;
	}
}
