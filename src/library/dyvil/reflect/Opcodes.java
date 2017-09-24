package dyvil.reflect;

import dyvil.annotation.Intrinsic;
import dyvil.annotation.internal.NonNull;
import dyvilx.tools.asm.Label;
import dyvilx.tools.asm.util.Printer;

/**
 * The <b>Opcodes</b> interface declares all opcodes that can be used in <i>Dyvil</i> Bytecode Expressions.
 *
 * @author Clashsoft
 * @version 1.0
 */
public interface Opcodes
{
	/**
	 * No-operation.
	 */
	int NOP = 0;

	/**
	 * Pushes {@code null} onto the stack.
	 */
	int ACONST_NULL = 1;

	/**
	 * Pushes the integer {@code -1} onto the stack.
	 */
	int ICONST_M1 = 2;

	/**
	 * Pushes the integer {@code 0} onto the stack.
	 */
	int ICONST_0 = 3;

	/**
	 * Pushes the integer {@code 1} onto the stack.
	 */
	int ICONST_1 = 4;

	/**
	 * Pushes the integer {@code 2} onto the stack.
	 */
	int ICONST_2 = 5;

	/**
	 * Pushes the integer {@code 3} onto the stack.
	 */
	int ICONST_3 = 6;

	/**
	 * Pushes the integer {@code 4} onto the stack.
	 */
	int ICONST_4 = 7;

	/**
	 * Pushes the integer {@code 5} onto the stack.
	 */
	int ICONST_5 = 8;

	/**
	 * Pushes the long {@code 0} onto the stack.
	 */
	int LCONST_0 = 9;

	/**
	 * Pushes the long {@code 1} onto the stack.
	 */
	int LCONST_1 = 10;

	/**
	 * Pushes the float {@code 0} onto the stack.
	 */
	int FCONST_0 = 11;

	/**
	 * Pushes the float {@code 1} onto the stack.
	 */
	int FCONST_1 = 12;

	/**
	 * Pushes the float {@code 2} onto the stack.
	 */
	int FCONST_2 = 13;

	/**
	 * Pushes the double {@code 0} onto the stack.
	 */
	int DCONST_0 = 14;

	/**
	 * Pushes the double {@code 1} onto the stack.
	 */
	int DCONST_1 = 15;

	/**
	 * Pushes a byte onto the stack.
	 *
	 * @param operand1 the byte
	 */
	int BIPUSH = 16;

	/**
	 * Pushes a short onto the stack.
	 *
	 * @param operand1 the first 8 bits of the short
	 * @param operand2 the second 8 bits of the short
	 */
	int SIPUSH = 17;

	/**
	 * Loads a constant. The operand can be of types {@code int}, {@code long},
	 * {@code float}, {@code double}, {@link String} or {@link Class}.
	 *
	 * @param operand1 the constant
	 */
	int LDC = 18;

	/**
	 * Loads an {@code int} value from a local variable and pushes it onto the
	 * stack.
	 *
	 * @param operand1 the local variable index
	 */
	int ILOAD = 21;

	/**
	 * Loads a {@code long} value from a local variable and pushes it onto the
	 * stack.
	 *
	 * @param operand1 the local variable index
	 */
	int LLOAD = 22;

	/**
	 * Loads a {@code float} value from a local variable and pushes it onto the
	 * stack.
	 *
	 * @param operand1 the local variable index
	 */
	int FLOAD = 23;

	/**
	 * Loads a {@code double} value from a local variable and pushes it onto the
	 * stack.
	 *
	 * @param operand1 the local variable index
	 */
	int DLOAD = 24;

	/**
	 * Loads an Object reference from a local variable and pushes it onto the
	 * stack.
	 *
	 * @param operand1 the local variable index
	 */
	int ALOAD = 25;

	int IALOAD = 46;

	int LALOAD = 47;

	int FALOAD = 48;

	int DALOAD = 49;

	int AALOAD = 50;

	int BALOAD = 51;

	int CALOAD = 52;

	int SALOAD = 53;

	/**
	 * Removes an {@code int} value from a the stack and stores it in a local
	 * variable.
	 *
	 * @param operand1 the local variable index
	 */
	int ISTORE = 54;

