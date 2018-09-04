package dyvilx.tools.compiler.ast.classes;

import dyvil.annotation.internal.NonNull;
import dyvil.collection.List;
import dyvil.collection.immutable.ArrayList;
import dyvil.collection.iterator.ArrayIterator;
import dyvil.collection.iterator.FilterIterator;
import dyvil.lang.Name;
import dyvil.math.MathUtils;
import dyvil.reflect.Modifiers;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.constructor.IConstructor;
import dyvilx.tools.compiler.ast.constructor.IInitializer;
import dyvilx.tools.compiler.ast.consumer.IMemberConsumer;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.access.FieldAccess;
import dyvilx.tools.compiler.ast.field.EnumConstant;
import dyvilx.tools.compiler.ast.field.Field;
import dyvilx.tools.compiler.ast.field.IField;
import dyvilx.tools.compiler.ast.field.IProperty;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.parameter.ParameterList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.classes.ClassWriter;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.config.Formatting;
import dyvilx.tools.compiler.phase.Resolvable;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.ASTNode;
import dyvilx.tools.parsing.marker.MarkerList;

public class ClassBody implements ASTNode, Resolvable, IMemberConsumer<IField>
{
	private static class MethodLink
	{
		protected IMethod    method;
		protected MethodLink next;
		protected int        hash;

		public MethodLink(IMethod method, int hash)
		{
			this.method = method;
			this.hash = hash;
		}

		public MethodLink(IMethod method, int hash, MethodLink next)
		{
			this.method = method;
			this.hash = hash;
			this.next = next;
		}
	}

	protected final IClass enclosingClass;

	private ClassList classes = new ClassList(0);

	private IField[] fields = new IField[3];
	private int fieldCount;

	private IProperty[] properties;
	private int         propertyCount;

	private IMethod[] methods = new IMethod[3];
	private int methodCount;

	private IConstructor[] constructors = new IConstructor[1];
	private int constructorCount;

	private IInitializer[] initializers;
	private int            initializerCount;

	// Caches
	protected MethodLink[]  namedMethodCache;
	protected List<IMethod> implicitCache;

	public ClassBody(IClass iclass)
	{
		this.enclosingClass = iclass;
	}

	@Override
	public SourcePosition getPosition()
	{
		return null;
	}

	@Override
	public void setPosition(SourcePosition position)
	{
	}

	public IClass getEnclosingClass()
	{
		return this.enclosingClass;
	}

	// region Nested Classes

	public ClassList getClasses()
	{
		return this.classes;
	}

	@Override
	public void addClass(IClass iclass)
	{
		this.linkClass(iclass);
		this.classes.add(iclass);
	}

	private void linkClass(IClass iclass)
	{
		iclass.setEnclosingClass(this.enclosingClass);
		iclass.setHeader(this.enclosingClass.getHeader());
	}

	// endregion

	// region Fields

	@Override
	public boolean acceptEnums()
	{
		return this.getEnclosingClass().hasModifier(Modifiers.ENUM);
	}

	public Iterable<IField> fields()
	{
		return () -> new ArrayIterator<>(this.fields, 0, this.fieldCount);
	}

	public Iterable<IField> enumConstants()
	{
		return () -> new FilterIterator<>(new ArrayIterator<>(this.fields, 0, this.fieldCount), IField::isEnumConstant);
	}

	public int fieldCount()
	{
		return this.fieldCount;
	}

	public IField getField(int index)
	{
		return this.fields[index];
	}

	public IField getField(Name name)
	{
		for (int i = 0; i < this.fieldCount; i++)
		{
			final IField field = this.fields[i];
			if (field.getName() == name)
			{
				return field;
			}
		}
		return null;
	}

	public void addField(IField field)
	{
		field.setEnclosingClass(this.enclosingClass);

		final int index = this.fieldCount++;
		if (index >= this.fields.length)
		{
			IField[] temp = new IField[index * 2];
			System.arraycopy(this.fields, 0, temp, 0, index);
			this.fields = temp;
		}
		this.fields[index] = field;

		final IProperty property = field.getProperty();
		if (property != null)
		{
			this.addToCache(property);
		}

		if (!(field instanceof EnumConstant))
		{
			return;
		}

		final EnumConstant enumConst = (EnumConstant) field;

		// set enum constant index
		for (int i = index - 1; i >= 0; i--)
		{
			final IField fieldI = this.fields[i];
			if (fieldI instanceof EnumConstant)
			{
				enumConst.setIndex(((EnumConstant) fieldI).getIndex() + 1);
				return;
			}
		}

		enumConst.setIndex(0);
	}

