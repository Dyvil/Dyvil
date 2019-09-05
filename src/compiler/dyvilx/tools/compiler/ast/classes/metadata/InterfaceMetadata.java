package dyvilx.tools.compiler.ast.classes.metadata;

import dyvil.reflect.Modifiers;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.classes.ClassBody;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.constructor.IConstructor;
import dyvilx.tools.compiler.ast.constructor.IInitializer;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.external.ExternalClass;
import dyvilx.tools.compiler.ast.field.IField;
import dyvilx.tools.compiler.ast.field.IProperty;
import dyvilx.tools.compiler.ast.member.Member;
import dyvilx.tools.compiler.ast.member.MemberKind;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.backend.classes.ClassWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.MarkerList;

public class InterfaceMetadata implements IClassMetadata
{
	protected final IClass theClass;

	private IMethod functionalMethod;
	private boolean functionalMethodSearched;

	public InterfaceMetadata(IClass theClass)
	{
		this.theClass = theClass;
	}

	@Override
	public MemberKind getKind()
	{
		return MemberKind.INTERFACE;
	}

	@Override
	public IMethod getFunctionalMethod()
	{
		if (this.functionalMethodSearched)
		{
			return this.functionalMethod;
		}

		this.functionalMethodSearched = true;
		final ClassBody body = this.theClass.getBody();
		if (body != null)
		{
			for (IMethod method : body.allMethods())
			{
				if (!method.isFunctional())
				{
					continue;
				}

				if (this.functionalMethod != null)
				{
					// duplicate detected
					return this.functionalMethod = null;
				}
				this.functionalMethod = method;
			}
		}

		for (final IType itf : this.theClass.getInterfaces())
		{
			final IMethod method = itf.getFunctionalMethod();
			if (method == null)
			{
				continue;
			}

			if (this.functionalMethod != null)
			{
				// duplicate detected
				return this.functionalMethod = null;
			}
			this.functionalMethod = method;
		}

		return this.functionalMethod;
	}

	@Override
	public void setFunctionalMethod(IMethod functionalMethod)
	{
		this.functionalMethod = functionalMethod;
		this.functionalMethodSearched = true;
	}

	@Override
	public void resolveTypesBeforeBody(MarkerList markers, IContext context)
	{
		if (this.theClass instanceof ExternalClass)
		{
			return;
		}

		if (!this.theClass.getParameters().isEmpty())
		{
			markers.add(Markers.semanticError(this.theClass.getPosition(), "interface.classparameters"));
		}

		final ClassBody classBody = this.theClass.getBody();
		if (classBody == null)
		{
			return;
		}

		for (int i = 0, count = classBody.constructorCount(); i < count; i++)
		{
			this.processConstructor(classBody.getConstructor(i), markers);
		}

		for (int i = 0, count = classBody.initializerCount(); i < count; i++)
		{
			this.processInitializer(classBody.getInitializer(i), markers);
		}

		for (int i = 0, count = classBody.fieldCount(); i < count; i++)
		{
			this.processField(classBody.getField(i), markers);
		}

		for (int i = 0, count = classBody.methodCount(); i < count; i++)
		{
			this.processMethod(classBody.getMethod(i), markers);
		}

		for (int i = 0, count = classBody.propertyCount(); i < count; i++)
		{
			this.processProperty(classBody.getProperty(i), markers);
		}
	}

	protected void processMember(Member member, MarkerList markers)
	{
	}

	protected void processInitializer(IInitializer initializer, MarkerList markers)
	{
		markers.add(Markers.semanticError(initializer.getPosition(), "interface.initializer"));
	}

	protected void processConstructor(IConstructor constructor, MarkerList markers)
	{
		markers.add(Markers.semanticError(constructor.getPosition(), "interface.constructor"));
	}

	protected void processMethod(IMethod method, MarkerList markers)
	{
		this.processMember(method, markers);

		if (!method.hasModifier(Modifiers.STATIC_FINAL) && method.getValue() == null)
		{
			// Make methods without an implementation abstract
			method.getAttributes().addFlag(Modifiers.ABSTRACT);
		}
	}

	protected void processField(IField field, MarkerList markers)
	{
		this.processMember(field, markers);

		field.getAttributes().addFlag(Modifiers.PUBLIC | Modifiers.STATIC | Modifiers.FINAL);
	}

	protected void processProperty(IProperty property, MarkerList markers)
	{
		this.processMember(property, markers);

		final IMethod getter = property.getGetter();
		if (getter != null)
		{
			this.processMethod(getter, markers);
		}

		final IMethod setter = property.getSetter();
		if (setter != null)
		{
			this.processMethod(setter, markers);
		}

		final SourcePosition initializerPosition = property.getInitializerPosition();
		if (initializerPosition != null)
		{
			this.processPropertyInitializer(initializerPosition, markers);
		}
	}

	protected void processPropertyInitializer(SourcePosition position, MarkerList markers)
	{
		markers.add(Markers.semanticError(position, "interface.property.initializer"));
	}

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
	}
}
