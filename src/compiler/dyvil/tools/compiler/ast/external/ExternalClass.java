package dyvil.tools.compiler.ast.external;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import dyvil.collection.List;
import dyvil.collection.mutable.ArrayList;
import dyvil.reflect.Modifiers;
import dyvil.tools.asm.*;
import dyvil.tools.compiler.ast.access.MethodCall;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.AbstractClass;
import dyvil.tools.compiler.ast.classes.ClassBody;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.classes.IClassMetadata;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.Array;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.generic.Variance;
import dyvil.tools.compiler.ast.generic.type.ClassGenericType;
import dyvil.tools.compiler.ast.generic.type.TypeVarType;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.Constructor;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.ClassParameter;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.ClassType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.backend.visitor.*;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class ExternalClass extends AbstractClass
{
	public Package thePackage;
	
	private List<IType> innerTypes;
	
	private boolean	metadataResolved;
	private boolean	superTypesResolved;
	private boolean	genericsResolved;
	private boolean	parametersResolved;
	private boolean	annotationsResolved;
	private boolean	innerTypesResolved;
	
	private String[] classParameters;
	
	public ExternalClass(Name name)
	{
		this.name = name;
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
	
	private void resolveMetadata()
	{
		this.metadata = IClass.getClassMetadata(this, this.modifiers);
		this.metadata.resolve(null, this);
	}
	
	private void resolveGenerics()
	{
		this.genericsResolved = true;
		if (this.genericCount > 0)
		{
			ClassGenericType type = new ClassGenericType(this);
			
			for (int i = 0; i < this.genericCount; i++)
			{
				ITypeVariable var = this.generics[i];
				var.resolveTypes(null, Package.rootPackage);
				type.addType(new TypeVarType(var));
			}
			
			this.type = type;
		}
	}
	
	private void resolveParameters()
	{
		this.parametersResolved = true;
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].resolveTypes(null, this);
		}
	}
	
	private void resolveSuperTypes()
	{
		this.superTypesResolved = true;
		if (this.superType != null)
		{
			this.superType = this.superType.resolveType(null, this);
		}
		
		for (int i = 0; i < this.interfaceCount; i++)
		{
			this.interfaces[i] = this.interfaces[i].resolveType(null, this);
		}
		
		if (!this.metadataResolved)
		{
			this.resolveMetadata();
		}
	}
	
	private void resolveAnnotations()
	{
		this.annotationsResolved = true;
		if (this.annotations != null)
		{
			this.annotations.resolveTypes(null, this, this);
		}
	}
	
	private void resolveInnerTypes()
	{
		this.innerTypesResolved = true;
		
		if (this.innerTypes != null)
		{
			int len = this.innerTypes.size();
			for (int i = 0; i < len; i++)
			{
				IType t = this.innerTypes.get(i);
				this.innerTypes.set(i, t.resolveType(null, Package.rootPackage));
				t.getTheClass().setOuterClass(this);
			}
		}
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
		if (!this.genericsResolved)
		{
			this.resolveGenerics();
		}
		return this.type;
	}
	
	@Override
	public IClass getThisClass()
	{
		if (!this.genericsResolved)
		{
			this.resolveGenerics();
		}
		return this;
	}
	
	@Override
	public IType getSuperType()
	{
		if (!this.superTypesResolved)
		{
			this.resolveSuperTypes();
		}
		return this.superType;
	}
	
	@Override
	public IClass getOuterClass()
	{
		if (!this.innerTypesResolved)
		{
			this.resolveInnerTypes();
		}
		return super.getOuterClass();
	}
	
	@Override
	public boolean isSubTypeOf(IType type)
	{
		if (!this.superTypesResolved)
		{
			this.resolveSuperTypes();
		}
		return super.isSubTypeOf(type);
	}
	
	@Override
	public int getSuperTypeDistance(IType superType)
	{
		if (!this.superTypesResolved)
		{
			this.resolveSuperTypes();
		}
		return super.getSuperTypeDistance(superType);
	}
	
	@Override
	public ITypeVariable getTypeVariable(int index)
	{
		if (!this.genericsResolved)
		{
			this.resolveGenerics();
		}
		return super.getTypeVariable(index);
	}
	
	@Override
	public IAnnotation getAnnotation(IClass type)
	{
		if (!this.annotationsResolved)
		{
			this.resolveAnnotations();
		}
		return super.getAnnotation(type);
	}
	
	@Override
	protected void readClassParameters(IAnnotation annotation)
	{
		Array array = (Array) annotation.getArguments().getFirstValue();
		int params = array.valueCount();
		this.classParameters = new String[params];
		for (int i = 0; i < params; i++)
		{
			IValue value = array.getValue(i);
			this.classParameters[i] = value.stringValue();
		}
	}
	
	@Override
	public IParameter getParameter(int index)
	{
		if (!this.parametersResolved)
		{
			this.resolveParameters();
		}
		return super.getParameter(index);
	}
	
	@Override
	public IType resolveType(ITypeVariable typeVar, IType concrete)
	{
		if (!this.genericsResolved)
		{
			this.resolveGenerics();
		}
		if (!this.superTypesResolved)
		{
			this.resolveSuperTypes();
		}
		return super.resolveType(typeVar, concrete);
	}
	
	@Override
	public IClassMetadata getMetadata()
	{
		if (!this.metadataResolved)
		{
			this.resolveMetadata();
		}
		return this.metadata;
	}
	
	@Override
	public IMethod getFunctionalMethod()
	{
		if ((this.modifiers & Modifiers.ABSTRACT | Modifiers.INTERFACE_CLASS) == 0)
		{
			return null;
		}
		
		if (!this.genericsResolved)
		{
			this.resolveGenerics();
		}
		
		IMethod m;
		if (this.body != null)
		{
			m = this.body.getFunctionalMethod();
			if (m != null)
			{
				return m;
			}
		}
		
		if (!this.superTypesResolved)
		{
			this.resolveSuperTypes();
		}
		
		if (this.superType != null)
		{
			m = this.superType.getFunctionalMethod();
			if (m != null)
			{
				return m;
			}
		}
		
		for (int i = 0; i < this.interfaceCount; i++)
		{
			m = this.interfaces[i].getFunctionalMethod();
			if (m != null)
			{
				return m;
			}
		}
		
		return null;
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
	public void checkMethods(MarkerList markers, IClass iclass, ITypeContext typeContext)
	{
		if (!this.genericsResolved)
		{
			this.resolveGenerics();
		}
		if (!this.superTypesResolved)
		{
			this.resolveSuperTypes();
		}
		super.checkMethods(markers, iclass, typeContext);
	}
	
	@Override
	public void checkSuperMethods(MarkerList markers, IClass iclass)
	{
		if (!this.superTypesResolved)
		{
			this.resolveSuperTypes();
		}
		super.checkSuperMethods(markers, iclass);
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
		if (!this.innerTypesResolved)
		{
			this.resolveInnerTypes();
		}
		
		if (this.innerTypes != null)
		{
			for (IType t : this.innerTypes)
			{
				if (t.getName() == name)
				{
					return t.getTheClass();
				}
			}
		}
		
		return null;
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		if (!this.genericsResolved)
		{
			this.resolveGenerics();
		}
		
		if (!this.parametersResolved)
		{
			this.resolveParameters();
		}
		for (int i = 0; i < this.parameterCount; i++)
		{
			IParameter param = this.parameters[i];
			if (param.getName() == name)
			{
				return param;
			}
		}
		
		// Own properties
		IDataMember field = this.body.getProperty(name);
		if (field != null)
		{
			return field;
		}
		
		// Own fields
		field = this.body.getField(name);
		if (field != null)
		{
			return field;
		}
		
		if (!this.superTypesResolved)
		{
			this.resolveSuperTypes();
		}
		
		// Inherited Fields
		if (this.superType != null)
		{
			IDataMember match = this.superType.resolveField(name);
			if (match != null)
			{
				return match;
			}
		}
		return null;
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
		if (!this.genericsResolved)
		{
			this.resolveGenerics();
		}
		
		this.body.getMethodMatches(list, instance, name, arguments);
		
		if (!list.isEmpty())
		{
			return;
		}
		
		if (!this.superTypesResolved)
		{
			this.resolveSuperTypes();
		}
		
		if (this.superType != null)
		{
			this.superType.getMethodMatches(list, instance, name, arguments);
		}
		
		if (!list.isEmpty())
		{
			return;
		}
		
		for (int i = 0; i < this.interfaceCount; i++)
		{
			this.interfaces[i].getMethodMatches(list, instance, name, arguments);
		}
	}
	
	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
	{
		if (!this.superTypesResolved)
		{
			this.resolveSuperTypes();
		}
		
		this.body.getConstructorMatches(list, arguments);
	}
	
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
	{
		this.modifiers = access;
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
			this.name = Name.getQualified(name);
			this.thePackage = Package.rootPackage;
			this.fullName = name;
		}
		else
		{
			this.name = Name.getQualified(name.substring(index + 1));
			this.fullName = name.replace('/', '.');
			this.thePackage = Package.rootPackage.resolvePackage(Name.getQualified(this.fullName.substring(0, index)));
		}
		
		if (signature != null)
		{
			this.generics = new ITypeVariable[2];
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
		
		this.type = new ClassType(this);
	}
	
	public AnnotationVisitor visitAnnotation(String type, boolean visible)
	{
		String internal = ClassFormat.extendedToInternal(type);
		if (this.addRawAnnotation(internal, null))
		{
			if (this.annotations == null)
			{
				this.annotations = new AnnotationList();
			}
			
			Annotation annotation = new Annotation(null, ClassFormat.internalToType(internal));
			return new AnnotationVisitorImpl(this, annotation);
		}
		return null;
	}
	
	public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible)
	{
		switch (TypeReference.getSort(typeRef))
		{
		// TODO implement other sorts
		case TypeReference.CLASS_TYPE_PARAMETER:
			ITypeVariable typeVar = this.generics[TypeReference.getTypeParameterIndex(typeRef)];
			switch (desc)
			{
			case "Ldyvil/annotation/Covariant;":
				typeVar.setVariance(Variance.COVARIANT);
				break;
			case "Ldyvil/annotation/Contravariant;":
				typeVar.setVariance(Variance.CONTRAVARIANT);
				break;
			}
			// TODO implement other type parameter annotations
		}
		return null;
	}
	
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value)
	{
		IType type = ClassFormat.extendedToType(signature == null ? desc : signature);
		
		if (this.classParameters != null)
		{
			for (String s : this.classParameters)
			{
				if (s.equals(name))
				{
					ClassParameter param = new ClassParameter(this, Name.get(name), type, access);
					this.addParameter(param);
					return new SimpleFieldVisitor(param);
				}
			}
		}
		
		ExternalField field = new ExternalField(this, access, Name.get(name), type);
		
		if (value != null)
		{
			field.setValue(IValue.fromObject(value));
		}
		
		if ((this.modifiers & Modifiers.OBJECT_CLASS) != 0 && name.equals("instance"))
		{
			// This is the instance field of a singleton object class
			this.getMetadata().setInstanceField(field);
		}
		else
		{
			this.body.addField(field);
		}
		
		return new SimpleFieldVisitor(field);
	}
	
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
	{
		Name name1 = Name.get(name);
		
		if ((this.modifiers & Modifiers.ANNOTATION) == Modifiers.ANNOTATION)
		{
			ClassParameter param = new ClassParameter(this, name1, ClassFormat.readReturnType(desc), access);
			this.addParameter(param);
			return new AnnotationClassVisitor(param);
		}
		
		if ((access & Modifiers.SYNTHETIC) != 0)
		{
			return null;
		}
		
		if ("<init>".equals(name))
		{
			Constructor constructor = new ExternalConstructor(this);
			constructor.setModifiers(access);
			
			ClassFormat.readConstructorType(desc, constructor);
			
			if ((access & Modifiers.VARARGS) != 0)
			{
				constructor.getParameter(constructor.parameterCount() - 1).setVarargs(true);
			}
			
			this.body.addConstructor(constructor);
			
			return new SimpleMethodVisitor(constructor);
		}
		
		ExternalMethod method = new ExternalMethod(this, name1, desc, access);
		
		if (signature != null)
		{
			method.setGeneric();
			ClassFormat.readMethodType(signature, method);
		}
		else
		{
			ClassFormat.readMethodType(desc, method);
			
			if (exceptions != null)
			{
				for (String s : exceptions)
				{
					method.addException(ClassFormat.internalToType(s));
				}
			}
		}
		
		if ((access & Modifiers.VARARGS) != 0)
		{
			method.setVarargsParameter();
		}
		
		if (name.startsWith("parDefault$"))
		{
			int i = name.indexOf('$', 12);
			Name methodName = Name.getQualified(name.substring(12, i));
			IMethod targetMethod = this.body.getMethod(methodName);
			int parIndex = Integer.parseInt(name.substring(i + 1));
			
			MethodCall call = new MethodCall(null, null, method, EmptyArguments.INSTANCE);
			targetMethod.getParameter(parIndex).setValue(call);
			return new BytecodeVisitor(method);
		}
		
		this.body.addMethod(method);
		return new BytecodeVisitor(method);
	}
	
	public void visitInnerClass(String name, String outerName, String innerName, int access)
	{
		if (this.innerTypes == null)
		{
			this.innerTypes = new ArrayList(1);
		}
		
		IType type = ClassFormat.internalToType(name);
		this.innerTypes.add(type);
	}
	
	public void visitEnd()
	{
	}
	
	@Override
	public void write(ClassWriter writer) throws BytecodeException
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
