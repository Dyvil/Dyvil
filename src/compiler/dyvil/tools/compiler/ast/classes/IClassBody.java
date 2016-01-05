package dyvil.tools.compiler.ast.classes;

import dyvil.tools.compiler.ast.consumer.IClassBodyConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.field.IProperty;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.method.ConstructorMatchList;
import dyvil.tools.compiler.ast.method.IConstructor;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.MarkerList;

public interface IClassBody extends IASTNode, IClassList, IClassBodyConsumer
{
	// Associated Class
	
	void setTheClass(IClass iclass);
	
	IClass getTheClass();
	
	// Fields
	
	int fieldCount();
	
	@Override
	void addField(IField field);
	
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
	
	// Constructors
	
	int constructorCount();
	
	@Override
	void addConstructor(IConstructor constructor);
	
	IConstructor getConstructor(int index);
	
	IConstructor getConstructor(IParameter[] parameters, int parameterCount);
	
	void getConstructorMatches(ConstructorMatchList list, IArguments arguments);
	
	// Methods
	
	int methodCount();
	
	@Override
	void addMethod(IMethod method);
	
	IMethod getMethod(int index);
	
	IMethod getMethod(Name name);
	
	void getMethodMatches(MethodMatchList list, IValue instance, Name name, IArguments arguments);
	
	default IMethod getFunctionalMethod()
	{
		return null;
	}
	
	boolean checkImplements(MarkerList markers, IClass checkedClass, IMethod candidate, ITypeContext typeContext);
	
	void checkMethods(MarkerList markers, IClass checkedClass, ITypeContext typeContext);
	
	// Phases
	
	void resolveTypes(MarkerList markers);
	
	void resolve(MarkerList markers);
	
	void checkTypes(MarkerList markers);
	
	void check(MarkerList markers);
	
	void foldConstants();
	
	void cleanup();
}
