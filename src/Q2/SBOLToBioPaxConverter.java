package Q2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.biopax.paxtools.impl.level3.ComplexImpl;
import org.biopax.paxtools.impl.level3.DnaImpl;
import org.biopax.paxtools.impl.level3.DnaRegionImpl;
import org.biopax.paxtools.impl.level3.ProteinImpl;
import org.biopax.paxtools.impl.level3.RnaImpl;
import org.biopax.paxtools.impl.level3.RnaRegionImpl;
import org.biopax.paxtools.impl.level3.SmallMoleculeImpl;
import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.BiochemicalReaction;
import org.biopax.paxtools.model.level3.Complex;
import org.biopax.paxtools.model.level3.DnaRegion;
import org.biopax.paxtools.model.level3.GeneticInteraction;
import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.Protein;
import org.biopax.paxtools.model.level3.Rna;
import org.biopax.paxtools.model.level3.SmallMolecule;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;

public class SBOLToBioPaxConverter {
	private static BioPAXFactory factory = null;
	private static Model model = null;

	public static void main(String[] args) throws SBOLValidationException, IOException, SBOLConversionException, URISyntaxException {
		SBOLToBioPaxConversion("GeneticToggleSBOL.xml");
	}
	
	public static void SBOLToBioPaxConversion(String fileToRead) throws SBOLValidationException, IOException, SBOLConversionException, URISyntaxException
	{
		//accept file to read into a buffer
		InputStream resourceAsStream = SBOLToBioPaxConverter.class.getResourceAsStream(fileToRead);
		if (resourceAsStream == null)
			resourceAsStream = SBOLToBioPaxConverter.class.getResourceAsStream("/" + fileToRead);

		assert resourceAsStream != null : "Failed to find test resource '" + fileToRead + "'";
		
		//create an SBOL Document and set initial data fields
		String prURI="http://www.async.ece.utah.edu";
		SBOLDocument doc = new SBOLDocument();
		doc.setDefaultURIprefix(prURI);
		doc.setTypesInURIs(true);
		doc.addNamespace(URI.create(prURI), prURI);
		
		//read in file to an SBOLDocument
		doc.read(resourceAsStream);
		
		//convert to BioPax
		factory = BioPAXLevel.L3.getDefaultFactory();
		model = factory.createModel();
		model.setXmlBase(prURI);
		
		BioPAXIOHandler io = new SimpleIOHandler();

		// convert CDs to PhysicalEntities
		for(ComponentDefinition cd : doc.getComponentDefinitions())
		{
			//make a BioPaxElement and add to model
			//TODO: check to make sure that a cd does have at least one type 		
			BioPAXElement elementToAdd = createPhysicalEntities(model, new URI(cd.getTypes().toArray()[0].toString()), cd.getIdentity());
			if(elementToAdd != null){
				System.out.println(elementToAdd);
			}
		}	
		// convert ModuleDefinitions to Pathways
		for(ModuleDefinition md : doc.getModuleDefinitions())
		{
			//each MD should have a new Pathway
			Pathway path = model.addNew(Pathway.class, md.getIdentity().toString());
			//add interactions and physicalEntities to each pathway for that md
			createInteractions(md, path);
			//iterate through modules within that md and create a nested pathway
		}
		// convert Participations to Genes
	}

	
	
	private static void createInteractions(ModuleDefinition md, Pathway path)
	{
		//conversion of Interactions only support the ontology terms as specified in SBOL Spec 
		for(org.sbolstandard.core2.Interaction interaction: md.getInteractions())
		{
			Interaction intToCreate = null;
			if(interaction.equals(URI.create("http://identifiers.org/biomodels.sbo/SBO:0000169")))
				intToCreate = model.addNew(GeneticInteraction.class, interaction.getIdentity().toString());
			else if (interaction.equals(URI.create("http://identifiers.org/biomodels.sbo/SBO:0000170")))
				intToCreate = model.addNew(GeneticInteraction.class, interaction.getIdentity().toString());
			else if(interaction.equals(URI.create("http://identifiers.org/biomodels.sbo/SBO:0000176")))
				intToCreate = model.addNew(BiochemicalReaction.class, interaction.getIdentity().toString());
			else if(interaction.equals(URI.create("http://identifiers.org/biomodels.sbo/SBO:0000177")))
				intToCreate = model.addNew(GeneticInteraction.class, interaction.getIdentity().toString());
			else if(interaction.equals(URI.create("http://identifiers.org/biomodels.sbo/SBO:0000179")))
				intToCreate = model.addNew(GeneticInteraction.class, interaction.getIdentity().toString());			
			else if(interaction.equals(URI.create("http://identifiers.org/biomodels.sbo/SBO:0000589")))
				intToCreate = model.addNew(GeneticInteraction.class, interaction.getIdentity().toString());			
			path.addPathwayComponent(intToCreate);	
		}
		//for(Module module : md.getModules())
	}
	
	

private static BioPAXElement createPhysicalEntities(Model model, URI cdType, URI identity)
		{
			BioPAXElement entityToCreate = null;
			if(cdType.equals(ComponentDefinition.PROTEIN))
				entityToCreate = model.addNew(Protein.class, identity.toString());
			else if(cdType.equals(ComponentDefinition.DNA))
				entityToCreate = model.addNew(DnaRegion.class, identity.toString());
			else if(cdType.equals(ComponentDefinition.SMALL_MOLECULE))
				entityToCreate = model.addNew(SmallMolecule.class, identity.toString());
			else if(cdType.equals(ComponentDefinition.RNA))
				entityToCreate = model.addNew(Rna.class, identity.toString());
			else if(cdType.equals(ComponentDefinition.COMPLEX))
				entityToCreate = model.addNew(Complex.class, identity.toString());
			
			return entityToCreate;
			
		}

}
