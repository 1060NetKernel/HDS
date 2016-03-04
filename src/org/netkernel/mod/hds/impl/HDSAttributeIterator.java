package org.netkernel.mod.hds.impl;

import java.util.*;

import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.netkernel.mod.hds.IHDSNode;

public class HDSAttributeIterator implements NodeIterator
{
	private List<IHDSNode> mAttributes;
	private int mPosition=0;
	private NodePointer mParent;
	
	public HDSAttributeIterator(QName aQName, IHDSNode aNode, NodePointer aParent)
	{
		mParent=aParent;
		String requestedName=aQName.getName();
		boolean wildCard=requestedName.equals("*");
		mAttributes=new ArrayList();
		for (IHDSNode n: aNode.getChildren())
		{	String name=n.getName();
			if (name!=null)
			{	if (wildCard)
				{	if (name.startsWith("@"))
					{	mAttributes.add(n);
					}
				}
				else
				{	if (name.equals("@"+requestedName))
					{	mAttributes.add(n);
					}
				}
			}
		}
	}

	@Override
	public int getPosition()
	{
		return mPosition;
	}

	@Override
	public boolean setPosition(int aPosition)
	{
		if (aPosition>0 && aPosition<=mAttributes.size())
		{	mPosition=aPosition;
			return true;
		}
		return false;
	}

	@Override
	public NodePointer getNodePointer()
	{
		if (mPosition == 0)
		{	setPosition(1);
        }
		NodePointer result=null;
		if (mPosition>=0)
		{	result=new HDSNodePointer(mAttributes.get(mPosition-1),mParent);
		}
		return result;
	}
}
