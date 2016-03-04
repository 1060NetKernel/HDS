package org.netkernel.mod.hds.impl;

import org.netkernel.mod.hds.IHDSNode;

public class HDSNodeImpl implements IHDSNode
{
	private static HDSNodeImpl[] NO_CHILDREN=new HDSNodeImpl[0];

	private final String mName;
	private final Object mValue;
	private IHDSNode[] mChildren=NO_CHILDREN;
	
	public HDSNodeImpl(String aName, Object aValue)
	{	mName=aName;
		mValue=aValue;
	}
		
	public void setChildren(IHDSNode[] aChildren)
	{	mChildren=aChildren;
	}
	
	@Override
	public String getName()
	{	return mName;
	}

	@Override
	public Object getValue()
	{	return mValue;
	}
	
	@Override
	public IHDSNode[] getChildren()
	{	return mChildren;
	}
	
	public String toString()
	{	StringBuilder b=new StringBuilder();
		recurseToString(b,this,0);
		return b.toString();
	}
	
	private static void recurseToString(StringBuilder b, IHDSNode aNode, int aDepth)
	{	boolean notNull=aNode.getValue()!=null;
		IHDSNode[] children=aNode.getChildren();
		boolean hasChildren=children.length>0;
		writeIndent(b,aDepth);
		{	b.append(aNode.getName());
			b.append(": ");
			if (notNull) b.append(aNode.getValue());
			if (hasChildren)
			{	for (IHDSNode child: children)
				{	b.append('\n');
					recurseToString(b,child,aDepth+1);
				}
			}
		}
	}
	
	private static void writeIndent(StringBuilder b, int aDepth)
	{	final String padding="                                                             ";
		int l=padding.length();
		int i=l-aDepth*2;
		if (i<0) i=0;
		b.append(padding.substring(i, l));
	}
}
