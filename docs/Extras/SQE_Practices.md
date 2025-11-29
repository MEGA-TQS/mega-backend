# Practices (Rules) for the Project's Development

---

## Feature-Branch Workflow:

- Never push to *main* directly.
- Create a branch when developing something with the adequate name.
- Pull Requests (PRs): Merging to main requires a Pull Request that must pass the Quality Gate (all tests green + SonarQube pass).


## The "Definition of Done":
#### A User Story is only "Done" when:

- Code is implemented.
- Unit Tests are written and passing.
- Acceptance Criteria (from the card) are covered by a BDD test.
- Code coverage does not drop below the defined threshold (80%).

## Testing Pyramid Strategy:

- Unit Tests (70%): Test small logic in isolation. Fast and cheap.
- Integration Tests (20%): Test Database interactions and API endpoints (e.g., "Does saving a Booking actually update the H2 database?").
- E2E/BDD Tests (10%): Test full user flows (e.g., "User logs in, searches for kayak, and books it"). Slower but realistic.

## Continuous Feedback:

- Continuous communication using WhatsApp. If the pipeline fails, fixing it is the team's top priority.