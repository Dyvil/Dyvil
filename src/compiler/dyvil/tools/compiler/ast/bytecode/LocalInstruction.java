package dyvil.tools.compiler.ast.bytecode;

import java.util.List;

import jdk.internal.org.objectweb.asm.Label;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.Marker;

public class LocalInstruction extends Instruction
{
	private String	varName;
	private String	desc;
	private IType	type;
	private int		index;
	
	private Label	startLabel	= new Label();
	private Label	endLabel;
	
	public LocalInstruction(String name)
	{
		super(0, name);
	}
	
	@Override
	public boolean addArgument(Object arg)
	{
		if (this.varName == null)
		{
			if (arg instanceof String)
			{
				this.varName = (String) arg;
				return true;
			}
		}
		else if (this.desc == null)
		{
			if (arg instanceof String)
			{
				this.desc = ClassFormat.userToInternal((String) arg);
				this.type = ClassFormat.internalToType(this.desc);
				return true;
			}
		}
		else if (this.index == -1)
		{
			if (arg instanceof Integer)
			{
				this.index = (Integer) arg;
			}
		}
		return false;
	}
	
	@Override
	public void resolve(List<Marker> markers, Bytecode bytecode)
	{
		this.endLabel = bytecode.endLabel;
	}
	
	@Override
	public void write(MethodWriter writer)
	{
		// FIXME Register the local variable using addLocal
		writer.visitLocalVariable(this.varName, this.type, this.startLabel, this.endLabel, this.index);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.name).append(' ');
		buffer.append('"').append(this.varName);
		buffer.append("\", \"").append(this.desc);
		buffer.append("\", ").append(this.index);
	}
}
