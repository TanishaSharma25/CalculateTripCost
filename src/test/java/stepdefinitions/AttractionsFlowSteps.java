// src/test/java/stepdefinitions/AttractionsResultsOnlySteps.java
package stepdefinitions;

import io.cucumber.java.en.*;
import org.openqa.selenium.WebDriver;
import pages.AttractionsResultsPage;

public class AttractionsFlowSteps  {

    private final WebDriver driver = BookingSteps.driver;
    private AttractionsResultsPage results;
    private AttractionsResultsPage.ActivityDetails selected;

    @Given("user is on attractions search results for {string}")
    public void user_is_on_attractions_search_results_for(String city) {
        results = new AttractionsResultsPage(driver);
        results.waitForResultsPage(); // SRP guard after search submit
    }

    @When("user applies Food & drinks category filter")
    public void user_applies_food_and_drinks_category_filter() {
        results.applyFoodFilter(); // clicks name=filter_by value=PTCMCi9PTIPL
    }

    @And("user waits for Rome attractions to refresh")
    public void user_waits_for_rome_attractions_to_refresh() {
        // This is built into applyFoodFilter(); called here for readability.
        // No-op or call a results.refreshWait() if you break it out.
    }

    @And("user opens the first attraction card")
    public void user_opens_the_first_attraction_card() {
        results.openFirstActivity(); // title link or See availability
    }

    @Then("user sees the attraction details page")
    public void user_sees_the_attraction_details_page() {
        // Lightweight guard happens inside captureDetails(); separate assert optional
    }

    @And("user stores activity details")
    public void user_stores_activity_details() {
        selected = results.captureDetails(); // name, rating, duration, description
    }

    @And("user displays the stored activity details")
    public void user_displays_the_stored_activity_details() {
        results.printDetails(selected);
    }
}