package dyvil.reflect;

public interface Opcodes
{
	/**
	 * No-operation.
	 */
	public static final int	NOP				= 0;
	
	/**
	 * Pushes {@code null} onto the stack.
	 */
	public static final int	ACONST_NULL		= 1;
	
	/**
	 * Pushes the integer {@code -1} onto the stack.
	 */
	public static final int	ICONST_M1		= 2;
	
	/**
	 * Pushes the integer {@code 0} onto the stack.
	 */
	public static final int	ICONST_0		= 3;
	
	/**
	 * Pushes the integer {@code 1} onto the stack.
	 */
	public static final int	ICONST_1		= 4;
	
	/**
	 * Pushes the integer {@code 2} onto the stack.
	 */
	public static final int	ICONST_2		= 5;
	
	/**
	 * Pushes the integer {@code 3} onto the stack.
	 */
	public static final int	ICONST_3		= 6;
	
	/**
	 * Pushes the integer {@code 4} onto the stack.
	 */
	public static final int	ICONST_4		= 7;
	
	/**
	 * Pushes the integer {@code 5} onto the stack.
	 */
	public static final int	ICONST_5		= 8;
	
	/**
	 * Pushes the long {@code 0} onto the stack.
	 */
	public static final int	LCONST_0		= 9;
	
	/**
	 * Pushes the long {@code 1} onto the stack.
	 */
	public static final int	LCONST_1		= 10;
	
	/**
	 * Pushes the float {@code 0} onto the stack.
	 */
	public static final int	FCONST_0		= 11;
	
	/**
	 * Pushes the float {@code 1} onto the stack.
	 */
	public static final int	FCONST_1		= 12;
	
	/**
	 * Pushes the float {@code 2} onto the stack.
	 */
	public static final int	FCONST_2		= 13;
	
	/**
	 * Pushes the double {@code 0} onto the stack.
	 */
	public static final int	DCONST_0		= 14;
	
	/**
	 * Pushes the double {@code 1} onto the stack.
	 */
	public static final int	DCONST_1		= 15;
	
	/**
	 * Pushes a byte onto the stack.
	 * 
	 * @param operand1
	 *            the byte
	 */
	public static final int	BIPUSH			= 16;
	
	/**
	 * Pushes a short onto the stack.
	 * 
	 * @param operand1
	 *            the first 8 bits of the short
	 * @param operand2
	 *            the second 8 bits of the short
	 */
	public static final int	SIPUSH			= 17;
	
	/**
	 * Loads a constant. The operand can be of types {@code int}, {@code long},
	 * {@code float}, {@code double}, {@link String} or {@link Class}.
	 * 
	 * @param operand1
	 *            the constant
	 */
	public static final int	LDC				= 18;
	
	/**
	 * Loads an {@code int} value from a local variable and pushes it onto the
	 * stack.
	 * 
	 * @param operand1
	 *            the local variable index
	 */
	public static final int	ILOAD			= 21;
	
	/**
	 * Loads a {@code long} value from a local variable and pushes it onto the
	 * stack.
	 * 
	 * @param operand1
	 *            the local variable index
	 */
	public static final int	LLOAD			= 22;
	
	/**
	 * Loads a {@code float} value from a local variable and pushes it onto the
	 * stack.
	 * 
	 * @param operand1
	 *            the local variable index
	 */
	public static final int	FLOAD			= 23;
	
	/**
	 * Loads a {@code double} value from a local variable and pushes it onto the
	 * stack.
	 * 
	 * @param operand1
	 *            the local variable index
	 */
	public static final int	DLOAD			= 24;
	
	/**
	 * Loads an Object reference from a local variable and pushes it onto the
	 * stack.
	 * 
	 * @param operand1
	 *            the local variable index
	 */
	public static final int	ALOAD			= 25;
	
	public static final int	IALOAD			= 46;
	
	public static final int	LALOAD			= 47;
	
	public static final int	FALOAD			= 48;
	
	public static final int	DALOAD			= 49;
	
	public static final int	AALOAD			= 50;
	
