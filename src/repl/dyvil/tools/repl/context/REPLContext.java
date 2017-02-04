package dyvil.tools.repl.context;

import dyvil.collection.List;
import dyvil.collection.Map;
import dyvil.collection.mutable.ArrayList;
import dyvil.collection.mutable.IdentityHashMap;
import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.classes.ClassBody;
import dyvil.tools.compiler.ast.classes.CodeClass;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constructor.IConstructor;
import dyvil.tools.compiler.ast.constructor.IInitializer;
import dyvil.tools.compiler.ast.consumer.IMemberConsumer;
import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.IDefaultContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.field.IProperty;
import dyvil.tools.compiler.ast.header.AbstractHeader;
import dyvil.tools.compiler.ast.header.ICompilable;
import dyvil.tools.compiler.ast.imports.ImportDeclaration;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.modifiers.BaseModifiers;
import dyvil.tools.compiler.ast.modifiers.ModifierList;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.operator.IOperator;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.alias.ITypeAlias;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;
import dyvil.tools.parsing.source.TextSource;
import dyvil.tools.repl.DyvilREPL;

public class REPLContext extends AbstractHeader
	implements IDefaultContext, IValueConsumer, IMemberConsumer<REPLVariable>
{
	private static final String CLASS_PACKAGE = "repl_classes";
	public static final  String CLASS_PREFIX  = "Result_";

	protected final DyvilREPL repl;

	// Persistent members

	private final Map<Name, IField>    fields     = new IdentityHashMap<>();
	private final Map<Name, IProperty> properties = new IdentityHashMap<>();
	private final List<IMethod>        methods    = new ArrayList<>();
	private final Map<Name, IClass>    classes    = new IdentityHashMap<>();

	// Updated for every input
	private int resultIndex;
	private int classIndex;
	protected TextSource currentSource = new TextSource();
	private CodeClass currentClass;

	// Cleared for every input
	protected final MarkerList        markers        = new MarkerList(Markers.INSTANCE);
	protected final List<ICompilable> innerClassList = new ArrayList<>();
	protected final List<IMember>     members        = new ArrayList<>();

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

	public Map<Name, IClass> getClasses()
	{
		return this.classes;
	}

	// Evaluation

	public void startEvaluation(String text)
	{
		final String className = CLASS_PREFIX + this.classIndex++;
		this.currentClass = new CodeClass(this, Name.fromRaw(className));
		this.currentClass.setBody(new ClassBody(this.currentClass));
		this.currentSource.read(text);
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

		for (IMember member : this.members)
		{
			this.processMember(member, theClass);
		}

		this.printErrors();
		this.cleanup();
	}

	private Class<?> compileAndLoad()
	{
		final REPLClassLoader classLoader = this.repl.getClassLoader();
		final List<Class<?>> classes = new ArrayList<>(this.innerClassList.size() + 1);

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

	private void processMember(IMember member, Class<?> theClass)
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
			this.classes.put(member.getName(), (IClass) member);
			break;
		}

		this.repl.getOutput().println(member);
	}

	private void processMethod(IMethod method)
	{
		int methods = this.methods.size();
		for (int i = 0; i < methods; i++)
		{
			if (this.methods.get(i).checkOverride(method, null))
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
		StringBuilder buf = new StringBuilder();
		this.markers.sort();
		for (Marker marker : this.markers)
		{
			marker.log(this.currentSource, buf, colors);
		}

		this.repl.getOutput().println(buf.toString());
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
		this.repl.getOutput().println("Defined " + operator);
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
		this.repl.getOutput().println("Imported " + component.getImport());
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
	public REPLVariable createDataMember(ICodePosition position, Name name, IType type, ModifierSet modifiers,
		                                    AnnotationList annotations)
	{
		return new REPLVariable(this, position, name, type, modifiers, annotations);
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

	@Override
	public DyvilCompiler getCompilationContext()
	{
		return this.repl.getCompiler();
	}

	@Override
	public IClass resolveClass(Name name)
	{
		return this.classes.get(name);
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		return this.fields.get(name);
	}

	@Override
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, IArguments arguments)
	{
		for (IMethod method : this.methods)
		{
			method.checkMatch(list, receiver, name, arguments);
		}

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
	public String getFullName(Name subClass)
	{
		return CLASS_PACKAGE + '.' + subClass.qualified;
	}

	@Override
	public String getInternalName()
	{
		return this.currentClass.getInternalName();
	}

	@Override
	public String getInternalName(Name subClass)
	{
		return CLASS_PACKAGE + '/' + subClass.qualified;
	}
}
