package Automation.StepDefinitions;

import Automation.Models.Users;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.io.FileReader;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class DynamicTableSteps {

    private WebDriver driver;
    private final List<Users> inputData = new ArrayList<>();  // Store user data
    private static final Duration TIMEOUT = Duration.ofSeconds(100); // Define timeout

    @Given("I am on the dynamic table page")
    public void iAmOnTheDynamicTablePage() {
        WebDriverManager.chromedriver().setup();  // Automatically manage ChromeDriver
        driver = new ChromeDriver();
        driver.get("https://testpages.herokuapp.com/styled/tag/dynamic-table.html");
    }

    @And("I click on the Test Table arrow button")
    public void iClickOnTheButton() {
        WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//details//summary"))).click();
    }

    @And("I input the data from JSON file")
    public void iInputTheDataFromJsonFile() {
        // Read data from the JSON file
        String filePath = "src/test/resources/InputData/Users.json"; // Update this to the actual file path
        inputData.addAll(readJsonData(filePath));
        inputDataToJsonField();
    }

    private List<Users> readJsonData(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {
            Gson gson = new Gson();
            Type userListType = new TypeToken<List<Users>>() {}.getType();
            return gson.fromJson(reader, userListType);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read JSON file: " + e.getMessage());
        }
    }

    private void inputDataToJsonField() {
        if (inputData.isEmpty()) {
            throw new IllegalArgumentException("Input data is empty. Cannot convert to JSON.");
        }

        StringBuilder jsonString = new StringBuilder("[");
        for (int i = 0; i < inputData.size(); i++) {
            Users user = inputData.get(i);
            jsonString.append("{")
                    .append("\"name\":\"").append(user.getName()).append("\",")
                    .append("\"age\":\"").append(user.getAge()).append("\",")
                    .append("\"gender\":\"").append(user.getGender()).append("\"")
                    .append("}");
            if (i < inputData.size() - 1) {
                jsonString.append(",");
            }
        }
        jsonString.append("]");

        WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
        WebElement inputField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("jsondata")));
        inputField.click();
        inputField.clear();
        inputField.sendKeys(jsonString.toString());

        System.out.println("JSON Data to input: " + jsonString.toString());
    }



    @And("I click on the Refresh button")
    public void iClickOnTheRefreshTableButton() throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@id='refreshtable']"))).click();
        Thread.sleep(2000); // Wait for 2 seconds to allow table to refresh
    }

    @Then("the table should display the entered data")
    public void theTableShouldDisplayTheEnteredData() {
        WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("table#dynamictable")));

        List<String[]> populatedData = new ArrayList<>();
        List<WebElement> rows = driver.findElements(By.cssSelector("table#dynamictable tr"));

        for (WebElement row : rows) {
            List<WebElement> cells = row.findElements(By.tagName("td"));
            if (cells.size() >= 2) {
                String[] rowData = {
                        cells.get(0).getText(),
                        cells.get(1).getText(),
                        cells.size() > 2 ? cells.get(2).getText() : "" // Handle optional gender field
                };
                populatedData.add(rowData);
            }
        }

        List<String[]> expectedData = new ArrayList<>();
        for (Users user : inputData) {
            expectedData.add(new String[]{user.getName(), user.getAge(), user.getGender()});
        }

        Assert.assertArrayEquals("The data displayed in the table does not match the expected input data.",
                expectedData.toArray(), populatedData.toArray());
    }

    @Then("I close the browser")
    public void iCloseTheBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }
}
