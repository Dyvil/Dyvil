package dyvil.tools.compiler.parser.bytecode;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.bytecode.*;
import dyvil.tools.compiler.ast.constant.*;
import dyvil.tools.compiler.ast.statement.Label;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.token.IToken;

public final class BytecodeParser extends Parser
{
	private static final int INSTRUCTION = 1;
	
	protected Bytecode bytecode;
	
	private Name label;
	
	public BytecodeParser(Bytecode bytecode)
	{
		this.bytecode = bytecode;
		this.mode = INSTRUCTION;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
	{
		int type = token.type();
		if (type == BaseSymbols.SEMICOLON)
		{
			return;
		}
		
		if (type == BaseSymbols.CLOSE_CURLY_BRACKET)
		{
			pm.popParser(true);
			return;
		}
		
		if (this.mode == INSTRUCTION)
		{
			IInstruction insn = null;
			if (type == DyvilKeywords.GOTO)
			{
				IToken next = token.next();
				if (!ParserUtil.isIdentifier(next.type()))
				{
					pm.report(token, "Invalid Jump Instruction - Identifier expected");
					return;
				}
				
				pm.skip();
				insn = new JumpInstruction(Opcodes.GOTO, new Label(next.nameValue()));
			}
			if (type == Tokens.LETTER_IDENTIFIER)
			{
				Name name = token.nameValue();
				if (token.next().type() == BaseSymbols.COLON)
				{
					pm.skip();
					this.label = name;
					return;
				}
				
				int opcode = Opcodes.parseOpcode(name.qualified);
				if (opcode == -1)
				{
					pm.report(token, "Invalid Instruction - Unknown Instruction Name '" + name + "'");
					return;
				}
				
				insn = handleOpcode(pm, token, opcode);
			}
			else
			{
				pm.report(token, "Invalid Instruction - Identifier expected");
				return;
			}
			if (insn != null)
			{
				if (this.label != null)
				{
					this.bytecode.addInstruction(insn, new Label(this.label));
					this.label = null;
					return;
				}
				
				this.bytecode.addInstruction(insn);
				return;
			}
			return;
		}
	}
	
	private static IInstruction handleOpcode(IParserManager pm, IToken token, int opcode)
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
			if (next.type() != Tokens.INT)
			{
				pm.report(next, "Invalid IINC Instruction - Integer expected");
				return null;
			}
			
			IToken next2 = next.next();
			if (next2.type() != Tokens.INT)
			{
				pm.report(next2, "Invalid IINC Instruction - Integer expected");
				return null;
			}
			
			pm.skip(2);
			return new IIncInstruction(next.intValue(), next2.intValue());
		}
			/* IntInstructions */
		case Opcodes.BIPUSH:
		case Opcodes.SIPUSH:
		case Opcodes.NEWARRAY:
		{
			IToken next = token.next();
			if (next.type() != Tokens.INT)
			{
				pm.report(token, "Invalid Int Instruction - Integer expected");
				return null;
			}
			
			pm.skip();
			return new IntInstruction(opcode, next.intValue());
		}
			/* LDCInstruction */
		case Opcodes.LDC:
		{
			IToken next = token.next();
			int nextType = next.type();
			if (nextType == Tokens.STRING)
			{
				pm.skip();
				return new LDCInstruction(new StringValue(next.stringValue()));
			}
			if (nextType == Tokens.SINGLE_QUOTED_STRING)
			{
				pm.skip();
				return new LDCInstruction(new CharValue(next, next.stringValue(), true));
			}
			if (nextType == Tokens.INT)
			{
				pm.skip();
				return new LDCInstruction(new IntValue(next.intValue()));
			}
			if (nextType == Tokens.LONG)
			{
				pm.skip();
				return new LDCInstruction(new LongValue(next.longValue()));
			}
			if (nextType == Tokens.FLOAT)
			{
				pm.skip();
				return new LDCInstruction(new FloatValue(next.floatValue()));
			}
			if (nextType == Tokens.DOUBLE)
			{
				pm.skip();
				return new LDCInstruction(new DoubleValue(next.doubleValue()));
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
			if (next.type() != Tokens.INT)
			{
				pm.report(token, "Invalid Var Instruction - Integer expected");
				return null;
			}
			
			pm.skip();
			return new VarInstruction(opcode, next.intValue());
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
				pm.report(token, "Invalid Jump Instruction - Identifier expected");
				return null;
			}
			
			pm.skip();
			return new JumpInstruction(opcode, new Label(next.nameValue()));
		}
			/* TODO TableSwitchInstruction */
		case Opcodes.TABLESWITCH:
			return null;
		/* TODO LookupSwitchInstruction */
		case Opcodes.LOOKUPSWITCH:
			break;
		case Opcodes.GETSTATIC:
		case Opcodes.PUTSTATIC:
		case Opcodes.GETFIELD:
		case Opcodes.PUTFIELD:
		{
			FieldInstruction fi = new FieldInstruction(opcode);
			pm.pushParser(new FieldInstructionParser(fi));
			return fi;
		}
		case Opcodes.INVOKEVIRTUAL:
		case Opcodes.INVOKESPECIAL:
		case Opcodes.INVOKESTATIC:
		case Opcodes.INVOKEINTERFACE:
		{
			MethodInstruction mi = new MethodInstruction(opcode);
			pm.pushParser(new MethodInstructionParser(mi));
			return mi;
		}
			/* TODO InvokeDynamicInstruction */
		case Opcodes.INVOKEDYNAMIC:
			break;
		case Opcodes.NEW:
		case Opcodes.ANEWARRAY:
		case Opcodes.CHECKCAST:
		case Opcodes.INSTANCEOF:
		{
			TypeInstruction ti = new TypeInstruction(opcode);
			pm.pushParser(new InternalTypeParser(ti));
			return ti;
		}
			/* TODO MultiArrayInstruction */
		case Opcodes.MULTIANEWARRAY:
			break;
		}
		
		return null;
	}
}
