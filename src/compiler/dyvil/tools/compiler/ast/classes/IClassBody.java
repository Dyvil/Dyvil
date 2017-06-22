package dyvil.tools.compiler.ast.classes;

import dyvil.tools.compiler.ast.constructor.IConstructor;
import dyvil.tools.compiler.ast.constructor.IInitializer;
import dyvil.tools.compiler.ast.consumer.IMemberConsumer;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.field.IProperty;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.parameter.ParameterList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.phase.IResolvable;
import dyvil.tools.parsing.ASTNode;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

public interface IClassBody extends ASTNode, IResolvable, IClassList, IMemberConsumer<IField>
{
	// Enclosing Class

	IClass getEnclosingClass();

	void setEnclosingClass(IClass enclosingClass);

	// Fields

	int fieldCount();

	@Override
	void addDataMember(IField field);

	IField getField(int index);

	IField getField(Name name);

	default IField getInstanceField()
	{
		return null;
	}

	// Properties

	int propertyCount();

	@Override
	void addProperty(IProperty property);

	IProperty getProperty(int index);

	IProperty getProperty(Name name);

	// Methods

	int methodCount();

	@Override
	void addMethod(IMethod method);

	IMethod getMethod(int index);

	IMethod getMethod(Name name);

	void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, ArgumentList arguments);

	void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType);

	default IMethod getFunctionalMethod()
	{
		return null;
	}

	default void setFunctionalMethod(IMethod method)
	{
	}

	boolean checkImplements(IMethod candidate, ITypeContext typeContext);

	void checkMethods(MarkerList markers, IClass checkedClass, ITypeContext typeContext);

	// Constructors

	int constructorCount();

	@Override
	void addConstructor(IConstructor constructor);

	IConstructor getConstructor(int index);

	IConstructor getConstructor(ParameterList parameters);

	void getConstructorMatches(MatchList<IConstructor> list, ArgumentList arguments);

	// Initializers

	int initializerCount();

	@Override
	void addInitializer(IInitializer initializer);

	IInitializer getInitializer(int index);

	// Phases

	default void initExternalMethodCache()
	{
	}

	default void initExternalImplicitCache()
	{
	}

	@Override
	void resolveTypes(MarkerList markers, IContext context);

	@Override
	void resolve(MarkerList markers, IContext context);

	@Override
	void checkTypes(MarkerList markers, IContext context);

	@Override
	void check(MarkerList markers, IContext context);

	@Override
	void foldConstants();

	@Override
	void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList);

	// Compilation

	void write(ClassWriter writer) throws BytecodeException;

	void writeClassInit(MethodWriter writer) throws BytecodeException;

	void writeStaticInit(MethodWriter writer) throws BytecodeException;
}
