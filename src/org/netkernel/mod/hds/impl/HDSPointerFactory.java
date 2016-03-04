package org.netkernel.mod.hds.impl;

import java.util.Locale;

import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.NodePointerFactory;
import org.netkernel.mod.hds.IHDSNode;

class HDSPointerFactory implements NodePointerFactory
{
	@Override
	public NodePointer createNodePointer(QName aName, Object aNode, Locale aLocale)
	{	return (aNode instanceof IHDSNode)?new HDSNodePointer((IHDSNode)aNode):null;
	}

	@Override
	public NodePointer createNodePointer(NodePointer aParent, QName aName, Object aNode)
	{	return (aNode instanceof IHDSNode)?new HDSNodePointer((IHDSNode)aNode,aParent):null;
	}

	@Override
	public int getOrder()
	{	return 100;
	}
}
