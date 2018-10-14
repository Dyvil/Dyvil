package dyvilx.tools.repl.context;

import dyvil.collection.List;
import dyvil.collection.Map;
import dyvil.collection.mutable.ArrayList;
import dyvil.collection.mutable.IdentityHashMap;
import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvil.source.TextSource;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.DyvilCompiler;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.classes.ClassBody;
import dyvilx.tools.compiler.ast.classes.CodeClass;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.constructor.IConstructor;
import dyvilx.tools.compiler.ast.constructor.IInitializer;
import dyvilx.tools.compiler.ast.consumer.IMemberConsumer;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IDefaultContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.access.FieldAccess;
import dyvilx.tools.compiler.ast.expression.operator.IOperator;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.field.IField;
import dyvilx.tools.compiler.ast.field.IProperty;
import dyvilx.tools.compiler.ast.header.AbstractHeader;
import dyvilx.tools.compiler.ast.header.ICompilable;
import dyvilx.tools.compiler.ast.imports.ImportDeclaration;
import dyvilx.tools.compiler.ast.member.ClassMember;
import dyvilx.tools.compiler.ast.member.Member;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.alias.ITypeAlias;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.compiler.util.Util;
import dyvilx.tools.parsing.marker.MarkerList;
import dyvilx.tools.repl.DyvilREPL;

import java.io.DataInput;
import java.io.DataOutput;

