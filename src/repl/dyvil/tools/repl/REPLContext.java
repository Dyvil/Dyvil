package dyvil.tools.repl;

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
import dyvil.tools.compiler.ast.header.ImportDeclaration;
import dyvil.tools.compiler.ast.header.IncludeDeclaration;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.method.IConstructor;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.modifiers.BaseModifiers;
import dyvil.tools.compiler.ast.modifiers.ModifierList;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
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

import java.util.concurrent.atomic.AtomicInteger;

public class REPLContext extends DyvilHeader implements IValueConsumer, IClassBodyConsumer, IClassCompilableList
{
	private static final String REPL$CLASSES     = "repl$classes/";
	public static final  int    ACCESS_MODIFIERS = Modifiers.PUBLIC | Modifiers.PRIVATE | Modifiers.PROTECTED;
	
	protected DyvilREPL repl;
	protected String    currentCode;
	private   int       classIndex;
	private   String    className;
	protected MarkerList markers = new MarkerList();
	
	public Map<Name, IField> fields  = new IdentityHashMap<>();
	public List<IMethod>     methods = new ArrayList<>();
	public Map<Name, IClass> classes = new IdentityHashMap<>();
	
	protected List<IClassCompilable> compilableList = new ArrayList<>();
	protected List<IClassCompilable> innerClassList = new ArrayList<>();
	
	private IClass memberClass;
	
	private Map<String, AtomicInteger> resultIndexes = new HashMap<>();
	
	public REPLContext(DyvilREPL repl)
	{
		super(Name.getQualified("REPL"));
		this.repl = repl;
	}
	
	protected void startEvaluation(String code)
	{
		this.currentCode = code;
		this.className = REPL$CLASSES + "REPL$Result$" + this.classIndex++;
		this.cleanup();
	}
	
	protected boolean hasErrors()
	{
		return this.markers.getErrors() > 0;
	}

	protected void reportErrors()
	{
		if (this.markers.isEmpty())
		{
			return;
		}

		StringBuilder buf = new StringBuilder();
		this.markers.sort();
		for (Marker m : this.markers)
		{
			m.log(this.currentCode, buf);
		}

		this.repl.getErrorOutput().println(buf.toString());
	}
	
	private void compileInnerClasses()
	{
		for (IClassCompilable icc : this.innerClassList)
		{
			try
			{
				String fileName = icc.getFileName();
				byte[] bytes = ClassWriter.compile(icc);
				REPLMemberClass.loadClass(this.repl, REPL$CLASSES.concat(fileName), bytes);
			}
			catch (Throwable t)
			{
				t.printStackTrace(this.repl.getErrorOutput());
			}
		}
	}
	