	/**
	 * Removes a {@code long} value from a the stack and stores it in a local
	 * variable.
	 *
	 * @param operand1 the local variable index
	 */
	int LSTORE = 55;

	/**
	 * Removes a {@code float} value from a the stack and stores it in a local
	 * variable.
	 *
	 * @param operand1 the local variable index
	 */
	int FSTORE = 56;

	/**
	 * Removes a {@code double} value from a the stack and stores it in a local
	 * variable.
	 *
	 * @param operand1 the local variable index
	 */
	int DSTORE = 57;

	/**
	 * Removes an {@code int} value from a the stack and stores it in a local
	 * variable.
	 *
	 * @param operand1 the local variable index
	 */
	int ASTORE = 58;

	int IASTORE = 79;

	int LASTORE = 80;

	int FASTORE = 81;

	int DASTORE = 82;

	int AASTORE = 83;

	int BASTORE = 84;

	int CASTORE = 85;

	int SASTORE = 86;

	/**
	 * Removes the first value from the stack.
	 */
	int POP = 87;

	/**
	 * Removes the first two values from the stack (the first value in case of a
	 * {@code long} or {@code double})
	 */
	int POP2 = 88;

	/**
	 * Duplicates the first value on the stack.
	 * <p>
	 * Stack:<br>
	 * value -> value, value
	 */
	int DUP = 89;

	/**
	 * Inserts a copy of the first value into the stack two values from the top.
	 * Both values must not be of the type {@code double} or {@code long}.
	 * <p>
	 * Stack:<br>
	 * value2, value1 -> value1, value2, value1
	 */
	int DUP_X1 = 90;

	/**
	 * Inserts a copy of the first value into the stack two (if value2 is
	 * {@code double} or {@code long} it takes up the entry of value3, too) or
	 * three values (if value2 is neither {@code double} nor {@code long}) from
	 * the top.
	 * <p>
	 * Stack:<br>
	 * value3, value2, value1 -> value1, value3, value2, value1
	 */
	int DUP_X2 = 91;

	/**
	 * Duplicates the first two values on the stack (two values, if value1 is
	 * not {@code double} nor {@code long}; a single value, if value1 is
	 * {@code double} or {@code long})
	 * <p>
	 * Stack:<br>
	 * {value2, value1} -> {value2, value1}, {value2, value1}
	 */
	int DUP2 = 92;

	/**
	 * Duplicates the first two values and inserts them beneath the third value.
	 * <p>
	 * Stack:<br>
	 * value3, {value2, value1} -> {value2, value1}, value3, {value2, value1}
	 */
	int DUP2_X1 = 93;

	/**
	 * Duplicates the first two values and inserts them beneath the fourth
	 * value.
	 * <p>
	 * Stack:<br>
	 * {value4, value3}, {value2, value1} -> {value2, value1}, {value4, value3},
	 * {value2, value1}
	 */
	int DUP2_X2 = 94;

	/**
	 * Swaps the first two values on the stack (value1 and value2 must not be
	 * {@code double} or {@code long})
	 * <p>
	 * Stack:<br>
	 * value1, value2 -> value2, value1
	 */
	int SWAP = 95;

	/**
	 * Removes two {@code int} values from the stack, adds them and pushes the
	 * result onto the stack.
	 */
	int IADD = 96;

	/**
	 * Removes two {@code long} values from the stack, adds them and pushes the
	 * result onto the stack.
	 */
	int LADD = 97;

	/**
	 * Removes two {@code float} values from the stack, adds them and pushes the
	 * result onto the stack.
	 */
	int FADD = 98;

	/**
	 * Removes two {@code double} values from the stack, adds them and pushes
	 * the result onto the stack.
	 */
	int DADD = 99;

	/**
	 * Removes two {@code int} values from the stack, subtracts them and pushes
	 * the result onto the stack.
	 */
	int ISUB = 100;

	/**
	 * Removes two {@code long} values from the stack, subtracts them and pushes
	 * the result onto the stack.
	 */
	int LSUB = 101;

	/**
	 * Removes two {@code float} values from the stack, subtracts them and
	 * pushes the result onto the stack.
	 */
	int FSUB = 102;