	@Override
	public void addDataMember(IField field)
	{
		this.addField(field);
	}

	@Override
	public IField createDataMember(SourcePosition position, Name name, IType type, AttributeList attributes)
	{
		return new Field(this.enclosingClass, position, name, type, attributes);
	}

	public IValue resolveImplicit(IType type)
	{
		if (type == null)
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

		for (int i = 0; i < this.fieldCount; i++)
		{
			final IField field = this.fields[i];
			if (!field.isImplicit() || !Types.isSuperType(type, field.getType()))
			{
				continue;
			}
			if (candidate != null)
			{
				return null; // ambiguous
			}

			// this<Class> is added automatically later
			candidate = new FieldAccess(field);
		}

		return candidate;
	}

	// endregion

	// region Properties

	public Iterable<IProperty> properties()
	{
		return () -> new ArrayIterator<>(this.properties, 0, this.propertyCount);
	}

	public Iterable<IProperty> allProperties()
	{
		final ArrayList.Builder<IProperty> builder = new ArrayList.Builder<>(this.propertyCount + this.fieldCount);
		builder.addAll(this.properties());
		for (int i = 0; i < this.fieldCount; i++)
		{
			final IProperty property = this.fields[i].getProperty();
			if (property != null)
			{
				builder.add(property);
			}
		}
		return builder.build();
	}

	public int propertyCount()
	{
		return this.propertyCount;
	}

	public IProperty getProperty(int index)
	{
		return this.properties[index];
	}

	public IProperty getProperty(Name name)
	{
		for (int i = 0; i < this.propertyCount; i++)
		{
			IProperty p = this.properties[i];
			if (p.getName() == name)
			{
				return p;
			}
		}
		return null;
	}

	@Override
	public void addProperty(IProperty property)
	{
		property.setEnclosingClass(this.enclosingClass);

		final int index = this.propertyCount++;
		if (index == 0)
		{
			this.properties = new IProperty[3];
			this.properties[0] = property;
			return;
		}
		if (index >= this.properties.length)
		{
			IProperty[] temp = new IProperty[this.propertyCount];
			System.arraycopy(this.properties, 0, temp, 0, index);
			this.properties = temp;
		}
		this.properties[index] = property;

		this.addToCache(property);
	}

	// endregion

	// region Methods

	public Iterable<IMethod> methods()
	{
		return () -> new ArrayIterator<>(this.methods, 0, this.methodCount);
	}

	public Iterable<IMethod> allMethods()
	{
		final ArrayList.Builder<IMethod> builder = new ArrayList.Builder<>(
			this.methodCount + this.propertyCount * 2 + this.fieldCount);

		for (int i = 0; i < this.methodCount; i++)
		{
			builder.add(this.methods[i]);
		}
		for (int i = 0; i < this.propertyCount; i++)
		{
			addPropertyMethods(builder, this.properties[i]);
		}
		for (int i = 0; i < this.fieldCount; i++)
		{
			final IProperty prop = this.fields[i].getProperty();
			if (prop != null)
			{
				addPropertyMethods(builder, prop);
			}
		}
		return builder.build();
	}

	private static void addPropertyMethods(ArrayList.Builder<IMethod> builder, IProperty prop)
	{
		final IMethod getter = prop.getGetter();
		if (getter != null)
		{
			builder.add(getter);
		}

		final IMethod setter = prop.getSetter();
		if (setter != null)
		{
			builder.add(setter);
		}
	}

	public int methodCount()
	{
		return this.methodCount;
	}

	public IMethod getMethod(int index)
	{
		return this.methods[index];
	}

	public IMethod getMethod(Name name)
	{
		for (int i = 0; i < this.methodCount; i++)
		{
			final IMethod method = this.methods[i];
			if (method.getName() == name)
			{
				return method;
			}
		}
		return null;
	}

