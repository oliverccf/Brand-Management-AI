Feature: RAG Filtered Context
  As a Brand Analyzer
  I want to retrieve only the relevant conversation history for a specific customer and brand
  So that the AI response is accurate and private

  Scenario: RAG retrieves context for the correct customer only
    Given the social media analysis system is running
    And a brand "DefaultBrand" exists
    And the following history exists in the vector store:
      | user   | content               | brand        |
      | user_a | I bought a laptop     | DefaultBrand |
      | user_b | I bought a smartphone | DefaultBrand |
    When I analyze a message from "user_a" for "DefaultBrand": "What did I buy?"
    Then the AI should receive context about "laptop"
    And the AI should NOT receive context about "smartphone"
