package dyvil.tools.compiler.ast.classes;

import dyvil.collection.List;
import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.ast.consumer.IClassBodyConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.field.IProperty;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.IConstructor;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public interface IClassBody extends IASTNode, IClassList, IClassBodyConsumer
{
	// Associated Class
	
	public void setTheClass(IClass iclass);
	
	public IClass getTheClass();
	
	// Fields
	
	public int fieldCount();
	
	@Override
	public void addField(IField field);
	
	public IField getField(int index);
	
	public IField getField(Name name);
	
	public default IField getInstanceField()
	{
		return null;
	}
	
	// Properties
	
	public int propertyCount();
	
	@Override
	public void addProperty(IProperty property);
	
	public IProperty getProperty(int index);
	
	public IProperty getProperty(Name name);
	
	// Constructors
	
	public int constructorCount();
	
	@Override
	public void addConstructor(IConstructor constructor);
	
	public IConstructor getConstructor(int index);
	
	public IConstructor getConstructor(IParameter[] parameters, int parameterCount);
	
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments);
	
	// Methods
	
	public int methodCount();
	
	@Override
	public void addMethod(IMethod method);
	
	public IMethod getMethod(int index);
	
	public IMethod getMethod(Name name);
	
	public IMethod getMethod(Name name, IParameter[] parameters, int parameterCount, IType concrete);
	
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments);
	
	public default IMethod getFunctionalMethod()
	{
		return null;
	}
	
	// Phases
	
	public void resolveTypes(MarkerList markers);
	
	public void resolve(MarkerList markers);
	
	public void checkTypes(MarkerList markers);
	
	public void check(MarkerList markers);
	
	public void foldConstants();
	
	public void cleanup();
}