	/**
	 * Removes two {@code double} values from the stack, subtracts them and
	 * pushes the result onto the stack.
	 */
	int DSUB = 103;

	/**
	 * Removes two {@code int} values from the stack, multiplies them and pushes
	 * the result onto the stack.
	 */
	int IMUL = 104;

	/**
	 * Removes two {@code long} values from the stack, multiplies them and
	 * pushes the result onto the stack.
	 */
	int LMUL = 105;

	/**
	 * Removes two {@code float} values from the stack, multiplies them and
	 * pushes the result onto the stack.
	 */
	int FMUL = 106;

	/**
	 * Removes two {@code double} values from the stack, multiplies them and
	 * pushes the result onto the stack.
	 */
	int DMUL = 107;

	/**
	 * Removes two {@code int} values from the stack, divides them and pushes
	 * the result onto the stack.
	 */
	int IDIV = 108;

	/**
	 * Removes two {@code long} values from the stack, divides them and pushes
	 * the result onto the stack.
	 */
	int LDIV = 109;

	/**
	 * Removes two {@code float} values from the stack, divides them and pushes
	 * the result onto the stack.
	 */
	int FDIV = 110;

	/**
	 * Removes two {@code double} values from the stack, divides them and pushes
	 * the result onto the stack.
	 */
	int DDIV = 111;

	/**
	 * Removes two {@code int} values from the stack, calculates the remainder
	 * of a division and pushes the result onto the stack.
	 */
	int IREM = 112;

	/**
	 * Removes two {@code long} values from the stack, calculates the remainder
	 * of a division and pushes the result onto the stack.
	 */
	int LREM = 113;

	/**
	 * Removes two {@code float} values from the stack, calculates the remainder
	 * of a division and pushes the result onto the stack.
	 */
	int FREM = 114;

	/**
	 * Removes two {@code double} values from the stack, calculates the
	 * remainder of a division and pushes the result onto the stack.
	 */
	int DREM = 115;

	/**
	 * Removes an {@code int} value from the stack, negates it and pushes the
	 * result onto the stack.
	 */
	int INEG = 116;

	/**
	 * Removes a {@code long} value from the stack, negates it and pushes the
	 * result onto the stack.
	 */
	int LNEG = 117;

	/**
	 * Removes a {@code float} value from the stack, negates it and pushes the
	 * result onto the stack.
	 */
	int FNEG = 118;

	/**
	 * Removes a {@code double} value from the stack, negates it and pushes the
	 * result onto the stack.
	 */
	int DNEG = 119;

	/**
	 * Removes two {@code int} values from the stack, left-shifts value1 by
	 * value2 bits and pushes the result onto the stack.
	 */
	int ISHL = 120;
	/**
	 * Removes two {@code long} values from the stack, left-shifts value1 by
	 * value2 bits and pushes the result onto the stack.
	 */
	int LSHL = 121;

	/**
	 * Removes two {@code int} values from the stack, arithmetic right-shifts
	 * value1 by value2 bits and pushes the result onto the stack.
	 */
	int ISHR = 122;

	/**
	 * Removes two {@code long} values from the stack, arithmetic right-shifts
	 * value1 by value2 bits and pushes the result onto the stack.
	 */
	int LSHR = 123;

	/**
	 * Removes two {@code int} values from the stack, logical right-shifts
	 * value1 by value2 bits and pushes the result onto the stack.
	 */
	int IUSHR = 124;

	/**
	 * Removes two {@code long} values from the stack, logical right-shifts
	 * value1 by value2 bits and pushes the result onto the stack.
	 */
	int LUSHR = 125;

	/**
	 * Removes two {@code int} values from the stack, bitwise ANDs them and
	 * pushes the result onto the stack.
	 */
	int IAND = 126;

	/**
	 * Removes two {@code long} values from the stack, bitwise ANDs them and
	 * pushes the result onto the stack.
	 */
	int LAND = 127;

	/**
	 * Removes two {@code int} values from the stack, bitwise ORs them and
	 * pushes the result onto the stack.
	 */
	int IOR = 128;

