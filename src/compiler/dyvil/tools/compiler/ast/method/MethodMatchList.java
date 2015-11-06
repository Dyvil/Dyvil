package dyvil.tools.compiler.ast.method;

public final class MethodMatchList
{
	private IMethod[]	methods	= new IMethod[4];
	private float[]		values	= new float[4];
	private int			size;
	
	public int size()
	{
		return this.size;
	}
	
	public boolean isEmpty()
	{
		return this.size <= 0;
	}
	
	public void ensureCapacity(int capacity)
	{
		if (capacity <= this.methods.length)
		{
			return;
		}
		
		IMethod[] tempMethods = new IMethod[capacity];
		System.arraycopy(this.methods, 0, tempMethods, 0, this.size);
		this.methods = tempMethods;
		
		float[] tempValues = new float[capacity];
		System.arraycopy(this.values, 0, tempValues, 0, this.size);
		this.values = tempValues;
	}
	
	public void add(IMethod method, float match)
	{
		this.ensureCapacity(this.size + 1);
		this.methods[this.size] = method;
		this.values[this.size] = match;
		this.size++;
	}
	
	public IMethod getBestMethod()
	{
		switch (this.size)
		{
		case 0:
			return null;
		case 1:
			return this.methods[0];
		}
		
		IMethod bestMethod = this.methods[0];
		float bestMatch = this.values[0];
		for (int i = 1; i < this.size; i++)
		{
			float match = this.values[i];
			if (match < bestMatch)
			{
				bestMethod = this.methods[i];
				bestMatch = match;
			}
		}
		
		return bestMethod;
	}
}
