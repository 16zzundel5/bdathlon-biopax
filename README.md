# bdathlon-biopax
BDAthlon question2

We have created a converter that round-trips from SBOL to BioPAX and BioPAX to SBOL.  Currently we have support for:
SBOL:
-ComponentDefinitions
-ModuleDefinitions
-Modules
-Interactions
-Participations

BioPAX:
-PhysicalEntities
-Interactions
-Pathways
-Genes (from BioPAX to SBOL)
-Participations (input/output not correct, but present)

The parts of either data model that cannot be represented in the other are converted by giving some SBOL objects descriptions.  This could be done more robustly in the future using annotations. 

To run the converters, import both projects in Eclipse, and run the main method in whatever converter you wish to use.  The file you want to convert should be located in the same directory and the src (.java) files, and the converted output will be in the root folder of the project.  

The coding requirements were mostly met as Biopax was imported as a Maven project and the latest version of sbol was used. However we were unable to commit our project as a Maven Project, but some future considerations that we are planning include finishing converting all aspects of both data models including supporting remaining SBOL data through annotations in BioPax to minimize lossyness. 