	/**
	 * Removes two {@code long} values from the stack, bitwise ORs them and
	 * pushes the result onto the stack.
	 */
	int LOR = 129;

	/**
	 * Removes two {@code int} values from the stack, bitwise XORs them and
	 * pushes the result onto the stack.
	 */
	int IXOR = 130;

	/**
	 * Removes two {@code long} values from the stack, bitwise XORs them and
	 * pushes the result onto the stack.
	 */
	int LXOR = 131;

	/**
	 * Increments a local variable.
	 *
	 * @param operand1 the local variable index
	 * @param operand2 the value
	 */
	int IINC = 132;

	/**
	 * Removes an {@code int} value from the stack, converts it to a
	 * {@code long} value and pushes the result onto the stack.
	 */
	int I2L = 133;

	/**
	 * Removes an {@code int} value from the stack, converts it to a
	 * {@code float} value and pushes the result onto the stack.
	 */
	int I2F = 134;

	/**
	 * Removes an {@code int} value from the stack, converts it to a
	 * {@code double} value and pushes the result onto the stack.
	 */
	int I2D = 135;

	/**
	 * Removes a {@code long} value from the stack, converts it to an
	 * {@code int} value and pushes the result onto the stack.
	 */
	int L2I = 136;

	/**
	 * Removes a {@code long} value from the stack, converts it to a
	 * {@code float} value and pushes the result onto the stack.
	 */
	int L2F = 137;

	/**
	 * Removes a {@code long} value from the stack, converts it to a
	 * {@code double} value and pushes the result onto the stack.
	 */
	int L2D = 138;

	/**
	 * Removes a {@code float} value from the stack, converts it to an
	 * {@code int} value and pushes the result onto the stack.
	 */
	int F2I = 139;

	/**
	 * Removes a {@code float} value from the stack, converts it to a
	 * {@code long} value and pushes the result onto the stack.
	 */
	int F2L = 140;

	/**
	 * Removes a {@code float} value from the stack, converts it to a
	 * {@code double} value and pushes the result onto the stack.
	 */
	int F2D = 141;

	/**
	 * Removes a {@code double} value from the stack, converts it to an
	 * {@code int} value and pushes the result onto the stack.
	 */
	int D2I = 142;

	/**
	 * Removes a {@code double} value from the stack, converts it to a
	 * {@code long} value and pushes the result onto the stack.
	 */
	int D2L = 143;

	/**
	 * Removes a {@code double} value from the stack, converts it to a
	 * {@code float} value and pushes the result onto the stack.
	 */
	int D2F = 144;

	/**
	 * Removes an {@code int} value from the stack, converts it to a
	 * {@code byte} value and pushes the result onto the stack.
	 */
	int I2B = 145;

	/**
	 * Removes an {@code int} value from the stack, converts it to a
	 * {@code char} value and pushes the result onto the stack.
	 */
	int I2C = 146;

	/**
	 * Removes an {@code int} value from the stack, converts it to a
	 * {@code short} value and pushes the result onto the stack.
	 */
	int I2S = 147;

	/**
	 * Removes two {@code long} values from the stack, compares them and pushes
	 * the result onto the stack.
	 */
	int LCMP = 148;

	/**
	 * Removes two {@code float} values from the stack, compares them and pushes
	 * the result onto the stack.
	 */
	int FCMPL = 149;

	/**
	 * Removes two {@code float} values from the stack, compares them and pushes
	 * the result onto the stack.
	 */
	int FCMPG = 150;

	/**
	 * Removes two {@code double} values from the stack, compares them and
	 * pushes the result onto the stack.
	 */
	int DCMPL = 151;

	/**
	 * Removes two {@code double} values from the stack, compares them and
	 * pushes the result onto the stack.
	 */
	int DCMPG = 152;

	/**
	 * Removes an {@code int} value from the stack and jumps to the given
	 * {@link Label} if it equals {@code 0}.
	 *
	 * @param label the label
	 */
	int IFEQ = 153;

	/**
	 * Removes an {@code int} value from the stack and jumps to the given
	 * {@link Label} if it does not equal {@code 0}.
	 *
	 * @param label the label
	 */
	int IFNE = 154;

