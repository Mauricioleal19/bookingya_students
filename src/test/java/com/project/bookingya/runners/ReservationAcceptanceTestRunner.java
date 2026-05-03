package com.project.bookingya.runners;

import net.serenitybdd.cucumber.CucumberWithSerenity;
import org.junit.runner.RunWith;
import io.cucumber.junit.CucumberOptions;


@RunWith(CucumberWithSerenity.class)
@CucumberOptions(
        features  = "src/test/resources/features/reservation_atdd.feature",
        glue      = "com.project.bookingya.steps.atdd",
        plugin    = {
                "pretty",
                "html:target/site/serenity/cucumber-reports.html"
        },
        tags      = ""
)
public class ReservationAcceptanceTestRunner {
}