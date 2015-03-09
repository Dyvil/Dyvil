package dyvil.tools.compiler.ast.bytecode;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.method.IBaseMethod;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.backend.MethodWriter;

public class InvokeInstruction extends Instruction implements IBaseMethod
{
	private String	owner;
	private String	methodName;
	private String	desc;
	private int		args;
	private IType	type;
	
	public InvokeInstruction(int opcode, String name)
	{
		super(opcode, name);
		if (opcode != Opcodes.INVOKESTATIC)
		{
			this.args++;
		}
	}
	
	@Override
	public boolean addArgument(Object arg)
	{
		if (arg instanceof String)
		{
			if (this.owner == null)
			{
				this.owner = ClassFormat.packageToInternal((String) arg);
				return true;
			}
			else if (this.methodName == null)
			{
				this.methodName = (String) arg;
				return true;
			}
			else if (this.desc == null)
			{
				this.desc = (String) arg;
				ClassFormat.readMethodType(this.desc, this);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void write(MethodWriter writer)
	{
		IType type = this.type == Type.VOID ? null : this.type;
		writer.writeInvokeInsn(this.opcode, this.owner, this.methodName, this.desc, false, this.args, type);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.name).append(' ');
		buffer.append('"').append(this.owner);
		buffer.append("\", \"").append(this.methodName);
		buffer.append("\", \"").append(this.desc).append('"');
	}
	
	// IMethod Interface
	
	@Override
	public void setName(String name, String qualifiedName)
	{
	}
	
	@Override
	public void setName(String name)
	{
	}
	
	@Override
	public void setQualifiedName(String name)
	{
	}
	
	@Override
	public String getQualifiedName()
	{
		return null;
	}
	
	@Override
	public boolean isName(String name)
	{
		return false;
	}
	
	@Override
	public void setType(IType type)
	{
		this.type = type;
	}
	
	@Override
	public IType getType()
	{
		return this.type;
	}
	
	@Override
	public void addType(IType type)
	{
		this.args++;
	}
	
	@Override
	public void setGeneric()
	{
	}
	
	@Override
	public boolean isGeneric()
	{
		return false;
	}
	
	@Override
	public int genericCount()
	{
		return 0;
	}
	
	@Override
	public void setTypeVariable(int index, ITypeVariable var)
	{
	}
	
	@Override
	public void addTypeVariable(ITypeVariable var)
	{
	}
	
	@Override
	public ITypeVariable getTypeVariable(int index)
	{
		return null;
	}
}