	public static final int	BALOAD			= 51;
	
	public static final int	CALOAD			= 52;
	
	public static final int	SALOAD			= 53;
	
	/**
	 * Removes an {@code int} value from a the stack and stores it in a local
	 * variable.
	 * 
	 * @param operand1
	 *            the local variable index
	 */
	public static final int	ISTORE			= 54;
	
	/**
	 * Removes a {@code long} value from a the stack and stores it in a local
	 * variable.
	 * 
	 * @param operand1
	 *            the local variable index
	 */
	public static final int	LSTORE			= 55;
	
	/**
	 * Removes a {@code float} value from a the stack and stores it in a local
	 * variable.
	 * 
	 * @param operand1
	 *            the local variable index
	 */
	public static final int	FSTORE			= 56;
	
	/**
	 * Removes a {@code double} value from a the stack and stores it in a local
	 * variable.
	 * 
	 * @param operand1
	 *            the local variable index
	 */
	public static final int	DSTORE			= 57;
	
	/**
	 * Removes an {@code int} value from a the stack and stores it in a local
	 * variable.
	 * 
	 * @param operand1
	 *            the local variable index
	 */
	public static final int	ASTORE			= 58;
	
	public static final int	IASTORE			= 79;
	
	public static final int	LASTORE			= 80;
	
	public static final int	FASTORE			= 81;
	
	public static final int	DASTORE			= 82;
	
	public static final int	AASTORE			= 83;
	
	public static final int	BASTORE			= 84;
	
	public static final int	CASTORE			= 85;
	
	public static final int	SASTORE			= 86;
	
	/**
	 * Removes the first value from the stack.
	 */
	public static final int	POP				= 87;
	
	/**
	 * Removes the first two values from the stack (the first value in case of a
	 * {@code long} or {@code double})
	 */
	public static final int	POP2			= 88;
	
	/**
	 * Duplicates the first value on the stack.
	 * <p>
	 * Stack:<br>
	 * value -> value, value
	 */
	public static final int	DUP				= 89;
	
	/**
	 * Inserts a copy of the first value into the stack two values from the top.
	 * Both values must not be of the type {@code double} or {@code long}.
	 * <p>
	 * Stack:<br>
	 * value2, value1 -> value1, value2, value1
	 */
	public static final int	DUP_X1			= 90;
	
	/**
	 * Inserts a copy of the first value into the stack two (if value2 is
	 * {@code double} or {@code long} it takes up the entry of value3, too) or
	 * three values (if value2 is neither {@code double} nor {@code long}) from
	 * the top.
	 * <p>
	 * Stack:<br>
	 * value3, value2, value1 -> value1, value3, value2, value1
	 */
	public static final int	DUP_X2			= 91;
	
	/**
	 * Duplicates the first two values on the stack (two values, if value1 is
	 * not {@code double} nor {@code long}; a single value, if value1 is
	 * {@code double} or {@code long})
	 * <p>
	 * Stack:<br>
	 * {value2, value1} -> {value2, value1}, {value2, value1}
	 */
	public static final int	DUP2			= 92;
	
	/**
	 * Duplicates the first two values and inserts them beneath the third value.
	 * <p>
	 * Stack:<br>
	 * value3, {value2, value1} -> {value2, value1}, value3, {value2, value1}
	 */
	public static final int	DUP2_X1			= 93;
	
	/**
	 * Duplicates the first two values and inserts them beneath the fourth
	 * value.
	 * <p>
	 * Stack:<br>
	 * {value4, value3}, {value2, value1} -> {value2, value1}, {value4, value3},
	 * {value2, value1}
	 */
	public static final int	DUP2_X2			= 94;
	
	/**
	 * Swaps the first two values on the stack (value1 and value2 must not be
	 * {@code double} or {@code long})
	 * <p>
	 * Stack:<br>
	 * value1, value2 -> value2, value1
	 */
	public static final int	SWAP			= 95;
	
	/**
	 * Removes two {@code int} values from the stack, adds them and pushes the
	 * result onto the stack.
	 */
	public static final int	IADD			= 96;
	
