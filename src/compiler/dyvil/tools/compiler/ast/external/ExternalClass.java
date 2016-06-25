package dyvil.tools.compiler.ast.external;

import dyvil.collection.Entry;
import dyvil.collection.Map;
import dyvil.collection.Set;
import dyvil.collection.immutable.ArraySet;
import dyvil.collection.mutable.HashMap;
import dyvil.reflect.Modifiers;
import dyvil.tools.asm.*;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.annotation.AnnotationUtil;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.AbstractClass;
import dyvil.tools.compiler.ast.classes.ClassBody;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.classes.metadata.IClassMetadata;
import dyvil.tools.compiler.ast.constructor.IConstructor;
import dyvil.tools.compiler.ast.context.CombiningContext;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.modifiers.FlagModifierSet;
import dyvil.tools.compiler.ast.parameter.ClassParameter;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.IParameterList;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.alias.ITypeAlias;
import dyvil.tools.compiler.ast.type.generic.ClassGenericType;
import dyvil.tools.compiler.ast.type.raw.ClassType;
import dyvil.tools.compiler.ast.type.typevar.TypeVarType;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.backend.visitor.*;
import dyvil.tools.compiler.sources.DyvilFileType;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public final class ExternalClass extends AbstractClass
{
	private static final int METADATA    = 1;
	private static final int SUPER_TYPES = 1 << 1;
	private static final int GENERICS    = 1 << 2;
	private static final int BODY_CACHE  = 1 << 3;
	private static final int ANNOTATIONS = 1 << 4;
	private static final int INNER_TYPES = 1 << 5;

	protected Package thePackage;

	private int                 resolved;
	private Map<String, String> innerTypes;
	private Set<String>         classParameters;

	public ExternalClass(Name name)
	{
		this.name = name;
		// this.modifiers = new FlagModifierSet();
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

	private IContext getCombiningContext()
	{
		return new CombiningContext(this, Package.rootPackage);
	}

	private void resolveMetadata()
	{
		this.resolved |= METADATA;

		final IContext context = this.getCombiningContext();

		this.metadata = IClass.getClassMetadata(this, this.modifiers.toFlags());
		this.metadata.resolveTypesHeader(null, context);
		this.metadata.resolveTypesBody(null, context);
	}

	private void resolveGenerics()
	{
		this.resolved |= GENERICS;
		if (this.typeParameterCount <= 0)
		{
			return;
		}

		final IContext context = this.getCombiningContext();

		final ClassGenericType type = new ClassGenericType(this);

		for (int i = 0; i < this.typeParameterCount; i++)
		{
			final ITypeParameter typeParameter = this.typeParameters[i];
			typeParameter.resolveTypes(null, context);
			type.addType(new TypeVarType(typeParameter));
		}

		this.thisType = type;
	}

	private void resolveBodyCache()
	{
		this.body.initExternalCache();
		this.resolved |= BODY_CACHE;
	}

	private void resolveSuperTypes()
	{
		this.resolved |= SUPER_TYPES;

		final IContext context = this.getCombiningContext();
		if (this.superType != null)
		{
			this.superType = this.superType.resolveType(null, context);
		}

		for (int i = 0; i < this.interfaceCount; i++)
		{
			this.interfaces[i] = this.interfaces[i].resolveType(null, context);
		}
	}

	private void resolveAnnotations()
	{
		this.resolved |= ANNOTATIONS;

		if (this.annotations != null)
		{
			this.annotations.resolveTypes(null, this.getCombiningContext(), this);
		}
	}

	private void resolveInnerTypes()
	{
		this.resolved |= INNER_TYPES;

		if (this.innerTypes == null)
		{
			return;
		}

		for (Entry<String, String> entry : this.innerTypes)
		{
			Name name = Name.fromQualified(entry.getKey());
			String internal = entry.getValue();

			// Resolve the class name
			String fileName = internal + DyvilFileType.CLASS_EXTENSION;
			IClass c = Package.loadClass(fileName, name);
			if (c != null)
			{
				c.setEnclosingClass(this);
				this.body.addClass(c);
			}
		}

		this.innerTypes = null;
	}

	public void setClassParameters(String[] classParameters)
	{
		this.classParameters = ArraySet.apply(classParameters);
	}

	@Override
	public IDyvilHeader getHeader()
	{
		return null;
	}

	@Override
	public void setHeader(IDyvilHeader unit)
	{
	}

	@Override
	public IType getType()
	{
		if ((this.resolved & GENERICS) == 0)
		{
			this.resolveGenerics();
		}
		return this.thisType;
	}

	@Override
	public IClass getThisClass()
	{
		if ((this.resolved & GENERICS) == 0)
		{
			this.resolveGenerics();
		}
		return this;
	}

	@Override
	public IType getSuperType()
	{
		if ((this.resolved & SUPER_TYPES) == 0)
		{
			this.resolveSuperTypes();
		}
		return this.superType;
	}

	@Override
	public IClass getEnclosingClass()
	{
		if ((this.resolved & INNER_TYPES) == 0)
		{
			this.resolveInnerTypes();
		}
		return super.getEnclosingClass();
	}

	@Override
	public boolean isSubClassOf(IType type)
	{
		if ((this.resolved & SUPER_TYPES) == 0)
		{
			this.resolveSuperTypes();
		}
		return super.isSubClassOf(type);
	}

	@Override
	public ITypeParameter[] getTypeParameters()
	{
		if ((this.resolved & GENERICS) == 0)
		{
			this.resolveGenerics();
		}
		return super.getTypeParameters();
	}

	@Override
	public ITypeParameter getTypeParameter(int index)
	{
		if ((this.resolved & GENERICS) == 0)
		{
			this.resolveGenerics();
		}
		return super.getTypeParameter(index);
	}

	@Override
	public IAnnotation getAnnotation(IClass type)
	{
		if ((this.resolved & ANNOTATIONS) == 0)
		{
			this.resolveAnnotations();
		}
		return super.getAnnotation(type);
	}

	@Override
	public IType resolveType(ITypeParameter typeVar, IType concrete)
	{
		if ((this.resolved & GENERICS) == 0)
		{
			this.resolveGenerics();
		}
		if ((this.resolved & SUPER_TYPES) == 0)
		{
			this.resolveSuperTypes();
		}
		return super.resolveType(typeVar, concrete);
	}

	@Override
	public IClassMetadata getMetadata()
	{
		if ((this.resolved & METADATA) == 0)
		{
			this.resolveMetadata();
		}
		return this.metadata;
	}

	@Override
	public IMethod getFunctionalMethod()
	{
		if (!this.isAbstract())
		{
			return null;
		}

		if ((this.resolved & GENERICS) == 0)
		{
			this.resolveGenerics();
		}

		if (this.body == null)
		{
			return null;
		}

		IMethod functionalMethod = this.body.getFunctionalMethod();
		if (functionalMethod != null)
		{
			return functionalMethod;
		}
		for (int i = 0, count = this.body.methodCount(); i < count; i++)
		{
			final IMethod method = this.body.getMethod(i);
			if (!method.isAbstract() || method.isObjectMethod())
			{
				continue;
			}
			if (functionalMethod != null)
			{
				return null;
			}
			functionalMethod = method;
		}
		if (functionalMethod != null)
		{
			this.body.setFunctionalMethod(functionalMethod);
		}
		return functionalMethod;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
	}

	@Override
	public boolean checkImplements(IMethod candidate, ITypeContext typeContext)
	{
		if ((this.resolved & GENERICS) == 0)
		{
			this.resolveGenerics();
		}
		if ((this.resolved & SUPER_TYPES) == 0)
		{
			this.resolveSuperTypes();
		}
		if ((this.resolved & BODY_CACHE) == 0)
		{
			this.resolveBodyCache();
		}
		return super.checkImplements(candidate, typeContext);
	}

	@Override
	public void checkMethods(MarkerList markers, IClass checkedClass, ITypeContext typeContext,
		                        Set<IClass> checkedClasses)
	{
		if ((this.resolved & GENERICS) == 0)
		{
			this.resolveGenerics();
		}
		if ((this.resolved & SUPER_TYPES) == 0)
		{
			this.resolveSuperTypes();
		}
		super.checkMethods(markers, checkedClass, typeContext, checkedClasses);
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
	}

	@Override
	public void foldConstants()
	{
	}

	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
	}

	@Override
	public IClass resolveClass(Name name)
	{
		if ((this.resolved & INNER_TYPES) == 0)
		{
			this.resolveInnerTypes();
		}

		return this.body.getClass(name);
	}

	@Override
	public ITypeAlias resolveTypeAlias(Name name, int arity)
	{
		if ((this.resolved & INNER_TYPES) == 0)
		{
			this.resolveInnerTypes();
		}

		return super.resolveTypeAlias(name, arity);
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		final IParameter parameter = this.parameters.resolveParameter(name);
		if (parameter != null)
		{
			return parameter;
		}

		// Own fields
		IDataMember field = this.body.getField(name);
		if (field != null)
		{
			return field;
		}

		if ((this.resolved & SUPER_TYPES) == 0)
		{
			this.resolveSuperTypes();
		}

		// Inherited Fields
		if (this.superType != null)
		{
			field = this.superType.resolveField(name);
			if (field != null)
			{
				return field;
			}
		}
		return null;
	}

	@Override
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, IArguments arguments)
	{
		if ((this.resolved & GENERICS) == 0)
		{
			this.resolveGenerics();
		}
		if ((this.resolved & BODY_CACHE) == 0)
		{
			this.resolveBodyCache();
		}

		/*
		Note: unlike AbstractClass.getMethodMatches, this does not check the Class Parameter Properties, because
		External classes do not have any class parameters with associated properties
		*/
		this.body.getMethodMatches(list, receiver, name, arguments);
		// The same applies for the Metadata

		if (!list.isEmpty())
		{
			return;
		}

		if ((this.resolved & SUPER_TYPES) == 0)
		{
			this.resolveSuperTypes();
		}

		if (this.superType != null)
		{
			this.superType.getMethodMatches(list, receiver, name, arguments);
		}

		if (!list.isEmpty())
		{
			return;
		}

		for (int i = 0; i < this.interfaceCount; i++)
		{
			this.interfaces[i].getMethodMatches(list, receiver, name, arguments);
		}
	}

	@Override
	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
		if ((this.resolved & GENERICS) == 0)
		{
			this.resolveGenerics();
		}

		this.body.getImplicitMatches(list, value, targetType);
	}

	@Override
	public void getConstructorMatches(MatchList<IConstructor> list, IArguments arguments)
	{
		if ((this.resolved & SUPER_TYPES) == 0)
		{
			this.resolveSuperTypes();
		}
		if ((this.resolved & GENERICS) == 0)
		{
			this.resolveGenerics();
		}

		this.body.getConstructorMatches(list, arguments);
	}

	public void visit(int access, String name, String signature, String superName, String[] interfaces)
	{
		this.modifiers = new FlagModifierSet(access);
		this.internalName = name;

		this.body = new ClassBody(this);
		if (interfaces != null)
		{
			this.interfaces = new IType[interfaces.length];
		}

		int index = name.lastIndexOf('$');
		if (index == -1)
		{
			index = name.lastIndexOf('/');
		}
		if (index == -1)
		{
			this.name = Name.fromQualified(name);
			this.thePackage = Package.rootPackage;
			this.fullName = name;
		}
		else
		{
			this.name = Name.fromQualified(name.substring(index + 1));
			this.fullName = name.replace('/', '.');
			this.thePackage = Package.rootPackage.resolvePackage(Name.fromQualified(this.fullName.substring(0, index)));
		}

		if (signature != null)
		{
			this.typeParameters = new ITypeParameter[2];
			ClassFormat.readClassSignature(signature, this);
		}
		else
		{
			if (superName != null)
			{
				this.superType = ClassFormat.internalToType(superName);
			}
			else
			{
				this.superType = null;
			}

			if (interfaces != null)
			{
				this.interfaceCount = interfaces.length;
				this.interfaces = new IType[this.interfaceCount];
				for (int i = 0; i < this.interfaceCount; i++)
				{
					this.interfaces[i] = ClassFormat.internalToType(interfaces[i]);
				}
			}
		}

		this.thisType = new ClassType(this);
	}

	public AnnotationVisitor visitAnnotation(String type)
	{
		switch (type)
		{
		case AnnotationUtil.DYVIL_MODIFIERS:
			return new ModifierVisitor(this.modifiers);
		case AnnotationUtil.CLASS_PARAMETERS:
			return new ClassParameterAnnotationVisitor(this);
		}

		String internal = ClassFormat.extendedToInternal(type);
		if (this.addRawAnnotation(internal, null))
		{
			if (this.annotations == null)
			{
				this.annotations = new AnnotationList();
			}

			Annotation annotation = new Annotation(null, ClassFormat.internalToType(internal));
			return new AnnotationReader(this, annotation);
		}
		return null;
	}

	public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc)
	{
		IAnnotation annotation = new Annotation(ClassFormat.extendedToType(desc));
		switch (TypeReference.getSort(typeRef))
		{
		case TypeReference.CLASS_EXTENDS:
		{
			int steps = typePath.getLength();
			int index = TypeReference.getSuperTypeIndex(typeRef);
			if (index == -1)
			{
				this.superType = IType.withAnnotation(this.superType, annotation, typePath, 0, steps);
			}
			else
			{
				this.interfaces[index] = IType.withAnnotation(this.interfaces[index], annotation, typePath, 0, steps);
			}
			break;
		}
		case TypeReference.CLASS_TYPE_PARAMETER:
		{
			ITypeParameter typeVar = this.typeParameters[TypeReference.getTypeParameterIndex(typeRef)];
			if (typeVar.addRawAnnotation(desc, annotation))
			{
				return null;
			}

			typeVar.addAnnotation(annotation);
			break;
		}
		case TypeReference.CLASS_TYPE_PARAMETER_BOUND:
		{
			ITypeParameter typeVar = this.typeParameters[TypeReference.getTypeParameterIndex(typeRef)];
			typeVar.addBoundAnnotation(annotation, TypeReference.getTypeParameterBoundIndex(typeRef), typePath);
			break;
		}
		}
		return new AnnotationReader(null, annotation);
	}

	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value)
	{
		IType type = ClassFormat.extendedToType(signature == null ? desc : signature);

		if (this.classParameters != null && this.classParameters.contains(name))
		{
			final ClassParameter param = new ExternalClassParameter(this, Name.fromQualified(name), type,
			                                                        new FlagModifierSet(access));
			this.parameters.addParameter(param);
			return new SimpleFieldVisitor(param);
		}

		final ExternalField field = new ExternalField(this, Name.fromQualified(name), desc, type,
		                                              new FlagModifierSet(access));

		if (value != null)
		{
			field.setConstantValue(value);
		}

		this.body.addDataMember(field);

		return new SimpleFieldVisitor(field);
	}

	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
	{
		if ((access & Modifiers.SYNTHETIC) != 0)
		{
			return null;
		}

		if ("<init>".equals(name))
		{
			ExternalConstructor constructor = new ExternalConstructor(this);
			constructor.setModifiers(new FlagModifierSet(access));

			if (signature != null)
			{
				ClassFormat.readConstructorType(signature, constructor);
			}
			else
			{
				ClassFormat.readConstructorType(desc, constructor);

				if (exceptions != null)
				{
					ClassFormat.readExceptions(exceptions, constructor);
				}
			}

			if ((access & Modifiers.VARARGS) != 0)
			{
				final IParameterList parameterList = constructor.getExternalParameterList();
				parameterList.get(parameterList.size() - 1).setVarargs(true);
			}

			this.body.addConstructor(constructor);

			return new SimpleMethodVisitor(constructor);
		}

		if (this.isAnnotation())
		{
			final ClassParameter param = new ExternalClassParameter(this, Name.fromQualified(name),
			                                                        ClassFormat.readReturnType(desc),
			                                                        new FlagModifierSet(access));
			this.parameters.addParameter(param);
			return new AnnotationClassVisitor(param);
		}

		final ExternalMethod method = new ExternalMethod(this, name, desc, signature, new FlagModifierSet(access));

		if (signature != null)
		{
			method.setTypeParametric();
			ClassFormat.readMethodType(signature, method);
		}
		else
		{
			ClassFormat.readMethodType(desc, method);

			if (exceptions != null)
			{
				ClassFormat.readExceptions(exceptions, method);
			}
		}

		if ((access & Modifiers.VARARGS) != 0)
		{
			final IParameterList parameterList = method.getExternalParameterList();
			parameterList.get(parameterList.size() - 1).setVarargs(true);
		}

		this.body.addMethod(method);
		return new SimpleMethodVisitor(method);
	}

	public void visitInnerClass(String name, String outerName, String innerName)
	{
		if (innerName == null || !this.internalName.equals(outerName))
		{
			return;
		}

		if (this.innerTypes == null)
		{
			this.innerTypes = new HashMap<>();
		}

		this.innerTypes.put(innerName, name);
	}

	public void visitEnd()
	{
	}

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
	}

	@Override
	public void writeClassInit(MethodWriter writer) throws BytecodeException
	{
	}

	@Override
	public void writeStaticInit(MethodWriter writer) throws BytecodeException
	{
	}

	@Override
	public void writeInnerClassInfo(ClassWriter writer)
	{
	}

	@Override
	public void writeSignature(DataOutput out) throws IOException
	{
	}

	@Override
	public void readSignature(DataInput in) throws IOException
	{
	}
}
