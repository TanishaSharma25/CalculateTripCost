package stepdefinitions;

import io.cucumber.java.en.*;
import org.openqa.selenium.WebDriver;
import pages.BookingResultsPage;
import utils.DriverFactory;

public class BookingResultsSteps {

    WebDriver driver = DriverFactory.getDriver();
    BookingResultsPage resultsPage = new BookingResultsPage(driver);


    @Given("user is on search results page")
    public void user_on_results_page() {
        resultsPage.verifyResultsPageLoaded();
    }

    @When("user filters by property type hotels")
    public void filter_property_type() {
        resultsPage.selectPropertyTypeHotel();
    }

    @When("user selects parking and free wifi facilities")
    public void select_facilities() {
        resultsPage.selectParking();
        resultsPage.selectFreeWifi();
    }

    @When("user selects wonderful review score")
    public void select_review_score() {
        resultsPage.selectWonderfulScore();
    }

    @Then("user captures top three hotel names and prices")
    public void capture_top_three() {
        resultsPage.printTopThreeHotels();
        /* DriverFactory.quitDriver(); */
    }

    @And("user navigates home and opens attractions")
    public void open_attractions_from_home() {
        WebDriver driver = DriverFactory.getDriver();
        driver.navigate().to("https://www.booking.com/");
        resultsPage.clickHeaderAttractions();
    }
}