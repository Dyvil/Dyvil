package dyvil.tools.compiler.ast.classes.metadata;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.classes.IClassBody;
import dyvil.tools.compiler.ast.constructor.CodeConstructor;
import dyvil.tools.compiler.ast.constructor.IConstructor;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.ArrayExpr;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.access.FieldAccess;
import dyvil.tools.compiler.ast.expression.access.InitializerCall;
import dyvil.tools.compiler.ast.expression.access.MethodCall;
import dyvil.tools.compiler.ast.field.Field;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.generic.GenericData;
import dyvil.tools.compiler.ast.method.Candidate;
import dyvil.tools.compiler.ast.method.CodeMethod;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.modifiers.FlagModifierSet;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.parameter.CodeParameter;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Mutability;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.compound.ArrayType;
import dyvil.tools.compiler.ast.type.generic.ClassGenericType;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

public class EnumClassMetadata extends ClassMetadata
{
	private Field      valuesField;
	private CodeMethod valuesMethod;

	public EnumClassMetadata(IClass forClass)
	{
		super(forClass);
	}

	@Override
	public void resolveTypesPre(MarkerList markers, IContext context)
	{
		super.resolveTypesPre(markers, context);

		this.theClass.getModifiers().addIntModifier(Modifiers.FINAL);

		// Ensure the super type is Enum<TYPE>
		final IType classType = this.theClass.getClassType();
		final IType superType = this.theClass.getSuperType();
		if (superType != Types.OBJECT) // default
		{
			markers.add(Markers.semanticError(superType.getPosition(), "enum.super_class"));
		}
		this.theClass.setSuperType(new ClassGenericType(Types.ENUM_CLASS, classType));
	}

	@Override
	public void resolveTypesGenerate(MarkerList markers, IContext context)
	{
		final IClassBody body = this.theClass.getBody();
		final IType classType = this.theClass.getClassType();
		final IType arrayType = new ArrayType(classType, Mutability.IMMUTABLE); // [final TYPE]

		// Initialize $VALUES Field
		this.initValuesField(body, arrayType);

		// Initialize values Method
		this.initValuesMethod(classType, arrayType);

		this.updateConstructors(markers, classType);
	}

	protected void updateConstructors(MarkerList markers, IType classType)
	{
		// Replace super initializer calls from constructors
		for (Candidate<IConstructor> candidates : IContext.resolveConstructors(null, classType, null))
		{
			final IConstructor constructor = candidates.getMember();
			this.updateConstructor(constructor, markers);
		}

		if ((this.members & CONSTRUCTOR) == 0)
		{
			this.constructor = new CodeConstructor(this.theClass,
			                                       new FlagModifierSet(Modifiers.PROTECTED | Modifiers.GENERATED));
			this.updateConstructor(this.constructor, markers);
			this.copyClassParameters(this.constructor);
		}
	}

	private void updateConstructor(IConstructor constructor, MarkerList markers)
	{
		// Prepend parameters and set super initializer
		final CodeParameter nameParam = new CodeParameter(constructor, null, Names.name, Types.STRING,
		                                                  new FlagModifierSet(Modifiers.SYNTHETIC), null);
		final CodeParameter ordParam = new CodeParameter(constructor, null, Names.ordinal, Types.INT,
		                                                 new FlagModifierSet(Modifiers.SYNTHETIC), null);

		constructor.getParameters().insert(0, nameParam);
		constructor.getParameters().insert(1, ordParam);

		final IConstructor enumConstructor = Types.ENUM_CLASS.getBody().getConstructor(0);

		final InitializerCall initializer = constructor.getInitializer();
		if (initializer == null)
		{
			final ArgumentList arguments = new ArgumentList(new FieldAccess(nameParam), new FieldAccess(ordParam));
			final InitializerCall init = new InitializerCall(null, true, arguments, this.theClass.getSuperType(),
			                                                 enumConstructor);
			constructor.setInitializer(init);
			return;
		}

		// Prepend access to new parameters
		final ArgumentList arguments = initializer.getArguments();
		arguments.insert(0, new FieldAccess(nameParam));
		arguments.insert(1, new FieldAccess(ordParam));
	}

	protected void initValuesField(IClassBody body, IType arrayType)
	{
		this.valuesField = new Field(this.theClass, Names.$VALUES, arrayType,
		                             new FlagModifierSet(Modifiers.PRIVATE | Modifiers.CONST | Modifiers.SYNTHETIC));

		final ArrayExpr value = new ArrayExpr();
		value.setType(arrayType);

		this.valuesField.setValue(value);

		if (body == null)
		{
			return;
		}

		final ArgumentList values = value.getValues();
		for (int i = 0, count = body.fieldCount(); i < count; i++)
		{
			final IField field = body.getField(i);
			if (!field.hasModifier(Modifiers.ENUM_CONST))
			{
				continue;
			}

			values.add(new FieldAccess(field)); // static, no receiver
		}
	}

	protected void initValuesMethod(IType classType, IType arrayType)
	{
		this.valuesMethod = new CodeMethod(this.theClass, Names.values, arrayType,
		                                   new FlagModifierSet(Modifiers.PUBLIC | Modifiers.STATIC));

		final IValue valuesFieldAccess = new FieldAccess(this.valuesField);
		final IMethod cloneMethod = IContext.resolveMethod(arrayType, valuesFieldAccess, Name.fromRaw("copy"),
		                                                   ArgumentList.EMPTY);
		final MethodCall cloneCall = new MethodCall(null, valuesFieldAccess, cloneMethod, ArgumentList.EMPTY);

		cloneCall.setGenericData(new GenericData(cloneMethod, classType));

		this.valuesMethod.setValue(cloneCall); // $VALUES.copy<EnumType>()
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		return name == Names.$VALUES ? this.valuesField : super.resolveField(name);
	}

	@Override
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, ArgumentList arguments)
	{
		super.getMethodMatches(list, receiver, name, arguments);
		if (name == Names.values)
		{
			this.valuesMethod.checkMatch(list, receiver, name, arguments);
		}
	}

	@Override
	public void writeStaticInit(MethodWriter writer) throws BytecodeException
	{
		super.writeStaticInit(writer);
		this.valuesField.writeStaticInit(writer);
	}

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		super.write(writer);
		this.valuesField.write(writer);
		this.valuesMethod.write(writer);
	}
}
