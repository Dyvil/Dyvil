package dyvil.reflect;

import java.lang.reflect.Field;

/**
 * The <b>Modifiers</b> interface declares all (visible and invisible) modifiers that can be used <i>Dyvil</i> source
 * code and that can appear in class files. Note that only modifiers less than {@code 0xFFFF} will actually appear in
 * the Bytecode, other modifiers such Dyvil-specific ones will be stored in {@link
 * dyvil.annotation.internal.DyvilModifiers DyvilModifiers} annotations.
 *
 * @author Clashsoft
 */
public interface Modifiers
{
	// Access Modifiers

	/**
	 * <i>Dyvil</i> {@code package} modifier. This modifier is used to mark that a member is {@code package}-private,
	 * i.e. it may only be accessed from compilation units within the same package.
	 */
	int PACKAGE = 0x08000000;

	/**
	 * {@code public} access modifier.
	 */
	int PUBLIC = 0x00000001;

	/**
	 * {@code private} access modifier.
	 */
	int PRIVATE = 0x00000002;

	/**
	 * {@code protected} access modifier.
	 */
	int PROTECTED = 0x00000004;

	int PRIVATE_PROTECTED = PRIVATE | PROTECTED;

	/**
	 * {@code static} modifier.
	 */
	int STATIC = 0x00000008;

	/**
	 * {@code final} modifier.
	 */
	int FINAL = 0x00000010;

	/**
	 * <i>Dyvil</i> {@code const} modifier. This modifier is just a shortcut for {@code static final} and should be used
	 * to declare constants.
	 */
	int CONST = STATIC | FINAL;

	int STATIC_FINAL = STATIC | FINAL;

	/**
	 * {@code synchronized} modifier.
	 */
	int SYNCHRONIZED = 0x00000020;

	/**
	 * {@code volatile} modifier.
	 */
	int VOLATILE = 0x00000040;

	/**
	 * Modifier used to declare a method to be a <i>bridge</i> method, i.e. a method generated by the compiler to
	 * support generic formal parameters.
	 */
	int BRIDGE = 0x00000040;

	/**
	 * {@code transient} modifier.
	 */
	int TRANSIENT = 0x00000080;

	/**
	 * Modifier used to declare that the last parameter of a method is a <i>varargs</i> parameter.
	 */
	int ACC_VARARGS = 0x00000080;

	/**
	 * {@code native} modifier.
	 */
	int NATIVE = 0x00000100;

	/**
	 * {@code abstract} modifier.
	 */
	int ABSTRACT = 0x00000400;

	/**
	 * Modifier used to declare that a class is an {@code interface}.
	 */
	int INTERFACE = 0x00000200;

	/**
	 * {@code stricfp} modifier.
	 */
	int STRICT = 0x00000800;

	/**
	 * Modifier used for fields and methods that are not present in the source code and generated by the compiler.
	 */
	int SYNTHETIC = 0x00001000;

	/**
	 * Modifier used to declare a class to be an annotation ({@code @interface} ).
	 */
	int ANNOTATION = 0x00002000;

	/**
	 * Modifier used to declare a class to be an {@code enum} class.
	 */
	int ENUM = 0x00004000;

	int ENUM_CONST = PUBLIC | ENUM | CONST;

	/**
	 * Modifier used for constructors and fields of anonymous classes.
	 */
	int MANDATED = 0x00008000;

	// Type Modifiers

	int OBJECT = 0x00010000;

	int ENUM_CLASS = ENUM;

	/**
	 * <i>Dyvil</i> {@code object} modifier. If a class is marked with this modifier, it is a singleton object class.
	 */
	int OBJECT_CLASS = OBJECT | FINAL;

	/**
	 * <i>Dyvil</i> {@code object} modifier. If a class is marked with this modifier, it is a case class. This modifier
	 * not be visible in the bytecode.
	 */
	int CASE_CLASS = 0x00020000;

	/**
	 * <i>Dyvil</i> {@code functional} modifier. This modifier is a shortcut for the {@link FunctionalInterface}
	 * annotation.
	 */
	int FUNCTIONAL = 0x00040000;

	int INTERFACE_CLASS = INTERFACE | ABSTRACT;

	int TRAIT = 0x00080000;

	/**
	 * <i>Dyvil</i> {@code trait} modifier.
	 */
	int TRAIT_CLASS = TRAIT | INTERFACE_CLASS;

	int ANNOTATION_CLASS = ANNOTATION | INTERFACE_CLASS;

	// Method Modifiers

	/**
	 * <i>Dyvil</i> {@code inline} modifier. If a method is marked with this modifier, it will be inlined by the
	 * compiler to reduce method call overhead.
	 */
	int INLINE = 0x00010000;

	int INFIX_FLAG = 0x00020000;

