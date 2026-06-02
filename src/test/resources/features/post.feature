Feature: Posts API

  Scenario: Get a single post
    When I send GET request to "/posts/1"
    Then status code should be 200
    And field "title" should not be null

  Scenario: Create a new post
    When I send POST request to "/posts"
    Then status code should be 201
    And field "title" should not be null

  Scenario: Delete a post
    When I send DELETE request to "/posts/1"
    Then status code should be 200