	/**
	 * Removes an {@code int} value from the stack and jumps to the given
	 * {@link Label} if it is less than {@code 0}.
	 *
	 * @param label the label
	 */
	int IFLT = 155;

	/**
	 * Removes an {@code int} value from the stack and jumps to the given
	 * {@link Label} if it is greater than or equal to {@code 0}.
	 *
	 * @param label the label
	 */
	int IFGE = 156;

	/**
	 * Removes an {@code int} value from the stack and jumps to the given
	 * {@link Label} if it is greater than {@code 0}.
	 *
	 * @param label the label
	 */
	int IFGT = 157;

	/**
	 * Removes an {@code int} value from the stack and jumps to the given
	 * {@link Label} if it is less than {@code 0}.
	 *
	 * @param label the label
	 */
	int IFLE = 158;

	int IF_ICMPEQ = 159;

	int IF_ICMPNE = 160;

	int IF_ICMPLT = 161;

	int IF_ICMPGE = 162;

	int IF_ICMPGT = 163;

	int IF_ICMPLE = 164;

	int IF_ACMPEQ = 165;

	int IF_ACMPNE = 166;

	int GOTO = 167;

	int JSR = 168;

	int RET = 169;

	int TABLESWITCH = 170;

	int LOOKUPSWITCH = 171;

	int IRETURN = 172;

	int LRETURN = 173;

	int FRETURN = 174;

	int DRETURN = 175;

	int ARETURN = 176;

	int RETURN = 177;

	int GETSTATIC = 178;

	int PUTSTATIC = 179;

	int GETFIELD = 180;

	int PUTFIELD = 181;

	int INVOKEVIRTUAL = 182;

	int INVOKESPECIAL = 183;

	int INVOKESTATIC = 184;

	int INVOKEINTERFACE = 185;

	int INVOKEDYNAMIC = 186;

	int NEW = 187;

	int NEWARRAY = 188;

	int ANEWARRAY = 189;

	int ARRAYLENGTH = 190;

	int ATHROW = 191;

	/**
	 * Checks if the value on top of the stack can be cast to the given type,
	 * and throws a {@link ClassCastException} otherwise.
	 *
	 * @param type the type
	 */
	int CHECKCAST = 192;

	/**
	 * Checks if the value on top of the stack can be cast to the given type and
	 * pushes the result as an {@code int} on the stack. If the cast is
	 * applicable, the result is {@code 1}, and if it is not applicable or the
	 * top value on the stack way {@code null}, the result is {@code 0}.
	 *
	 * @param type the type
	 */
	int INSTANCEOF = 193;

	int MONITORENTER = 194;

	int MONITOREXIT = 195;

	int MULTIANEWARRAY = 197;

	/**
	 * Removes a reference from the stack and jumps to the given {@link Label}
	 * if it is a {@code null} reference
	 *
	 * @param label the label
	 */
	int IFNULL = 198;

	/**
	 * Removes a reference from the stack and jumps to the given {@link Label}
	 * if it is not a {@code null} reference
	 *
	 * @param label the label
	 */
	int IFNONNULL = 199;
	
	/*
	 * --------------------------- SPECIAL OPCODES ---------------------------
	 * Special Opcodes are only used for the @Intrinsic annotation. They are
	 * treated specially by the Dyvil Compiler in order to work with the JVM.
	 * Because of that, these Opcodes should only ever be used in annotations
	 * that are directly processed by the Dyvil Compiler, and they are likely to
	 * cause problems elsewhere.
	 */

	/**
	 * Pushes the instance of the current {@link Intrinsic} method call onto the
	 * stack.
	 */
	int LOAD_0 = -1;

	/**
	 * Pushes the first argument of the current {@link Intrinsic} method call
	 * onto the stack.
	 */
	int LOAD_1 = -2;

	/**
	 * Pushes the second argument of the current {@link Intrinsic} method call
	 * onto the stack.
	 */
	int LOAD_2 = -3;

	/**
	 * Pushes the long {@code -1} onto the stack.
	 */
	int LCONST_M1 = 264;

	/**
	 * Removes a {@code boolean} value from the stack, inverts it and pushes the
	 * result back onto the stack.
	 */
	int BNOT = 270;