	/**
	 * Removes two {@code long} values from the stack, adds them and pushes the
	 * result onto the stack.
	 */
	public static final int	LADD			= 97;
	
	/**
	 * Removes two {@code float} values from the stack, adds them and pushes the
	 * result onto the stack.
	 */
	public static final int	FADD			= 98;
	
	/**
	 * Removes two {@code double} values from the stack, adds them and pushes
	 * the result onto the stack.
	 */
	public static final int	DADD			= 99;
	
	/**
	 * Removes two {@code int} values from the stack, subtracts them and pushes
	 * the result onto the stack.
	 */
	public static final int	ISUB			= 100;
	
	/**
	 * Removes two {@code long} values from the stack, subtracts them and pushes
	 * the result onto the stack.
	 */
	public static final int	LSUB			= 101;
	
	/**
	 * Removes two {@code float} values from the stack, subtracts them and
	 * pushes the result onto the stack.
	 */
	public static final int	FSUB			= 102;
	
	/**
	 * Removes two {@code double} values from the stack, subtracts them and
	 * pushes the result onto the stack.
	 */
	public static final int	DSUB			= 103;
	
	/**
	 * Removes two {@code int} values from the stack, multiplies them and pushes
	 * the result onto the stack.
	 */
	public static final int	IMUL			= 104;
	
	/**
	 * Removes two {@code long} values from the stack, multiplies them and
	 * pushes the result onto the stack.
	 */
	public static final int	LMUL			= 105;
	
	/**
	 * Removes two {@code float} values from the stack, multiplies them and
	 * pushes the result onto the stack.
	 */
	public static final int	FMUL			= 106;
	
	/**
	 * Removes two {@code double} values from the stack, multiplies them and
	 * pushes the result onto the stack.
	 */
	public static final int	DMUL			= 107;
	
	/**
	 * Removes two {@code int} values from the stack, divides them and pushes
	 * the result onto the stack.
	 */
	public static final int	IDIV			= 108;
	
	/**
	 * Removes two {@code long} values from the stack, divides them and pushes
	 * the result onto the stack.
	 */
	public static final int	LDIV			= 109;
	
	/**
	 * Removes two {@code float} values from the stack, divides them and pushes
	 * the result onto the stack.
	 */
	public static final int	FDIV			= 110;
	
	/**
	 * Removes two {@code double} values from the stack, divides them and pushes
	 * the result onto the stack.
	 */
	public static final int	DDIV			= 111;
	
	/**
	 * Removes two {@code int} values from the stack, calculates the remainder
	 * of a division and pushes the result onto the stack.
	 */
	public static final int	IREM			= 112;
	
	/**
	 * Removes two {@code long} values from the stack, calculates the remainder
	 * of a division and pushes the result onto the stack.
	 */
	public static final int	LREM			= 113;
	
	/**
	 * Removes two {@code float} values from the stack, calculates the remainder
	 * of a division and pushes the result onto the stack.
	 */
	public static final int	FREM			= 114;
	
	/**
	 * Removes two {@code double} values from the stack, calculates the
	 * remainder of a division and pushes the result onto the stack.
	 */
	public static final int	DREM			= 115;
	
	/**
	 * Removes an {@code int} value from the stack, negates it and pushes the
	 * result onto the stack.
	 */
	public static final int	INEG			= 116;
	
	/**
	 * Removes a {@code long} value from the stack, negates it and pushes the
	 * result onto the stack.
	 */
	public static final int	LNEG			= 117;
	
	/**
	 * Removes a {@code float} value from the stack, negates it and pushes the
	 * result onto the stack.
	 */
	public static final int	FNEG			= 118;
	
	/**
	 * Removes a {@code double} value from the stack, negates it and pushes the
	 * result onto the stack.
	 */
	public static final int	DNEG			= 119;
	
