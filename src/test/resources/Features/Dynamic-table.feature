Feature: Dynamic Table Feature

  Scenario: Add data to the dynamic table
    Given I am on the dynamic table page
    And I click on the Test Table arrow button
    And I input the data from JSON file
    And I click on the Refresh button
    Then the table should display the entered data
    And I close the browser