	@Override
	public void addMethod(IMethod method)
	{
		method.setEnclosingClass(this.enclosingClass);

		final int index = this.methodCount++;
		if (index >= this.methods.length)
		{
			final IMethod[] temp = new IMethod[index * 2];
			System.arraycopy(this.methods, 0, temp, 0, index);
			this.methods = temp;
		}
		this.methods[index] = method;

		this.addToCache(method);
	}

	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, ArgumentList arguments)
	{
		if (name == null)
		{
			for (IMethod method : this.methods())
			{
				method.checkMatch(list, receiver, null, arguments);
			}
			for (IProperty property : this.allProperties())
			{
				property.checkMatch(list, receiver, null, arguments);
			}

			return;
		}

		final MethodLink[] cache = this.getNamedMethodCache();
		if (cache.length == 0)
		{
			// no methods
			return;
		}

		final int hash = hash(name);
		final int index = hash & (cache.length - 1);
		for (MethodLink link = cache[index]; link != null; link = link.next)
		{
			if (link.hash == hash)
			{
				link.method.checkMatch(list, receiver, name, arguments);
			}
		}

		return;
	}

	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
		for (IMethod method : this.getImplicitMethodCache())
		{
			method.checkImplicitMatch(list, value, targetType);
		}
	}

	public boolean checkImplements(IMethod candidate, ITypeContext typeContext)
	{
		boolean result = false;

		final MethodLink[] cache = this.getNamedMethodCache();
		if (cache.length == 0)
		{
			// no methods
			return false;
		}

		final int hash = hash(candidate.getName());
		final int index = hash & (cache.length - 1);

		for (MethodLink link = cache[index]; link != null; link = link.next)
		{
			if (link.hash == hash && checkMethodImplements(link.method, candidate, typeContext))
			{
				result = true;
			}
		}

		return result;
	}

	public static boolean checkMethodImplements(IMethod method, IMethod candidate, ITypeContext typeContext)
	{
		if (method.overrides(candidate, typeContext))
		{
			method.addOverride(candidate);
			return !method.hasModifier(Modifiers.ABSTRACT);
		}
		return false;
	}

	public static boolean checkPropertyImplements(IProperty property, IMethod candidate, ITypeContext typeContext)
	{
		final IMethod getter = property.getGetter();
		final IMethod setter = property.getSetter();

		return getter != null && checkMethodImplements(getter, candidate, typeContext)
		       || setter != null && checkMethodImplements(setter, candidate, typeContext);
	}

	public void checkMethods(MarkerList markers, IClass checkedClass, ITypeContext typeContext)
	{
		for (int i = 0; i < this.methodCount; i++)
		{
			checkMethod(this.methods[i], markers, checkedClass, typeContext);
		}
		for (int i = 0; i < this.propertyCount; i++)
		{
			checkProperty(this.properties[i], markers, checkedClass, typeContext);
		}
		for (int i = 0; i < this.fieldCount; i++)
		{
			final IProperty property = this.fields[i].getProperty();
			if (property != null)
			{
				checkProperty(property, markers, checkedClass, typeContext);
			}
		}
	}

	public static void checkMethod(IMethod method, MarkerList markers, IClass checkedClass, ITypeContext typeContext)
	{
		if (method.hasModifier(Modifiers.STATIC_FINAL))
		{
			// Don't check static final methods
			return;
		}

		// Check if the super class implements the method
		// We cannot do the modifier checks beforehand, because methods need to know which methods they implement to
		// generate bridges.
		if (checkedClass.checkImplements(method, typeContext))
		{
			return;
		}

		// If it doesn't, check for abstract modifiers
		if (method.hasModifier(Modifiers.ABSTRACT) && !checkedClass.hasModifier(Modifiers.ABSTRACT))
		{
			// Create an abstract method error
			markers.add(Markers.semantic(checkedClass.getPosition(), "class.method.abstract", checkedClass.getName(),
			                             method.getName(), method.getEnclosingClass().getName()));
		}
	}

	public static void checkProperty(IProperty property, MarkerList markers, IClass checkedClass,
		ITypeContext typeContext)
	{
		final IMethod getter = property.getGetter();
		if (getter != null)
		{
			checkMethod(getter, markers, checkedClass, typeContext);
		}

		final IMethod setter = property.getSetter();
		if (setter != null)
		{
			checkMethod(setter, markers, checkedClass, typeContext);
		}
	}

	// Cache

	public MethodLink[] getNamedMethodCache()
	{
		if (this.namedMethodCache != null)
		{
			return this.namedMethodCache;
		}

		/*
		 * The cache size is calculated as follows: We sum the number of methods assuming they all have unique names, add
		 * twice the amount of properties with the same assumption and half the amount of fields, assuming there are
		 * as many property getters and setters as there are fields. At the end, we compute the next power of two that
		 * is larger than our sum, and use it as the cache size.
		 */
		final int cacheSize = MathUtils.nextPowerOf2(this.methodCount + (this.propertyCount << 1) + this.fieldCount);
		final int mask = cacheSize - 1;
		this.namedMethodCache = new MethodLink[cacheSize];

		for (IMethod method : this.allMethods())
		{
			addToCache(this.namedMethodCache, method, mask);
		}

		return this.namedMethodCache;
	}

	public List<IMethod> getImplicitMethodCache()
	{
		if (this.implicitCache != null)
		{
			return this.implicitCache;
		}

		this.implicitCache = ((List<IMethod>) this.allMethods()).filtered(IMethod::isImplicitConversion);

		return this.implicitCache;
	}

	private void addToCache(IProperty property)
	{
		final IMethod getter = property.getGetter();
		if (getter != null)
		{
			this.addToCache(getter);
		}
		final IMethod setter = property.getSetter();
		if (setter != null)
		{
			this.addToCache(setter);
		}
	}

	private void addToCache(IMethod method)
	{
		if (this.namedMethodCache != null)
		{
			final int cacheSize = this.namedMethodCache.length;
			if (cacheSize > 0)
			{
				addToCache(this.namedMethodCache, method, cacheSize - 1);
			}
			else
			{
				this.namedMethodCache = null; // force cache rebuild later
			}
		}
		if (this.implicitCache != null && method.isImplicitConversion())
		{
			this.implicitCache.add(method);
		}
	}

	private static void addToCache(MethodLink[] cache, IMethod method, int mask)
	{
		final int hash = hash(method.getName());
		final int index = hash & mask;
		cache[index] = new MethodLink(method, hash, cache[index]);
	}

	private static int hash(Name name)
	{
		return name.unqualified.hashCode();
	}

	// endregion

	// region Constructors

	public Iterable<IConstructor> constructors()
	{
		return () -> new ArrayIterator<>(this.constructors, 0, this.constructorCount);
	}

	public int constructorCount()
	{
		return this.constructorCount;
	}

	public IConstructor getConstructor(int index)
	{
		return this.constructors[index];
	}

	public IConstructor getConstructor(ParameterList parameters)
	{
		for (int i = 0; i < this.constructorCount; i++)
		{
			final IConstructor constructor = this.constructors[i];
			final ParameterList constructorParameterList = constructor.getParameters();

			if (parameters.matches(constructorParameterList))
			{
				return constructor;
			}
		}

		return null;
	}

	@Override
	public void addConstructor(IConstructor constructor)
	{
		constructor.setEnclosingClass(this.enclosingClass);

		final int index = this.constructorCount++;
		if (index >= this.constructors.length)
		{
			IConstructor[] temp = new IConstructor[index + 1];
			System.arraycopy(this.constructors, 0, temp, 0, index);
			this.constructors = temp;
		}
		this.constructors[index] = constructor;
	}

	public void getConstructorMatches(MatchList<IConstructor> list, ArgumentList arguments)
	{
		for (int i = 0; i < this.constructorCount; i++)
		{
			this.constructors[i].checkMatch(list, arguments);
		}
	}

	// endregion

	// region Initializers

	public Iterable<IInitializer> initializers()
	{
		return () -> new ArrayIterator<>(this.initializers, 0, this.initializerCount);
	}

	public int initializerCount()
	{
		return this.initializerCount;
	}

	public IInitializer getInitializer(int index)
	{
		return this.initializers[index];
	}

	@Override
	public void addInitializer(IInitializer initializer)
	{
		initializer.setEnclosingClass(this.enclosingClass);

		final int index = this.initializerCount++;
		if (index == 0)
		{
			this.initializers = new IInitializer[2];
			this.initializers[0] = initializer;
			return;
		}
		if (index >= this.initializers.length)
		{
			final IInitializer[] temp = new IInitializer[index + 1];
			System.arraycopy(this.initializers, 0, temp, 0, index);
			this.initializers = temp;
		}
		this.initializers[index] = initializer;
	}

	// endregion

	// region Phases

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.classes.forEach(this::linkClass);
		this.classes.resolveTypes(markers, context);

		for (int i = 0; i < this.fieldCount; i++)
		{
			this.fields[i].resolveTypes(markers, context);
		}
		for (int i = 0; i < this.propertyCount; i++)
		{
			this.properties[i].resolveTypes(markers, context);
		}
		for (int i = 0; i < this.methodCount; i++)
		{
			this.methods[i].resolveTypes(markers, context);
		}
		for (int i = 0; i < this.constructorCount; i++)
		{
			this.constructors[i].resolveTypes(markers, context);
		}
		for (int i = 0; i < this.initializerCount; i++)
		{
			this.initializers[i].resolveTypes(markers, context);
		}
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		this.classes.resolve(markers, context);

		for (int i = 0; i < this.fieldCount; i++)
		{
			this.fields[i].resolve(markers, context);
		}
		for (int i = 0; i < this.propertyCount; i++)
		{
			this.properties[i].resolve(markers, context);
		}
		for (int i = 0; i < this.methodCount; i++)
		{
			this.methods[i].resolve(markers, context);
		}
		for (int i = 0; i < this.constructorCount; i++)
		{
			this.constructors[i].resolve(markers, context);
		}
		for (int i = 0; i < this.initializerCount; i++)
		{
			this.initializers[i].resolve(markers, context);
		}
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.classes.checkTypes(markers, context);

		for (int i = 0; i < this.fieldCount; i++)
		{
			this.fields[i].checkTypes(markers, context);
		}
		for (int i = 0; i < this.propertyCount; i++)
		{
			this.properties[i].checkTypes(markers, context);
		}
		for (int i = 0; i < this.methodCount; i++)
		{
			this.methods[i].checkTypes(markers, context);
		}
		for (int i = 0; i < this.constructorCount; i++)
		{
			this.constructors[i].checkTypes(markers, context);
		}
		for (int i = 0; i < this.initializerCount; i++)
		{
			this.initializers[i].checkTypes(markers, context);
		}
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.classes.check(markers, context);

		for (int i = 0; i < this.fieldCount; i++)
		{
			this.fields[i].check(markers, context);
		}
		for (int i = 0; i < this.propertyCount; i++)
		{
			this.properties[i].check(markers, context);
		}
		for (int i = 0; i < this.methodCount; i++)
		{
			this.methods[i].check(markers, context);
		}
		for (int i = 0; i < this.constructorCount; i++)
		{
			this.constructors[i].check(markers, context);
		}
		for (int i = 0; i < this.initializerCount; i++)
		{
			this.initializers[i].check(markers, context);
		}
	}

	@Override
	public void foldConstants()
	{
		this.classes.foldConstants();

		for (int i = 0; i < this.fieldCount; i++)
		{
			this.fields[i].foldConstants();
		}
		for (int i = 0; i < this.propertyCount; i++)
		{
			this.properties[i].foldConstants();
		}
		for (int i = 0; i < this.methodCount; i++)
		{
			this.methods[i].foldConstants();
		}
		for (int i = 0; i < this.constructorCount; i++)
		{
			this.constructors[i].foldConstants();
		}
		for (int i = 0; i < this.initializerCount; i++)
		{
			this.initializers[i].foldConstants();
		}
	}

	@Override
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		this.classes.forEach(compilableList::addCompilable);
		this.classes.cleanup(compilableList, classCompilableList);

		for (int i = 0; i < this.fieldCount; i++)
		{
			this.fields[i].cleanup(compilableList, classCompilableList);
		}
		for (int i = 0; i < this.propertyCount; i++)
		{
			this.properties[i].cleanup(compilableList, classCompilableList);
		}
		for (int i = 0; i < this.methodCount; i++)
		{
			this.methods[i].cleanup(compilableList, classCompilableList);
		}
		for (int i = 0; i < this.constructorCount; i++)
		{
			this.constructors[i].cleanup(compilableList, classCompilableList);
		}
		for (int i = 0; i < this.initializerCount; i++)
		{
			this.initializers[i].cleanup(compilableList, classCompilableList);
		}
	}

	// endregion

	// region Compilation

	public void write(ClassWriter writer) throws BytecodeException
	{
		for (IClass iclass : this.classes)
		{
			iclass.writeInnerClassInfo(writer);
		}

		for (int i = 0; i < this.fieldCount; i++)
		{
			this.fields[i].write(writer);
		}

		for (int i = 0; i < this.constructorCount; i++)
		{
			this.constructors[i].write(writer);
		}

		for (int i = 0; i < this.propertyCount; i++)
		{
			this.properties[i].write(writer);
		}

		for (int i = 0; i < this.methodCount; i++)
		{
			this.methods[i].write(writer);
		}
	}

	public void writeClassInit(MethodWriter writer) throws BytecodeException
	{
		for (int i = 0, count = this.fieldCount; i < count; i++)
		{
			this.fields[i].writeClassInit(writer);
		}
		for (int i = 0, count = this.propertyCount; i < count; i++)
		{
			this.properties[i].writeClassInit(writer);
		}
		for (int i = 0, count = this.initializerCount; i < count; i++)
		{
			this.initializers[i].writeClassInit(writer);
		}
	}

	public void writeStaticInit(MethodWriter writer) throws BytecodeException
	{
		for (int i = 0, count = this.fieldCount; i < count; i++)
		{
			this.fields[i].writeStaticInit(writer);
		}
		for (int i = 0, count = this.propertyCount; i < count; i++)
		{
			this.properties[i].writeStaticInit(writer);
		}
		for (int i = 0, count = this.initializerCount; i < count; i++)
		{
			this.initializers[i].writeStaticInit(writer);
		}
	}

	// endregion

	@Override
	public void toString(@NonNull String prefix, @NonNull StringBuilder buffer)
	{
		final String bodyPrefix = Formatting.getIndent("class.body.indent", prefix);
		if (Formatting.getBoolean("class.body.open_bracket.newline"))
		{
			buffer.append('\n').append(prefix);
		}
		else
		{
			buffer.append(' ');
		}

		buffer.append('{').append('\n').append(bodyPrefix);
		this.bodyToString(bodyPrefix, buffer);

		// Trim extra lines

		final int length = buffer.length();
		final int index = length - bodyPrefix.length();
		if (buffer.indexOf(bodyPrefix, index) == index)
		{
			buffer.delete(index - 1, length);
			buffer.append(prefix);
		}

		buffer.append('}');
	}

	private void bodyToString(String prefix, StringBuilder buffer)
	{
		this.classes.toString(prefix, buffer);

		if (this.fieldCount > 0)
		{
			for (int i = 0; i < this.fieldCount; i++)
			{
				this.fields[i].toString(prefix, buffer);

				if (Formatting.getBoolean("field.declaration.semicolon"))
				{
					buffer.append(';');
				}

				buffer.append('\n').append(prefix);
			}
			buffer.append('\n').append(prefix);
		}

		if (this.constructorCount > 0)
		{
			this.membersToString(prefix, this.constructors, this.constructorCount, buffer);
		}

		if (this.initializerCount > 0)
		{
			this.membersToString(prefix, this.initializers, this.initializerCount, buffer);
		}

		if (this.propertyCount > 0)
		{
			this.membersToString(prefix, this.properties, this.propertyCount, buffer);
		}

		if (this.methodCount > 0)
		{
			this.membersToString(prefix, this.methods, this.methodCount, buffer);
		}
	}

	private void membersToString(String prefix, ASTNode[] members, int count, StringBuilder buffer)
	{
		for (int i = 0; i < count; i++)
		{
			members[i].toString(prefix, buffer);
			buffer.append('\n').append('\n').append(prefix);
		}
	}
}
