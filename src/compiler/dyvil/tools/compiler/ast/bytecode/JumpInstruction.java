package dyvil.tools.compiler.ast.bytecode;

import java.util.List;

import org.objectweb.asm.Label;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.Markers;

public class JumpInstruction extends Instruction
{
	private String	dest;
	private Label	destLabel;
	
	public JumpInstruction(int opcode, String name)
	{
		super(opcode, name);
	}
	
	@Override
	public boolean addArgument(Object arg)
	{
		if (arg instanceof String && this.dest == null)
		{
			this.dest = (String) arg;
			return true;
		}
		return false;
	}
	
	@Override
	public void resolve(List<Marker> markers, Bytecode bytecode)
	{
		this.destLabel = bytecode.getLabel(this.dest);
		if (this.destLabel == null)
		{
			markers.add(Markers.create(this.position, "resolve.label", this.dest));
		}
	}
	
	@Override
	public void write(MethodWriter writer)
	{
		writer.writeJumpInsn(this.opcode, this.destLabel);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.name).append(' ').append(this.dest);
	}
}
