Feature: Activity log

  Background:
    Given the first admin "admin@example.test" / "hunter22!" / "Admin" has been created
    And I am logged in as "admin@example.test" / "hunter22!"
    And a project "ENG" / "Engineering" exists

  Scenario: Creating an issue emits a single issue_created activity row
    Given an issue in "ENG" with title "First issue" exists
    When I list the activity feed for issue "ENG-1"
    Then the activity feed has 1 entries
    And the activity feed entry at index 0 has action "issue_created"

  Scenario: PATCH-ing an issue title emits a title_edited row
    Given an issue in "ENG" with title "Old title" exists
    When I PATCH "/api/projects/ENG/issues/1" with body:
      """
      {"title":"New title"}
      """
    Then the response status is 200
    When I list the activity feed for issue "ENG-1"
    Then the activity feed has 2 entries
    And the activity feed entry at index 1 has action "title_edited"
    And the activity feed entry at index 1 payload has "before" "Old title"
    And the activity feed entry at index 1 payload has "after" "New title"

  Scenario: Assigning a label emits a label_added row with snapshot
    Given a label "bug" / "red" exists in project "ENG"
    And an issue in "ENG" with title "First" exists
    When I assign label "bug" in project "ENG" to issue 1
    Then the response status is 200
    When I list the activity feed for issue "ENG-1"
    Then the activity feed has 2 entries
    And the activity feed entry at index 1 has action "label_added"
    And the activity feed entry at index 1 payload has "labelName" "bug"
    And the activity feed entry at index 1 payload has "labelColor" "red"

  Scenario: Activity rows survive label deletion (tombstone resilience)
    Given a label "bug" / "red" exists in project "ENG"
    And an issue in "ENG" with title "First" exists
    And label "bug" is assigned to issue 1 in "ENG"
    When I DELETE a label in project "ENG" named "bug"
    Then the response status is 204
    When I list the activity feed for issue "ENG-1"
    Then the activity feed has 2 entries
    And the activity feed entry at index 1 has action "label_added"
    And the activity feed entry at index 1 payload has "labelName" "bug"
    And the activity feed entry at index 1 payload has "labelColor" "red"
