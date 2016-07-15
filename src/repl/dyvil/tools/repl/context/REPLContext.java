package dyvil.tools.repl.context;

import dyvil.collection.List;
import dyvil.collection.Map;
import dyvil.collection.mutable.ArrayList;
import dyvil.collection.mutable.IdentityHashMap;
import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.classes.ClassBody;
import dyvil.tools.compiler.ast.classes.CodeClass;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constructor.IConstructor;
import dyvil.tools.compiler.ast.constructor.IInitializer;
import dyvil.tools.compiler.ast.consumer.IMemberConsumer;
import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.field.IProperty;
import dyvil.tools.compiler.ast.header.ImportDeclaration;
import dyvil.tools.compiler.ast.header.IncludeDeclaration;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.modifiers.BaseModifiers;
import dyvil.tools.compiler.ast.modifiers.ModifierList;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.operator.IOperator;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.DyvilHeader;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.alias.ITypeAlias;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.IClassCompilable;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;
import dyvil.tools.repl.DyvilREPL;

public class REPLContext extends DyvilHeader implements IValueConsumer, IMemberConsumer<REPLVariable>
{
	private static final String REPL$CLASSES = "repl$classes/";

	protected final DyvilREPL repl;

	// Persistent members

	private final Map<Name, IField>    fields     = new IdentityHashMap<>();
	private final Map<Name, IProperty> properties = new IdentityHashMap<>();
	private final List<IMethod>        methods    = new ArrayList<>();
	private final Map<Name, IClass>    classes    = new IdentityHashMap<>();

	// Updated for every input
	private   int       resultIndex;
	private   int       classIndex;
	protected String    currentCode;
	private   CodeClass currentClass;

	// Cleared for every input
	protected final MarkerList             markers        = new MarkerList(Markers.INSTANCE);
	protected final List<IClassCompilable> innerClassList = new ArrayList<>();
	protected final List<IMember>          members        = new ArrayList<>();

	public REPLContext(DyvilREPL repl)
	{
		super(repl.getCompiler(), Name.fromRaw("REPL"));
		this.repl = repl;
	}

	// Getters

	public Map<Name, IField> getFields()
	{
		return this.fields;
	}

	public Map<Name, IProperty> getProperties()
	{
		return this.properties;
	}

	public List<IMethod> getMethods()
	{
		return this.methods;
	}

	public Map<Name, IClass> getClasses()
	{
		return this.classes;
	}

	// Evaluation

	public void startEvaluation(String code)
	{
		final String className = "REPL$Result" + this.classIndex++;
		this.currentClass = new CodeClass(this, Name.fromRaw(className));
		this.currentClass.setBody(new ClassBody(this.currentClass));
		this.currentCode = code;
	}

	public MarkerList getMarkers()
	{
		return this.markers;
	}

	protected boolean hasErrors()
	{
		return this.markers.getErrors() > 0;
	}

	public void endEvaluation()
	{
		this.currentClass.resolveTypes(this.markers, this);
		this.currentClass.resolve(this.markers, this);
		this.currentClass.checkTypes(this.markers, this);
		this.currentClass.check(this.markers, this);

		if (this.markers.getErrors() > 0)
		{
			this.printErrors();
			this.cleanup();
			return;
		}

		for (int i = this.compiler.config.getConstantFolding() - 1; i >= 0; i--)
		{
			this.currentClass.foldConstants();
		}

		this.currentClass.cleanup(this, this.currentClass);

		final Class<?> theClass = this.compileAndLoad();

		for (IMember member : this.members)
		{
			this.processMember(member, theClass);
		}

		this.printErrors();
		this.cleanup();
	}

	private Class<?> compileAndLoad()
	{
		// Compile all inner class
		for (IClassCompilable innerClass : this.innerClassList)
		{
			try
			{
				final String fileName = innerClass.getFileName();
				final byte[] bytes = ClassWriter.compile(innerClass);
				REPLCompiler.loadClass(this.repl, REPL$CLASSES.concat(fileName), bytes);
			}
			catch (Throwable t)
			{
				t.printStackTrace(this.compiler.getErrorOutput());
			}
		}

		return REPLCompiler.compile(this.repl, this.currentClass);
	}

	public void processMember(IMember member, Class<?> theClass)
	{
		if (member instanceof REPLVariable)
		{
			final REPLVariable replVariable = (REPLVariable) member;
			replVariable.setRuntimeClass(theClass);
			replVariable.updateValue(this.repl);
		}

		this.repl.getOutput().println(member);
	}

	@Override
	public void cleanup()
	{
		this.innerClassList.clear();
		this.markers.clear();
		this.members.clear();
	}

	private void printErrors()
	{
		if (this.markers.isEmpty())
		{
			return;
		}

		boolean colors = this.compiler.config.useAnsiColors();
		StringBuilder buf = new StringBuilder();
		this.markers.sort();
		for (Marker m : this.markers)
		{
			m.log(this.currentCode, buf, colors);
		}

		this.compiler.getOutput().println(buf.toString());
	}

	private static void getClassName(StringBuilder builder, IType type)
	{
		if (type.isArrayType())
		{
			getClassName(builder, type.getElementType());
			builder.append("Array");
			return;
		}
		if (type.typeTag() == IType.REFERENCE)
		{
			getClassName(builder, type.getElementType());
			builder.append("Ref");
			return;
		}

		builder.append(type.getName().unqualified);
	}

