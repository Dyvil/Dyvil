package dyvil.tools.repl;

import java.util.concurrent.atomic.AtomicInteger;

import dyvil.collection.List;
import dyvil.collection.Map;
import dyvil.collection.mutable.ArrayList;
import dyvil.collection.mutable.HashMap;
import dyvil.collection.mutable.IdentityHashMap;
import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.consumer.IClassBodyConsumer;
import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.field.IProperty;
import dyvil.tools.compiler.ast.imports.ImportDeclaration;
import dyvil.tools.compiler.ast.imports.IncludeDeclaration;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.IConstructor;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.operator.Operator;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.DyvilHeader;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.ast.type.alias.ITypeAlias;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.IClassCompilable;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Util;

public class REPLContext extends DyvilHeader implements IValueConsumer, IClassBodyConsumer, IClassCompilableList
{
	private static int			classIndex;
	private static String		className;
	protected static MarkerList	markers	= new MarkerList();
	
	private static Map<Name, IField>	fields	= new IdentityHashMap();
	private static List<IMethod>		methods	= new ArrayList();
	private static Map<Name, IClass>	classes	= new IdentityHashMap();
	
	protected static List<IClassCompilable>	compilableList	= new ArrayList();
	protected static List<IClassCompilable>	innerClassList	= new ArrayList();
	
	private static IClass memberClass;
	
	private static Map<String, AtomicInteger> resultIndexes = new HashMap();
	
	public REPLContext()
	{
		super(Name.getQualified("REPL"));
	}
	
	protected static void reset()
	{
		className = "repl$results/REPL$Result$" + classIndex++;
		markers.clear();
	}
	
	protected static boolean reportErrors(MarkerList markers)
	{
		if (!markers.isEmpty())
		{
			StringBuilder buf = new StringBuilder();
			String code = DyvilREPL.currentCode;
			markers.sort();
			for (Marker m : markers)
			{
				m.log(code, buf);
			}
			
			System.err.println(buf.toString());
			
			if (markers.getErrors() > 0)
			{
				return true;
			}
		}
		return false;
	}
	
	private static void compileInnerClasses()
	{
		for (IClassCompilable icc : innerClassList)
		{
			try
			{
				String fileName = icc.getFileName();
				byte[] bytes = ClassWriter.compile(icc);
				REPLMemberClass.loadClass(fileName, bytes);
			}
			catch (Throwable t)
			{
				t.printStackTrace();
			}
		}
	}
	
	private boolean computeVariable(REPLVariable field)
	{
		field.resolveTypes(markers, this);
		field.resolve(markers, this);
		field.checkTypes(markers, this);
		field.check(markers, this);
		
		if (reportErrors(markers))
		{
			this.cleanup();
			return false;
		}
		
		field.foldConstants();
		field.cleanup(this, this);
		this.compileVariable(field);
		return true;
	}
	
	private static void getClassName(StringBuilder builder, IType type)
	{
		if (type.isArrayType())
		{
			getClassName(builder, type.getElementType());
			builder.append("Array");
			return;
		}
		
		builder.append(type.getTheClass().getName().unqualified);
	}
	
	private static Name getFieldName(IType type)
	{
		StringBuilder sb = new StringBuilder();
		getClassName(sb, type);
		
		// Make the first character lower case
		sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
		
		// Strip trailing digits
		for (int i = 0, len = sb.length(); i < len; i++)
		{
			if (Character.isDigit(sb.charAt(i)))
			{
				sb.delete(i, len);
				break;
			}
		}
		
		// The final variable name, without the index
		String shortName = sb.toString();
		
		AtomicInteger ai = resultIndexes.get(shortName);
		if (ai == null)
		{
			resultIndexes.put(shortName, ai = new AtomicInteger(0));
		}
		
		int index = ai.incrementAndGet();
		return Name.get(sb.append(index).toString());
	}
	
	private void compileVariable(REPLVariable field)
	{
		compileInnerClasses();
		field.compute();
		
		this.cleanup();
	}
	
	private static REPLMemberClass getREPLClass(IClassMember member)
	{
		REPLMemberClass iclass = new REPLMemberClass(Name.getQualified(className), member);
		member.setTheClass(iclass);
		member.addModifier(Modifiers.STATIC);
		return iclass;
	}
	
	private static void compileClass(IClass iclass)
	{
		compileInnerClasses();
		REPLMemberClass.compile(iclass);
	}
	
	@Override
	public void cleanup()
	{
		compilableList.clear();
		innerClassList.clear();
		markers.clear();
	}
	
	@Override
	public void setValue(IValue value)
	{
		REPLVariable field = new REPLVariable(ICodePosition.ORIGIN, null, Types.UNKNOWN, value, className, Modifiers.FINAL);
		memberClass = getREPLClass(field);
		
		value.resolveTypes(markers, this);
		value = value.resolve(markers, this);
		
		IType type = value.getType();
		value = value.withType(type, type, markers, this);
		if (value == null)
		{
			throw new Error("Invalid Value - Invalid Type " + type);
		}
		
		type = value.getType();
		
		value.checkTypes(markers, this);
		value.check(markers, this);
		
		if (reportErrors(markers))
		{
			this.cleanup();
			return;
		}
		
		value = value.cleanup(this, this);
		field.setValue(value);
		field.setType(type);
		
		field.setName(getFieldName(type));
		
		this.compileVariable(field);
		if (type != Types.VOID)
		{
			fields.put(field.getName(), field);
			System.out.println(field.toString());
		}
	}
	
	@Override
	public void addOperator(Operator op)
	{
		this.operators.put(op.name, op);
		System.out.println("Defined " + op);
	}
	
	@Override
	public Operator getOperator(Name name)
	{
		return this.operators.get(name);
	}
	
