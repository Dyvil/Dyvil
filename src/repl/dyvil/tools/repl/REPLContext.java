package dyvil.tools.repl;

import dyvil.collection.List;
import dyvil.collection.Map;
import dyvil.collection.mutable.ArrayList;
import dyvil.collection.mutable.IdentityHashMap;
import dyvil.reflect.Modifiers;
import dyvil.reflect.ReflectUtils;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.consumer.IClassBodyConsumer;
import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.external.ExternalClass;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.field.IProperty;
import dyvil.tools.compiler.ast.imports.ImportDeclaration;
import dyvil.tools.compiler.ast.imports.IncludeDeclaration;
import dyvil.tools.compiler.ast.member.IClassCompilable;
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
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.CodePosition;
import dyvil.tools.compiler.util.Util;

public class REPLContext extends DyvilHeader implements IValueConsumer, IClassBodyConsumer, IClassCompilableList
{
	private static final CodePosition		CODE_POSITION	= new CodePosition(1, 0, 1);
	
	private static int						resultIndex;
	private static int						classIndex;
	private static String					className;
	
	private static Map<Name, IField>		fields			= new IdentityHashMap();
	private static List<IMethod>			methods			= new ArrayList();
	private static Map<Name, IClass>		classes			= new IdentityHashMap();
	
	protected static List<IClassCompilable>	compilableList	= new ArrayList();
	private static List<IClassCompilable>	innerClassList	= new ArrayList();
	
	public REPLContext()
	{
		super("REPL");
	}
	
	protected static void newClassName()
	{
		className = "REPL" + classIndex++;
	}
	
	private static boolean reportErrors(MarkerList markers)
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
				ReflectUtils.unsafe.defineClass(fileName, bytes, 0, bytes.length, null, null);
			}
			catch (Throwable t)
			{
				t.printStackTrace();
			}
		}
	}
	
	private boolean computeVariable(REPLVariable field)
	{
		MarkerList markers = new MarkerList();
		field.resolveTypes(markers, this);
		field.resolve(markers, this);
		field.checkTypes(markers, this);
		field.check(markers, this);
		
		if (reportErrors(markers))
		{
			this.cleanup();
			return false;
		}
		
		field.cleanup(this, this);
		this.compileVariable(field);
		return true;
	}
	
	private void compileVariable(REPLVariable field)
	{
		field.foldConstants();
		
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
	}
	
	@Override
	public void setValue(IValue value)
	{
		Name name = Name.getQualified("res" + resultIndex);
		
		MarkerList markers = new MarkerList();
		value.resolveTypes(markers, this);
		value = value.resolve(markers, this);
		
		IType type = value.getType();
		IValue value1 = value.withType(type, type, markers, this);
		if (value1 == null)
		{
			// TODO Report error?
		}
		else
		{
			value = value1;
			type = value1.getType();
		}
		
		value.checkTypes(markers, this);
		value.check(markers, this);
		
		if (reportErrors(markers))
		{
			this.cleanup();
			return;
		}
		
		value = value.cleanup(this, this);
		
		REPLVariable field = new REPLVariable(CODE_POSITION, name, type, value, className);
		field.modifiers = Modifiers.FINAL;
		
		this.compileVariable(field);
		if (type != Types.VOID)
		{
			fields.put(field.name, field);
			System.out.println(field.toString());
			resultIndex++;
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
		MarkerList markers = new MarkerList();
		declaration.resolveTypes(markers, this, false);
		
		if (reportErrors(markers))
		{
			return;
		}
		
		super.addImport(declaration);
		System.out.println("Added import declaration for '" + declaration.theImport + "'");
	}
	
	@Override
	public void addUsing(ImportDeclaration declaration)
	{
		MarkerList markers = new MarkerList();
		declaration.resolveTypes(markers, this, true);
		
		if (reportErrors(markers))
		{
			return;
		}
		
		super.addUsing(declaration);
		System.out.println("Added using declaration for '" + declaration.theImport + "'");
	}
	
	@Override
	public void addInclude(IncludeDeclaration component)
	{
		MarkerList markers = new MarkerList();
		component.resolve(markers);
		
		if (reportErrors(markers))
		{
			return;
		}
		
		this.addIncludeToArray(component);
		component.addOperators(this.operators);
		System.out.println("Included the header '" + component.getHeader().getFullName() + "'");
	}
	
	@Override
	public void addTypeAlias(ITypeAlias typeAlias)
	{
		MarkerList markers = new MarkerList();
		
		typeAlias.resolve(markers, this);
		if (reportErrors(markers))
		{
			return;
		}
		
		super.addTypeAlias(typeAlias);
		System.out.println("Added Type Alias '" + typeAlias.getName() + "' for '" + typeAlias.getType() + "'");
	}
	
	@Override
	public void addClass(IClass iclass)
	{
		MarkerList markers = new MarkerList();
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
		System.out.println("Defined class " + iclass.getName());
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
		REPLVariable var = new REPLVariable(field.getPosition(), field.getName(), field.getType(), field.getValue(), className);
		var.setAnnotations(field.getAnnotations(), field.annotationCount());
		var.modifiers = field.getModifiers();
		
		if (this.computeVariable(var))
		{
			fields.put(var.name, var);
			System.out.println(var.toString());
		}
	}
	
	@Override
	public void addProperty(IProperty property)
	{
		MarkerList markers = new MarkerList();
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
		MarkerList markers = new MarkerList();
		REPLMemberClass iclass = REPLContext.getREPLClass(method);
		
		method.resolveTypes(markers, this);
		method.resolve(markers, this);
		method.checkTypes(markers, this);
		method.check(markers, this);
		
		if (reportErrors(markers))
		{
			this.cleanup();
			return;
		}
		
		method.foldConstants();
		method.cleanup(this, this);
		
		REPLContext.compileClass(iclass);
		
		methods.add(method);
		
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
	public byte getVisibility(IClassMember member)
	{
		IClass iclass = member.getTheClass();
		if (iclass == null || iclass instanceof REPLMemberClass)
		{
			return VISIBLE;
		}
		
		int level = member.getAccessLevel();
		if ((level & Modifiers.SEALED) != 0)
		{
			if (iclass instanceof ExternalClass)
			{
				return SEALED;
			}
			// Clear the SEALED bit by ANDing with 0b1111
			level &= 0b1111;
		}
		if (level == Modifiers.PUBLIC)
		{
			return VISIBLE;
		}
		
		return INVISIBLE;
	}
	
	@Override
	public String getName()
	{
		return className;
	}
	
	@Override
	public String getFullName()
	{
		return className;
	}
	
	@Override
	public String getFullName(String name)
	{
		return name;
	}
	
	@Override
	public String getInternalName()
	{
		return className;
	}
	
	@Override
	public String getInternalName(String name)
	{
		return name;
	}
}
