package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.DriverFactory;
import utils.ExcelUtils;
import java.util.ArrayList;

import java.time.Duration;
import java.util.List;

public class BookingResultsPage {

    WebDriver driver;
    WebDriverWait wait;
    private final String excelPath = "booking_results.xlsx";

    public BookingResultsPage(WebDriver driver) {
        this.driver = driver;
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    // ✅ verify page loaded
    public void verifyResultsPageLoaded() {
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("div[data-testid='property-card']")
        ));
    }

    // ✅ reusable scroll helper
    private void scrollToElement(WebElement element) {
        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView({block:'center'});", element);
    }

    // ✅ PROPERTY TYPE → HOTELS
    public void selectPropertyTypeHotel() {

        // wait for property filter group container
        WebElement container = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("div[id^='filter_group_ht_id']")
        ));

        // locate the Hotels filter block
        WebElement hotelBlock = container.findElement(
                By.cssSelector("div[data-filters-item*='204']")
        );

        scrollToElement(hotelBlock);

        // click the label (more stable than inner span)
        WebElement label = hotelBlock.findElement(By.tagName("label"));

        wait.until(ExpectedConditions.elementToBeClickable(label));

        try {
            label.click();
        } catch (Exception e) {
            // fallback if overlay blocks click
            ((JavascriptExecutor) driver)
                    .executeScript("arguments[0].click();", label);
        }

        // wait for page refresh
        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.cssSelector("[data-testid='overlay-spinner']")
        ));
    }

    // ✅ FACILITIES → SHOW ALL → PARKING
    public void selectParking() {

        // wait for Facilities section
        WebElement facilitiesSection = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//span[text()='Facilities']")
        ));

        scrollToElement(facilitiesSection);

        // click "Show all" (dynamic text)
        WebElement showAll = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[data-testid='filters-group-expand-collapse']")
        ));

        showAll.click();

        // locate Parking filter block
        WebElement parkingBlock = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("div[data-filters-item*='hotelfacility=2']")
        ));

        WebElement label = parkingBlock.findElement(By.tagName("label"));

        scrollToElement(label);

        try {
            label.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver)
                    .executeScript("arguments[0].click();", label);
        }

        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.cssSelector("[data-testid='overlay-spinner']")
        ));
    }

    // ✅ FACILITIES → FREE WIFI
    public void selectFreeWifi() {

        // wait for Facilities section
        WebElement facilitiesSection = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//span[text()='Facilities']")
        ));

        scrollToElement(facilitiesSection);

        WebElement freeWifiBlock = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("div[data-filters-item*='hotelfacility=107']")
        ));

        WebElement label = freeWifiBlock.findElement(By.tagName("label"));

        scrollToElement(label);

        try {
            label.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver)
                    .executeScript("arguments[0].click();", label);
        }

        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.cssSelector("[data-testid='overlay-spinner']")
        ));
    }

    // ✅ REVIEW SCORE → WONDERFUL 9+
    public void selectWonderfulScore() {

        WebElement reviewSection = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//span[contains(text(),'Review score')]")
        ));

        scrollToElement(reviewSection);

        WebElement wonderful = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//label[contains(.,'Wonderful: 9+')]")
        ));

        wonderful.click();

        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.cssSelector("[data-testid='overlay-spinner']")
        ));
    }

    // ✅ CAPTURE TOP 3 HOTEL NAMES & PRICES
    public void printTopThreeHotels() {

        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("div[data-testid='property-card']")
        ));

        List<WebElement> cards = driver.findElements(
                By.cssSelector("div[data-testid='property-card']")
        );

        System.out.println("\n==== TOP 3 HOTELS ====\n");
        List<ExcelUtils.HotelRow> rows = new ArrayList<>();

        for (int i = 0; i < 3 && i < cards.size(); i++) {

            WebElement card = cards.get(i);

            String title = card.findElement(
                    By.cssSelector("[data-testid='title']")
            ).getText();

            String price = "Price not available";

            try {
                price = card.findElement(
                        By.cssSelector("[data-testid='price-and-discounted-price']")
                ).getText();
            } catch (Exception ignored) {}

            System.out.println((i + 1) + ". " + title);
            System.out.println("   " + price);
            System.out.println();
            rows.add(new ExcelUtils.HotelRow(title, price));
        }


        if (!rows.isEmpty()) {
            ExcelUtils.appendTopHotels(rows, excelPath);
        }

    }

    public void clickHeaderAttractions() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // If cookie popup appears again on homepage, try closing it
        try {
            WebElement popup = wait.until(ExpectedConditions.elementToBeClickable(
                    By.id("onetrust-accept-btn-handler")
            ));
            popup.click();
        } catch (Exception ignored) {}

        // Click the Attractions tab using unique id
        WebElement attractions = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("attractions")
        ));
        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView({block:'center'});", attractions);

        attractions.click();
    }
}