	/**
	 * Removes an {@code int} value from the stack, bitwise-inverts it and
	 * pushes the result back onto the stack.
	 */
	int INOT = 271;

	/**
	 * Removes a {@code long} value from the stack, bitwise-inverts it and
	 * pushes the result back onto the stack.
	 */
	int LNOT = 272;

	int L2B = 280;
	int L2S = 281;
	int L2C = 282;
	int F2B = 283;
	int F2S = 284;
	int F2C = 285;
	int D2B = 286;
	int D2S = 287;
	int D2C = 289;

	int ICMPEQ = 290;
	int ICMPNE = 291;
	int ICMPLT = 292;
	int ICMPGE = 293;
	int ICMPGT = 294;
	int ICMPLE = 295;

	int LCMPEQ = 296;
	int LCMPNE = 297;
	int LCMPLT = 298;
	int LCMPGE = 299;
	int LCMPGT = 300;
	int LCMPLE = 301;

	int FCMPEQ = 302;
	int FCMPNE = 303;
	int FCMPLT = 304;
	int FCMPGE = 305;
	int FCMPGT = 306;
	int FCMPLE = 307;

	int DCMPEQ = 308;
	int DCMPNE = 309;
	int DCMPLT = 310;
	int DCMPGE = 311;
	int DCMPGT = 312;
	int DCMPLE = 313;

	int ACMPEQ = 314;
	int ACMPNE = 315;

	int IS_NULL    = 316;
	int IS_NONNULL = 317;

	int IF_LCMPEQ = 330;
	int IF_LCMPNE = 331;
	int IF_LCMPLT = 332;
	int IF_LCMPGE = 333;
	int IF_LCMPGT = 334;
	int IF_LCMPLE = 335;

	int IF_FCMPEQ = 336;
	int IF_FCMPNE = 337;
	int IF_FCMPLT = 338;
	int IF_FCMPGE = 339;
	int IF_FCMPGT = 340;
	int IF_FCMPLE = 341;

	int IF_DCMPEQ = 342;
	int IF_DCMPNE = 343;
	int IF_DCMPLT = 344;
	int IF_DCMPGE = 345;
	int IF_DCMPGT = 346;
	int IF_DCMPLE = 347;

	int EQ0 = 348;
	int NE0 = 349;
	int LT0 = 350;
	int GE0 = 351;
	int GT0 = 352;
	int LE0 = 355;

	int SWAP2 = 400;

	int AUTO_SWAP   = 411;
	int AUTO_POP    = 412;
	int AUTO_DUP    = 414;
	int AUTO_DUP_X1 = 415;

	int OBJECT_EQUALS = 450;

	static String toString(int op)
	{
		return Printer.OPCODES[op];
	}

	static int parseOpcode(@NonNull String opcode)
	{
		int len = Printer.OPCODES.length;
		for (int i = 0; i < len; i++)
		{
			if (opcode.equalsIgnoreCase(Printer.OPCODES[i]))
			{
				return i;
			}
		}
		return -1;
	}

	static boolean isReturnOpcode(int opcode)
	{
		switch (opcode)
		{
		case RETURN:
		case ARETURN:
		case IRETURN:
		case LRETURN:
		case FRETURN:
		case DRETURN:
			return true;
		}
		return false;
	}

	static boolean isLoadOpcode(int opcode)
	{
		switch (opcode)
		{
		case ALOAD:
		case ILOAD:
		case LLOAD:
		case FLOAD:
		case DLOAD:
			return true;
		}
		return false;
	}

	static boolean isStoreOpcode(int opcode)
	{
		switch (opcode)
		{
		case ASTORE:
		case ISTORE:
		case LSTORE:
		case FSTORE:
		case DSTORE:
			return true;
		}
		return false;
	}

	static boolean isFieldOpcode(int opcode)
	{
		switch (opcode)
		{
		case PUTFIELD:
		case GETFIELD:
		case PUTSTATIC:
		case GETSTATIC:
			return true;
		}
		return false;
	}

