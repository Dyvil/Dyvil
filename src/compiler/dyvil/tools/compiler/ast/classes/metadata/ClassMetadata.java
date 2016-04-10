package dyvil.tools.compiler.ast.classes.metadata;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.access.*;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.classes.IClassBody;
import dyvil.tools.compiler.ast.constant.StringValue;
import dyvil.tools.compiler.ast.constructor.CodeConstructor;
import dyvil.tools.compiler.ast.constructor.ConstructorMatchList;
import dyvil.tools.compiler.ast.constructor.IConstructor;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.NilExpr;
import dyvil.tools.compiler.ast.field.Field;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.field.IProperty;
import dyvil.tools.compiler.ast.intrinsic.NullCheckOperator;
import dyvil.tools.compiler.ast.method.CodeMethod;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.modifiers.FlagModifierSet;
import dyvil.tools.compiler.ast.parameter.*;
import dyvil.tools.compiler.ast.statement.IfStatement;
import dyvil.tools.compiler.ast.statement.StatementList;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

public class ClassMetadata implements IClassMetadata
{
	protected static final int CONSTRUCTOR = 1;
	protected static final int APPLY       = 1 << 1;
	protected static final int EQUALS      = 1 << 2;
	protected static final int HASHCODE    = 1 << 3;
	protected static final int TOSTRING    = 1 << 4;
	protected static final int NULLVALUE   = 1 << 5;
	protected static final int NILVALUE    = 1 << 6;

	protected static final int READ_RESOLVE  = 1 << 7;
	protected static final int WRITE_REPLACE = 1 << 8;
	// protected static final int WRITE_OBJECT  = 1 << 9;
	// protected static final int READ_OBJECT   = 1 << 10;

	protected static final int INSTANCE_FIELD = 1 << 16;
	protected static final int NULL_FIELD     = 1 << 17;
	protected static final int NIL_FIELD      = 1 << 18;

	public static final Name NULL      = Name.getQualified("NULL");
	public static final Name nullValue = Name.getQualified("nullValue");

	protected final IClass theClass;

	protected int members;

	protected IConstructor    constructor;
	protected InitializerCall superInitializer;

	protected IField  nullField;
	protected IMethod nullValueMethod;

	public ClassMetadata(IClass iclass)
	{
		this.theClass = iclass;
	}

	@Override
	public IConstructor getConstructor()
	{
		return this.constructor;
	}

	private void checkMembers(IClassBody body)
	{
		for (int i = 0, count = body.methodCount(); i < count; i++)
		{
			this.checkMethod(body.getMethod(i));
		}

		for (int i = 0, count = body.propertyCount(); i < count; i++)
		{
			final IMethod getter = body.getProperty(i).getGetter();
			if (getter != null)
			{
				this.checkMethod(getter);
			}

			// only check the getter
		}

		for (int i = 0, count = body.fieldCount(); i < count; i++)
		{
			final IField field = body.getField(i);
			this.checkField(field);

			final IProperty property = field.getProperty();
			final IMethod getter;
			if (property != null && (getter = property.getGetter()) != null)
			{
				this.checkMethod(getter);
			}

			// only check the getter
		}
	}

	private void checkField(IField field)
	{
		switch (field.getName().qualified)
		{
		case "NULL":
			this.members |= NULL_FIELD;
			this.nullField = field;
			return;
		case "NIL":
			this.members |= NIL_FIELD;
			return;
		case "instance":
			this.members |= INSTANCE_FIELD;
			this.setInstanceField(field);
		}
	}

