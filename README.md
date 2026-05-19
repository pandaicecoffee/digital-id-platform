# Digital-ID-Platform
**IOT452U - Software Engineering Tools and Techniques and Practice**

A console-based backend system for managing digital identities across a federated ecosystem of organisations. The Central Authority creates and manages Digital IDs; other organisations verify them through dedicated portals — without ever modifying identity data.

---

## GitHub Repository

...

---

## How to Run

**Prerequisites:** Java 17+, Maven 3.6+

```bash
#Clone the repository
git clone https://github.com/YOUR-USERNAME/digital-id-platform.git
cd digital-id-platform

#Build and run the tests
mvn clean install

#Run the demonstration
mvn exec:java -Dexec.mainClass="com.qmul.digitalid.Main"
```

Running `Main` executes 16 scenarios that walk through the full Digital ID lifecycle: creation, updates, suspension, reactivation, revocation, multi-portal verification, and rejection of invalid operations. Labelled output is printed to the console.

To run only the unit tests:

```bash
mvn test
```

---

## System Structure

```
src/main/java/com/qmul/digitalid/
│
├── model/
│   ├── DigitalID.java              #Core entity; immutable fields (id, nationalIdNumber, DOB)
│   ├── DigitalIDStatus.java        #Enum: ACTIVE, SUSPENDED, REVOKED + transition rules for each of the states
│   ├── LogEvent.java               #Audit Log record (type, digitalId, actor, description, timestamp)
│   └── LogEventType.java           #Enum of recordable actions
│
├── repository/
│   ├── DigitalIdRepository.java    #Storage interface
│   └── InMemoryDigitalIdRepository.java  #In-memory implementation
│
├── service/
│   ├── IdentityManagementService.java          #Interface: create, update, status changes
│   ├── IdentityManagementServiceImpl.java      #Implementation with validation and logging
│   ├── IdentityConsumptionService.java         #Interface: verify, lookup
│   ├── IdentityConsumptionServiceImpl.java     #Implementation with audit trail
│   ├── LogService.java                         #Audit log interface
│   └── InMemoryLogService.java                 #In-memory log implementation
│
├── portal/
│   ├── Portal.java                 #Base interface (organisation name, portal type)
│   ├── ManagementPortal.java       #Interface for identity management operations
│   ├── VerificationPortal.java     #Interface for verification operations
│   ├── VerificationResult.java     #Record: valid (boolean) + reason (String)
│   └── implementation/
│       ├── CentralAuthorityPortal.java       #Implements ManagementPortal
│       ├── TaxAuthorityPortal.java           #Period-based suspension check
│       ├── DrivingLicenceAuthorityPortal.java #Active + restriction check
│       ├── BankPortal.java                   #Simple valid/invalid response only
│       ├── EmployerPortal.java               #Simple valid/invalid response only
│       └── AirportServicesPortal.java        #Full status check with specific reasons
│
├── exception/
│   ├── DigitalIdNotFoundException.java
│   ├── DuplicateIdentityException.java
│   └── InvalidOperationException.java
│
└── Main.java                       #Entry point; wires all components and runs scenarios
```

---

## Architecture and Design Decisions

### Separation of Identity Management and Identity Consumption

The two core capabilities of the system are handled by separate service interfaces: `IdentityManagementService` and `IdentityConsumptionService`. This is a deliberate architectural choice, not just an organisational one.

The management service accepts a `requestedBy` parameter on every operation, allowing the audit log to record who performed an action. The consumption service does the same. Keeping these interfaces separate means consuming organisations cannot accidentally call management operations. The portal layer enforces this by only holding a reference to the service interface appropriate to its role.

### Status Transition Rules on the Enum

The business rules for status transitions (e.g. a REVOKED identity cannot be suspended) live directly on the `DigitalIDStatus` enum as boolean methods: `canBeSuspended()`, `canBeRevoked()`, `canBeReactivated()`, `canBeUpdated()`. This means the transition logic is co-located with the status values themselves, making it impossible for a new status to be added without the transition rules being explicitly considered.

The service layer simply calls `if (!digitalID.getStatus().canBeSuspended())` before acting. This keeps the service clean and places responsibility where it belongs.

### Immutable Core Attributes

`DigitalID` uses Java's `final` keyword to enforce immutability on `id`, `nationalIdNumber`, and `dateOfBirth`. These fields can never be changed after creation. Mutable attributes (name, address, nationality) expose update methods, but only the service layer calls them and only after checking the identity's current status.

### Portal Layer as Organisational Boundary

Each organisation accesses the platform through its own portal class, which implements either `ManagementPortal` or `VerificationPortal`. The portal controls what information is returned to the calling organisation. For example:

- `BankPortal` and `EmployerPortal` strip the reason from a successful verification, returning only a generic `"Identity verified"`. This ensures they receive no more information than they need.
- `AirportServicesPortal` returns a specific reason for each failure type (suspended vs. revoked vs. not found), because airport staff need to act differently depending on the cause.
- `TaxAuthorityPortal` uses `verifyActiveForPeriod()` rather than `verifyIsActive()`, checking the audit log for suspension events within a specified reporting window.

### Audit Logging

Every significant action (creation, updates, status changes, successful and failed verifications) is recorded through `LogService`. The log records the action type, the Digital ID it relates to, the actor that performed it, a description, and a timestamp. Rejected operations are also logged before the exception is thrown, meaning the audit trail is complete even for failed requests.

### Deterministic Rejection

All requests that conflict with the current state of a Digital ID are rejected consistently via named exceptions (`InvalidOperationException`, `DuplicateIdentityException`, `DigitalIdNotFoundException`). The `guardAgainstNonUpdatable` method in the management service centralises this rejection logic, logging the attempted operation before throwing therefore making rejections both auditable and consistent.

---

## Testing

Unit tests are written with JUnit 5 and cover:

| Test Class | What it covers |
|---|---|
| `IdentityManagementImplTest` | Identity creation, attribute validation, all status transitions, rejection of invalid transitions, updates on revoked IDs, not-found handling |
| `IdentityConsumptionServiceImplTest` | Active/suspended/revoked/non-existent verification, failure reasons |
| `CentralAuthorityPortalTest` | Portal-level management operations |
| `DigitalIDStatusTest` | Enum transition rules in isolation |

Tests use real service and repository instances (no mocking framework), which validates the full component interaction rather than just isolated units.

**Continuous integration** is configured via `.github/workflows/ci.yml`. On every push or pull request to `main`, GitHub Actions automatically compiles the project with Maven and runs the full test suite on Java 17.

---

## Development Approach Reflection

Task tracking for this project was finalised retroactively by mapping completed milestones from the commit history to GitHub Issues. While commits reflect genuine incremental development (starting from the `DigitalID` model and building upward through the service and portal layers) the GitHub Issues and Project Board were created after implementation to make that progression explicit and traceable.

In future projects, I would establish issues and a project board at the start of each development cycle, updating them as work progresses rather than after the fact. The commit history demonstrates the actual development order clearly, but real-time task tracking would have made the intent behind each phase more visible throughout development rather than only in retrospect.
