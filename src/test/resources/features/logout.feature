Feature: Logout

  Background:
    Given the first admin "admin@example.test" / "hunter22!" / "Admin" has been created
    And I am logged in as "admin@example.test" / "hunter22!"

  Scenario: Logout invalidates the session
    When I POST to "/api/logout" with body:
      """
      {}
      """
    Then the response status is 204
    When I GET "/api/me"
    Then the response status is 401