	/**
	 * Removes two {@code int} values from the stack, left-shifts value1 by
	 * value2 bits and pushes the result onto the stack.
	 */
	public static final int	ISHL			= 120;
	/**
	 * Removes two {@code long} values from the stack, left-shifts value1 by
	 * value2 bits and pushes the result onto the stack.
	 */
	public static final int	LSHL			= 121;
	
	/**
	 * Removes two {@code int} values from the stack, arithmetic right-shifts
	 * value1 by value2 bits and pushes the result onto the stack.
	 */
	public static final int	ISHR			= 122;
	
	/**
	 * Removes two {@code long} values from the stack, arithmetic right-shifts
	 * value1 by value2 bits and pushes the result onto the stack.
	 */
	public static final int	LSHR			= 123;
	
	/**
	 * Removes two {@code int} values from the stack, logical right-shifts
	 * value1 by value2 bits and pushes the result onto the stack.
	 */
	public static final int	IUSHR			= 124;
	
	/**
	 * Removes two {@code long} values from the stack, logical right-shifts
	 * value1 by value2 bits and pushes the result onto the stack.
	 */
	public static final int	LUSHR			= 125;
	
	/**
	 * Removes two {@code int} values from the stack, bitwise ANDs them and
	 * pushes the result onto the stack.
	 */
	public static final int	IAND			= 126;
	
	/**
	 * Removes two {@code long} values from the stack, bitwise ANDs them and
	 * pushes the result onto the stack.
	 */
	public static final int	LAND			= 127;
	
	/**
	 * Removes two {@code int} values from the stack, bitwise ORs them and
	 * pushes the result onto the stack.
	 */
	public static final int	IOR				= 128;
	
	/**
	 * Removes two {@code long} values from the stack, bitwise ORs them and
	 * pushes the result onto the stack.
	 */
	public static final int	LOR				= 129;
	
	/**
	 * Removes two {@code int} values from the stack, bitwise XORs them and
	 * pushes the result onto the stack.
	 */
	public static final int	IXOR			= 130;
	
	/**
	 * Removes two {@code long} values from the stack, bitwise XORs them and
	 * pushes the result onto the stack.
	 */
	public static final int	LXOR			= 131;
	
	/**
	 * Increments a local variable.
	 * 
	 * @param operand1
	 *            the local variable index
	 * @param operand2
	 *            the value
	 */
	public static final int	IINC			= 132;
	
	/**
	 * Removes an {@code int} value from the stack, converts it to a
	 * {@code long} value and pushes the result onto the stack.
	 */
	public static final int	I2L				= 133;
	
	/**
	 * Removes an {@code int} value from the stack, converts it to a
	 * {@code float} value and pushes the result onto the stack.
	 */
	public static final int	I2F				= 134;
	
	/**
	 * Removes an {@code int} value from the stack, converts it to a
	 * {@code double} value and pushes the result onto the stack.
	 */
	public static final int	I2D				= 135;
	
	/**
	 * Removes a {@code long} value from the stack, converts it to an
	 * {@code int} value and pushes the result onto the stack.
	 */
	public static final int	L2I				= 136;
	
	/**
	 * Removes a {@code long} value from the stack, converts it to a
	 * {@code float} value and pushes the result onto the stack.
	 */
	public static final int	L2F				= 137;
	
	/**
	 * Removes a {@code long} value from the stack, converts it to a
	 * {@code double} value and pushes the result onto the stack.
	 */
	public static final int	L2D				= 138;
	
	/**
	 * Removes a {@code float} value from the stack, converts it to an
	 * {@code int} value and pushes the result onto the stack.
	 */
	public static final int	F2I				= 139;
	
	/**
	 * Removes a {@code float} value from the stack, converts it to a
	 * {@code long} value and pushes the result onto the stack.
	 */
	public static final int	F2L				= 140;
	
	/**
	 * Removes a {@code float} value from the stack, converts it to a
	 * {@code double} value and pushes the result onto the stack.
	 */
	public static final int	F2D				= 141;
	
	/**
	 * Removes a {@code double} value from the stack, converts it to an
	 * {@code int} value and pushes the result onto the stack.
	 */
	public static final int	D2I				= 142;
	
