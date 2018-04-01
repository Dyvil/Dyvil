package dyvilx.tools.compiler.ast.classes.metadata;

import dyvil.reflect.Modifiers;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.classes.ClassBody;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.CastOperator;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.InstanceOfOperator;
import dyvilx.tools.compiler.ast.expression.TupleExpr;
import dyvilx.tools.compiler.ast.expression.access.ConstructorCall;
import dyvilx.tools.compiler.ast.expression.access.FieldAccess;
import dyvilx.tools.compiler.ast.expression.access.MethodCall;
import dyvilx.tools.compiler.ast.expression.constant.NullValue;
import dyvilx.tools.compiler.ast.expression.intrinsic.VarargsOperator;
import dyvilx.tools.compiler.ast.field.IProperty;
import dyvilx.tools.compiler.ast.member.MemberKind;
import dyvilx.tools.compiler.ast.method.CodeMethod;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.parameter.ClassParameter;
import dyvilx.tools.compiler.ast.parameter.CodeParameter;
import dyvilx.tools.compiler.ast.parameter.IParameter;
import dyvilx.tools.compiler.ast.statement.IfStatement;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.TypeList;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.ast.type.compound.NullableType;
import dyvilx.tools.compiler.ast.type.compound.TupleType;
import dyvilx.tools.compiler.backend.ClassWriter;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.MethodWriterImpl;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.transform.CaseClasses;
import dyvilx.tools.compiler.transform.Names;
import dyvilx.tools.parsing.marker.MarkerList;

public final class CaseClassMetadata extends ClassMetadata
{
	protected IMethod applyMethod;
	protected IMethod unapplyMethod;
	protected IMethod unapplyAnyMethod;

	public CaseClassMetadata(IClass iclass)
	{
		super(iclass);
	}

	@Override
	public MemberKind getKind()
	{
		return MemberKind.CLASS;
	}

	@Override
	public void resolveTypesPre(MarkerList markers, IContext context)
	{
		super.resolveTypesPre(markers, context);

		for (IParameter param : this.theClass.getParameters())
		{
			final ClassParameter classParameter = (ClassParameter) param;
			if (classParameter.isOverride() || classParameter.getProperty() != null)
			{
				// Ignore override class parameters and class parameters that already have a property
				continue;
			}

			// Create a property with getter
			final IProperty property = classParameter.createProperty();
			property.getAttributes().addFlag(Modifiers.GENERATED);
			property.initGetter();

			if (!classParameter.hasModifier(Modifiers.FINAL))
			{
				// and setter, for non-final class parameters
				property.initSetter();
			}
		}
	}

	@Override
	public void resolveTypesHeader(MarkerList markers, IContext context)
	{
		super.resolveTypesHeader(markers, context);

		if (!this.theClass.isSubClassOf(Types.SERIALIZABLE))
		{
			this.theClass.getInterfaces().add(Types.SERIALIZABLE);
		}
	}

	@Override
	public void resolveTypesGenerate(MarkerList markers, IContext context)
	{
		super.resolveTypesGenerate(markers, context);

		final ClassBody body = this.theClass.createBody();

		if ((this.members & APPLY) == 0)
		{
			this.applyMethod = this.createApplyMethod();
			body.addMethod(this.applyMethod);
		}

		if ((this.members & UNAPPLY) == 0)
		{
			this.unapplyMethod = this.createUnapplyMethod();
			body.addMethod(this.unapplyMethod);
		}

		if ((this.members & UNAPPLY_ANY) == 0)
		{
			this.unapplyAnyMethod = this.createUnapplyAnyMethod();
			body.addMethod(this.unapplyAnyMethod);
		}
	}

	private CodeMethod createApplyMethod()
	{
		// static final func apply<TypeParams...>(classParams...: ClassParamTypes...) -> This

		final SourcePosition position = this.theClass.position();
		final AttributeList attributes = AttributeList
			                                 .of(Modifiers.PUBLIC | Modifiers.STATIC_FINAL | Modifiers.GENERATED);
		final IType type = this.theClass.getThisType();

		final CodeMethod applyMethod = new CodeMethod(this.theClass, Names.apply, type, attributes);

		applyMethod.setPosition(position);
		applyMethod.getTypeParameters().addAll(this.theClass.getTypeParameters());
		this.copyClassParameters(applyMethod);

		// = new This<TypeParams...>(classParams...)

		final ArgumentList arguments = new ArgumentList();

		for (IParameter param : applyMethod.getParameters())
		{
			// no need to check for override class parameters here, since we are dealing with parameters of the
			// apply method

			final IValue access;
			if (param.isVarargs())
			{
				access = new VarargsOperator(position, new FieldAccess(param));
			}
			else
			{
				access = new FieldAccess(param);
			}
			arguments.add(access);
		}

		// = new This(params...)
		applyMethod.setValue(new ConstructorCall(this.theClass.position(), this.theClass.getThisType(), arguments));

		return applyMethod;
	}

