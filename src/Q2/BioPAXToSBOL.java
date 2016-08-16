package Q2;

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
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.protein;
import org.biopax.paxtools.model.level3.BioSource;
import org.biopax.paxtools.model.level3.Complex;
import org.biopax.paxtools.model.level3.Dna;
import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.Gene;
import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.InteractionVocabulary;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.Protein;
import org.biopax.paxtools.model.level3.Rna;
import org.biopax.paxtools.model.level3.SmallMolecule;
import org.sbolstandard.core2.AccessType;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.DirectionType;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SBOLWriter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Converter from BioPAX level 3 to SBOL 2.0
 * 
 * @author Michael Zhang
 */
public class BioPAXToSBOL {
	static Map<Object, URI> entityMap = new HashMap<Object, URI>();

	static Map<String, URI> interactionMap = new HashMap<String, URI>();

	public static void main(String[] args) throws SBOLValidationException, IOException, SBOLConversionException {
		String fileToRead = "GeneticToggleBioPax.owl";
		InputStream resourceAsStream = BioPAXToSBOL.class.getResourceAsStream(fileToRead);
		if (resourceAsStream == null)
			resourceAsStream = SBOLToBioPaxConverter.class.getResourceAsStream("/" + fileToRead);

		assert resourceAsStream != null : "Failed to find test resource '" + fileToRead + "'";

		BioPAXIOHandler handler = new SimpleIOHandler();
		Model model = handler.convertFromOWL(resourceAsStream);

		entityMap.put(ProteinImpl.class, ComponentDefinition.PROTEIN);
		entityMap.put(DnaImpl.class, ComponentDefinition.DNA);
		entityMap.put(DnaRegionImpl.class, ComponentDefinition.DNA);
		entityMap.put(RnaImpl.class, ComponentDefinition.RNA);
		entityMap.put(RnaRegionImpl.class, ComponentDefinition.DNA);
		entityMap.put(ComplexImpl.class, ComponentDefinition.COMPLEX);
		entityMap.put(SmallMoleculeImpl.class, ComponentDefinition.SMALL_MOLECULE);

		// TODO correctness
		// inhibition
		interactionMap.put("MI:0623", URI.create("http://identifiers.org/biomodels.sbo/SBO:0000169"));
		// stimulation
		interactionMap.put("MI:0840", URI.create("http://identifiers.org/biomodels.sbo/SBO:0000170"));
		// biochemical reaction
		interactionMap.put("MI:0414", URI.create("http://identifiers.org/biomodels.sbo/SBO:0000176"));
		// non-covalent binding
		interactionMap.put("MI:0405", URI.create("http://identifiers.org/biomodels.sbo/SBO:0000177"));
		// degradation
		interactionMap.put("MI:0570", URI.create("http://identifiers.org/biomodels.sbo/SBO:0000179"));
		// genetic production
		interactionMap.put("MI:0935", URI.create("http://identifiers.org/biomodels.sbo/SBO:0000589"));

		SBOLDocument doc = convertToSBOL(model);
		SBOLWriter.write(doc, "outputSBOL");
	}

	private static SBOLDocument convertToSBOL(Model model) throws SBOLValidationException {
		SBOLDocument doc = new SBOLDocument();
		doc.setDefaultURIprefix(model.getXmlBase());

		for (PhysicalEntity entity : model.getObjects(PhysicalEntity.class)) {
			convertPhysicalEntity(doc, model, entity);
		}
		for (Gene gene : model.getObjects(Gene.class)) {
			convertGene(doc, model, gene);
		}
		for (Pathway pathway : model.getObjects(Pathway.class)) {
			convertPathways(doc, model, pathway);
		}

		return doc;
	}

	private static String getNameFromRDFId(String RDFId, String XmlBase) {
		return RDFId.substring(XmlBase.length());
	}

	private static void convertPhysicalEntity(SBOLDocument doc, Model model, PhysicalEntity entity)
			throws SBOLValidationException {
		doc.createComponentDefinition(getNameFromRDFId(entity.getRDFId(), model.getXmlBase()), "1",
				entityMap.get(entity.getClass()));
	}

	private static void convertGene(SBOLDocument doc, Model model, Gene gene) throws SBOLValidationException {
		// TODO unsure if this is the proper way to handle genes
		ComponentDefinition cd = doc.createComponentDefinition(getNameFromRDFId(gene.getRDFId(), model.getXmlBase()),
				"1", entityMap.get(gene.getClass()));
		// BioSource organism = gene.getOrganism();
		// cd.setDescription("This is an organism of tissue: " +
		// organism.getTissue().getTerm().toString()
		// + "and of cell type: " +
		// organism.getCellType().getTerm().toString());
	}

	private static void convertPathways(SBOLDocument doc, Model model, Pathway pathway) throws SBOLValidationException {
		ModuleDefinition md = doc.createModuleDefinition(getNameFromRDFId(pathway.getRDFId(), model.getXmlBase()), "1");
		// BioSource organism = pathway.getOrganism();
		// // TODO null pointer
		// md.setDescription("This is an organism of tissue: " +
		// organism.getTissue().getTerm().toString()
		// + "and of cell type: " +
		// organism.getCellType().getTerm().toString());
		for (org.biopax.paxtools.model.level3.Process process : pathway.getPathwayComponent()) {
			convertProcess(model, md, process);
		}
	}

	private static void convertProcess(Model model, ModuleDefinition md,
			org.biopax.paxtools.model.level3.Process process) throws SBOLValidationException {
		if (process instanceof Interaction) {
			convertInteraction(model, md, (Interaction) process);
		} else if (process instanceof Pathway) {
			// TODO nested Pathway
		} else {
			System.out.println("Process unable to be handled");
		}
	}

	private static void convertInteraction(Model model, ModuleDefinition md, Interaction interaction)
			throws SBOLValidationException {
		Set<InteractionVocabulary> types = interaction.getInteractionType();
		URI role;
		if (!types.isEmpty()) {
			role = interactionMap.get(types.iterator().next());
		} else {
			role = URI.create("Whatgoeshere");
		}
		org.sbolstandard.core2.Interaction sbolInteraction = md
				.createInteraction(getNameFromRDFId(interaction.getRDFId(), model.getXmlBase()), role);

		// deal with participants
		for (Entity participant : interaction.getParticipant()) {
			// create functional components
			FunctionalComponent fc = md.createFunctionalComponent(
					getNameFromRDFId(participant.getRDFId(), model.getXmlBase()) + "FC", AccessType.PUBLIC,
					URI.create(participant.getRDFId()), DirectionType.IN);
			// add as participants
			sbolInteraction.createParticipation(
					getNameFromRDFId(participant.getRDFId(), model.getXmlBase()) + "Participant", fc.getIdentity(),
					URI.create("whatgoeshere"));
		}
	}
}
