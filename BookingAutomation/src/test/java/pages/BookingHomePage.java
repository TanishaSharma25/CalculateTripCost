package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;

public class BookingHomePage {

    WebDriver driver;
    WebDriverWait wait;

    public BookingHomePage(WebDriver driver) {
        this.driver = driver;
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    By popupClose = By.cssSelector("button[aria-label='Dismiss sign-in info.']");
    By destination = By.name("ss");
    By dateBox = By.cssSelector("span[data-testid='searchbox-dates-container']");
    By guests = By.id("xp__guests__toggle");
    By adultsIncrease = By.xpath("//button[@aria-label='Increase number of Adults']");
    By roomsIncrease = By.xpath("//button[@aria-label='Increase number of Rooms']");
    By searchBtn = By.cssSelector("button[type='submit']");

    public void closePopup() {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(popupClose)).click();
            Thread.sleep(1000);
        } catch (Exception ignored) {
        }
    }

    public void enterDestination(String city) {

        WebElement input = wait.until(
                ExpectedConditions.elementToBeClickable(destination));

        input.clear();
        input.sendKeys(city);

        try {
            Thread.sleep(1000);
        } catch (Exception e) {
        }

        input.sendKeys(Keys.TAB);
    }

    public void selectDates() {

        // open calendar if not already opened
        driver.findElement(By.cssSelector("div[data-testid='searchbox-dates-container'], button[data-testid='searchbox-dates-container']")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        // wait for February 2026 header
        WebElement monthHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h3[contains(text(),'February 2026')]")));

        // select 17
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(
                "span[data-date='2026-02-17']"
        ))).click();

        // select 21
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(
                "span[data-date='2026-02-21']"
        ))).click();
    }

    public void setGuests() {

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        // open occupancy dropdown
        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[data-testid='occupancy-config']"))).click();

        // wait for popup
        WebElement popup = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("div[data-testid='occupancy-popup']")));

        // locate Adults section using label text
        WebElement adultsSection = popup.findElement(By.xpath(
                ".//label[text()='Adults']/ancestor::div[contains(@class,'e484bb5b7a')]"));

        // count element inside section
        WebElement countElement = adultsSection.findElement(By.xpath(".//span[contains(@class,'e32aa465fd')]"));

        int currentAdults = Integer.parseInt(countElement.getText());

        // + button (second button in the control)
        WebElement plusBtn = adultsSection.findElement(By.xpath(".//button[last()]"));

        // increase until 4
        while (currentAdults < 4) {
            plusBtn.click();
            currentAdults++;
        }

        // click Done button
        popup.findElement(By.xpath(".//span[text()='Done']/ancestor::button")).click();
    }

    public void clickSearch() {
        driver.findElement(searchBtn).click();
    }
}
