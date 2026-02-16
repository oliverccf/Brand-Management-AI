Feature: Multi-tenant Brand Isolation
  As a System Operator
  I want to ensure data isolation between different brands
  So that brand information is never leaked or mixed

  Background:
    Given the social media analysis system is running
    And the following brands exist:
      | name    | instructions             |
      | Brand A | Instructions for Brand A |
      | Brand B | Instructions for Brand B |

  Scenario: Identities are isolated by brand
    When I send the following messages to the "social-media-messages" topic for "Brand A":
      | content | platform | user    |
      | Hello   | TWITTER  | user123 |
    And I send the following messages to the "social-media-messages" topic for "Brand B":
      | content | platform | user    |
      | Hello   | TWITTER  | user123 |
    Then 2 analysis results should be saved in the database
    And the customer identities for "user123" should be different for each brand

  Scenario: Analysis results are associated with the correct brand
    When I send the following messages to the "social-media-messages" topic for "Brand A":
      | content | platform | user   |
      | Great   | TWITTER  | user_a |
    Then 1 analysis results should be saved in the database
    And the analysis result for "user_a" should be associated with "Brand A"
