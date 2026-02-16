package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Minimal page object for Booking.com -> Attractions (simple & robust)
 * Works with the DOM you shared.
 */
public class AttractionsPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // --- locators from your page ---
    private final By destinationInput = By.cssSelector("input[data-testid='search-input-field'][name='query']");
    private final By datesTriggerBtn  = By.cssSelector("button[aria-label='Select dates']");
    private final By searchButton     = By.cssSelector("button[data-testid='search-button'][type='submit']");
    // autosuggest
    private final By listbox   = By.cssSelector("[role='listbox']");
    private final By listItems = By.cssSelector("[role='listbox'] [role='option']");

    public AttractionsPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    // Quick "page is ready" probe
    public void ensureAttractionsHeaderLoaded() {
        wait.until(ExpectedConditions.presenceOfElementLocated(datesTriggerBtn));
    }

    // Type the city and pick a suggestion (or ARROWDOWN+ENTER fallback)
    public void enterDestination(String city) {
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(destinationInput));
        input.click();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(Keys.DELETE);
        input.sendKeys(city);

        try {
            // If listbox appears, click the first matching option (or first item)
            wait.until(ExpectedConditions.visibilityOfElementLocated(listbox));
            List<WebElement> options =
                    wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(listItems, 0));

            WebElement pick = options.stream()
                    .filter(o -> o.getText().toLowerCase().contains(city.toLowerCase()))
                    .findFirst()
                    .orElse(options.get(0));

            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", pick);
            wait.until(ExpectedConditions.invisibilityOfElementLocated(listbox));
        } catch (TimeoutException ignore) {
            // Keyboard fallback if autosuggest didn’t render fast enough
            input.sendKeys(Keys.ARROW_DOWN);
            input.sendKeys(Keys.ENTER);
        }
    }

    public void openDatePicker() {
        wait.until(ExpectedConditions.elementToBeClickable(datesTriggerBtn)).click();
    }

    /**
     * Pick a single date by ISO (YYYY-MM-DD). Your calendar uses:
     *   <span role="button" data-date="YYYY-MM-DD" ...>
     */
    public void pickSingleDate(String isoDate) {
        // 0) Locators from the Attractions page DOM
        By datesTriggerBtn = By.cssSelector("button[aria-label='Select dates']"); // Dates open button
        By targetCell      = By.cssSelector("span[role='button'][data-date='" + isoDate + "']"); // your day cell
        By nextMonth1      = By.cssSelector("button[aria-label='Next month']");                   // primary next
        By nextMonth2      = By.cssSelector("button[data-testid='date-picker-paging']");         // fallback next

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        // 1) Open the date panel (click again if the first click is swallowed)
        for (int i = 0; i < 2; i++) {
            try {
                wait.until(ExpectedConditions.elementToBeClickable(datesTriggerBtn)).click();
                // If any date cell exists, the panel is open
                if (!driver.findElements(By.cssSelector("[data-date]")).isEmpty()) break;
            } catch (Exception ignored) { }
            try { Thread.sleep(150); } catch (InterruptedException ignored) {}
        }

        // 2) Page forward until our isoDate is in the DOM (lazy-rendered calendar)
        int maxTurns = 24; // safety cap (~2 years)
        while (driver.findElements(targetCell).isEmpty() && maxTurns-- > 0) {
            boolean clicked = false;
            for (By b : new By[]{nextMonth1, nextMonth2}) {
                try {
                    WebElement next = driver.findElement(b);
                    if (next.isDisplayed() && next.isEnabled()) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", next);
                        clicked = true;
                        break;
                    }
                } catch (NoSuchElementException ignored) { }
            }
            if (!clicked) {
                // Tiny nudge in case this build pages on scroll
                ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 200);");
            }
            try { Thread.sleep(200); } catch (InterruptedException ignored) {}
        }

        // 3) Click the day and confirm Booking set it (aria-pressed="true")
        for (int i = 0; i < 3; i++) {
            try {
                WebElement day = wait.until(ExpectedConditions.presenceOfElementLocated(targetCell));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", day);
                wait.until(ExpectedConditions.elementToBeClickable(day)).click();
                wait.until(ExpectedConditions.attributeToBe(targetCell, "aria-pressed", "true"));
                break;
            } catch (StaleElementReferenceException | TimeoutException e) {
                if (i == 2) throw e;
                try { Thread.sleep(150); } catch (InterruptedException ignored) {}
            }
        }

        // 4) Close the panel so the Search button is clickable
        try { driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE); } catch (Exception ignored) {}
        try { Thread.sleep(120); } catch (InterruptedException ignored) {}
    }

    public void clickSearch() {
        // Close any stray overlay just in case
        try { driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE); } catch (Exception ignore) {}

        // Ensure button is enabled (not aria-disabled)
        wait.until(d -> {
            WebElement b = d.findElement(searchButton);
            return b.isDisplayed() && b.isEnabled()
                    && !"true".equalsIgnoreCase(String.valueOf(b.getAttribute("aria-disabled")));
        });

        // 1) normal click
        try {
            wait.until(ExpectedConditions.elementToBeClickable(searchButton)).click();
            return;
        } catch (Exception ignore) {}

        // 2) JS click
        try {
            WebElement b = driver.findElement(searchButton);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", b);
            return;
        } catch (Exception ignore) {}

        // 3) final fallback: submit the parent form
        WebElement form = driver.findElement(searchButton).findElement(By.xpath("ancestor::form"));
        form.submit();
    }
}