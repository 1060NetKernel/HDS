package org.netkernel.mod.hds;

/**
 * @author tab
 * The underlying node interface
 */
public interface IHDSNode
{
	/**
	 * @return the name of the node
	 */
	String getName();
	/**
	 * @return the value held by the node. It may be null.
	 */
	Object getValue();
	
	/**
	 * @return return array of children !DO NOT MODIFY MEMBERS!
	 */
	IHDSNode[] getChildren();
}
