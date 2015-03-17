package dyvil.tools.compiler.ast.classes;

import java.util.List;

import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.field.IProperty;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.Parameter;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public interface IClassBody extends IASTNode
{
	// Associated Class
	
	public void setTheClass(IClass iclass);
	
	public IClass getTheClass();
	
	// Nested Classes
	
	public int classCount();
	
	public void addClass(IClass iclass);
	
	public IClass getClass(int index);
	
	public IClass getClass(String name);
	
	// Fields
	
	public int fieldCount();
	
	public void addField(IField field);
	
	public IField getField(int index);
	
	public IField getField(String name);
	
	public default IField getInstanceField()
	{
		return null;
	}
	
	// Properties
	
	public int propertyCount();
	
	public void addProperty(IProperty property);
	
	public IProperty getProperty(int index);
	
	public IProperty getProperty(String name);
	
	// Methods
	
	public int methodCount();
	
	public void addMethod(IMethod method);
	
	public IMethod getMethod(int index);
	
	public IMethod getMethod(String name);
	
	public IMethod getMethod(String name, Parameter[] parameters, int parameterCount);
	
	public void getMethodMatches(List<MethodMatch> list, IValue instance, String name, IArguments arguments);
	
	public default IMethod getFunctionalMethod()
	{
		return null;
	}
	
	// Phases
	
	public void resolveTypes(MarkerList markers, IContext context);
	
	public void resolve(MarkerList markers, IContext context);
	
	public void checkTypes(MarkerList markers, IContext context);
	
	public void check(MarkerList markers, IContext context);
	
	public void foldConstants();
}
