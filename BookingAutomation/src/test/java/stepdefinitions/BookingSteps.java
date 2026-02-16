package stepdefinitions;

import io.cucumber.java.en.*;
import org.openqa.selenium.WebDriver;
import pages.BookingHomePage;
import utils.DriverFactory;

public class BookingSteps {

    WebDriver driver;
    BookingHomePage page;

    @Given("user launches browser")
    public void launch_browser() {
        driver = DriverFactory.initDriver();
        page = new BookingHomePage(driver);
    }

    @When("user navigates to booking site")
    public void navigate_site() {
        driver.get("https://www.booking.com");
    }

    @And("user closes popup")
    public void close_popup() {
        page.closePopup();
    }

    @And("user enters destination")
    public void enter_destination() {
        page.enterDestination("Nairobi");
    }

    @And("user selects travel dates")
    public void select_dates() {
        page.selectDates();
    }

    @And("user sets guests")
    public void set_guests() {
        page.setGuests();
    }

    @Then("user clicks search")
    public void click_search() {
        page.clickSearch();

    }
}