	@Override
	public void addImport(ImportDeclaration declaration)
	{
		declaration.resolveTypes(markers, this, false);
		
		if (reportErrors(markers))
		{
			return;
		}
		
		super.addImport(declaration);
		System.out.println(declaration);
	}
	
	@Override
	public void addUsing(ImportDeclaration declaration)
	{
		declaration.resolveTypes(markers, this, true);
		
		if (reportErrors(markers))
		{
			return;
		}
		
		super.addUsing(declaration);
		System.out.println(declaration);
	}
	
	@Override
	public void addInclude(IncludeDeclaration component)
	{
		component.resolve(markers);
		
		if (reportErrors(markers))
		{
			return;
		}
		
		super.addInclude(component);
		this.operators.putAll(component.getHeader().getOperators());
		System.out.println(component);
	}
	
	@Override
	public void addTypeAlias(ITypeAlias typeAlias)
	{
		typeAlias.resolve(markers, this);
		if (reportErrors(markers))
		{
			return;
		}
		
		super.addTypeAlias(typeAlias);
		System.out.println(typeAlias);
	}
	
	@Override
	public void addClass(IClass iclass)
	{
		iclass.setHeader(this);
		
		iclass.resolveTypes(markers, this);
		iclass.resolve(markers, this);
		iclass.checkTypes(markers, this);
		iclass.check(markers, this);
		
		if (reportErrors(markers))
		{
			return;
		}
		
		iclass.foldConstants();
		iclass.cleanup(this, this);
		REPLContext.compileClass(iclass);
		
		classes.put(iclass.getName(), iclass);
		
		StringBuilder buf = new StringBuilder("Defined ");
		Util.classSignatureToString(iclass, buf);
		System.out.println(buf.toString());
	}
	
	@Override
	public void addInnerClass(IClassCompilable iclass)
	{
		if (iclass.hasSeparateFile())
		{
			iclass.setInnerIndex(className, innerClassList.size());
			innerClassList.add(iclass);
		}
		else
		{
			this.addCompilable(iclass);
		}
	}
	
	@Override
	public int compilableCount()
	{
		return compilableList.size();
	}
	
	@Override
	public void addCompilable(IClassCompilable compilable)
	{
		compilable.setInnerIndex(className, compilableList.size());
		compilableList.add(compilable);
	}
	
	@Override
	public IClassCompilable getCompilable(int index)
	{
		return compilableList.get(index);
	}
	
	@Override
	public void addField(IField field)
	{
		REPLVariable var = new REPLVariable(field.getPosition(), field.getName(), field.getType(), field.getValue(), className, field.getModifiers());
		var.setAnnotations(field.getAnnotations());
		memberClass = getREPLClass(var);
		
		if (this.computeVariable(var))
		{
			fields.put(var.getName(), var);
			System.out.println(var.toString());
		}
	}
	
	@Override
	public void addProperty(IProperty property)
	{
		REPLMemberClass iclass = REPLContext.getREPLClass(property);
		
		property.resolveTypes(markers, this);
		property.resolve(markers, this);
		property.checkTypes(markers, this);
		property.check(markers, this);
		
		if (reportErrors(markers))
		{
			this.cleanup();
			return;
		}
		
		property.foldConstants();
		property.cleanup(this, this);
		
		REPLContext.compileClass(iclass);
		
		fields.put(property.getName(), property);
		
		StringBuilder buf = new StringBuilder("Defined Property '");
		Util.propertySignatureToString(property, buf);
		System.out.println(buf.append('\'').toString());
		
		this.cleanup();
	}
	
	@Override
	public void addMethod(IMethod method)
	{
		REPLMemberClass iclass = REPLContext.getREPLClass(method);
		
		method.resolveTypes(markers, this);
		if (reportErrors(markers))
		{
			this.cleanup();
			return;
		}
		
		methods.add(method);
		method.resolve(markers, this);
		method.checkTypes(markers, this);
		method.check(markers, this);
		
		if (reportErrors(markers))
		{
			methods.remove(method);
			this.cleanup();
			return;
		}
		
		method.foldConstants();
		method.cleanup(this, this);
		
		REPLContext.compileClass(iclass);
		
		StringBuilder buf = new StringBuilder("Defined Method '");
		Util.methodSignatureToString(method, buf);
		System.out.println(buf.append('\'').toString());
		
		this.cleanup();
	}
	
	@Override
	public void addConstructor(IConstructor constructor)
	{
	}
	
	@Override
	public IClass resolveClass(Name name)
	{
		IClass c = classes.get(name);
		if (c != null)
		{
			return c;
		}
		
		return super.resolveClass(name);
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		IField f = fields.get(name);
		if (f != null)
		{
			return f;
		}
		
		return super.resolveField(name);
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
		for (IMethod m : methods)
		{
			float match = m.getSignatureMatch(name, instance, arguments);
			if (match > 0)
			{
				list.add(new MethodMatch(m, match));
			}
		}
		
		if (!list.isEmpty())
		{
			return;
		}
		
		super.getMethodMatches(list, instance, name, arguments);
	}
	
	@Override
	public boolean handleException(IType type)
	{
		return true;
	}
	
	@Override
	public IClass getThisClass()
	{
		return memberClass;
	}
	
	@Override
	public Name getName()
	{
		return Name.getQualified(className);
	}
	
	@Override
	public String getFullName()
	{
		return className;
	}
	
	@Override
	public String getFullName(Name name)
	{
		return "repl$." + name.qualified;
	}
	
	@Override
	public String getInternalName()
	{
		return className;
	}
	
	@Override
	public String getInternalName(Name name)
	{
		return "repl$/" + name.qualified;
	}
}
