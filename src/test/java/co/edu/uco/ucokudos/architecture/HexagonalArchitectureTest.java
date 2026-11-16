package co.edu.uco.ucokudos.architecture;

import co.edu.uco.ucokudos.infrastructure.input.controller.PushController;
import co.edu.uco.ucokudos.infrastructure.input.scheduler.MessageCatalogRefreshScheduler;
import co.edu.uco.ucokudos.infrastructure.output.message.RemoteMessageSource;
import co.edu.uco.ucokudos.infrastructure.output.notification.PushNotificationSender;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@AnalyzeClasses(locations = ArchUnitClassesLocation.class, importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureTest {

    private static final String DOMAIN = "co.edu.uco.ucokudos.domain..";
    private static final String SERVICE = "co.edu.uco.ucokudos.service..";
    private static final String INPUT = "co.edu.uco.ucokudos.infrastructure.input..";
    private static final String OUTPUT = "co.edu.uco.ucokudos.infrastructure.output..";
    private static final String BOOTSTRAP = "co.edu.uco.ucokudos.bootstrap..";

    @ArchTest
    static final ArchRule theDependencyRuleHoldsBetweenLayers = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .layer("Domain").definedBy(DOMAIN)
            .layer("Service").definedBy(SERVICE)
            .layer("Input adapters").definedBy(INPUT)
            .layer("Output adapters").definedBy(OUTPUT)
            .layer("Bootstrap").definedBy(BOOTSTRAP)
            .whereLayer("Bootstrap").mayNotBeAccessedByAnyLayer()
            .whereLayer("Input adapters").mayNotBeAccessedByAnyLayer()
            .whereLayer("Output adapters").mayOnlyBeAccessedByLayers("Bootstrap")
            .whereLayer("Service").mayOnlyBeAccessedByLayers("Input adapters", "Output adapters", "Bootstrap")
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Service", "Input adapters", "Output adapters", "Bootstrap")
            .ignoreDependency(PushController.class, PushNotificationSender.class)
            .ignoreDependency(MessageCatalogRefreshScheduler.class, RemoteMessageSource.class)
            .as("The dependency points inwards: adapters and bootstrap depend on the service, the service on the domain");

    @ArchTest
    static final ArchRule theDomainDoesNotKnowTheOtherLayers = noClasses().that().resideInAPackage(DOMAIN)
            .should().dependOnClassesThat().resideInAnyPackage(SERVICE, INPUT, OUTPUT, BOOTSTRAP);

    @ArchTest
    static final ArchRule theDomainIsFreeOfFrameworks = noClasses().that().resideInAPackage(DOMAIN)
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta..", "org.hibernate..", "com.fasterxml..", "tools.jackson..",
                    "org.mapstruct..");

    @ArchTest
    static final ArchRule theServiceDoesNotKnowTheAdaptersNorTheBootstrap = noClasses().that().resideInAPackage(SERVICE)
            .should().dependOnClassesThat().resideInAnyPackage(INPUT, OUTPUT, BOOTSTRAP);

    @ArchTest
    static final ArchRule theServiceOnlyUsesTheSpringTransactionAndValidationAnnotations = noClasses()
            .that().resideInAPackage(SERVICE)
            .should().dependOnClassesThat(
                    resideInAnyPackage("org.springframework..", "jakarta..", "org.hibernate..")
                            .and(not(
                                    resideInAnyPackage("org.springframework.transaction..", "jakarta.validation.."))))
            .as("The service uses no framework except transaction and validation annotations");

    @ArchTest
    static final ArchRule inputAdaptersDoNotCallOutputAdapters = noClasses().that().resideInAPackage(INPUT)
            .and().haveSimpleNameNotEndingWith("PushController")
            .and().haveSimpleNameNotEndingWith("MessageCatalogRefreshScheduler")
            .should().dependOnClassesThat().resideInAPackage(OUTPUT)
            .as("Input adapters reach the outside only through the service (PushController and "
                    + "MessageCatalogRefreshScheduler are the two declared exceptions)");

    @ArchTest
    static final ArchRule outputAdaptersDoNotCallInputAdapters = noClasses().that().resideInAPackage(OUTPUT)
            .should().dependOnClassesThat().resideInAPackage(INPUT);

    @ArchTest
    static final ArchRule controllersTalkToInputPortsNotToUseCases = noClasses()
            .that().resideInAPackage("co.edu.uco.ucokudos.infrastructure.input.controller..")
            .should().dependOnClassesThat().resideInAPackage("co.edu.uco.ucokudos.service.usecase..");

    @ArchTest
    static final ArchRule portsAreInterfaces = classes().that().resideInAPackage("co.edu.uco.ucokudos.service..port..")
            .and().areNotRecords().and().areNotEnums()
            .should().beInterfaces();

    @ArchTest
    static final ArchRule useCasesLiveInTheServiceLayer = classes().that().haveSimpleNameEndingWith("UseCase")
            .should().resideInAPackage("co.edu.uco.ucokudos.service.usecase..");

    @ArchTest
    static final ArchRule adaptersThatImplementOutputPortsLiveInInfrastructureOutput = classes()
            .that().areAssignableTo(resideInAPackage("co.edu.uco.ucokudos.service.output.port.."))
            .and().areNotInterfaces().and().areNotRecords().and().areNotEnums()
            .should().resideInAnyPackage(OUTPUT, "co.edu.uco.ucokudos.service..");
}
