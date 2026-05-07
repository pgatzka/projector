Feature: Issue labels

  Background:
    Given the first admin "admin@example.test" / "hunter22!" / "Admin" has been created
    And I am logged in as "admin@example.test" / "hunter22!"
    And a project "ENG" / "Engineering" exists
    And a label "bug" / "red" exists in project "ENG"

  Scenario: Assign a label on issue creation
    When I create an issue in "ENG" titled "First" with labels "bug"
    Then the response status is 201
    And the response body has 1 label named "bug"

  Scenario: Assign a label after creation
    Given an issue in "ENG" with title "First" exists
    When I assign label "bug" in project "ENG" to issue 1
    Then the response status is 200
    And the response body has 1 label named "bug"

  Scenario: Assign is idempotent
    Given an issue in "ENG" with title "First" exists
    And label "bug" is assigned to issue 1 in "ENG"
    When I assign label "bug" in project "ENG" to issue 1
    Then the response status is 200
    And the response body has 1 label named "bug"

  Scenario: Unassign a label
    Given an issue in "ENG" with title "First" exists
    And label "bug" is assigned to issue 1 in "ENG"
    When I unassign label "bug" in project "ENG" from issue 1
    Then the response status is 204

  Scenario: Reject cross-project label
    Given a project "OPS" / "Operations" exists
    And a label "infra" / "blue" exists in project "OPS"
    And an issue in "ENG" with title "First" exists
    When I assign label "infra" in project "ENG" to issue 1
    Then the response status is 400
