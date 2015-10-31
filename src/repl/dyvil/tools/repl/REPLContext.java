package dyvil.tools.repl;

import java.util.concurrent.atomic.AtomicInteger;

import dyvil.collection.List;
import dyvil.collection.Map;
import dyvil.collection.mutable.ArrayList;
import dyvil.collection.mutable.HashMap;
import dyvil.collection.mutable.IdentityHashMap;
import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.access.FieldAccess;
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
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class REPLContext extends DyvilHeader implements IValueConsumer, IClassBodyConsumer, IClassCompilableList
{
	private static final String REPL$CLASSES = "repl$classes/";
	
	protected DyvilREPL		repl;
	private int				classIndex;
	private String			className;
	protected MarkerList	markers	= new MarkerList();
	
	public Map<Name, IField>	fields	= new IdentityHashMap();
	public List<IMethod>		methods	= new ArrayList();
	public Map<Name, IClass>	classes	= new IdentityHashMap();
	
	protected List<IClassCompilable>	compilableList	= new ArrayList();
	protected List<IClassCompilable>	innerClassList	= new ArrayList();
	
	private IClass memberClass;
	
	private Map<String, AtomicInteger> resultIndexes = new HashMap();
	
	public REPLContext(DyvilREPL repl)
	{
		super(Name.getQualified("REPL"));
		this.repl = repl;
	}
	
	protected void reset()
	{
		className = REPL$CLASSES + "REPL$Result$" + classIndex++;
		markers.clear();
	}
	
	protected static boolean reportErrors(MarkerList markers, String code)
	{
		if (!markers.isEmpty())
		{
			StringBuilder buf = new StringBuilder();
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
	
	private void compileInnerClasses()
	{
		for (IClassCompilable icc : innerClassList)
		{
			try
			{
				String fileName = icc.getFileName();
				byte[] bytes = ClassWriter.compile(icc);
				REPLMemberClass.loadClass(this.repl, REPL$CLASSES.concat(fileName), bytes);
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
		
		if (markers.getErrors() > 0)
		{
			this.cleanup();
			return false;
		}
		
		for (int i = 0; i < DyvilCompiler.constantFolding; i++)
		{
			field.foldConstants();
		}
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
	
	private Name getFieldName(IType type)
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
		field.compute(this.compilableList);
		
		this.cleanup();
	}
	
	private REPLMemberClass getREPLClass(IClassMember member)
	{
		REPLMemberClass iclass = new REPLMemberClass(Name.getQualified(className), member, this);
		member.setTheClass(iclass);
		member.addModifier(Modifiers.STATIC);
		return iclass;
	}
	
	private void compileClass(IClass iclass)
	{
		compileInnerClasses();
		REPLMemberClass.compile(this.repl, iclass);
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
		REPLVariable field = new REPLVariable(this, ICodePosition.ORIGIN, null, Types.UNKNOWN, value, className, Modifiers.FINAL);
		memberClass = getREPLClass(field);
		
		value.resolveTypes(markers, this);
		value = value.resolve(markers, this);
		
		if (value.valueTag() == IValue.FIELD_ACCESS)
		{
			IDataMember f = ((FieldAccess) value).getField();
			if (f instanceof REPLVariable)
			{
				((REPLVariable) f).updateValue();
				System.out.println(f);
				return;
			}
		}
		
		IType type = value.getType();
		value = value.withType(type, type, markers, this);
		if (value == null)
		{
			throw new Error("Invalid Value - Invalid Type " + type);
		}
		
		type = value.getType();
		
		value.checkTypes(markers, this);
		value.check(markers, this);
		
		if (markers.getErrors() > 0)
		{
			this.cleanup();
			return;
		}
		
		for (int i = 0; i < DyvilCompiler.constantFolding; i++)
		{
			value = value.foldConstants();
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
		
		if (markers.getErrors() > 0)
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
		
		if (markers.getErrors() > 0)
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
		
		if (markers.getErrors() > 0)
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
		if (markers.getErrors() > 0)
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
		
		if (markers.getErrors() > 0)
		{
			return;
		}
		
		for (int i = 0; i < DyvilCompiler.constantFolding; i++)
		{
			iclass.foldConstants();
		}
		iclass.cleanup(this, this);
		this.compileClass(iclass);
		
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
		REPLVariable var = new REPLVariable(this, field.getPosition(), field.getName(), field.getType(), field.getValue(), className, field.getModifiers());
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
		REPLMemberClass iclass = this.getREPLClass(property);
		
		property.resolveTypes(markers, this);
		property.resolve(markers, this);
		property.checkTypes(markers, this);
		property.check(markers, this);
		
		if (markers.getErrors() > 0)
		{
			this.cleanup();
			return;
		}
		
		for (int i = 0; i < DyvilCompiler.constantFolding; i++)
		{
			property.foldConstants();
		}
		property.cleanup(this, this);
		
		this.compileClass(iclass);
		
		fields.put(property.getName(), property);
		
		StringBuilder buf = new StringBuilder("Defined Property '");
		Util.propertySignatureToString(property, buf);
		System.out.println(buf.append('\'').toString());
		
		this.cleanup();
	}
	
	@Override
	public void addMethod(IMethod method)
	{
		REPLMemberClass iclass = this.getREPLClass(method);
		
		method.resolveTypes(markers, this);
		if (markers.getErrors() > 0)
		{
			this.cleanup();
			return;
		}
		
		method.resolve(markers, this);
		method.checkTypes(markers, this);
		method.check(markers, this);
		
		if (markers.getErrors() > 0)
		{
			this.cleanup();
			return;
		}
		
		for (int i = 0; i < DyvilCompiler.constantFolding; i++)
		{
			method.foldConstants();
		}
		method.cleanup(this, this);
		
		this.compileClass(iclass);
		
		this.registerMethod(method, iclass);
		
		this.cleanup();
	}
	
	private void registerMethod(IMethod method, REPLMemberClass iclass)
	{
		boolean replaced = false;
		int methods = this.methods.size();
		for (int i = 0; i < methods; i++)
		{
			if (this.methods.get(i).checkOverride(markers, iclass, method, null))
			{
				this.methods.set(i, method);
				replaced = true;
				break;
			}
		}
		
		StringBuilder buf = new StringBuilder();
		if (!replaced)
		{
			this.methods.add(method);
			buf.append("Defined method ");
		}
		else
		{
			buf.append("Re-defined method ");
		}
		
		buf.append('\'');
		Util.methodSignatureToString(method, buf);
		System.out.println(buf.append('\'').toString());
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
		return "repl$classes." + name.qualified;
	}
	
	@Override
	public String getInternalName()
	{
		return className;
	}
	
	@Override
	public String getInternalName(Name name)
	{
		return REPL$CLASSES + name.qualified;
	}
}
