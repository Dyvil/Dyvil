package dyvilx.tools.compiler.ast.classes.metadata;

import dyvil.reflect.Modifiers;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.constructor.IConstructor;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.field.IField;
import dyvilx.tools.compiler.ast.header.ClassCompilable;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.member.MemberKind;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.backend.classes.ClassWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.phase.Resolvable;
import dyvilx.tools.parsing.marker.MarkerList;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.util.Set;

public interface IClassMetadata extends ClassCompilable, Resolvable
{
	// =============== Static Methods ===============

	static IClassMetadata getClassMetadata(IClass forClass, long modifiers)
	{
		if ((modifiers & Modifiers.ANNOTATION) != 0)
		{
			return new AnnotationMetadata(forClass);
		}
		if ((modifiers & Modifiers.TRAIT) != 0)
		{
			return new TraitMetadata(forClass);
		}
		if ((modifiers & Modifiers.INTERFACE) != 0)
		{
			return new InterfaceMetadata(forClass);
		}
		if ((modifiers & Modifiers.ENUM) != 0)
		{
			return new EnumClassMetadata(forClass);
		}
		if ((modifiers & Modifiers.OBJECT) != 0)
		{
			return new ObjectClassMetadata(forClass);
		}
		if ((modifiers & Modifiers.EXTENSION) != 0)
		{
			return new ExtensionMetadata(forClass);
		}
		// All modifiers above are single-bit flags
		if ((modifiers & Modifiers.CASE_CLASS) != 0)
		{
			return new CaseClassMetadata(forClass);
		}
		return new ClassMetadata(forClass);
	}

	// =============== Properties ===============

	// --------------- Kind ---------------

	MemberKind getKind();

	// --------------- Default Constructor (regular and case classes) ---------------

	default IConstructor getConstructor()
	{
		return null;
	}

	// --------------- Instance Field (object classes) ---------------

	default IField getInstanceField()
	{
		return null;
	}

	default void setInstanceField(IField field)
	{
	}

	// --------------- Functional Method (interfaces) ---------------

	default IMethod getFunctionalMethod()
	{
		return null;
	}

	default void setFunctionalMethod(IMethod method)
	{
	}

	// --------------- Retention and Target (annotations) ---------------

	default RetentionPolicy getRetention()
	{
		return null;
	}

	default boolean isTarget(ElementType target)
	{
		return false;
	}

	default Set<ElementType> getTargets()
	{
		return null;
	}

	// =============== Methods ===============

	// --------------- Resolution Phases ---------------

	@Override
	default void resolveTypes(MarkerList markers, IContext context)
	{
		throw new UnsupportedOperationException();
	}

	default void resolveTypesAfterAttributes(MarkerList markers, IContext context)
	{
	}

	/**
	 * Called before the class body goes through RESOLVE_TYPES. Super-types and -interfaces and type parameters have
	 * already been resolved.
	 */
	default void resolveTypesBeforeBody(MarkerList markers, IContext context)
	{
	}

	/**
	 * Called after the class body went through RESOLVE_TYPES.<p/> Checks which synthetic members have to be generated
	 */
	default void resolveTypesAfterBody(MarkerList markers, IContext context)
	{
	}

	/**
	 * Generates the signatures of the synthetic members. Concrete implementations should be added in {@link
	 * #resolve(MarkerList, IContext)}.
	 */
	default void resolveTypesGenerate(MarkerList markers, IContext context)
	{
	}

	@Override
	default void resolve(MarkerList markers, IContext context)
	{
	}

	// --------------- Diagnostic Phases ---------------

	@Override
	default void checkTypes(MarkerList markers, IContext context)
	{
	}

	@Override
	default void check(MarkerList markers, IContext context)
	{
	}

	// --------------- Pre-Compilation Phases ---------------

	@Override
	default void foldConstants()
	{
	}

	@Override
	default void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
	}

	// --------------- Compilation ---------------

	default void writePost(ClassWriter writer)
	{
	}

	default void writeStaticInitPost(MethodWriter writer) throws BytecodeException
	{
	}

	default void writeClassInitPost(MethodWriter writer) throws BytecodeException
	{
	}
}
