Feature: Social Media Brand Analysis
  As a Brand Manager
  I want to automatically analyze social media messages
  So that I can identify customer sentiment and potential issues

  Scenario: Analyze incoming social media traffic
    Given the social media analysis system is running
    When I send the following messages to the "social-media-messages" topic:
      | content                                 | platform | user            |
      | I love the new features! #awesome       | TWITTER  | @happy_user     |
      | Wait time for support is too long.      | FACEBOOK | angry_customer  |
      | Check out this unboxing video!          | TIKTOK   | influencer_123  |
      | Is this compatible with Mac?            | REDDIT   | tech_question   |
    Then 4 analysis results should be saved in the database
    And the results should contain the following sentiments:
      | POSITIVE |
      | POSITIVE |
      | POSITIVE |
      | POSITIVE |
