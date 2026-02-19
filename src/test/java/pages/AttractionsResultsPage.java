package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import utils.ExcelUtils;

import java.time.Duration;
import java.util.List;

public class AttractionsResultsPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final String excelPath = "booking_results.xlsx";

    public static class ActivityDetails {
        public String name;
        public String departurePoint;
        public String duration;
        public String price;
    }

    public AttractionsResultsPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    /** Guard: ensure the SRP is loaded and cards are present */
    public void waitForResultsPage() {
        wait.until(d -> d.getCurrentUrl().contains("/attractions/"));
        wait.until(d ->
                !d.findElements(By.cssSelector("[data-testid='card'], [data-testid='product-card']")).isEmpty()
                        || !d.findElements(By.xpath("//span[normalize-space()='See availability']/ancestor::a")).isEmpty()
        );
    }

    /** Step 4: Apply Food & drinks (Category) filter */
    public void applyFoodFilter() {
        // Your DOM shows: <input name="filter_by" value="PTCMCi9PTIPL" ...> with label id "PTCMCi9PTIPL-label"
        By foodCheckbox = By.xpath("//input[@name='filter_by' and @value='PTCMCi9PTIPL']");
        WebElement input = wait.until(ExpectedConditions.presenceOfElementLocated(foodCheckbox));

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", input);

        boolean checked = Boolean.parseBoolean(String.valueOf(input.getAttribute("checked")));
        if (!checked) {
            WebElement label = driver.findElement(By.xpath("//label[@for='" + input.getAttribute("id") + "']"));
            wait.until(ExpectedConditions.elementToBeClickable(label)).click();
        }

        waitForResultsRefresh();
    }

    /** Small pause + ensure cards are back after filtering */
    private void waitForResultsRefresh() {
        try { Thread.sleep(800); } catch (InterruptedException ignored) {}
        wait.until(d ->
                !d.findElements(By.cssSelector("[data-testid='card'], [data-testid='product-card']")).isEmpty()
                        || !d.findElements(By.xpath("//span[normalize-space()='See availability']/ancestor::a")).isEmpty()
        );
    }

    /** Step 5: get card containers (first non-empty selector wins) */
    public List<WebElement> getResultCards() {
        List<WebElement> cards = driver.findElements(By.cssSelector("[data-testid='card']"));
        if (!cards.isEmpty()) return cards;

        cards = driver.findElements(By.cssSelector("[data-product-card='true'][data-testid='card']"));
        if (!cards.isEmpty()) return cards;

        // Fallback: get ancestors of “See availability” CTA
        cards = driver.findElements(By.xpath("//span[normalize-space()='See availability']/ancestor::a/ancestor::*[self::article or self::div or self::li][1]"));
        return cards;
    }

    /** Step 6: Open the first activity */
    public void openFirstActivity() {
        // Retry a few times because SRP re-renders on scroll/IO observers
        final int maxTries = 4;
        By cardLocator       = By.cssSelector("[data-testid='card']");
        By titleLinkInCard   = By.cssSelector("h3[data-testid='card-title'] a");
        By seeAvailCtaInCard = By.xpath(".//span[normalize-space()='See availability']/ancestor::a");

        for (int attempt = 1; attempt <= maxTries; attempt++) {
            try {
                // (1) Re-fetch the first card fresh each attempt
                List<WebElement> cards = driver.findElements(cardLocator);
                if (cards.isEmpty()) {
                    // Fallback if the primary locator is temporarily empty
                    cards = driver.findElements(By.cssSelector("[data-product-card='true'][data-testid='card']"));
                }
                if (cards.isEmpty()) {
                    // Last fallback: any ancestor of "See availability"
                    cards = driver.findElements(By.xpath("//span[normalize-space()='See availability']/ancestor::a/ancestor::*[self::article or self::div or self::li][1]"));
                }
                if (cards.isEmpty()) throw new RuntimeException("No attraction cards found after filtering.");

                WebElement firstCard = cards.get(0);

                // (2) Scroll the CARD container (more stable than inner children)
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", firstCard);

                // (3) Within this card, prefer the title link; else click “See availability”
                if (!firstCard.findElements(titleLinkInCard).isEmpty()) {
                    // Re-locate the title link immediately before clicking
                    WebElement titleLink = firstCard.findElement(titleLinkInCard);
                    wait.until(ExpectedConditions.elementToBeClickable(titleLink));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", titleLink);
                } else if (!firstCard.findElements(seeAvailCtaInCard).isEmpty()) {
                    WebElement cta = firstCard.findElement(seeAvailCtaInCard);
                    wait.until(ExpectedConditions.elementToBeClickable(cta));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", cta);
                } else {
                    // Rare variant: click the whole card
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstCard);
                }

                // If we got here, click succeeded
                return;

            } catch (StaleElementReferenceException | ElementClickInterceptedException e) {
                // Brief backoff and retry with fresh references
                if (attempt == maxTries) throw e;
                try { Thread.sleep(250L * attempt); } catch (InterruptedException ignored) {}
            }
        }
    }


    /** Step 7: Capture details on the activity page */
    public ActivityDetails captureDetailsFromPage() {

        ActivityDetails d = new ActivityDetails();

        // TITLE
        try {
            d.name = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("h3[data-testid='card-title'] a")
            )).getText();
        } catch (Exception e) {
            d.name = "(title not found)";
        }

        // DURATION
        try {
            d.duration = driver.findElement(
                    By.xpath(".//div[contains(text(),'Duration')]")
            ).getText().replace("Duration:","").trim();
        } catch (Exception e) {
            d.duration = "(duration not found)";
        }

        // PRICE
        try {
            d.price = driver.findElement(
                    By.xpath(".//div[@data-testid='price']//div[contains(text(),'Rs')]")
            ).getText().trim();
        } catch (Exception e) {
            d.price = "(price not found)";
        }

        // DEPARTURE POINT
        try {

            JavascriptExecutor js = (JavascriptExecutor) driver;

            // scroll page gradually to trigger lazy load
            js.executeScript("window.scrollBy(0,800)");
            Thread.sleep(500);

            js.executeScript("window.scrollBy(0,800)");
            Thread.sleep(500);

            js.executeScript("window.scrollBy(0,800)");
            Thread.sleep(500);

            // now wait for Departure point heading
            WebElement departureHeader = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//h3[normalize-space()='Departure point']")
            ));

            js.executeScript("arguments[0].scrollIntoView({block:'center'});", departureHeader);

            // capture the value
            WebElement departureText = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//h3[normalize-space()='Departure point']/following-sibling::div[1]")
            ));

            d.departurePoint = departureText.getText().trim();

        } catch (Exception e) {
            d.departurePoint = "(departure point not found)";
        }

        return d;
    }

    public void waitForDetailsPage() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h3[normalize-space()='Location']")
        ));
    }




    /** Step 8: Print details to console */
    public void printDetails(ActivityDetails d) {
        System.out.println("\n==== SELECTED ACTIVITY DETAILS ====");
        System.out.println("Name      : " + d.name);
        System.out.println("Price    : " + d.price);
        System.out.println("Duration  : " + d.duration);
        System.out.println("Departure : " + d.departurePoint);
        System.out.println("===================================\n");
        ExcelUtils.appendActivityDetails(d.name, d.price, d.duration, d.departurePoint, excelPath);
    }
}