	private boolean computeVariable(REPLVariable field)
	{
		field.resolveTypes(this.markers, this);
		field.resolve(this.markers, this);
		field.checkTypes(this.markers, this);
		field.check(this.markers, this);
		
		if (this.hasErrors())
		{
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
		
		AtomicInteger ai = this.resultIndexes.get(shortName);
		if (ai == null)
		{
			this.resultIndexes.put(shortName, ai = new AtomicInteger(0));
		}
		
		int index = ai.incrementAndGet();
		return Name.get(sb.append(index).toString());
	}
	
	private void compileVariable(REPLVariable field)
	{
		this.compileInnerClasses();
		field.compute(this.repl, this.compilableList);
	}
	
	private REPLMemberClass getREPLClass(IClassMember member)
	{
		REPLMemberClass iclass = new REPLMemberClass(Name.getQualified(this.className), member, this);
		member.setTheClass(iclass);
		member.getModifiers().addIntModifier(Modifiers.STATIC);
		return iclass;
	}
	
	private void compileClass(IClass iclass)
	{
		this.compileInnerClasses();
		REPLMemberClass.compile(this.repl, iclass);
	}
	
	@Override
	public void cleanup()
	{
		this.compilableList.clear();
		this.innerClassList.clear();
		this.markers.clear();
	}
	
	@Override
	public void setValue(IValue value)
	{
		ModifierList modifierList = new ModifierList();
		modifierList.addModifier(BaseModifiers.FINAL);

		REPLVariable field = new REPLVariable(this, ICodePosition.ORIGIN, null, Types.UNKNOWN, value, this.className,
		                                      modifierList);
		this.memberClass = this.getREPLClass(field);
		
		value.resolveTypes(this.markers, this);
		value = value.resolve(this.markers, this);
		
		if (value.valueTag() == IValue.FIELD_ACCESS)
		{
			IDataMember f = ((FieldAccess) value).getField();
			if (f instanceof REPLVariable)
			{
				((REPLVariable) f).updateValue(this.repl);
				this.repl.getOutput().println(f);
				return;
			}
		}
		
		IType type = value.getType();
		IValue typedValue = value.withType(type, type, this.markers, this);
		if (typedValue != null)
		{
			value = typedValue;
		}
		
		type = value.getType();
		
		value.checkTypes(this.markers, this);
		value.check(this.markers, this);
		
		if (this.hasErrors())
		{
			return;
		}
		
		for (int i = 0; i < DyvilCompiler.constantFolding; i++)
		{
			value = value.foldConstants();
		}
		
		value = value.cleanup(this, this);
		field.setValue(value);
		field.setType(type);
		
		field.setName(this.getFieldName(type));
		
		this.compileVariable(field);
		if (type != Types.VOID)
		{
			this.fields.put(field.getName(), field);
			this.repl.getOutput().println(field.toString());
		}
	}
	
	@Override
	public void addOperator(Operator op)
	{
		this.operators.put(op.name, op);
		this.repl.getOutput().println("Defined " + op);
	}
	
	@Override
	public Operator getOperator(Name name)
	{
		return this.operators.get(name);
	}
	
	@Override
	public void addImport(ImportDeclaration declaration)
	{
		declaration.resolveTypes(this.markers, this, false);
		
		if (this.hasErrors())
		{
			return;
		}
		
		super.addImport(declaration);
		this.repl.getOutput().println(declaration);
	}
	
	@Override
	public void addUsing(ImportDeclaration declaration)
	{
		declaration.resolveTypes(this.markers, this, true);
		
		if (this.hasErrors())
		{
			return;
		}
		
		super.addUsing(declaration);
		this.repl.getOutput().println(declaration);
	}
	
	@Override
	public void addInclude(IncludeDeclaration component)
	{
		component.resolve(this.markers, this);
		
		if (this.hasErrors())
		{
			return;
		}
		
		super.addInclude(component);
		this.operators.putAll(component.getHeader().getOperators());
		this.repl.getOutput().println(component);
	}
	
	@Override
	public void addTypeAlias(ITypeAlias typeAlias)
	{
		typeAlias.resolveTypes(this.markers, this);
		typeAlias.resolve(this.markers, this);
		typeAlias.checkTypes(this.markers, this);
		typeAlias.check(this.markers, this);

		if (this.hasErrors())
		{
			return;
		}

		typeAlias.foldConstants();
		typeAlias.cleanup(this, this);

		super.addTypeAlias(typeAlias);
		this.repl.getOutput().println(typeAlias);
	}
	
	@Override
	public void addClass(IClass iclass)
	{
		iclass.setHeader(this);
		
		// Check if the class is already defined
		try
		{
			Class.forName(iclass.getFullName(), false, REPLMemberClass.CLASS_LOADER);
			System.err.println("The class '" + iclass.getName() + "' cannot be re-defined");
			return;
		}
		catch (ClassNotFoundException ex)
		{
		}
		
		// Run the usual phases
		iclass.resolveTypes(this.markers, this);
		iclass.resolve(this.markers, this);
		iclass.checkTypes(this.markers, this);
		iclass.check(this.markers, this);
		
		if (this.hasErrors())
		{
			return;
		}
		
		for (int i = 0; i < DyvilCompiler.constantFolding; i++)
		{
			iclass.foldConstants();
		}
		iclass.cleanup(this, this);
		
		// Compile and load the class
		
		this.compileClass(iclass);
		
		this.classes.put(iclass.getName(), iclass);
		
		StringBuilder buf = new StringBuilder("Defined ");
		Util.classSignatureToString(iclass, buf);
		this.repl.getOutput().println(buf.toString());
	}
	
	@Override
	public void addInnerClass(IClassCompilable iclass)
	{
		if (iclass.hasSeparateFile())
		{
			iclass.setInnerIndex(this.className, this.innerClassList.size());
			this.innerClassList.add(iclass);
		}
		else
		{
			this.addCompilable(iclass);
		}
	}
	
	@Override
	public int compilableCount()
	{
		return this.compilableList.size();
	}
	
	@Override
	public void addCompilable(IClassCompilable compilable)
	{
		compilable.setInnerIndex(this.className, this.compilableList.size());
		this.compilableList.add(compilable);
	}
	
	@Override
	public IClassCompilable getCompilable(int index)
	{
		return this.compilableList.get(index);
	}
	
	@Override
	public void addField(IField field)
	{
		REPLVariable var = new REPLVariable(this, field.getPosition(), field.getName(), field.getType(),
		                                    field.getValue(), this.className, field.getModifiers());
		var.setAnnotations(field.getAnnotations());
		this.memberClass = this.getREPLClass(var);
		
		if (this.computeVariable(var))
		{
			this.fields.put(var.getName(), var);
			this.repl.getOutput().println(var.toString());
		}
	}
	
	@Override
	public void addProperty(IProperty property)
	{
		REPLMemberClass iclass = this.getREPLClass(property);
		
		property.resolveTypes(this.markers, this);
		property.resolve(this.markers, this);
		property.checkTypes(this.markers, this);
		property.check(this.markers, this);
		
		if (this.hasErrors())
		{
			return;
		}
		
		for (int i = 0; i < DyvilCompiler.constantFolding; i++)
		{
			property.foldConstants();
		}
		property.cleanup(this, this);
		
		this.compileClass(iclass);
		
		this.fields.put(property.getName(), property);
		
		StringBuilder buf = new StringBuilder("Defined Property '");
		Util.propertySignatureToString(property, buf);
		this.repl.getOutput().println(buf.append('\'').toString());
		
		this.cleanup();
	}
	
	@Override
	public void addMethod(IMethod method)
	{
		updateModifiers(method.getModifiers());

		REPLMemberClass iclass = this.getREPLClass(method);
		
		method.resolveTypes(this.markers, this);
		if (this.hasErrors())
		{
			return;
		}
		
		method.resolve(this.markers, this);
		method.checkTypes(this.markers, this);
		method.check(this.markers, this);
		
		if (this.hasErrors())
		{
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

	public static void updateModifiers(ModifierSet modifiers)
	{
		if ((modifiers.toFlags() & ACCESS_MODIFIERS) == 0)
		{
			modifiers.addIntModifier(Modifiers.PUBLIC);
		}
		modifiers.addIntModifier(Modifiers.STATIC);
	}
	
	private void registerMethod(IMethod method, REPLMemberClass iclass)
	{
		boolean replaced = false;
		int methods = this.methods.size();
		for (int i = 0; i < methods; i++)
		{
			if (this.methods.get(i).checkOverride(this.markers, iclass, method, null))
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
		this.repl.getOutput().println(buf.append('\'').toString());
	}
	
	@Override
	public void addConstructor(IConstructor constructor)
	{
	}
	
	@Override
	public IClass resolveClass(Name name)
	{
		IClass c = this.classes.get(name);
		if (c != null)
		{
			return c;
		}
		
		return super.resolveClass(name);
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		IField f = this.fields.get(name);
		if (f != null)
		{
			return f;
		}
		
		return super.resolveField(name);
	}
	
	@Override
	public void getMethodMatches(MethodMatchList list, IValue instance, Name name, IArguments arguments)
	{
		for (IMethod method : this.methods)
		{
			float match = method.getSignatureMatch(name, instance, arguments);
			if (match > 0)
			{
				list.add(method, match);
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
		return this.memberClass;
	}
	
	@Override
	public Name getName()
	{
		return Name.getQualified(this.className);
	}
	
	@Override
	public String getFullName()
	{
		return this.className;
	}
	
	@Override
	public String getFullName(Name name)
	{
		return "repl$classes." + name.qualified;
	}
	
	@Override
	public String getInternalName()
	{
		return this.className;
	}
	
	@Override
	public String getInternalName(Name name)
	{
		return REPL$CLASSES + name.qualified;
	}
}
