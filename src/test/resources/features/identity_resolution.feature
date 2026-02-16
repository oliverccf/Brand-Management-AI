Feature: Customer Identity Resolution
  As a Brand Intelligence System
  I want to unify customer identities across different channels
  So that I can maintain a consistent history for each customer

  Scenario: Resolve and link multiple identities
    Given the social media analysis system is running
    And a brand "MainBrand" exists
    When I process a message for "MainBrand" from "WHATSAPP" with user "5511999999999"
    Then a new customer identity should be created
    When I link the identity "5511999999999" on "WHATSAPP" to "5511888888888" on "INSTAGRAM" as "TRUSTED"
    Then both identities should share the same unified Customer ID
    And the trust level for "5511888888888" should be "TRUSTED"
