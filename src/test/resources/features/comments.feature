Feature: Comments

  Background:
    Given the first admin "admin@example.test" / "hunter22!" / "Admin" has been created
    And I am logged in as "admin@example.test" / "hunter22!"
    And a project "ENG" / "Engineering" exists
    And an issue in "ENG" with title "First issue" exists

  Scenario: Create a comment
    When I POST to "/api/projects/ENG/issues/1/comments" with body:
      """
      {"bodyMd":"first thoughts"}
      """
    Then the response status is 201
    And the response body field "bodyMd" equals "first thoughts"

  Scenario: List comments returns sorted ascending by creation
    Given a comment "alpha" exists on issue "ENG-1"
    And a comment "beta" exists on issue "ENG-1"
    When I list comments on issue "ENG-1"
    Then the comment list at index 0 has bodyMd "alpha"
    And the comment list at index 1 has bodyMd "beta"

  Scenario: Comments cascade when issue is deleted
    Given a comment "alpha" exists on issue "ENG-1"
    When I DELETE "/api/projects/ENG/issues/1"
    Then the response status is 204
    When I GET "/api/projects/ENG/issues/1/comments"
    Then the response status is 404

  Scenario: Comments cascade when project is deleted
    Given a comment "alpha" exists on issue "ENG-1"
    When I DELETE "/api/projects/ENG"
    Then the response status is 204
    When I GET "/api/projects/ENG/issues/1/comments"
    Then the response status is 404

  Scenario: Reject empty comment body
    When I POST to "/api/projects/ENG/issues/1/comments" with body:
      """
      {"bodyMd":""}
      """
    Then the response status is 400

  Scenario: Reject overlong comment body
    When I POST a comment of 10001 chars to "ENG-1"
    Then the response status is 400