	private void checkMethod(IMethod method)
	{
		switch (method.getName().unqualified)
		{
		case "equals":
			if (method.parameterCount() == 1
				    && method.getParameter(0).getInternalType().getTheClass() == Types.OBJECT_CLASS)
			{
				this.members |= EQUALS;
			}
			return;
		case "hashCode":
			if (method.parameterCount() == 0)
			{
				this.members |= HASHCODE;
			}
			return;
		case "toString":
			if (method.parameterCount() == 0)
			{
				this.members |= TOSTRING;
			}
			return;
		case "apply":
			if (method.parameterCount() == this.theClass.parameterCount())
			{
				final int len = this.theClass.parameterCount();
				for (int i = 0; i < len; i++)
				{
					final IType methodParameterType = method.getParameter(i).getType();
					final IType classParameterType = this.theClass.getParameter(i).getType();
					if (!Types.isSameType(methodParameterType, classParameterType))
					{
						return;
					}
				}

				this.members |= APPLY;
			}
			return;
		case "readResolve":
			if (method.parameterCount() == 0)
			{
				this.members |= READ_RESOLVE;
			}
			return;
		case "writeReplace":
			if (method.parameterCount() == 0)
			{
				this.members |= WRITE_REPLACE;
			}
			return;
		case "nullValue":
			if (method.parameterCount() == 0)
			{
				this.members |= NULLVALUE;
			}
			return;
		case "nilValue":
			if (method.parameterCount() == 0)
			{
				this.members |= NILVALUE;
			}
		}
	}

	@Override
	public void resolveTypesHeader(MarkerList markers, IContext context)
	{
		final IArguments superConstructorArguments = this.theClass.getSuperConstructorArguments();
		if (superConstructorArguments != null)
		{
			superConstructorArguments.resolveTypes(markers, context);
		}
	}

	@Override
	public void resolveTypesBody(MarkerList markers, IContext context)
	{
		// Check if a constructor needs to be generated

		final IClassBody body = this.theClass.getBody();
		if (body == null)
		{
			return;
		}

		this.checkMembers(body);

		if (body.constructorCount() <= 0)
		{
			return;
		}

		final int parameterCount = this.theClass.parameterCount();
		final IConstructor constructor = body.getConstructor(this.theClass.getParameters(), parameterCount);
		if (constructor != null)
		{
			this.constructor = constructor;
			this.members |= CONSTRUCTOR;
			return;
		}

		if (parameterCount == 0)
		{
			this.members |= CONSTRUCTOR;
		}
	}

	@Override
	public void resolveTypesGenerate(MarkerList markers, IContext context)
	{
		if ((this.members & CONSTRUCTOR) == 0)
		{
			// Generate the constructor signature

			final CodeConstructor constructor = new CodeConstructor(this.theClass,
			                                                        new FlagModifierSet(Modifiers.PUBLIC));

			this.copyClassParameters(constructor);

			constructor.resolveTypes(markers, context);
			this.constructor = constructor;
		}

		if (this.theClass.hasModifier(Modifiers.NULL_CLASS))
		{
			this.generateNullMembers(markers);
		}
	}

	private void generateNullMembers(MarkerList markers)
	{
		// Generate the NilConvertible annotation
		if (this.theClass.getAnnotation(NilExpr.LazyFields.NIL_CONVERTIBLE_CLASS) == null)
		{
			final Annotation annotation = new Annotation(this.theClass.getPosition(),
			                                             NilExpr.LazyFields.NIL_CONVERTIBLE_CLASS.getClassType());
			annotation.setArguments(new SingleArgument(new StringValue("nullValue")));
			this.theClass.addAnnotation(annotation); // @NilConvertible("nullValue")
		}

		// Generate the NULL field
		if ((this.members & NULL_FIELD) == 0)
		{
			this.nullField = new Field(this.theClass, NULL, this.theClass.getClassType(),
			                           new FlagModifierSet(Modifiers.PRIVATE | Modifiers.STATIC));
		}
		else
		{
			markers
				.add(Markers.semanticError(this.nullField.getPosition(), "class.null.field", this.theClass.getName()));
		}

		if ((this.members & NULLVALUE) == 0)
		{
			// Generate the nullValue() method

			// public static TypeName nullValue()

			this.nullValueMethod = new CodeMethod(this.theClass, nullValue, this.theClass.getClassType(),
			                                      new FlagModifierSet(Modifiers.PUBLIC | Modifiers.STATIC));
		}
	}

