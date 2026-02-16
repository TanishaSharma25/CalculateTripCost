package stepdefinitions;

import io.cucumber.java.en.*;
import org.openqa.selenium.WebDriver;
import utils.DriverFactory;
import pages.AttractionsPage;

public class AttractionsSteps {

    private WebDriver driver;
    private AttractionsPage attractions;

    @Given("user is on attractions home")
    public void user_is_on_attractions_home() {
        driver = DriverFactory.getDriver();
        // Assumes you already navigated to Attractions via header.
        // If the test is run standalone, uncomment the next line:
        // driver.get("https://www.booking.com/attractions/index.html");
        attractions = new AttractionsPage(driver);
        attractions.ensureAttractionsHeaderLoaded();
    }

    @When("user enters attractions destination as {string}")
    public void user_enters_attractions_destination_as(String city) {
        attractions.enterDestination(city);
    }

    @When("user selects attractions date {string}")
    public void user_selects_attractions_date(String isoDate) {
        attractions.openDatePicker();
        attractions.pickSingleDate(isoDate); // e.g., "2026-04-30"
    }

    @Then("user clicks attractions search")
    public void user_clicks_attractions_search() {
        attractions.clickSearch();
    }
}