	/**
	 * Removes a {@code double} value from the stack, converts it to a
	 * {@code long} value and pushes the result onto the stack.
	 */
	public static final int	D2L				= 143;
	
	/**
	 * Removes a {@code double} value from the stack, converts it to a
	 * {@code float} value and pushes the result onto the stack.
	 */
	public static final int	D2F				= 144;
	
	/**
	 * Removes an {@code int} value from the stack, converts it to a
	 * {@code byte} value and pushes the result onto the stack.
	 */
	public static final int	I2B				= 145;
	
	/**
	 * Removes an {@code int} value from the stack, converts it to a
	 * {@code char} value and pushes the result onto the stack.
	 */
	public static final int	I2C				= 146;
	
	/**
	 * Removes an {@code int} value from the stack, converts it to a
	 * {@code short} value and pushes the result onto the stack.
	 */
	public static final int	I2S				= 147;
	
	/**
	 * Removes two {@code long} values from the stack, compares them and pushes
	 * the result onto the stack.
	 */
	public static final int	LCMP			= 148;
	
	/**
	 * Removes two {@code float} values from the stack, compares them and pushes
	 * the result onto the stack.
	 */
	public static final int	FCMPL			= 149;
	
	/**
	 * Removes two {@code float} values from the stack, compares them and pushes
	 * the result onto the stack.
	 */
	public static final int	FCMPG			= 150;
	
	/**
	 * Removes two {@code double} values from the stack, compares them and
	 * pushes the result onto the stack.
	 */
	public static final int	DCMPL			= 151;
	
	/**
	 * Removes two {@code double} values from the stack, compares them and
	 * pushes the result onto the stack.
	 */
	public static final int	DCMPG			= 152;
	
	/**
	 * Removes an {@code int} value from the stack and jumps to the given
	 * {@link Label} if it equals {@code 0}.
	 * 
	 * @param label
	 *            the label
	 */
	public static final int	IFEQ			= 153;
	
	/**
	 * Removes an {@code int} value from the stack and jumps to the given
	 * {@link Label} if it does not equal {@code 0}.
	 * 
	 * @param label
	 *            the label
	 */
	public static final int	IFNE			= 154;
	
	/**
	 * Removes an {@code int} value from the stack and jumps to the given
	 * {@link Label} if it is less than {@code 0}.
	 * 
	 * @param label
	 *            the label
	 */
	public static final int	IFLT			= 155;
	
	/**
	 * Removes an {@code int} value from the stack and jumps to the given
	 * {@link Label} if it is greater than or equal to {@code 0}.
	 * 
	 * @param label
	 *            the label
	 */
	public static final int	IFGE			= 156;
	
	/**
	 * Removes an {@code int} value from the stack and jumps to the given
	 * {@link Label} if it is greater than {@code 0}.
	 * 
	 * @param label
	 *            the label
	 */
	public static final int	IFGT			= 157;
	
	/**
	 * Removes an {@code int} value from the stack and jumps to the given
	 * {@link Label} if it is less than {@code 0}.
	 * 
	 * @param label
	 *            the label
	 */
	public static final int	IFLE			= 158;
	
	public static final int	IF_ICMPEQ		= 159;
	
	public static final int	IF_ICMPNE		= 160;
	
	public static final int	IF_ICMPLT		= 161;
	
	public static final int	IF_ICMPGE		= 162;
	
	public static final int	IF_ICMPGT		= 163;
	
	public static final int	IF_ICMPLE		= 164;
	
	public static final int	IF_ACMPEQ		= 165;
	
	public static final int	IF_ACMPNE		= 166;
	
	public static final int	GOTO			= 167;
	
	public static final int	JSR				= 168;
	
	public static final int	RET				= 169;
	
	public static final int	TABLESWITCH		= 170;
	
	public static final int	LOOKUPSWITCH	= 171;
	
	public static final int	IRETURN			= 172;
	
	public static final int	LRETURN			= 173;
	
	public static final int	FRETURN			= 174;
	
