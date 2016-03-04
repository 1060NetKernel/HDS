package org.netkernel.mod.hds.transrept;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.nkf.INKFResponse;
import org.netkernel.layer0.nkf.INKFResponseReadOnly;
import org.netkernel.layer0.representation.IBinaryStreamRepresentation;
import org.netkernel.layer0.representation.IHDSNode;
import org.netkernel.layer0.representation.IReadableBinaryStreamRepresentation;
import org.netkernel.layer0.util.Utils;
import org.netkernel.mod.hds.IHDSDocument;
import org.netkernel.module.standard.endpoint.StandardTransreptorImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class ToHDSTransreptor extends StandardTransreptorImpl
{
	public ToHDSTransreptor()
	{	this.declareThreadSafe();
		this.declareToRepresentation(IHDSDocument.class);

		this.declareFromRepresentation(IReadableBinaryStreamRepresentation.class);
		this.declareFromRepresentation(String.class);
		this.declareFromRepresentation(Node.class);
		this.declareFromRepresentation(org.netkernel.layer0.representation.IHDSNode.class);
	}
	
	@Override
	public void onTransrept(INKFRequestContext aContext) throws Exception
	{
		INKFResponseReadOnly primaryResponse = aContext.getThisRequest().getPrimaryAsResponse();
		Object primary=primaryResponse.getRepresentation();

		IHDSDocument result=null;
		if (primary instanceof IReadableBinaryStreamRepresentation)
		{	IReadableBinaryStreamRepresentation bs=(IReadableBinaryStreamRepresentation)primary;
			result=HDSConversions.parseXML(bs.getInputStream());
		}
		else if (primary instanceof IBinaryStreamRepresentation)
		{	IBinaryStreamRepresentation bs=(IBinaryStreamRepresentation)primary;
			ByteArrayOutputStream baos=new ByteArrayOutputStream(2048);
			bs.write(baos);
			baos.flush();
			baos.close();
			result=HDSConversions.parseXML(new ByteArrayInputStream(baos.toByteArray()));
		}
		else if (primary instanceof String)
		{	String s=(String)primary;
			byte[] b=s.getBytes("UTF-8");
			result=HDSConversions.parseXML(new ByteArrayInputStream(b));
		}
		else if (primary instanceof Node)
		{	Node n=(Node)primary;
			result=HDSConversions.parseDOM(n);
		}
		else if (primary instanceof IHDSNode)
		{	IHDSNode n=(IHDSNode)primary;
			result=HDSConversions.convertHDS1(n);
		}
		INKFResponse responseOut=aContext.createResponseFrom(result);
	}
	
	
}
