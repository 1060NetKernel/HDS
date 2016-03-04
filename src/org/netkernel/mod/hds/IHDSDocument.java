package org.netkernel.mod.hds;

/**
 * @author tab
 * An whole HDS document ready to be used as an immutable represention.
 * HDS stands for Hierarchical Data Structure and is NetKernels universal
 * datatype. It is designed to a lightweight alternative to XML or JSON
 * for internal processing.
 */
public interface IHDSDocument
{
	/**
	 * @return the root node of this document. The root node will always have
	 * a null name and null value which may contain one or more child nodes. 
	 */
	IHDSNode getRootNode();
	
	/**
	 * @return a clone of this document wrapped in a mutator ready to make
	 * a variant of this document.
	 */
	IHDSMutator getMutableClone();
	
	/**
	 * @return return a ready ready to query this document 
	 */
	IHDSReader getReader();
}
