package org.netkernel.mod.hds;

import org.netkernel.mod.hds.impl.HDSMutatorImpl;

public class HDSFactory
{	
	/**
	 * @return a new empty HDS document
	 */
	public static IHDSMutator newDocument()
	{	return new HDSMutatorImpl();
	}

}
