package Q2;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;

public class SBOLToBioPaxConverter {

	public static void main(String[] args) throws SBOLValidationException, IOException, SBOLConversionException {
		
		SBOLDocument doc = SBOLToBioPaxConversion("AnnotationOutput.rdf");
		for(ComponentDefinition cd : doc.getComponentDefinitions())
		{
			 System.out.println(cd.getDisplayId());
		}
	}
	
	
	public static SBOLDocument SBOLToBioPaxConversion(String fileToRead) throws SBOLValidationException, IOException, SBOLConversionException
	{
		//accept file to read into a buffer
		InputStream resourceAsStream = SBOLToBioPaxConverter.class.getResourceAsStream(fileToRead);
		if (resourceAsStream == null)
			resourceAsStream = SBOLToBioPaxConverter.class.getResourceAsStream("/" + fileToRead);

		assert resourceAsStream != null : "Failed to find test resource '" + fileToRead + "'";
		
		//create an SBOL Document and set initial data fields
		String prURI="http://partsregistry.org";
		SBOLDocument doc = new SBOLDocument();
		doc.setDefaultURIprefix(prURI);
		doc.setTypesInURIs(true);
		doc.addNamespace(URI.create(prURI), prURI);
		
		//read in file to an SBOLDocument
		doc.read(resourceAsStream);
		
		return doc;
		
		
		
	}


}