	private TupleType getUnapplyReturnType()
	{
		final TupleType returnType = new TupleType();
		final TypeList typeArgs = returnType.getArguments();
		for (IParameter param : this.theClass.getParameters())
		{
			typeArgs.add(param.getType());
		}
		return returnType;
	}

	private CodeMethod createUnapplyMethod()
	{
		// static final func unapply<TypeParams...>(value: This) -> (T...)

		final SourcePosition position = this.theClass.position();
		final AttributeList attributes = AttributeList
			                                 .of(Modifiers.PUBLIC | Modifiers.STATIC_FINAL | Modifiers.GENERATED);
		final IType type = this.getUnapplyReturnType();
		final CodeMethod unapply = new CodeMethod(this.theClass, Names.unapply, type, attributes);

		unapply.setPosition(position);
		unapply.getTypeParameters().addAll(this.theClass.getTypeParameters());

		final CodeParameter parameter = new CodeParameter(unapply, position, Names.value, this.theClass.getThisType());
		unapply.getParameters().add(parameter);

		// = (value.classParams...)

		final TupleExpr tupleExpr = new TupleExpr(position);
		final ArgumentList arguments = tupleExpr.getValues();

		for (IParameter param : this.theClass.getParameters())
		{
			// value
			final FieldAccess thisAccess = new FieldAccess(position, null, parameter);
			// value.classParam
			final IValue fieldAccess;
			if (param.isOverride())
			{
				// if the class parameter is marked as 'override', we have to resolve it from a super-class
				// the easiest way to do this is by name
				fieldAccess = new FieldAccess(position, thisAccess, param.getName());
			}
			else
			{
				fieldAccess = new FieldAccess(position, thisAccess, param);
			}
			arguments.add(fieldAccess);
		}

		unapply.setValue(tupleExpr);

		return unapply;
	}

	private CodeMethod createUnapplyAnyMethod()
	{
		// static final func unapply<TypeParams...>(value: any) -> (T...)?

		final SourcePosition position = this.theClass.position();
		final AttributeList attributes = AttributeList
			                                 .of(Modifiers.PUBLIC | Modifiers.STATIC_FINAL | Modifiers.GENERATED);
		final IType type = NullableType.apply(this.getUnapplyReturnType());
		final CodeMethod unapply = new CodeMethod(this.theClass, Names.unapply, type, attributes);

		unapply.setPosition(position);
		unapply.getTypeParameters().addAll(this.theClass.getTypeParameters());

		final CodeParameter parameter = new CodeParameter(unapply, position, Names.value, Types.NULLABLE_ANY);
		unapply.getParameters().add(parameter);

		// = (param is This) ? unapply(param as This) : null

		final InstanceOfOperator isOperator = new InstanceOfOperator(new FieldAccess(parameter),
		                                                             this.theClass.getClassType());
		final CastOperator castOperator = new CastOperator(new FieldAccess(parameter), this.theClass.getThisType());
		final IValue call = new MethodCall(position, null, Names.unapply, new ArgumentList(castOperator));
		final IfStatement ifStatement = new IfStatement(isOperator, call, new NullValue());

		unapply.setValue(ifStatement);

		return unapply;
	}

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		super.write(writer);

		MethodWriter mw;
		final String internal = this.theClass.getInternalName();
		if ((this.members & EQUALS) == 0)
		{
			mw = new MethodWriterImpl(writer, writer.visitMethod(Modifiers.PUBLIC, "equals", "(Ljava/lang/Object;)Z",
			                                                     null, null));
			mw.setThisType(internal);
			mw.visitParameter(1, "obj", Types.OBJECT, 0);
			mw.visitCode();
			CaseClasses.writeEquals(mw, this.theClass);
			mw.visitEnd();
		}

		if ((this.members & HASHCODE) == 0)
		{
			mw = new MethodWriterImpl(writer, writer.visitMethod(Modifiers.PUBLIC, "hashCode", "()I", null, null));
			mw.setThisType(internal);
			mw.visitCode();
			CaseClasses.writeHashCode(mw, this.theClass);
			mw.visitEnd();
		}

		if ((this.members & TOSTRING) == 0)
		{
			mw = new MethodWriterImpl(writer, writer.visitMethod(Modifiers.PUBLIC, "toString", "()Ljava/lang/String;",
			                                                     null, null));
			mw.setThisType(internal);
			mw.visitCode();
			CaseClasses.writeToString(mw, this.theClass);
			mw.visitEnd();
		}
	}
}