	static boolean isMethodOpcode(int opcode)
	{
		switch (opcode)
		{
		case INVOKEVIRTUAL:
		case INVOKEINTERFACE:
		case INVOKESPECIAL:
		case INVOKESTATIC:
			return true;
		}
		return false;
	}

	static boolean isFieldOrMethodOpcode(int opcode)
	{
		switch (opcode)
		{
		case PUTFIELD:
		case GETFIELD:
		case PUTSTATIC:
		case GETSTATIC:
		case INVOKEVIRTUAL:
		case INVOKEINTERFACE:
		case INVOKESPECIAL:
		case INVOKESTATIC:
			return true;
		}
		return false;
	}

	static boolean isJumpOpcode(int opcode)
	{
		return opcode >= IFEQ && opcode <= JSR || opcode == GOTO || opcode == IFNULL || opcode == IFNONNULL
			       || opcode >= IF_LCMPEQ && opcode <= IF_DCMPLE;
	}

	static int getInverseOpcode(int opcode)
	{
		// @formatter:off
		switch (opcode)
		{
		case EQ0: return NE0;
		case NE0: return EQ0;
		case LT0: return GE0;
		case GE0: return LT0;
		case GT0: return LE0;
		case LE0: return GT0;
		case IFEQ: return IFNE;
		case IFNE: return IFEQ;
		case IFLT: return IFGE;
		case IFGE: return IFLT;
		case IFGT: return IFLE;
		case IFLE: return IFGT;
			
		case ICMPEQ: return ICMPNE;
		case ICMPNE: return ICMPEQ;
		case ICMPGE: return ICMPLT;
		case ICMPLT: return ICMPGE;
		case ICMPLE: return ICMPGT;
		case ICMPGT: return ICMPLE;
		case IF_ICMPEQ: return IF_ICMPNE;
		case IF_ICMPNE: return IF_ICMPEQ;
		case IF_ICMPLT: return IF_ICMPGE;
		case IF_ICMPGE: return IF_ICMPLT;
		case IF_ICMPGT: return IF_ICMPLE;
		case IF_ICMPLE: return IF_ICMPGT;
			
		case LCMPEQ: return LCMPNE;
		case LCMPNE: return LCMPEQ;
		case LCMPLT: return LCMPGE;
		case LCMPGE: return LCMPLT;
		case LCMPGT: return LCMPLE;
		case LCMPLE: return LCMPGT;
		case IF_LCMPEQ: return IF_LCMPNE;
		case IF_LCMPNE: return IF_LCMPEQ;
		case IF_LCMPLT: return IF_LCMPGE;
		case IF_LCMPGE: return IF_LCMPLT;
		case IF_LCMPGT: return IF_LCMPLE;
		case IF_LCMPLE: return IF_LCMPGT;
			
		case FCMPEQ: return FCMPNE;
		case FCMPNE: return FCMPEQ;
		case FCMPLT: return FCMPGE;
		case FCMPGE: return FCMPLT;
		case FCMPGT: return FCMPLE;
		case FCMPLE: return FCMPGT;
		case IF_FCMPEQ: return IF_FCMPNE;
		case IF_FCMPNE: return IF_FCMPEQ;
		case IF_FCMPLT: return IF_FCMPGE;
		case IF_FCMPGE: return IF_FCMPLT;
		case IF_FCMPGT: return IF_FCMPLE;
		case IF_FCMPLE: return IF_FCMPGT;
			
		case DCMPEQ: return DCMPNE;
		case DCMPNE: return DCMPEQ;
		case DCMPLT: return DCMPGE;
		case DCMPGE: return DCMPLT;
		case DCMPGT: return DCMPLE;
		case DCMPLE: return DCMPGT;
		case IF_DCMPEQ: return IF_DCMPNE;
		case IF_DCMPNE: return IF_DCMPEQ;
		case IF_DCMPLT: return IF_DCMPGE;
		case IF_DCMPGE: return IF_DCMPLT;
		case IF_DCMPGT: return IF_DCMPLE;
		case IF_DCMPLE: return IF_DCMPGT;
			
		case ACMPEQ: return ACMPNE;
		case ACMPNE: return ACMPEQ;
		case IF_ACMPEQ: return IF_ACMPNE;
		case IF_ACMPNE: return IF_ACMPEQ;
			
		case IS_NULL: return IS_NONNULL;
		case IS_NONNULL: return IS_NULL;
		case IFNULL: return IFNONNULL;
		case IFNONNULL: return IFNULL;
		}
		// @formatter:on
		return -1;
	}

