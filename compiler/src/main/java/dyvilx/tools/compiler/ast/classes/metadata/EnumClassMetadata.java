package dyvilx.tools.compiler.ast.classes.metadata;

import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.classes.ClassBody;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.constructor.IConstructor;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.ArrayExpr;
import dyvilx.tools.compiler.ast.expression.ClassOperator;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.access.*;
import dyvilx.tools.compiler.ast.field.Field;
import dyvilx.tools.compiler.ast.field.IField;
import dyvilx.tools.compiler.ast.member.MemberKind;
import dyvilx.tools.compiler.ast.method.CodeMethod;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.parameter.CodeParameter;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.Mutability;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.ast.type.compound.ArrayType;
import dyvilx.tools.compiler.ast.type.compound.ImplicitNullableType;
import dyvilx.tools.compiler.ast.type.generic.ClassGenericType;
import dyvilx.tools.compiler.transform.Names;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.MarkerList;

public class EnumClassMetadata extends ClassMetadata
{

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
	public void resolveTypesAfterAttributes(MarkerList markers, IContext context)
	{
		super.resolveTypesAfterAttributes(markers, context);

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
		super.resolveTypesGenerate(markers, context);

		final ClassBody body = this.theClass.createBody();

		body.addDataMember(this.createValuesField());

		body.addMethod(this.createValuesMethod());

		body.addMethod(this.createFromOrdMethod());

		body.addMethod(this.createFromNameMethod());

		// Replace super initializer calls from constructors
		for (IConstructor ctor : body.constructors())
		{
			this.updateConstructor(ctor);
		}
	}

	private void updateConstructor(IConstructor constructor)
	{
		constructor.getAttributes().addFlag(Modifiers.PRIVATE_PROTECTED);

		// Prepend parameters and set super initializer
		final CodeParameter nameParam = new CodeParameter(constructor, null, Names.name,
		                                                  new ImplicitNullableType(Types.STRING),
		                                                  AttributeList.of(Modifiers.SYNTHETIC));
		final CodeParameter ordParam = new CodeParameter(constructor, null, Names.ordinal, Types.INT,
		                                                 AttributeList.of(Modifiers.SYNTHETIC));

		constructor.getParameters().add(0, nameParam);
		constructor.getParameters().add(1, ordParam);

		final IConstructor enumConstructor = Types.ENUM_CLASS.getBody().getConstructor(0);

		final InitializerCall initializer = constructor.getInitializer();
		if (initializer == null)
		{
			final ArgumentList arguments = new ArgumentList(new FieldAccess(nameParam), new FieldAccess(ordParam));
			final SourcePosition position = constructor.getPosition();
			final InitializerCall init = new InitializerCall(position, true, arguments, this.theClass.getSuperType(),
			                                                 enumConstructor);
			constructor.setInitializer(init);
			return;
		}

		// Prepend access to new parameters
		final ArgumentList arguments = initializer.getArguments();
		arguments.insert(0, new FieldAccess(nameParam));
		arguments.insert(1, new FieldAccess(ordParam));
	}

	private Field createValuesField()
	{
		final SourcePosition position = this.theClass.getPosition();
		final ArrayType type = new ArrayType(this.theClass.getClassType(), Mutability.IMMUTABLE);
		final AttributeList attributes = AttributeList.of(Modifiers.PRIVATE | Modifiers.CONST | Modifiers.SYNTHETIC);
		final Field field = new Field(this.theClass, position, Names.$VALUES, type, attributes);

		field.setValue(this.createValuesArray());

		return field;
	}

	private IValue createValuesArray()
	{
		final ArrayExpr arrayExpr = new ArrayExpr();
		arrayExpr.setElementType(this.theClass.getClassType());

		final ClassBody body = this.theClass.getBody();
		if (body != null)
		{
			final ArgumentList values = arrayExpr.getValues();
			for (IField enumConstant : body.enumConstants())
			{
				values.add(new FieldAccess(enumConstant));
			}
		}

		return arrayExpr;
	}

	private CodeMethod createValuesMethod()
	{
		// public static func values() -> [final EnumType]

		final SourcePosition position = this.theClass.getPosition();
		final ArrayType type = new ArrayType(this.theClass.getClassType());
		final AttributeList attributes = AttributeList.of(Modifiers.PUBLIC | Modifiers.STATIC);
		final CodeMethod method = new CodeMethod(this.theClass, Names.values, type, attributes);

		method.setPosition(position);

		// = $VALUES.copy()

		final FieldAccess valuesField = new FieldAccess(position, null, Names.$VALUES);
		final MethodCall cloneCall = new MethodCall(position, valuesField, Name.fromRaw("copy"), ArgumentList.EMPTY);

		method.setValue(cloneCall);

		return method;
	}

	private CodeMethod createFromOrdMethod()
	{
		// @BytecodeName("valueOf")
		// public static func from(ordinal: int) -> EnumType

		final SourcePosition position = this.theClass.getPosition();
		final IType type = this.theClass.getClassType();
		final CodeMethod method = new CodeMethod(this.theClass, Name.fromRaw("from"), type,
		                                         AttributeList.of(Modifiers.PUBLIC | Modifiers.STATIC));

		method.setPosition(position);
		method.setInternalName("valueOf");

		final CodeParameter parameter = new CodeParameter(Name.fromRaw("ordinal"), Types.INT);
		method.getParameters().add(parameter);

		// = $VALUES[ordinal]

		final FieldAccess valuesField = new FieldAccess(position, null, Names.$VALUES);
		final SubscriptAccess getCall = new SubscriptAccess(position, valuesField,
		                                                    new ArgumentList(new FieldAccess(parameter)));
		method.setValue(getCall);

		return method;
	}

	private CodeMethod createFromNameMethod()
	{
		// @BytecodeName("valueOf")
		// public static func from(name: String) -> EnumType

		final SourcePosition position = this.theClass.getPosition();
		final IType type = this.theClass.getClassType();
		final CodeMethod method = new CodeMethod(this.theClass, Name.fromRaw("from"), type,
		                                         AttributeList.of(Modifiers.PUBLIC | Modifiers.STATIC));

		method.setPosition(position);
		method.setInternalName("valueOf");

		final CodeParameter parameter = new CodeParameter(Name.fromRaw("name"), Types.STRING);
		method.getParameters().add(parameter);

		// = Enum.valueOf(class<EnumType>, name)

		final MethodCall valueOfCall = new MethodCall(position, new ClassAccess(Types.ENUM), Name.fromRaw("valueOf"),
		                                              new ArgumentList(new ClassOperator(this.theClass.getClassType()),
		                                                               new FieldAccess(parameter)));

		method.setValue(valueOfCall);

		return method;
	}
}
