Feature: Login

  Background:
    Given the first admin "admin@example.test" / "hunter22!" / "Admin" has been created

  Scenario: Login succeeds with correct credentials
    When I POST to "/api/login" with body:
      """
      {"email":"admin@example.test","password":"hunter22!"}
      """
    Then the response status is 200
    And the response body field "email" equals "admin@example.test"

  Scenario: Login fails with wrong password
    When I POST to "/api/login" with body:
      """
      {"email":"admin@example.test","password":"wrong"}
      """
    Then the response status is 401

  Scenario: Login fails with unknown email
    When I POST to "/api/login" with body:
      """
      {"email":"nobody@example.test","password":"hunter22!"}
      """
    Then the response status is 401
