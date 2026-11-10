package co.com.pragma.accounts.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Reglas de arquitectura hexagonal imperativa (Spring MVC). Importa el paquete
 * raiz (recorre los jars de los submodulos en el classpath de app-service) para
 * evaluar las clases de domain/infra.
 */
class HexagonalArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_PACKAGE_INFOS)
            .importPackages("co.com.pragma.accounts");
    }

    // El dominio son los paquetes model + ports + usecase.
    private static final String[] DOMAIN_PACKAGES = {"..model..", "..ports..", "..usecase.."};

    @Test
    void domainMustNotDependOnInfrastructure() {
        noClasses()
            .that().resideInAnyPackage(DOMAIN_PACKAGES)
            .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
            .check(classes);
    }

    @Test
    void domainMustNotDependOnFrameworkNorPersistence() {
        noClasses()
            .that().resideInAnyPackage(DOMAIN_PACKAGES)
            .should().dependOnClassesThat()
            .resideInAnyPackage("org.springframework..", "jakarta.persistence..")
            .check(classes);
    }

    @Test
    void domainMustNotDependOnApplication() {
        noClasses()
            .that().resideInAnyPackage(DOMAIN_PACKAGES)
            .should().dependOnClassesThat().resideInAPackage("..application..")
            .check(classes);
    }

    @Test
    void portsMustBeInterfaces() {
        classes()
            .that().resideInAPackage("..ports..")
            .should().beInterfaces()
            .check(classes);
    }

    @Test
    void portsMustHaveIPrefix() {
        classes()
            .that().resideInAPackage("..ports..")
            .should().haveSimpleNameStartingWith("I")
            .check(classes);
    }

    @Test
    void gatewaysMustHaveIPrefixAndGatewaySuffix() {
        classes()
            .that().resideInAPackage("..ports..")
            .and().haveSimpleNameEndingWith("Gateway")
            .should().haveSimpleNameStartingWith("I")
            .check(classes);
    }

    @Test
    void usecasesMustNotUseSpringAnnotations() {
        noClasses()
            .that().resideInAPackage("..usecase..")
            .should().beAnnotatedWith("org.springframework.stereotype.Service")
            .orShould().beAnnotatedWith("org.springframework.stereotype.Component")
            .check(classes);
    }

    @Test
    void usecasesMustEndWithUseCase() {
        classes()
            .that().resideInAPackage("..usecase..")
            .should().haveSimpleNameEndingWith("UseCase")
            .check(classes);
    }

    /** Estandar imperativo: los entry-points REST usan @RestController. */
    @Test
    void controllersMustBeRestControllers() {
        classes()
            .that().resideInAPackage("..infrastructure.entrypoint..")
            .and().haveSimpleNameEndingWith("Controller")
            .should().beAnnotatedWith("org.springframework.web.bind.annotation.RestController")
            .check(classes);
    }

    /** Prohibicion del estandar imperativo: NUNCA WebClient (es de WebFlux). */
    @Test
    void noWebClientAllowed() {
        noClasses()
            .that().resideInAnyPackage("co.com.pragma.accounts..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("org.springframework.web.reactive.function.client..")
            .check(classes);
    }

    @Test
    void entryPointsMustNotDependOnDrivenAdapters() {
        noClasses()
            .that().resideInAPackage("..infrastructure.entrypoint..")
            .should().dependOnClassesThat().resideInAPackage("..infrastructure.persistence..")
            .orShould().dependOnClassesThat().resideInAPackage("..infrastructure.client..")
            .check(classes);
    }
}
