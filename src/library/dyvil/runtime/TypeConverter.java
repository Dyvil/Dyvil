package dyvil.runtime;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.MethodVisitor;
import dyvil.tools.asm.Type;

import java.lang.invoke.MethodHandleInfo;

import static dyvil.reflect.Opcodes.*;
import static dyvil.runtime.Wrapper.*;

public class TypeConverter
{
	private static final int NUM_WRAPPERS = Wrapper.values().length;
	
	private static final String NAME_OBJECT    = "java/lang/Object";
	private static final String WRAPPER_PREFIX = "Ljava/lang/";
	
	// Same for all primitives; name of the boxing method
	private static final String NAME_BOX_METHOD   = "valueOf";
	private static final String NAME_UNBOX_METHOD = "Value";
	
	// Table of opcodes for widening primitive conversions
	private static final int[][] wideningOpcodes = new int[NUM_WRAPPERS][NUM_WRAPPERS];
	
	private static final Wrapper[] FROM_WRAPPER_NAME = new Wrapper[16];
	
	// Table of wrappers for primitives, indexed by ASM type sorts
	private static final Wrapper[] FROM_TYPE_SORT = new Wrapper[16];
	
	static
	{
		for (Wrapper w : Wrapper.values())
		{
			if (w.basicTypeChar() != 'L')
			{
				int wi = hashWrapperName(w.wrapperSimpleName());
				assert FROM_WRAPPER_NAME[wi] == null;
				FROM_WRAPPER_NAME[wi] = w;
			}
		}
		
		initWidening(LONG, Opcodes.I2L, BYTE, SHORT, INT, CHAR);
		initWidening(LONG, Opcodes.F2L, FLOAT);
		initWidening(FLOAT, Opcodes.I2F, BYTE, SHORT, INT, CHAR);
		initWidening(FLOAT, Opcodes.L2F, LONG);
		initWidening(DOUBLE, Opcodes.I2D, BYTE, SHORT, INT, CHAR);
		initWidening(DOUBLE, Opcodes.F2D, FLOAT);
		initWidening(DOUBLE, Opcodes.L2D, LONG);
		
		FROM_TYPE_SORT[Type.BYTE] = Wrapper.BYTE;
		FROM_TYPE_SORT[Type.SHORT] = Wrapper.SHORT;
		FROM_TYPE_SORT[Type.INT] = Wrapper.INT;
		FROM_TYPE_SORT[Type.LONG] = Wrapper.LONG;
		FROM_TYPE_SORT[Type.CHAR] = Wrapper.CHAR;
		FROM_TYPE_SORT[Type.FLOAT] = Wrapper.FLOAT;
		FROM_TYPE_SORT[Type.DOUBLE] = Wrapper.DOUBLE;
		FROM_TYPE_SORT[Type.BOOLEAN] = Wrapper.BOOLEAN;
	}
	
	private static void initWidening(Wrapper to, int opcode, Wrapper... from)
	{
		for (Wrapper f : from)
		{
			wideningOpcodes[f.ordinal()][to.ordinal()] = opcode;
		}
	}
	
	private static int hashWrapperName(String xn)
	{
		if (xn.length() < 3)
		{
			return 0;
		}
		return (3 * xn.charAt(1) + xn.charAt(2)) % 16;
	}
	
	private static Wrapper wrapperOrNullFromDescriptor(String desc)
	{
		if (!desc.startsWith(WRAPPER_PREFIX))
		{
			// Not a class type (array or method), so not a boxed type
			// or not in the right package
			return null;
		}
		// Pare it down to the simple class name
		String cname = desc.substring(WRAPPER_PREFIX.length(), desc.length() - 1);
		// Hash to a Wrapper
		Wrapper w = FROM_WRAPPER_NAME[hashWrapperName(cname)];
		if (w == null || w.wrapperSimpleName().equals(cname))
		{
			return w;
		}
		return null;
	}
	
	private static String wrapperName(Wrapper w)
	{
		return "java/lang/" + w.wrapperSimpleName();
	}
	
	private static String boxingDescriptor(Wrapper w)
	{
		return "(" + w.basicTypeChar() + ")Ljava/lang/" + w.wrapperSimpleName() + ";";
	}
	
	private static String unboxingDescriptor(Wrapper w)
	{
		return "()" + w.basicTypeChar();
	}
	
	static void boxIfTypePrimitive(MethodVisitor mv, Type t)
	{
		Wrapper w = FROM_TYPE_SORT[t.getSort()];
		if (w != null)
		{
			box(mv, w);
		}
	}
	
	static void widen(MethodVisitor mv, Wrapper ws, Wrapper wt)
	{
		if (ws != wt)
		{
			int opcode = wideningOpcodes[ws.ordinal()][wt.ordinal()];
			if (opcode != Opcodes.NOP)
			{
				mv.visitInsn(opcode);
			}
		}
	}
	
