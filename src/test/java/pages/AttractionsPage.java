package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class AttractionsPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    // Simplified Locators
    private final By destinationInput = By.cssSelector("input[data-testid='search-input-field']");
    private final By datesTriggerBtn  = By.cssSelector("button[aria-label='Select dates']");
    private final By searchButton     = By.cssSelector("button[data-testid='search-button']");
    private final By suggestion       = By.cssSelector("[role='option']");
    private final By nextMonthBtn     = By.cssSelector("button[aria-label='Next month'], button[data-testid='date-picker-paging']");

    public AttractionsPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void ensureAttractionsHeaderLoaded() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(datesTriggerBtn));
    }

    public void enterDestination(String city) {
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(destinationInput));
        input.clear();
        input.sendKeys(city);
        wait.until(ExpectedConditions.elementToBeClickable(suggestion)).click();
    }

    public void openDatePicker() {
        wait.until(ExpectedConditions.elementToBeClickable(datesTriggerBtn)).click();
    }

    public void pickSingleDate(String isoDate) {
        // Locator for the specific date span
        By targetDate = By.cssSelector("span[data-date='" + isoDate + "']");

        // Loop to click "Next" until the date appears
        int maxMonths = 12;
        while (driver.findElements(targetDate).isEmpty() && maxMonths-- > 0) {
            try {
                WebElement next = wait.until(ExpectedConditions.elementToBeClickable(nextMonthBtn));
                next.click();
            } catch (Exception e) {
                // If standard click fails, use JS as a backup for the next button
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", driver.findElement(nextMonthBtn));
            }
        }

        // Final click on the date itself using JavaScript (most reliable for calendars)
        WebElement dateElement = wait.until(ExpectedConditions.presenceOfElementLocated(targetDate));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", dateElement);
    }

    public void clickSearch() {
        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(searchButton));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
    }
}