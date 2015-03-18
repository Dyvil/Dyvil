package dyvil.tools.compiler.parser.bytecode;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.bytecode.*;
import dyvil.tools.compiler.ast.constant.*;
import dyvil.tools.compiler.ast.statement.Label;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.compiler.util.Tokens;

public final class BytecodeParser extends Parser
{
	private static final int	INSTRUCTION	= 1;
	
	protected Bytecode			bytecode;
	
	private String				label;
	
	public BytecodeParser(Bytecode bytecode)
	{
		this.bytecode = bytecode;
		this.mode = INSTRUCTION;
	}
	
	@Override
	public void reset()
	{
		this.mode = INSTRUCTION;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (type == Tokens.SEMICOLON)
		{
			return;
		}
		
		if (type == Tokens.CLOSE_CURLY_BRACKET)
		{
			pm.popParser(true);
			return;
		}
		
		if (this.mode == INSTRUCTION)
		{
			if (ParserUtil.isIdentifier(type) || type == Tokens.GOTO)
			{
				String name = token.value();
				if (token.next().type() == Tokens.COLON)
				{
					pm.skip();
					this.label = name;
					return;
				}
				
				int opcode = Opcodes.parseOpcode(name);
				if (opcode == -1)
				{
					throw new SyntaxError(token, "Invalid Instruction - Unknown Instruction Name '" + name + "'");
				}
				
				IInstruction insn = handleOpcode(pm, token, opcode);
				if (this.label != null)
				{
					this.bytecode.addInstruction(insn, new Label(this.label));
					this.label = null;
					return;
				}
				
				this.bytecode.addInstruction(insn);
				return;
			}
			throw new SyntaxError(token, "Invalid Instruction - Name expected");
		}
	}
	
