Feature: Booking search

  Scenario: Search hotels in Nairobi
    Given user launches browser
    When user navigates to booking site
    And user closes popup
    And user enters destination
    And user selects travel dates
    And user sets guests
    Then user clicks search

  Scenario: Apply filters and capture top hotels
    Given user is on search results page
    When user filters by property type hotels
    And user selects parking and free wifi facilities
    And user selects wonderful review score
    Then user captures top three hotel names and prices


  Scenario: Search attractions in Rome for a specific date
    Given user navigates home and opens attractions
    When user enters attractions destination as "rome"
    And user selects attractions date "2026-03-15"
    Then user clicks attractions search


  Scenario: Filter to Food & drinks and open the first attraction (Rome)
  # Precondition: user is already on Attractions SRP for Rome after search
    Given user is on attractions search results for "Rome"
    When  user applies Food & drinks category filter
    And   user waits for Rome attractions to refresh
    And   user opens the first attraction card
    Then  user sees the attraction details page
    And   user stores activity details
    And   user displays the stored activity details