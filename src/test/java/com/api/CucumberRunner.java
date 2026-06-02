package com.api;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
    features = "src/test/resources/features",
    glue     = "com.api",
    plugin   = { "pretty" }
)
public class CucumberRunner extends AbstractTestNGCucumberTests {
}