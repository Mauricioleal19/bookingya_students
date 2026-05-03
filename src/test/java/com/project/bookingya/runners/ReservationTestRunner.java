package com.project.bookingya.runners;

import io.cucumber.junit.CucumberOptions;
import net.serenitybdd.cucumber.CucumberWithSerenity;
import org.junit.runner.RunWith;

// Esta clase le dice a JUnit cómo y dónde encontrar las pruebas
@RunWith(CucumberWithSerenity.class)
@CucumberOptions(

        features = "src/test/resources/features/reservation_bdd.feature",
        glue = "com.project.bookingya.steps.bdd",
        // Formato del reporte generado por Serenity
        plugin = {
                "pretty",                          // muestra los pasos en consola
                "json:target/cucumber-reports/cucumber.json"
        }
)
public class ReservationTestRunner {

}