	private static IInstruction handleOpcode(IParserManager pm, IToken token, int opcode) throws SyntaxError
	{
		switch (opcode)
		{
		/* Instructions */
		case Opcodes.NOP:
		case Opcodes.ACONST_NULL:
		case Opcodes.ICONST_M1:
		case Opcodes.ICONST_0:
		case Opcodes.ICONST_1:
		case Opcodes.ICONST_2:
		case Opcodes.ICONST_3:
		case Opcodes.ICONST_4:
		case Opcodes.ICONST_5:
		case Opcodes.LCONST_0:
		case Opcodes.LCONST_1:
		case Opcodes.FCONST_0:
		case Opcodes.FCONST_1:
		case Opcodes.FCONST_2:
		case Opcodes.DCONST_0:
		case Opcodes.DCONST_1:
		case Opcodes.IALOAD:
		case Opcodes.LALOAD:
		case Opcodes.FALOAD:
		case Opcodes.DALOAD:
		case Opcodes.AALOAD:
		case Opcodes.BALOAD:
		case Opcodes.CALOAD:
		case Opcodes.SALOAD:
		case Opcodes.IASTORE:
		case Opcodes.LASTORE:
		case Opcodes.FASTORE:
		case Opcodes.DASTORE:
		case Opcodes.AASTORE:
		case Opcodes.BASTORE:
		case Opcodes.CASTORE:
		case Opcodes.SASTORE:
		case Opcodes.POP:
		case Opcodes.POP2:
		case Opcodes.DUP:
		case Opcodes.DUP_X1:
		case Opcodes.DUP_X2:
		case Opcodes.DUP2:
		case Opcodes.DUP2_X1:
		case Opcodes.DUP2_X2:
		case Opcodes.SWAP:
		case Opcodes.IADD:
		case Opcodes.LADD:
		case Opcodes.FADD:
		case Opcodes.DADD:
		case Opcodes.ISUB:
		case Opcodes.LSUB:
		case Opcodes.FSUB:
		case Opcodes.DSUB:
		case Opcodes.IMUL:
		case Opcodes.LMUL:
		case Opcodes.FMUL:
		case Opcodes.DMUL:
		case Opcodes.IDIV:
		case Opcodes.LDIV:
		case Opcodes.FDIV:
		case Opcodes.DDIV:
		case Opcodes.IREM:
		case Opcodes.LREM:
		case Opcodes.FREM:
		case Opcodes.DREM:
		case Opcodes.INEG:
		case Opcodes.LNEG:
		case Opcodes.FNEG:
		case Opcodes.DNEG:
		case Opcodes.ISHL:
		case Opcodes.LSHL:
		case Opcodes.ISHR:
		case Opcodes.LSHR:
		case Opcodes.IUSHR:
		case Opcodes.LUSHR:
		case Opcodes.IAND:
		case Opcodes.LAND:
		case Opcodes.IOR:
		case Opcodes.LOR:
		case Opcodes.IXOR:
		case Opcodes.LXOR:
		case Opcodes.I2L:
		case Opcodes.I2F:
		case Opcodes.I2D:
		case Opcodes.L2I:
		case Opcodes.L2F:
		case Opcodes.L2D:
		case Opcodes.F2I:
		case Opcodes.F2L:
		case Opcodes.F2D:
		case Opcodes.D2I:
		case Opcodes.D2L:
		case Opcodes.D2F:
		case Opcodes.I2B:
		case Opcodes.I2C:
		case Opcodes.I2S:
		case Opcodes.LCMP:
		case Opcodes.FCMPL:
		case Opcodes.FCMPG:
		case Opcodes.DCMPL:
		case Opcodes.DCMPG:
		case Opcodes.IRETURN:
		case Opcodes.LRETURN:
		case Opcodes.FRETURN:
		case Opcodes.DRETURN:
		case Opcodes.ARETURN:
		case Opcodes.RETURN:
		case Opcodes.ARRAYLENGTH:
		case Opcodes.ATHROW:
		case Opcodes.MONITORENTER:
		case Opcodes.MONITOREXIT:
			return new Instruction(opcode);
		case Opcodes.IINC:
		{
			IToken next = token.next();
			if (next.type() != Tokens.TYPE_INT)
			{
				throw new SyntaxError(next, "Invalid IINC Instruction - Integer expected");
			}
			
			IToken next2 = next.next();
			if (next2.type() != Tokens.TYPE_INT)
			{
				throw new SyntaxError(next2, "Invalid IINC Instruction - Integer expected");
			}
			
			pm.skip(2);
			return new IIncInstruction((Integer) next.object(), (Integer) next2.object());
		}
		/* IntInstructions */
		case Opcodes.BIPUSH:
		case Opcodes.SIPUSH:
		{
			IToken next = token.next();
			if (next.type() != Tokens.TYPE_INT)
			{
				throw new SyntaxError(token, "Invalid Int Instruction - Integer expected");
			}
			
			pm.skip();
			return new IntInstruction(opcode, (Integer) next.object());
		}
		/* LDCInstruction */
		case Opcodes.LDC:
		{
			IToken next = token.next();
			int nextType = next.type();
			if (nextType == Tokens.TYPE_STRING)
			{
				pm.skip();
				return new LDCInstruction(new StringValue((String) next.object()));
			}
			if (nextType == Tokens.TYPE_CHAR)
			{
				pm.skip();
				return new LDCInstruction(new CharValue((Character) next.object()));
			}
			if (nextType == Tokens.TYPE_INT)
			{
				pm.skip();
				return new LDCInstruction(new IntValue((Integer) next.object()));
			}
			if (nextType == Tokens.TYPE_LONG)
			{
				pm.skip();
				return new LDCInstruction(new LongValue((Long) next.object()));
			}
			if (nextType == Tokens.TYPE_FLOAT)
			{
				pm.skip();
				return new LDCInstruction(new FloatValue((Float) next.object()));
			}
			if (nextType == Tokens.TYPE_DOUBLE)
			{
				pm.skip();
				return new LDCInstruction(new DoubleValue((Double) next.object()));
			}
			return null;
		}
		/* VarInstructions */
		case Opcodes.ILOAD:
		case Opcodes.LLOAD:
		case Opcodes.FLOAD:
		case Opcodes.DLOAD:
		case Opcodes.ALOAD:
		case Opcodes.ISTORE:
		case Opcodes.LSTORE:
		case Opcodes.FSTORE:
		case Opcodes.DSTORE:
		case Opcodes.ASTORE:
		{
			IToken next = token.next();
			if (next.type() != Tokens.TYPE_INT)
			{
				throw new SyntaxError(token, "Invalid Var Instruction - Integer expected");
			}
			
			pm.skip();
			return new VarInstruction(opcode, (Integer) next.object());
		}
		/* Jump Instructions */
		case Opcodes.IFEQ:
		case Opcodes.IFNE:
		case Opcodes.IFLT:
		case Opcodes.IFGE:
		case Opcodes.IFGT:
		case Opcodes.IFLE:
		case Opcodes.IF_ICMPEQ:
		case Opcodes.IF_ICMPNE:
		case Opcodes.IF_ICMPLT:
		case Opcodes.IF_ICMPGE:
		case Opcodes.IF_ICMPGT:
		case Opcodes.IF_ICMPLE:
		case Opcodes.IF_ACMPEQ:
		case Opcodes.IF_ACMPNE:
		case Opcodes.GOTO:
		case Opcodes.IFNULL:
		case Opcodes.IFNONNULL:
		{
			IToken next = token.next();
			if (!ParserUtil.isIdentifier(next.type()))
			{
				throw new SyntaxError(token, "Invalid Jump Instruction - Identifier expected");
			}
			
			pm.skip();
			return new JumpInstruction(opcode, new Label(next.value()));
		}
		/* TODO TableSwitchInstruction */
		case Opcodes.TABLESWITCH:
			return null;
			/* TODO LookupSwitchInstruction */
		case Opcodes.LOOKUPSWITCH:
			break;
		/* TODO FieldInstructions */
		case Opcodes.GETSTATIC:
		case Opcodes.PUTSTATIC:
		case Opcodes.GETFIELD:
		case Opcodes.PUTFIELD:
			break;
		/* TODO MethodInstructions */
		case Opcodes.INVOKEVIRTUAL:
		case Opcodes.INVOKESPECIAL:
		case Opcodes.INVOKESTATIC:
		case Opcodes.INVOKEINTERFACE:
			break;
		/* TODO InvokeDynamicInstruction */
		case Opcodes.INVOKEDYNAMIC:
			break;
		/* TODO TypeInstructions */
		case Opcodes.NEW:
		case Opcodes.NEWARRAY:
		case Opcodes.ANEWARRAY:
		case Opcodes.CHECKCAST:
		case Opcodes.INSTANCEOF:
			break;
		/* TODO MultiArrayInstruction */
		case Opcodes.MULTIANEWARRAY:
			break;
		}
		
		return null;
	}
}
