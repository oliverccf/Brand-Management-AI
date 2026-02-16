Feature: Multi-Channel Analysis Simulation
  As a Brand Manager
  I want to verify how the system responds to different communication channels
  So that I can ensure the identity security rules are applied correctly for each platform

  Background:
    Given the social media analysis system is running
    And a brand named "GlobalCorp" exists

  Scenario Outline: Processing messages from verified and unverified channels
    When I send a message from "<platform>" for user "<user>" with content "I need help with my last order" to "GlobalCorp"
    Then 1 analysis results should be saved in the database
    And the analysis result for "<user>" should contain the suggested action "<expected_action>"

    Examples:
      | platform       | user           | expected_action                            |
      | WHATSAPP       |  +551199999999 | Request account linkage for history access |
      | INSTAGRAM      | @customer_test | Request account linkage for history access |
      | TWITTER        | @twitter_user  | Request account linkage for history access |
      | RECLAME_AQUI   | ra_user_123    | Request account linkage for history access |
      | CONSUMIDOR_GOV | gov_user_456   | Request account linkage for history access |
      | EMAIL          | user@email.com | Request account linkage for history access |
      | LINKEDIN       | linkedin_prof  | Request account linkage for history access |
      | TIKTOK         | tiktok_fan     | Request account linkage for history access |