	private void initMember(IClassMember member)
	{
		member.setEnclosingClass(this.currentClass);

		// Ensure public & static
		final ModifierSet modifiers = member.getModifiers();
		if ((modifiers.toFlags() & Modifiers.VISIBILITY_MODIFIERS) == 0)
		{
			modifiers.addIntModifier(Modifiers.PUBLIC);
		}
		modifiers.addIntModifier(Modifiers.STATIC);
	}

	@Override
	public void setValue(IValue value)
	{
		final ModifierList modifierList = new ModifierList();
		modifierList.addModifier(BaseModifiers.FINAL);

		final Name name = Name.fromRaw("res" + this.resultIndex++);
		final REPLVariable field = new REPLVariable(this, value.getPosition(), name, Types.UNKNOWN, modifierList, null);
		field.setValue(value);

		this.initMember(field);
		this.currentClass.getBody().addDataMember(field);
		this.members.add(field);
	}

	@Override
	public void addOperator(IOperator operator)
	{
		super.addOperator(operator);
		this.compiler.getOutput().println("Defined " + operator);
	}

	@Override
	public void addImport(ImportDeclaration declaration)
	{
		declaration.resolveTypes(this.markers, this);

		if (this.hasErrors())
		{
			return;
		}

		super.addImport(declaration);
		this.compiler.getOutput().println("Imported " + declaration.getImport());
	}

	@Override
	public void addUsing(ImportDeclaration usingDeclaration)
	{
		usingDeclaration.resolveTypes(this.markers, this);

		if (this.hasErrors())
		{
			return;
		}

		super.addUsing(usingDeclaration);
		this.compiler.getOutput().println("Imported " + usingDeclaration.getImport());
	}

	@Override
	public void addInclude(IncludeDeclaration includeDeclaration)
	{
		includeDeclaration.resolve(this.markers, this);

		if (this.hasErrors())
		{
			return;
		}

		super.addInclude(includeDeclaration);
		this.compiler.getOutput().println("Included " + includeDeclaration.getHeader().getFullName());
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
		typeAlias.cleanup(this, this.currentClass);

		super.addTypeAlias(typeAlias);
		this.compiler.getOutput().println(typeAlias);
	}

	@Override
	public void addClass(IClass iclass)
	{
		this.initMember(iclass);
		this.currentClass.getBody().addClass(iclass);
		this.members.add(iclass);
	}

	@Override
	public void addInnerClass(IClassCompilable iclass)
	{
		iclass.setInnerIndex(this.currentClass.getInternalName(), this.innerClassList.size());
		this.innerClassList.add(iclass);
	}

	@Override
	public REPLVariable createDataMember(ICodePosition position, Name name, IType type, ModifierSet modifiers,
		                                    AnnotationList annotations)
	{
		return new REPLVariable(this, position, name, type, modifiers, annotations);
	}

	@Override
	public void addDataMember(REPLVariable variable)
	{
		this.initMember(variable);
		this.currentClass.getBody().addDataMember(variable);
		this.members.add(variable);
	}

	@Override
	public void addProperty(IProperty property)
	{
		this.initMember(property);
		this.currentClass.getBody().addProperty(property);
		this.members.add(property);
	}

	@Override
	public void addMethod(IMethod method)
	{
		this.initMember(method);
		this.currentClass.getBody().addMethod(method);
		this.members.add(method);
	}

	private void registerMethod(IMethod method)
	{
		boolean replaced = false;
		int methods = this.methods.size();
		for (int i = 0; i < methods; i++)
		{
			if (this.methods.get(i).checkOverride(method, null))
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
		Util.methodSignatureToString(method, null, buf);
		this.compiler.getOutput().println(buf.append('\'').toString());
	}

	@Override
	public void addInitializer(IInitializer initializer)
	{
	}

	@Override
	public void addConstructor(IConstructor constructor)
	{
	}

	// region IContext overrides

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
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, IArguments arguments)
	{
		for (IMethod method : this.methods)
		{
			method.checkMatch(list, receiver, name, arguments);
		}

		if (name != null)
		{
			final Name removeEq = Util.removeEq(name);

			IProperty property = this.properties.get(removeEq);
			if (property != null)
			{
				property.checkMatch(list, receiver, name, arguments);
			}

			final IField field = this.fields.get(removeEq);
			if (field != null)
			{
				property = field.getProperty();
				if (property != null)
				{
					property.checkMatch(list, receiver, name, arguments);
				}
			}
		}

		if (!list.isEmpty() && name != null)
		{
			return;
		}

		super.getMethodMatches(list, receiver, name, arguments);
	}

	@Override
	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
		for (IMethod method : this.methods)
		{
			method.checkImplicitMatch(list, value, targetType);
		}

		if (!list.isEmpty())
		{
			return;
		}

		super.getImplicitMatches(list, value, targetType);
	}

	@Override
	public IType getReturnType()
	{
		return null;
	}

	@Override
	public byte checkException(IType type)
	{
		return TRUE;
	}

	@Override
	public IClass getThisClass()
	{
		return this.currentClass;
	}

	// endregion

	@Override
	public Name getName()
	{
		return this.currentClass.getName();
	}

	@Override
	public String getFullName()
	{
		return this.currentClass.getFullName();
	}

	@Override
	public String getFullName(Name name)
	{
		return "repl$classes." + name.qualified;
	}

	@Override
	public String getInternalName()
	{
		return this.currentClass.getInternalName();
	}

	@Override
	public String getInternalName(Name name)
	{
		return REPL$CLASSES + name.qualified;
	}
}
