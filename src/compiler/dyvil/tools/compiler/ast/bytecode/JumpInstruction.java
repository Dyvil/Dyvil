package dyvil.tools.compiler.ast.bytecode;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.statement.Label;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class JumpInstruction implements IInstruction
{
	private ICodePosition	position;
	private int				opcode;
	private Label			target;
	
	public JumpInstruction(int opcode, Label target)
	{
		this.opcode = opcode;
		this.target = target;
	}
	
	public JumpInstruction(ICodePosition position, int opcode, Name target)
	{
		this.position = position;
		this.opcode = opcode;
		this.target = new Label(target);
	}
	
	@Override
	public void resolve(MarkerList markers, Bytecode bytecode)
	{
		if (this.target != null)
		{
			this.target = bytecode.resolveLabel(this.target.name);
			if (this.target == null)
			{
				markers.add(this.position, "resolve.label", this.target);
				return;
			}
		}
	}
	
	@Override
	public void write(MethodWriter writer)
	{
		writer.writeJumpInsn(this.opcode, this.target.target);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(Opcodes.toString(this.opcode)).append(' ').append(this.target);
	}
}