	/**
	 * <i>Dyvil</i> {@code infix} modifier. If a method is marked with this modifier, it can be called on any Object and
	 * virtually has the receiver as the first parameter. An infix method is always static.
	 */
	int INFIX = INFIX_FLAG | STATIC;

	int EXTENSION_FLAG = 0x00080000;

	int EXTENSION = EXTENSION_FLAG | INFIX;

	// Field Modifiers

	/**
	 * <i>Dyvil</i> {@code lazy} modifier. The {@code lazy} modifier can be applied on fields, variables and parameters
	 * and has a different behavior on each different type.
	 */
	int LAZY = 0x00010000;

	// Parameter Modifiers

	int DEFAULT = 0x00020000;

	int VARARGS = 0x00040000;

	// Member Modifiers

	/**
	 * <i>Dyvil</i> {@code internal} modifier. This is used to mark that a class, method or field is only visible from
	 * inside the current library / project.
	 */
	int INTERNAL = 0x00100000;

	/**
	 * <i>Dyvil</i> {@code implicit} modifier.
	 */
	int IMPLICIT = 0x00200000;

	/**
	 * <i>Dyvil</i> {@code explicit} modifier.
	 */
	int EXPLICIT = 0x00400000;

	// Compile-time only Modifiers

	/**
	 * Modifier that marks a member with an {@link java.lang.Deprecated} or {@link dyvil.annotation.Deprecated}
	 * annotation.
	 */
	int DEPRECATED = 0x10000000;

	/**
	 * <i>Dyvil</i> {@code override} modifier. This modifier is a shortcut for the {@link Override} annotation.
	 */
	int OVERRIDE = 0x20000000;

	int GENERATED = 0x80000000;

	// Masks

	/**
	 * The modifiers that can be used to declare the class type (i.e., {@code class}, {@code interface}, {@code trait},
	 * {@code enum}, {@code object} or {@code annotation} / {@code @interface}). This value excludes the {@code
	 * ABSTRACT} bit flag.
	 */
	int CLASS_TYPE_MODIFIERS = INTERFACE | ANNOTATION | ENUM | OBJECT | TRAIT;

	int VISIBILITY_MODIFIERS = PUBLIC | PROTECTED | PRIVATE | PACKAGE;

	/**
	 * The access modifiers.
	 */
	int ACCESS_MODIFIERS = VISIBILITY_MODIFIERS | INTERNAL;

	/**
	 * The modifiers that can be used on any member.
	 */
	int MEMBER_MODIFIERS = ACCESS_MODIFIERS | STATIC | FINAL // denotable
		                       | SYNTHETIC;

	/**
	 * The modifiers that can be used on classes.
	 */
	int CLASS_MODIFIERS = MEMBER_MODIFIERS | ABSTRACT | CASE_CLASS // denotable
		                      | CLASS_TYPE_MODIFIERS | STRICT;

	/**
	 * The modifiers that can be used on fields.
	 */
	int FIELD_MODIFIERS = MEMBER_MODIFIERS | LAZY | ENUM | IMPLICIT // denotable
		                      | TRANSIENT | VOLATILE;

	/**
	 * The modifiers that can be used on methods.
	 */
	int METHOD_MODIFIERS =
		MEMBER_MODIFIERS | ABSTRACT | SYNCHRONIZED | INLINE | INFIX | EXTENSION | IMPLICIT | OVERRIDE // denotable
			| NATIVE | STRICT | BRIDGE | ACC_VARARGS;

	/**
	 * The modifiers that can be applied to variables and method parameters.
	 */
	int VARIABLE_MODIFIERS = FINAL | IMPLICIT;

	/**
	 * The modifiers that can be used on parameters.
	 */
	int PARAMETER_MODIFIERS = VARIABLE_MODIFIERS | EXPLICIT // denotable
		                          | DEFAULT | MANDATED | EXTENSION | VARARGS | ACC_VARARGS | SYNTHETIC;

	/**
	 * The modifiers that can be applied to class parameters.
	 */
	int CLASS_PARAMETER_MODIFIERS = ACCESS_MODIFIERS | FINAL | IMPLICIT | EXPLICIT | OVERRIDE // denotable
	                                | DEFAULT;

	int CONSTRUCTOR_MODIFIERS = ACCESS_MODIFIERS;

	int INITIALIZER_MODIFIERS = PRIVATE | STATIC;

	static void main(String[] args)
	{
		int used = 0;
		for (Field f : Modifiers.class.getDeclaredFields())
		{
			if (f.getType() != int.class)
			{
				continue;
			}

			try
			{
				used |= (int) f.get(null);
			}
			catch (IllegalAccessException ignored)
			{
			}
		}

		for (int i = 0; i < 32; i++)
		{
			int mask = 1 << i;
			if ((used & mask) == 0)
			{
				System.out.printf("%#010x\n", mask);
			}
		}
	}
}