public class REPLContext extends AbstractHeader
	implements IDefaultContext, IMemberConsumer<REPLVariable>
{
	private static final String CLASS_PACKAGE = "replgen";
	public static final  String CLASS_PREFIX  = "Result";

	protected final DyvilREPL repl;

	// Persistent members

	private final Map<Name, IField>    fields     = new IdentityHashMap<>();
	private final Map<Name, IProperty> properties = new IdentityHashMap<>();
	private final List<IMethod>        methods    = new ArrayList<>();

	// Updated for every input
	private   int        resultIndex;
	private   int        classIndex;
	protected TextSource currentSource;
	private   CodeClass  currentClass;

	// Cleared for every input
	protected final MarkerList        markers        = new MarkerList(Markers.INSTANCE);
	protected final List<ICompilable> innerClassList = new ArrayList<>();
	protected final List<Member>      members        = new ArrayList<>();

	public REPLContext(DyvilREPL repl)
	{
		super(Name.fromRaw("REPL"));
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

	// Evaluation

	public void startEvaluation(String text)
	{
		final String className = CLASS_PREFIX + this.classIndex++;
		this.currentClass = new CodeClass(this, Name.fromRaw(className));
		this.currentClass.setBody(new ClassBody(this.currentClass));
		this.currentSource = new TextSource(text);
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
		final IContext context = this.getContext();

		this.currentClass.resolveTypes(this.markers, context);
		this.currentClass.resolve(this.markers, context);
		this.currentClass.checkTypes(this.markers, context);
		this.currentClass.check(this.markers, context);

		if (this.markers.getErrors() > 0)
		{
			this.printErrors();
			this.cleanup();
			return;
		}

		for (int i = this.getCompilationContext().config.getConstantFolding() - 1; i >= 0; i--)
		{
			this.currentClass.foldConstants();
		}

		this.currentClass.cleanup(this, this.currentClass);

		final Class<?> theClass = this.compileAndLoad();

		for (Member member : this.members)
		{
			this.processMember(member, theClass);
		}

		this.printErrors();
		this.cleanup();
	}

	private Class<?> compileAndLoad()
	{
		final REPLClassLoader classLoader = this.repl.getClassLoader();

		// Compile all inner class
		for (ICompilable innerClass : this.innerClassList)
		{
			classLoader.register(innerClass);
		}
		classLoader.register(this.currentClass);

		for (ICompilable innerClass : this.innerClassList)
		{
			classLoader.initialize(innerClass);
		}
		return classLoader.initialize(this.currentClass);
	}

	private void processMember(Member member, Class<?> theClass)
	{
		switch (member.getKind())
		{
		case FIELD:
			if (Types.isVoid(member.getType()))
			{
				// Don't register the variable
				return;
			}

			if (member instanceof REPLVariable)
			{
				final REPLVariable replVariable = (REPLVariable) member;
				replVariable.setRuntimeClass(theClass);
				replVariable.updateValue(this.repl);
			}

			this.fields.put(member.getName(), (IField) member);
			break;
		case METHOD:
			this.processMethod((IMethod) member);
			break;
		case PROPERTY:
			this.properties.put(member.getName(), (IProperty) member);
			break;
		case CLASS:
		case INTERFACE:
		case TRAIT:
		case ANNOTATION:
		case ENUM:
		case OBJECT:
			this.classes.add((IClass) member);
			break;
		}

		this.printColored(member.toString());
	}

	private void printColored(String colorize)
	{
		if (this.repl.getCompiler().config.useAnsiColors())
		{
			this.repl.getOutput().println(Colorizer.colorize(colorize, this));
		}
		else
		{
			this.repl.getOutput().println(colorize);
		}
	}

	private void processMethod(IMethod method)
	{
		int methods = this.methods.size();
		for (int i = 0; i < methods; i++)
		{
			if (this.methods.get(i).overrides(method, null))
			{
				this.methods.set(i, method);
				return;
			}
		}
		this.methods.add(method);
	}

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

		boolean colors = this.getCompilationContext().config.useAnsiColors();
		StringBuilder buffer = new StringBuilder();
		this.markers.log(this.currentSource, buffer, colors);

		this.repl.getOutput().println(buffer.toString());
	}

	private void initMember(ClassMember member)
	{
		member.setEnclosingClass(this.currentClass);

		final AttributeList attributes = member.getAttributes();

		// ensure public unless another visibility modifier is present
		if (!attributes.hasAnyFlag(Modifiers.VISIBILITY_MODIFIERS))
		{
			attributes.addFlag(Modifiers.PUBLIC);
		}

		// ensure static unless it's an extension method
		if (!attributes.hasFlag(Modifiers.EXTENSION))
		{
			attributes.addFlag(Modifiers.STATIC);
		}
	}

	public void addValue(IValue value)
	{
		final Name name = Name.fromRaw("res" + this.resultIndex++);
		final REPLVariable field = new REPLVariable(this, name, value);

		this.initMember(field);
		this.currentClass.getBody().addDataMember(field);
		this.members.add(field);
	}

	@Override
	public void addOperator(IOperator operator)
	{
		super.addOperator(operator);
		this.printColored(operator.toString());
	}

	@Override
	public void addImport(ImportDeclaration component)
	{
		component.resolveTypes(this.markers, this);
		component.resolve(this.markers, this);

		if (this.hasErrors())
		{
			return;
		}

		super.addImport(component);
		this.printColored(component.toString());
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
		this.repl.getOutput().println(typeAlias);
	}

	@Override
	public void addClass(IClass iclass)
	{
		this.initMember(iclass);
		this.currentClass.getBody().addClass(iclass);
		this.members.add(iclass);
	}

	@Override
	public void addCompilable(ICompilable compilable)
	{
		this.innerClassList.add(compilable);
	}

	@Override
	public REPLVariable createDataMember(SourcePosition position, Name name, IType type, AttributeList attributes)
	{
		return new REPLVariable(this, position, name, type, attributes);
	}

	@Override
	public void addDataMember(REPLVariable field)
	{
		this.initMember(field);
		this.currentClass.getBody().addDataMember(field);
		this.members.add(field);
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

	@Override
	public void addInitializer(IInitializer initializer)
	{
	}

	@Override
	public void addConstructor(IConstructor constructor)
	{
	}

	// region IContext overrides

	public boolean isMember(Name name)
	{
		if (this.resolveField(name) != null || this.resolveClass(name) != null || this.properties.get(name) != null)
		{
			return true;
		}

		for (IMethod method : this.methods)
		{
			if (method.getName() == name)
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public DyvilCompiler getCompilationContext()
	{
		return this.repl.getCompiler();
	}

	@Override
	public IClass resolveClass(Name name)
	{
		// iterate backwards to use youngest definition
		for (int i = this.classes.size() - 1; i >= 0; i--)
		{
			final IClass iclass = this.classes.get(i);
			if (iclass.getName() == name)
			{
				return iclass;
			}
		}
		return null;
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		return this.fields.get(name);
	}

	@Override
	public IValue resolveImplicit(IType type)
	{
		if (type == null || this.fields.isEmpty())
		{
			return null;
		}

		IValue candidate = null;

		for (IClass iclass : this.classes)
		{
			if (!iclass.isImplicit() || !iclass.isObject() || !Types.isSuperType(type, iclass.getClassType()))
			{
				continue;
			}
			if (candidate != null)
			{
				return null; // ambiguous
			}
			candidate = new FieldAccess(iclass.getMetadata().getInstanceField());
		}

		for (IField field : this.fields.values())
		{
			if (!field.isImplicit() || !Types.isSuperType(type, field.getType()))
			{
				continue;
			}
			if (candidate != null)
			{
				return null; // ambiguous
			}
			candidate = new FieldAccess(field);
		}

		return candidate;
	}

	@Override
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, ArgumentList arguments)
	{
		for (IMethod method : this.methods)
		{
			method.checkMatch(list, receiver, name, arguments);
		}

		// here normal order is ok, since all extension classes are considered
		this.classes.getExtensionMethodMatches(list, receiver, name, arguments);

		if (name == null)
		{
			return;
		}

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

	@Override
	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
		for (IMethod method : this.methods)
		{
			method.checkImplicitMatch(list, value, targetType);
		}

		// here normal order is ok, since all extension classes are considered
		this.classes.getExtensionImplicitMatches(list, value, targetType);
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
	public String getInternalName()
	{
		return this.currentClass.getInternalName();
	}

	// --------------- Header Compilation ---------------

	@Override
	public void read(DataInput in)
	{
	}

	@Override
	public void write(DataOutput out)
	{
	}
}
