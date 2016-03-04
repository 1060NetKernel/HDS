package org.netkernel.mod.hds.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.netkernel.mod.hds.IHDSNode;

class HDSNodePointer extends NodePointer
{
	static
	{	JXPathContextReferenceImpl.addNodePointerFactory(new HDSPointerFactory());
	}
	
	private final IHDSNode mNode;
	
	public HDSNodePointer(IHDSNode aNode)
	{	this(aNode,null);
	}
	public HDSNodePointer(IHDSNode aNode,NodePointer aParent)
	{	super(aParent);
		mNode=aNode;
	}
	
	@Override
	public int compareChildNodePointers(NodePointer arg0, NodePointer arg1)
	{	//not sure this is needed
		return 0;
	}
	
	public boolean equals(Object aOther)
	{	return (aOther instanceof HDSNodePointer && ((HDSNodePointer)aOther).mNode==mNode);
	}
	
	public int hashCode()
	{	return mNode.hashCode();
	}

	@Override
	public Object getBaseValue()
	{	return mNode;
	}

	@Override
	public Object getImmediateNode()
	{	return mNode;
	}

	@Override
	public int getLength()
	{	return 1;
	}

	@Override
	public QName getName()
	{	String name=mNode.getName();
		if (name==null) name="null";
		int i=name.indexOf(':');
		if (i>=0)
		{	String prefix=name.substring(0, i);
			name=name.substring(i+1,name.length());
			return new QName(prefix,name);
		}
		else
		{	return new QName(name);
		}
	}

	@Override
	public boolean isCollection()
	{	return false;
	}

	@Override
	public boolean isLeaf()
	{	return mNode.getChildren().length==0;
	}
	
	public boolean isAttribute()
	{	String name=mNode.getName();
		return name!=null && name.startsWith("@");
    }

	@Override
	public void setValue(Object arg0)
	{	throw new UnsupportedOperationException("trying to set value on IHDSNode");
	}
	
	public Object getValue()
	{	return mNode.getValue();
	}
	
	public NodeIterator childIterator(NodeTest aTest, boolean aReverse, NodePointer aStartWith)
	{	return new HDSNodeIterator(this,mNode,aTest,aReverse,aStartWith);
	}
	
	public NodeIterator attributeIterator(QName qname)
	{	return new HDSAttributeIterator(qname,mNode,this);
    }
	
	public String asPath()
	{	
		NodePointer p=this;
		List<HDSNodePointer> pointers=new ArrayList();
		while (p!=null && p instanceof HDSNodePointer)
		{	pointers.add((HDSNodePointer)p);
			p=p.getParent();
		}
		StringBuilder sb=new StringBuilder();
		for (int i=pointers.size()-1; i>=0; i--)
		{	IHDSNode node=pointers.get(i).mNode;
			if (node.getName()==null) continue;
			int childPosition=-1;
			int count=0;
			if (i<pointers.size()-1)
			{	IHDSNode parentNode=pointers.get(i+1).mNode;
				int len=parentNode.getChildren().length;
				for (int j=0; j<len; j++)
				{	IHDSNode child=parentNode.getChildren()[j];
					if (child.getName().equals(node.getName()))
					{	count++;
						if (child==node)
						{	childPosition=count;
						}
					}
				}
			}
			sb.append('/');
			sb.append(node.getName());
			if (count>1 && childPosition>=1)
			{	sb.append('[');
				sb.append(childPosition);
				sb.append(']');
			}	
		}
		return sb.toString();
	}
}