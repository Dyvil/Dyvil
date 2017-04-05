package dyvil.tools.compiler.ast.classes;

import dyvil.annotation.internal.NonNull;
import dyvil.math.MathUtils;
import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.constructor.IConstructor;
import dyvil.tools.compiler.ast.constructor.IInitializer;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.Field;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.field.IProperty;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.header.IHeaderUnit;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.parameter.ParameterList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class ClassBody implements IClassBody
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

	public IClass enclosingClass;

	public IClass[] classes;
	public int      classCount;

	private IField[] fields = new IField[3];
	private int fieldCount;

	private IProperty[] properties;
	private int         propertyCount;

	private IMethod[] methods = new IMethod[3];
	private int methodCount;

	private IConstructor[] constructors = new IConstructor[1];
	private int            constructorCount;
	private IInitializer[] initializers;
	private int            initializerCount;

	// Caches
	protected MethodLink[] namedMethodCache;
	protected IMethod[]    implicitCache;
	protected IMethod      functionalMethod;

	public ClassBody(IClass iclass)
	{
		this.enclosingClass = iclass;
	}

	@Override
	public ICodePosition getPosition()
	{
		return null;
	}

	@Override
	public void setPosition(ICodePosition position)
	{
	}

	@Override
	public IClass getEnclosingClass()
	{
		return this.enclosingClass;
	}

	@Override
	public void setEnclosingClass(IClass enclosingClass)
	{
		this.enclosingClass = enclosingClass;
	}

	// Nested Classes

	@Override
	public int classCount()
	{
		return this.classCount;
	}

	@Override
	public void addClass(IClass iclass)
	{
		iclass.setEnclosingClass(this.enclosingClass);

		if (this.classes == null)
		{
			this.classes = new IClass[2];
			this.classes[0] = iclass;
			this.classCount = 1;
			return;
		}

		int index = this.classCount++;
		if (index >= this.classes.length)
		{
			IClass[] temp = new IClass[this.classCount];
			System.arraycopy(this.classes, 0, temp, 0, index);
			this.classes = temp;
		}
		this.classes[index] = iclass;
	}

	@Override
	public IClass getClass(int index)
	{
		return this.classes[index];
	}

	@Override
	public IClass getClass(Name name)
	{
		for (int i = 0; i < this.classCount; i++)
		{
			final IClass iclass = this.classes[i];
			if (iclass.getName() == name)
			{
				return iclass;
			}
		}
		return null;
	}

	// Fields

	@Override
	public int fieldCount()
	{
		return this.fieldCount;
	}

	@Override
	public void addDataMember(IField field)
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
	}

	@Override
	public IField createDataMember(ICodePosition position, Name name, IType type, ModifierSet modifiers,
		                              AnnotationList annotations)
	{
		return new Field(this.enclosingClass, position, name, type, modifiers, annotations);
	}

	@Override
	public IField getField(int index)
	{
		return this.fields[index];
	}

	@Override
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

	// Properties

	@Override
	public int propertyCount()
	{
		return this.propertyCount;
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
	}

	@Override
	public IProperty getProperty(int index)
	{
		return this.properties[index];
	}

	@Override
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

	// Methods

	@Override
	public int methodCount()
	{
		return this.methodCount;
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
	}

	@Override
	public IMethod getMethod(int index)
	{
		return this.methods[index];
	}

	@Override
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
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, ArgumentList arguments)
	{
		final int cacheSize = this.namedMethodCache.length;
		if (cacheSize == 0)
		{
			return;
		}

		if (name != null)
		{
			final int hash = hash(name);
			final int index = hash & (cacheSize - 1);
			for (MethodLink link = this.namedMethodCache[index]; link != null; link = link.next)
			{
				if (link.hash == hash)
				{
					link.method.checkMatch(list, receiver, name, arguments);
				}
			}

			return;
		}

		for (int i = 0; i < this.methodCount; i++)
		{
			this.methods[i].checkMatch(list, receiver, null, arguments);
		}
		for (int i = 0; i < this.propertyCount; i++)
		{
			this.properties[i].checkMatch(list, receiver, null, arguments);
		}
		for (int i = 0; i < this.fieldCount; i++)
		{
			final IProperty property = this.fields[i].getProperty();
			if (property != null)
			{
				property.checkMatch(list, receiver, null, arguments);
			}
		}
	}

	@Override
	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
		if (this.implicitCache == null)
		{
			return;
		}

		for (IMethod method : this.implicitCache)
		{
			method.checkImplicitMatch(list, value, targetType);
		}
	}

	@Override
	public IMethod getFunctionalMethod()
	{
		return this.functionalMethod;
	}

	@Override
	public void setFunctionalMethod(IMethod method)
	{
		this.functionalMethod = method;
	}

	@Override
	public boolean checkImplements(IMethod candidate, ITypeContext typeContext)
	{
		final int cacheSize = this.namedMethodCache.length;
		if (cacheSize == 0)
		{
			return false;
		}

		final int hash = hash(candidate.getName());
		final int index = hash & (cacheSize - 1);
		boolean result = false;

		for (MethodLink link = this.namedMethodCache[index]; link != null; link = link.next)
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

	@Override
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

	// Constructors

	@Override
	public int constructorCount()
	{
		return this.constructorCount;
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

	@Override
	public IConstructor getConstructor(int index)
	{
		return this.constructors[index];
	}

	@Override
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
	public void getConstructorMatches(MatchList<IConstructor> list, ArgumentList arguments)
	{
		for (int i = 0; i < this.constructorCount; i++)
		{
			this.constructors[i].checkMatch(list, arguments);
		}
	}

	// Initializers

	@Override
	public int initializerCount()
	{
		return this.initializerCount;
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

	@Override
	public IInitializer getInitializer(int index)
	{
		return this.initializers[index];
	}

	// Phases

	@Override
	public void initExternalMethodCache()
	{
		final int cacheSize = MathUtils.powerOfTwo(this.methodCount);
		final int mask = cacheSize - 1;
		final MethodLink[] cache = this.namedMethodCache = new MethodLink[cacheSize];

		// External Classes do not have any properties or fields with properties
		for (int i = 0; i < this.methodCount; i++)
		{
			addMethod(cache, this.methods[i], mask);
		}
	}

	@Override
	public void initExternalImplicitCache()
	{
		IMethod[] implicitCache = null;
		int implicitCount = 0;

		for (int i = 0; i < this.methodCount; i++)
		{
			final IMethod method = this.methods[i];

			// Add method to implicit cache
			if (method.isImplicitConversion())
			{
				if (implicitCache == null)
				{
					implicitCache = new IMethod[this.methodCount];
				}

				implicitCache[implicitCount++] = method;
			}
		}

		if (implicitCount > 0)
		{
			this.initImplicitCache(implicitCount, implicitCache);
		}
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		final IHeaderUnit header = this.enclosingClass.getHeader();

		/*
		 * The cache size is calculated as follows: We sum the number of methods assuming they all have unique names, add
		 * twice the amount of properties with the same assumption and half the amount of fields, assuming there are
		 * as many property getters and setters as there are fields. At the end, we compute the next power of two that
		 * is larger than our sum, and use it as the cache size.
		 */
		final int cacheSize = MathUtils.powerOfTwo(this.methodCount + (this.propertyCount << 1) + this.fieldCount);
		final int mask = cacheSize - 1;
		final MethodLink[] cache = this.namedMethodCache = new MethodLink[cacheSize];

		for (int i = 0; i < this.classCount; i++)
		{
			final IClass innerClass = this.classes[i];
			innerClass.setHeader(header);
			innerClass.resolveTypes(markers, context);
		}

		for (int i = 0; i < this.fieldCount; i++)
		{
			final IField field = this.fields[i];
			field.resolveTypes(markers, context);

			final IProperty property = field.getProperty();
			if (property != null)
			{
				addProperty(cache, property, mask);
			}
		}

		for (int i = 0; i < this.propertyCount; i++)
		{
			final IProperty property = this.properties[i];
			property.resolveTypes(markers, context);
			addProperty(cache, property, mask);
		}

		int implicitCount = 0;
		IMethod[] implicitCache = null;
		for (int i = 0; i < this.methodCount; i++)
		{
			final IMethod method = this.methods[i];
			method.resolveTypes(markers, context);
			addMethod(cache, method, mask);

			// Add method to implicit cache
			if (method.isImplicitConversion())
			{
				if (implicitCache == null)
				{
					implicitCache = new IMethod[this.methodCount];
				}
				implicitCache[implicitCount++] = method;
			}
		}
		if (implicitCount > 0)
		{
			this.initImplicitCache(implicitCount, implicitCache);
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

	private void initImplicitCache(int implicitCount, IMethod[] implicitCache)
	{
		this.implicitCache = new IMethod[implicitCount];
		System.arraycopy(implicitCache, 0, this.implicitCache, 0, implicitCount);
	}

	private static void addProperty(MethodLink[] cache, IProperty property, int mask)
	{
		final IMethod getter = property.getGetter();
		if (getter != null)
		{
			addMethod(cache, getter, mask);
		}

		final IMethod setter = property.getSetter();
		if (setter != null)
		{
			addMethod(cache, setter, mask);
		}
	}

	private static void addMethod(MethodLink[] cache, IMethod method, int mask)
	{
		final int hash = hash(method.getName());
		final int index = hash & mask;
		cache[index] = new MethodLink(method, hash, cache[index]);
	}

	private static int hash(Name name)
	{
		return name.unqualified.hashCode();
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.classCount; i++)
		{
			this.classes[i].resolve(markers, context);
		}
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
		for (int i = 0; i < this.classCount; i++)
		{
			this.classes[i].checkTypes(markers, context);
		}
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
		for (int i = 0; i < this.classCount; i++)
		{
			this.classes[i].check(markers, context);
		}
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
		for (int i = 0; i < this.classCount; i++)
		{
			this.classes[i].foldConstants();
		}
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
		for (int i = 0; i < this.classCount; i++)
		{
			final IClass innerClass = this.classes[i];

			compilableList.addCompilable(innerClass);
			innerClass.cleanup(compilableList, classCompilableList);
		}
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
		if (this.classCount > 0)
		{
			this.membersToString(prefix, this.classes, this.classCount, buffer);
		}

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

	private void membersToString(String prefix, IASTNode[] members, int count, StringBuilder buffer)
	{
		for (int i = 0; i < count; i++)
		{
			members[i].toString(prefix, buffer);
			buffer.append('\n').append('\n').append(prefix);
		}
	}
}
