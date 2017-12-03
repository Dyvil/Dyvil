package dyvilx.tools.compiler.ast.classes.metadata;

import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.classes.ClassBody;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.constructor.CodeConstructor;
import dyvilx.tools.compiler.ast.constructor.IConstructor;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.ArrayExpr;
import dyvilx.tools.compiler.ast.expression.ClassOperator;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.access.*;
import dyvilx.tools.compiler.ast.field.EnumConstant;
import dyvilx.tools.compiler.ast.field.Field;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.field.IField;
import dyvilx.tools.compiler.ast.member.MemberKind;
import dyvilx.tools.compiler.ast.method.Candidate;
import dyvilx.tools.compiler.ast.method.CodeMethod;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.parameter.CodeParameter;
import dyvilx.tools.compiler.ast.parameter.IParameter;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.Mutability;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.ast.type.compound.ArrayType;
import dyvilx.tools.compiler.ast.type.compound.ImplicitNullableType;
import dyvilx.tools.compiler.ast.type.generic.ClassGenericType;
import dyvilx.tools.compiler.backend.ClassWriter;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.transform.Names;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.MarkerList;

public class EnumClassMetadata extends ClassMetadata
{
	private Field      valuesField;
	private CodeMethod valuesMethod;
	private CodeMethod fromOrdMethod;
	private CodeMethod fromNameMethod;

	public EnumClassMetadata(IClass forClass)
	{
		super(forClass);
	}

	@Override
	public MemberKind getKind()
	{
		return MemberKind.ENUM;
	}

	@Override
	public void resolveTypesPre(MarkerList markers, IContext context)
	{
		super.resolveTypesPre(markers, context);

		this.theClass.getAttributes().addFlag(Modifiers.FINAL);

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
		final ClassBody body = this.theClass.getBody();
		final IType classType = this.theClass.getClassType();
		final IType arrayType = new ArrayType(classType, Mutability.IMMUTABLE); // [final TYPE]

		// Initialize $VALUES Field
		this.initValuesField(body, arrayType);

		// Initialize values Method
		this.initMethods(classType, arrayType);

		this.updateConstructors(classType);
	}

	protected void updateConstructors(IType classType)
	{
		// Replace super initializer calls from constructors
		for (Candidate<IConstructor> candidates : IContext.resolveConstructors(null, classType, null))
		{
			final IConstructor constructor = candidates.getMember();
			this.updateConstructor(constructor);
		}

		if ((this.members & CONSTRUCTOR) == 0)
		{
			this.constructor = new CodeConstructor(this.theClass,
			                                       AttributeList.of(Modifiers.PROTECTED | Modifiers.GENERATED));
			this.updateConstructor(this.constructor);
			this.copyClassParameters(this.constructor);
		}
	}

	private void updateConstructor(IConstructor constructor)
	{
		// Prepend parameters and set super initializer
		final CodeParameter nameParam = new CodeParameter(constructor, null, Names.name,
		                                                  new ImplicitNullableType(Types.STRING),
		                                                  AttributeList.of(Modifiers.SYNTHETIC));
		final CodeParameter ordParam = new CodeParameter(constructor, null, Names.ordinal, Types.INT,
		                                                 AttributeList.of(Modifiers.SYNTHETIC));

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

	protected void initValuesField(ClassBody body, IType arrayType)
	{
		this.valuesField = new Field(this.theClass, Names.$VALUES, arrayType,
		                             AttributeList.of(Modifiers.PRIVATE | Modifiers.CONST | Modifiers.SYNTHETIC));

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

			if (field instanceof EnumConstant)
			{
				((EnumConstant) field).setIndex(i);
			}

			values.add(new FieldAccess(field)); // static, no receiver
		}
	}

	protected void initMethods(IType classType, IType arrayType)
	{
		// public static func values() -> [final EnumType]
		this.valuesMethod = new CodeMethod(this.theClass, Names.values, arrayType,
		                                   AttributeList.of(Modifiers.PUBLIC | Modifiers.STATIC));

		final Name from = Name.fromRaw("from");

		// public static func valueOf(ordinal: int) -> EnumType
		this.fromOrdMethod = new CodeMethod(this.theClass, from, classType,
		                                    AttributeList.of(Modifiers.PUBLIC | Modifiers.STATIC));
		this.fromOrdMethod.setInternalName("valueOf");
		this.fromOrdMethod.getParameters().add(new CodeParameter(Name.fromRaw("ordinal"), Types.INT));

		// public static func valueOf(name: String) -> EnumType
		this.fromNameMethod = new CodeMethod(this.theClass, from, classType,
		                                     AttributeList.of(Modifiers.PUBLIC | Modifiers.STATIC));
		this.fromNameMethod.setInternalName("valueOf");
		this.fromNameMethod.getParameters().add(new CodeParameter(Name.fromRaw("name"), Types.STRING));
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		super.resolve(markers, context);

		// $VALUES.copy()
		final MethodCall cloneCall = new MethodCall(null, new FieldAccess(this.valuesField), Name.fromRaw("copy"),
		                                            ArgumentList.EMPTY);

		this.valuesMethod.setValue(cloneCall.resolve(markers, context));

		final IParameter ordParam = this.fromOrdMethod.getParameters().get(0);
		final IParameter nameParam = this.fromNameMethod.getParameters().get(0);

		// $VALUES[ordinal]
		final SubscriptAccess getCall = new SubscriptAccess(null, new FieldAccess(this.valuesField),
		                                                    new ArgumentList(new FieldAccess(ordParam)));
		this.fromOrdMethod.setValue(getCall.resolve(markers, this.fromOrdMethod));

		// Enum.valueOf(class<EnumType>, name)
		final MethodCall valueOfCall = new MethodCall(null, new ClassAccess(Types.ENUM), Name.fromRaw("valueOf"),
		                                              new ArgumentList(new ClassOperator(this.theClass.getClassType()),
		                                                               new FieldAccess(nameParam)));
		this.fromNameMethod.setValue(valueOfCall.resolve(markers, this.fromNameMethod));
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
		this.valuesMethod.checkMatch(list, receiver, name, arguments);
		this.fromOrdMethod.checkMatch(list, receiver, name, arguments);
		this.fromNameMethod.checkMatch(list, receiver, name, arguments);
	}

	@Override
	public void writeStaticInitPost(MethodWriter writer) throws BytecodeException
	{
		super.writeStaticInitPost(writer);
		this.valuesField.writeStaticInit(writer);
	}

	@Override
	public void writePost(ClassWriter writer) throws BytecodeException
	{
		super.writePost(writer);
		this.valuesField.write(writer);
		this.valuesMethod.write(writer);
		this.fromOrdMethod.write(writer);
		this.fromNameMethod.write(writer);
	}
}