	static void box(MethodVisitor mv, Wrapper w)
	{
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, wrapperName(w), NAME_BOX_METHOD, boxingDescriptor(w), false);
	}
	
	static void unbox(MethodVisitor mv, String wrapperClassName, Wrapper wt)
	{
		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, wrapperClassName, wt.primitiveSimpleName() + NAME_UNBOX_METHOD,
		                   unboxingDescriptor(wt), false);
	}
	
	private static String descriptorToName(String desc)
	{
		int last = desc.length() - 1;
		if (desc.charAt(0) == 'L' && desc.charAt(last) == ';')
		{
			// In descriptor form
			return desc.substring(1, last);
		}
		// Already in internal name form
		return desc;
	}
	
	static void cast(MethodVisitor mv, String ds, String dt)
	{
		String ns = descriptorToName(ds);
		String nt = descriptorToName(dt);
		if (!nt.equals(ns) && !nt.equals(NAME_OBJECT))
		{
			mv.visitTypeInsn(Opcodes.CHECKCAST, nt);
		}
	}
	
	private boolean isPrimitive(Wrapper w)
	{
		return w != OBJECT;
	}
	
	private static Wrapper toWrapper(String desc)
	{
		char first = desc.charAt(0);
		if (first == '[' || first == '(')
		{
			first = 'L';
		}
		return Wrapper.forBasicType(first);
	}
	
	public static void convertType(MethodVisitor mv, Class<?> arg, Class<?> target, Class<?> functional)
	{
		if (arg.equals(target) && arg.equals(functional))
		{
			return;
		}
		if (arg == Void.TYPE)
		{
			if (target == Void.TYPE)
			{
				return;
			}
			if (target == Object.class || target == dyvil.lang.Void.class)
			{
				mv.visitFieldInsn(Opcodes.GETSTATIC, "dyvil/lang/Void", "instance", "Ldyvil/lang/Void;");
				return;
			}
			if (target != Void.TYPE)
			{
				mv.visitInsn(Opcodes.ACONST_NULL);
			}
			return;
		}
		if (target == Void.TYPE)
		{
			if (arg == long.class || arg == double.class)
			{
				mv.visitInsn(Opcodes.POP2);
				return;
			}
			mv.visitInsn(Opcodes.POP);
			return;
		}
		
		if (arg.isPrimitive())
		{
			Wrapper wArg = Wrapper.forPrimitiveType(arg);
			if (target.isPrimitive())
			{
				// Both primitives: widening
				widen(mv, wArg, Wrapper.forPrimitiveType(target));
			}
			else
			{
				// Primitive argument to reference target
				String dTarget = Type.getDescriptor(target);
				Wrapper wPrimTarget = wrapperOrNullFromDescriptor(dTarget);
				if (wPrimTarget != null)
				{
					// The target is a boxed primitive type, widen to get there
					// before boxing
					widen(mv, wArg, wPrimTarget);
					box(mv, wPrimTarget);
				}
				else
				{
					// Otherwise, box and cast
					box(mv, wArg);
					cast(mv, wrapperName(wArg), dTarget);
				}
			}
		}
		else
		{
			String dArg = Type.getDescriptor(arg);
			String dSrc;
			if (functional.isPrimitive())
			{
				dSrc = dArg;
			}
			else
			{
				// Cast to convert to possibly more specific type, and generate
				// CCE for invalid arg
				dSrc = Type.getDescriptor(functional);
				cast(mv, dArg, dSrc);
			}
			String dTarget = Type.getDescriptor(target);
			if (target.isPrimitive())
			{
				Wrapper wTarget = toWrapper(dTarget);
				// Reference argument to primitive target
				Wrapper wps = wrapperOrNullFromDescriptor(dSrc);
				if (wps != null)
				{
					if (wps.isSigned() || wps.isFloating())
					{
						// Boxed number to primitive
						unbox(mv, wrapperName(wps), wTarget);
					}
					else
					{
						// Character or Boolean
						unbox(mv, wrapperName(wps), wps);
						widen(mv, wps, wTarget);
					}
				}
				else
				{
					// Source type is reference type, but not boxed type,
					// assume it is super type of target type
					String intermediate = wrapperName(wTarget);
					cast(mv, dSrc, intermediate);
					unbox(mv, intermediate, wTarget);
				}
			}
			else
			{
				// Both reference types: just case to target type
				cast(mv, dSrc, dTarget);
			}
		}
	}

	public static int invocationOpcode(int kind) throws InternalError
	{
		switch (kind)
		{
		case MethodHandleInfo.REF_invokeStatic:
			return INVOKESTATIC;
		case MethodHandleInfo.REF_newInvokeSpecial:
			return INVOKESPECIAL;
		case MethodHandleInfo.REF_invokeVirtual:
			return INVOKEVIRTUAL;
		case MethodHandleInfo.REF_invokeInterface:
			return INVOKEINTERFACE;
		case MethodHandleInfo.REF_invokeSpecial:
			return INVOKESPECIAL;
		default:
			throw new InternalError("Unexpected invocation kind: " + kind);
		}
	}

	public static String getInternalName(Class<?> c)
	{
		return c.getName().replace('.', '/');
	}

	public static int getParameterSize(Class<?> c)
	{
		if (c == Void.TYPE)
		{
			return 0;
		}
		else if (c == Long.TYPE || c == Double.TYPE)
		{
			return 2;
		}
		return 1;
	}

	public static int getLoadOpcode(Class<?> c)
	{
		if (c == Void.TYPE)
		{
			throw new InternalError("Unexpected void type of load opcode");
		}
		return ILOAD + getOpcodeOffset(c);
	}

	public static int getReturnOpcode(Class<?> c)
	{
		if (c == Void.TYPE)
		{
			return RETURN;
		}
		return IRETURN + getOpcodeOffset(c);
	}

	private static int getOpcodeOffset(Class<?> c)
	{
		if (c.isPrimitive())
		{
			if (c == Long.TYPE)
			{
				return 1;
			}
			else if (c == Float.TYPE)
			{
				return 2;
			}
			else if (c == Double.TYPE)
			{
				return 3;
			}
			return 0;
		}
		return 4;
	}
}