	public static final int	DRETURN			= 175;
	
	public static final int	ARETURN			= 176;
	
	public static final int	RETURN			= 177;
	
	public static final int	GETSTATIC		= 178;
	
	public static final int	PUTSTATIC		= 179;
	
	public static final int	GETFIELD		= 180;
	
	public static final int	PUTFIELD		= 181;
	
	public static final int	INVOKEVIRTUAL	= 182;
	
	public static final int	INVOKESPECIAL	= 183;
	
	public static final int	INVOKESTATIC	= 184;
	
	public static final int	INVOKEINTERFACE	= 185;
	
	public static final int	INVOKEDYNAMIC	= 186;
	
	public static final int	NEW				= 187;
	
	public static final int	NEWARRAY		= 188;
	
	public static final int	ANEWARRAY		= 189;
	
	public static final int	ARRAYLENGTH		= 190;
	
	public static final int	ATHROW			= 191;
	
	/**
	 * Checks if the value on top of the stack can be cast to the given type,
	 * and throws a {@link ClassCastException} otherwise.
	 * 
	 * @param type
	 *            the type
	 */
	public static final int	CHECKCAST		= 192;
	
	/**
	 * Checks if the value on top of the stack can be cast to the given type and
	 * pushes the result as an {@code int} on the stack. If the cast is
	 * applicable, the result is {@code 1}, and if it is not applicable or the
	 * top value on the stack way {@code null}, the result is {@code 0}.
	 * 
	 * @param type
	 *            the type
	 */
	public static final int	INSTANCEOF		= 193;
	
	public static final int	MONITORENTER	= 194;
	
	public static final int	MONITOREXIT		= 195;
	
	public static final int	MULTIANEWARRAY	= 197;
	
	/**
	 * Removes a reference from the stack and jumps to the given {@link Label}
	 * if it is a {@code null} reference
	 * 
	 * @param label
	 *            the label
	 */
	public static final int	IFNULL			= 198;
	
	/**
	 * Removes a reference from the stack and jumps to the given {@link Label}
	 * if it is not a {@code null} reference
	 * 
	 * @param label
	 *            the label
	 */
	public static final int	IFNONNULL		= 199;
	
	/*
	 * --------------------------- SPECIAL OPCODES ---------------------------
	 * Special Opcodes are only used for the @Bytecode annotation. They are
	 * treated specially by the Dyvil Compiler in order to work with the JVM.
	 * Because of that, these Opcodes should only ever be used in annotations
	 * that are directly processed by the Dyvil Compiler, and they are likely to
	 * cause problems elsewhere.
	 */
	
	/**
	 * Pushes the long {@code -1} onto the stack.
	 */
	public static final int	LCONST_M1		= 264;
	
	public static final int	IBIN			= 270;
	public static final int	LBIN			= 271;
	
	public static final int	L2B				= 280;
	public static final int	L2S				= 281;
	public static final int	L2C				= 282;
	public static final int	F2B				= 283;
	public static final int	F2S				= 284;
	public static final int	F2C				= 285;
	public static final int	D2B				= 286;
	public static final int	D2S				= 287;
	public static final int	D2C				= 289;
	
	public static final int IF_LCMPEQ = 300;
	public static final int IF_LCMPNE = 301;
	public static final int IF_LCMPLT = 302;
	public static final int IF_LCMPGE = 303;
	public static final int IF_LCMPGT = 304;
	public static final int IF_LCMPLE = 305;
	
	public static final int IF_FCMPEQ = 306;
	public static final int IF_FCMPNE = 307;
	public static final int IF_FCMPLT = 308;
	public static final int IF_FCMPGE = 309;
	public static final int IF_FCMPGT = 310;
	public static final int IF_FCMPLE = 311;
	
	public static final int IF_DCMPEQ = 312;
	public static final int IF_DCMPNE = 313;
	public static final int IF_DCMPLT = 314;
	public static final int IF_DCMPGE = 315;
	public static final int IF_DCMPGT = 316;
	public static final int IF_DCMPLE = 317;
}
