package dyvil.tools.compiler.ast.method.intrinsic;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Label;
import dyvil.tools.asm.Type;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;

public class SpecialIntrinsicData implements IntrinsicData
{
	private IMethod method;
	
	private int[]		instructions;
	private String[]	strings;
	private Label[]		targets;
	
	public SpecialIntrinsicData(IMethod method, int[] instructions, String[] strings, Label[] targets)
	{
		this.method = method;
		this.instructions = instructions;
		this.strings = strings;
		this.targets = targets;
	}
	
	@Override
	public void writeIntrinsic(MethodWriter writer, IValue instance, IArguments arguments, int lineNumber) throws BytecodeException
	{
		int[] ints = this.instructions;
		int insn = 0;
		
		int length = this.instructions.length;
		for (int i = 0; i < length; i++)
		{
			Label label = targets[insn++];
			if (label != null)
			{
				writer.writeTargetLabel(label);
			}
			
			int opcode = ints[i];
			if (Opcodes.isFieldOpcode(opcode))
			{
				String owner = strings[ints[i + 1]];
				String name = strings[ints[i + 2]];
				String desc = strings[ints[i + 3]];
				writer.writeFieldInsn(opcode, owner, name, desc);
				i += 3;
				continue;
			}
			if (Opcodes.isMethodOpcode(opcode))
			{
				String owner = strings[ints[i + 1]];
				String name = strings[ints[i + 2]];
				String desc = strings[ints[i + 3]];
				
				IClass iclass = Package.rootPackage.resolveInternalClass(owner);
				boolean isInterface = iclass.isInterface();
				writer.writeInvokeInsn(opcode, owner, name, desc, isInterface);
				i += 3;
				continue;
			}
			if (Opcodes.isJumpOpcode(opcode))
			{
				writer.writeJumpInsn(opcode, targets[ints[i + 1]]);
				i += 1;
				continue;
			}
			
			switch (opcode)
			{
			case Opcodes.LOAD_0:
				IntrinsicData.writeArgument(writer, this.method, 0, instance, arguments);
				continue;
			case Opcodes.LOAD_1:
				IntrinsicData.writeArgument(writer, this.method, 1, instance, arguments);
				continue;
			case Opcodes.LOAD_2:
				IntrinsicData.writeArgument(writer, this.method, 2, instance, arguments);
				continue;
			case Opcodes.BIPUSH:
			case Opcodes.SIPUSH:
				writer.writeLDC(ints[i + 1]);
				i++;
				continue;
			case Opcodes.LDC:
				String constant = strings[ints[i + 1]];
				writeLDC(writer, constant);
				i++;
				continue;
			}
			
			writer.writeInsn(opcode, lineNumber);
		}
	}
	
	@Override
	public void writeIntrinsic(MethodWriter writer, Label dest, IValue instance, IArguments arguments, int lineNumber) throws BytecodeException
	{
		this.writeIntrinsic(writer, instance, arguments, lineNumber);
		writer.writeJumpInsn(Opcodes.IFNE, dest);
	}
	
	@Override
	public void writeInvIntrinsic(MethodWriter writer, Label dest, IValue instance, IArguments arguments, int lineNumber) throws BytecodeException
	{
		this.writeIntrinsic(writer, instance, arguments, lineNumber);
		writer.writeJumpInsn(Opcodes.IFEQ, dest);
	}
	
	private static void writeLDC(MethodWriter writer, String constant)
	{
		switch (constant.charAt(0))
		{
		case 'I':
			writer.writeLDC(Integer.parseInt(constant.substring(1)));
			return;
		case 'L':
			writer.writeLDC(Long.parseLong(constant.substring(1)));
			return;
		case 'F':
			writer.writeLDC(Float.parseFloat(constant.substring(1)));
			return;
		case 'D':
			writer.writeLDC(Double.parseDouble(constant.substring(1)));
			return;
		case 'S':
		case '"':
		case '\'':
			writer.writeLDC(constant.substring(1));
			return;
		case 'C':
			writer.writeLDC(Type.getType(constant.substring(1)));
			return;
		}
	}
}