	protected void copyClassParameters(IParametric constructor)
	{
		final int parameterCount = this.theClass.parameterCount();
		final IParameter[] parameters = new IParameter[parameterCount];

		for (int i = 0; i < parameterCount; i++)
		{
			final IParameter classParameter = this.theClass.getParameter(i);

			int modifiers = classParameter.getModifiers().toFlags() & Modifiers.PARAMETER_MODIFIERS;
			if (classParameter.isVarargs())
			{
				modifiers |= Modifiers.VARARGS;
				constructor.setVariadic();
			}

			parameters[i] = new MethodParameter(classParameter.getPosition(), classParameter.getName(),
			                                    classParameter.getType(), new FlagModifierSet(modifiers),
			                                    classParameter.getAnnotations());
			parameters[i].setIndex(i);
		}

		constructor.setParameters(parameters, parameterCount);
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		if (name == NULL && this.nullField != null)
		{
			return this.nullField;
		}
		return null;
	}

	@Override
	public void getMethodMatches(MethodMatchList list, IValue instance, Name name, IArguments arguments)
	{
		if (name == nullValue && this.nullValueMethod != null)
		{
			IContext.getMethodMatch(list, instance, name, arguments, this.nullValueMethod);
		}
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		if ((this.members & CONSTRUCTOR) == 0 && this.theClass.getSuperType() != null)
		{
			// Generate the constructor body
			this.superInitializer = (InitializerCall) new InitializerCall(this.theClass.getPosition(), null,
			                                                              this.theClass.getSuperConstructorArguments(),
			                                                              true).resolve(markers, this.constructor);
			this.constructor.setInitializer(this.superInitializer);

			final StatementList constructorBody = new StatementList();
			for (int i = 0, count = this.theClass.parameterCount(); i < count; i++)
			{
				constructorBody.addValue(new ClassParameterSetter(this.theClass, this.constructor.getParameter(i)));
			}

			this.constructor.setValue(constructorBody);
		}

		if ((this.members & NULLVALUE) == 0 && this.nullField != null)
		{
			// Generate the nullValue body

			// public static TypeName nullValue() = if (NULL != null) NULL else NULL = new TypeName(0, false, null, ...)

			final ArgumentList arguments = new ArgumentList();
			for (int i = 0, count = this.theClass.parameterCount(); i < count; i++)
			{
				arguments.addValue(this.theClass.getParameter(i).getInternalType().getDefaultValue());
			}

			final FieldAccess fieldAccess = new FieldAccess(this.nullField);
			final IValue value = new ConstructorCall(this.constructor, arguments);
			this.nullValueMethod.setValue(new IfStatement(new NullCheckOperator(fieldAccess, false), fieldAccess,
			                                              new FieldAssignment(this.nullField, value)));
		}
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if ((this.members & CONSTRUCTOR) == 0)
		{
			this.superInitializer.checkTypes(markers, this.constructor);
		}
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		if ((this.members & CONSTRUCTOR) == 0)
		{
			this.superInitializer.checkNoError(markers, this.constructor);
		}
	}

	@Override
	public void foldConstants()
	{
		if ((this.members & CONSTRUCTOR) == 0)
		{
			this.superInitializer.foldConstants();
		}
	}

	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		if ((this.members & CONSTRUCTOR) == 0)
		{
			this.superInitializer.cleanup(this.constructor, compilableList);
		}
	}

	@Override
	public void getConstructorMatches(ConstructorMatchList list, IArguments arguments)
	{
		if ((this.members & CONSTRUCTOR) != 0)
		{
			return;
		}

		float match = this.constructor.getSignatureMatch(arguments);
		if (match > 0)
		{
			list.add(this.constructor, match);
		}
	}

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		if ((this.members & CONSTRUCTOR) == 0)
		{
			this.constructor.write(writer);
		}

		if (this.nullField != null && (this.members & NULL_FIELD) == 0)
		{
			this.nullField.write(writer);
		}
		if (this.nullValueMethod != null && (this.members & NULLVALUE) == 0)
		{
			this.nullValueMethod.write(writer);
		}
	}
}
