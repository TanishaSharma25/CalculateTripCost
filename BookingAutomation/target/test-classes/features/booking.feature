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