	static int getJumpOpcode(int opcode)
	{
		// @formatter:off
		switch (opcode)
		{
		case EQ0: return IFEQ;
		case NE0: return IFNE;
		case GE0: return IFGE;
		case LT0: return IFLT;
		case LE0: return IFLE;
		case GT0: return IFGT;

		case ICMPEQ: return IF_ICMPEQ;
		case ICMPNE: return IF_ICMPNE;
		case ICMPGE: return IF_ICMPGE;
		case ICMPLT: return IF_ICMPLT;
		case ICMPLE: return IF_ICMPLE;
		case ICMPGT: return IF_ICMPGT;
			                         
		case LCMPEQ: return IF_LCMPEQ;
		case LCMPNE: return IF_LCMPNE;
		case LCMPLT: return IF_LCMPLT;
		case LCMPGE: return IF_LCMPGE;
		case LCMPGT: return IF_LCMPGT;
		case LCMPLE: return IF_LCMPLE;
			                         
		case FCMPEQ: return IF_FCMPEQ;
		case FCMPNE: return IF_FCMPNE;
		case FCMPLT: return IF_FCMPLT;
		case FCMPGE: return IF_FCMPGE;
		case FCMPGT: return IF_FCMPGT;
		case FCMPLE: return IF_FCMPLE;
			                         
		case DCMPEQ: return IF_DCMPEQ;
		case DCMPNE: return IF_DCMPNE;
		case DCMPLT: return IF_DCMPLT;
		case DCMPGE: return IF_DCMPGE;
		case DCMPGT: return IF_DCMPGT;
		case DCMPLE: return IF_DCMPLE;
			                         
		case ACMPEQ: return IF_ACMPEQ;
		case ACMPNE: return IF_ACMPNE;
			
		case IS_NULL: return IFNULL;
		case IS_NONNULL: return IFNONNULL;
		
		case BNOT: return IFEQ;
		}
		// @formatter:on
		return -1;
	}

	static int getInvJumpOpcode(int opcode)
	{
		// @formatter:off
		switch (opcode)
		{
		case EQ0: return IFNE;
		case NE0: return IFEQ;
		case GE0: return IFLT;
		case LT0: return IFGE;
		case LE0: return IFGT;
		case GT0: return IFLE;

		case ICMPEQ: return IF_ICMPNE;
		case ICMPNE: return IF_ICMPEQ;
		case ICMPGE: return IF_ICMPLT;
		case ICMPLT: return IF_ICMPGE;
		case ICMPLE: return IF_ICMPGT;
		case ICMPGT: return IF_ICMPLE;
			
		case LCMPEQ: return IF_LCMPNE;
		case LCMPNE: return IF_LCMPEQ;
		case LCMPLT: return IF_LCMPGE;
		case LCMPGE: return IF_LCMPLT;
		case LCMPGT: return IF_LCMPLE;
		case LCMPLE: return IF_LCMPGT;
			
		case FCMPEQ: return IF_FCMPNE;
		case FCMPNE: return IF_FCMPEQ;
		case FCMPLT: return IF_FCMPGE;
		case FCMPGE: return IF_FCMPLT;
		case FCMPGT: return IF_FCMPLE;
		case FCMPLE: return IF_FCMPGT;
			
		case DCMPEQ: return IF_DCMPNE;
		case DCMPNE: return IF_DCMPEQ;
		case DCMPLT: return IF_DCMPGE;
		case DCMPGE: return IF_DCMPLT;
		case DCMPGT: return IF_DCMPLE;
		case DCMPLE: return IF_DCMPGT;
			
		case ACMPEQ: return IF_ACMPNE;
		case ACMPNE: return IF_ACMPEQ;
			
		case IS_NULL: return IFNONNULL;
		case IS_NONNULL: return IFNULL;
		
		case BNOT: return IFNE;
		}
		// @formatter:on
		return -1;
	}
}
