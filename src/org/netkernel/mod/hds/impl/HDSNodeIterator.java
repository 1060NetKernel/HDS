package org.netkernel.mod.hds.impl;

import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.netkernel.mod.hds.IHDSNode;

class HDSNodeIterator implements NodeIterator
{
	private final NodePointer mParent;
	private final IHDSNode mNode;
	private final NodeTest mTest;
	private final boolean mReverse;
	private int mPosition;
	private int mParentPosition=-1;
	
	public HDSNodeIterator(NodePointer aParent, IHDSNode aNode, NodeTest aTest, boolean aReverse, NodePointer aStartWith)
	{	mParent=aParent;
		mNode=aNode;
		mTest=aTest;
		mReverse=aReverse;
		mPosition=0;
		int p=0;
		if (aStartWith!=null)
		{	HDSNodeImpl startWith=(HDSNodeImpl)(aStartWith.getNode());
			for (IHDSNode node : mNode.getChildren())
			{	if (node==startWith)
				{	mParentPosition=p;
					break;
				}
				p++;
			}
		}
	}
	
	@Override
	public int getPosition()
	{	return mPosition;
	}

	@Override
	public NodePointer getNodePointer()
	{	if (mPosition == 0)
		{	setPosition(1);
        }
        NodePointer result= (mParentPosition<0 || mParentPosition>=mNode.getChildren().length) ? null : new HDSNodePointer(mNode.getChildren()[mParentPosition],mParent);
        return result;
	}

	@Override
	public boolean setPosition(int aPosition) {
        while (this.mPosition < aPosition) {
            if (!next()) {
                return false;
            }
        }
        while (this.mPosition > aPosition) {
            if (!previous()) {
                return false;
            }
        }
        return true;
    }

	private boolean previous()
	{
		mPosition--;
        if (!mReverse) {
            if (mPosition == 0) {
            	mParentPosition=-1;
            }
            else if (mParentPosition<0) {
            	mParentPosition=mNode.getChildren().length-1;
            }
            else {
            	mParentPosition--;
            }
            while (mParentPosition>=0 && !testChild()) {
            	mParentPosition--;
            }
        }
        else {
        	mParentPosition++;
            while (mParentPosition<mNode.getChildren().length && !testChild()) {
            	mParentPosition++;
            }
        }
        
        boolean result= mParentPosition>=0 && mParentPosition<mNode.getChildren().length;
        return result;
	}
	
	private boolean next() {
        mPosition++;
        if (!mReverse) {
            if (mPosition == 1) {
                if (mParentPosition<0) {
                	mParentPosition=0;
                }
                else {
                	mParentPosition++;
                }
            }
            else {
            	mParentPosition++;
            }
            while (mParentPosition<mNode.getChildren().length && !testChild()) {
            	mParentPosition++;
            }
        }
        else {
        	
            if (mPosition == 1) {
                if (mParentPosition< 0) {
                	mParentPosition=mNode.getChildren().length-1;
                }
                else {
                	mParentPosition--;
                }
            }
            else {
            	mParentPosition--;
            }
            while (mParentPosition>=0 && !testChild()) {
            	mParentPosition--;
            }
            
        }
        boolean result= mParentPosition>=0 && mParentPosition<mNode.getChildren().length;
        return result;
    }
	
	private boolean testChild()
	{
		if (mTest instanceof NodeNameTest)
		{	NodeNameTest nodeNameTest = (NodeNameTest) mTest;
			IHDSNode child=mNode.getChildren()[mParentPosition];
			if (nodeNameTest.isWildcard())
			{	return !child.getName().startsWith("@");
			}
			else
			{	String name=child.getName();
				String prefix=null;
				if (name!=null)
				{
					int i=name.indexOf(':');
					if (i>=0)
					{	prefix=name.substring(0,i);
						name=name.substring(i+1, name.length());
					}
				}
				QName expected=nodeNameTest.getNodeName();
				String expectedName=expected.getName();
				if (expectedName.equals(name))
				{	String expectedPrefix=expected.getPrefix();
					return (expectedPrefix==null && prefix==null) || (expectedPrefix!=null && expectedPrefix.equals(prefix));
				}
				else
				{	return false;
				}
			}
		}
		else if (mTest instanceof NodeTypeTest)
		{
			return true;
		}
		else
		{	return true;
		}
	}
}
