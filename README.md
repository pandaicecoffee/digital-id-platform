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

Running `Main` executes 16 scenarios that walk through the full Digital ID lifecycle: creation, attribute updates, nationality changes, multi-portal verification, age-based rejection, nationality-based denial at airport check-in, suspension, reactivation, tax-period checks, revocation, rejection of invalid operations on revoked IDs, non-existent ID handling, input validation, duplicate national ID rejection, portal type listing and an audit log summary. Labelled output is printed to the console.

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
│   ├── DigitalIDOperations.java    #Static helper that exposes package-private mutators to the service layer
│   ├── DigitalIDStatus.java        #Enum: ACTIVE, SUSPENDED, REVOKED + transition rules for each of the states
│   ├── LogEvent.java               #Audit log record (type, digitalId, actor, description, timestamp)
│   ├── LogEventType.java           #Enum of recordable actions
│   └── VerificationResult.java     #Record: valid (boolean) + reason (String)
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
│   └── implementation/
│       ├── CentralAuthorityPortal.java        #Implements ManagementPortal
│       ├── TaxAuthorityPortal.java            #Period-based suspension check
│       ├── DrivingLicenceAuthorityPortal.java #Active check + minimum driving age check (17)
│       ├── BankPortal.java                    #Simple valid/invalid response only
│       ├── EmployerPortal.java                #Returns verified name on success; generic message on failure
│       └── AirportServicesPortal.java         #Full status check + nationality permit-list filtering
│
├── exception/
│   ├── DigitalIdNotFoundException.java
│   ├── DuplicateIdentityException.java
│   └── InvalidOperationException.java
│
└── Main.java                       #Entry point; wires all components and runs 16 demo scenarios
```

---
## User Stories

### Central Authority

1. As a central authority, I want to create a new Digital ID so that a citizen has a registered digital identity in the system
2. As a central authority, I want to update a citizen's address so that their identity record is accurate
3. As a central authority, I want to be able to suspend a Digital ID so that it is temporarily unavailable for use
4. As a central authority, I want to be able to reactivate a suspended Digital ID so that its usage can be resumed
5. As a central authority, I want to revoke a Digital ID so that it is permanently removed from active use
6. As a central authority, I want the system to reject any attempts at an update to a revoked Digital ID so that details cannot be modified

### Tax Authority
1. As a tax authority, I want to verify that a Digital ID is active so that I can confirm a citizen is eligible to submit a tax return
2. As a tax authority, I want to check that a Digital ID was not suspended during a reporting period so that I can ensure the identity was valid throughout the period

### Driving Licence Authority
1. As a driving licence authority, I want to verify that a Digital ID is active so that I can confirm a citizen's identity before issuing a licence
2. As a driving licence authority, I want to check that an applicant meets the minimum driving age so that I can confirm eligibility before issuing a licence

### Airport Services
1. As an airport service, I want to verify that a Digital ID exists so that I can confirm a traveller has a registered identity before allowing them to check in
2. As an airport service, I want to check that a Digital ID is currently active so that I can confirm a traveller's identity is valid at the time of travel
3. As an airport service, I want to check that a Digital ID has not been suspended so that I can prevent travellers with temporarily invalid identities from boarding
4. As an airport service, I want to check that a Digital ID has not been revoked so that I can prevent travellers with permanently invalid identities from checking in
5. As an airport service, I want to receive a clear reason when a verification fails so that I can inform the traveller why they have been denied check in
6. As an airport service, I want to check that a traveller's nationality is on the permitted list for a route so that only eligible nationalities can check in

### Employer
1. As an employer, I want to check whether a Digital ID is valid so that I can confirm a candidate's identity at the time of their application

### Bank
1. As a bank, I want to check whether a Digital ID is currently valid so that I can verify a customer's identity before opening an account

---

## Architecture and Design Decisions

### Separation of Identity Management and Identity Consumption

The two core capabilities of the system are handled by separate service interfaces: `IdentityManagementService` and `IdentityConsumptionService`. This is a deliberate architectural choice, not just an organisational one.

The management service accepts a `requestedBy` parameter on every operation, allowing the audit log to record who performed an action. The consumption service does the same. Keeping these interfaces separate means consuming organisations cannot accidentally call management operations. The portal layer enforces this by only holding a reference to the service interface appropriate to its role.

### Package-Private Mutators with a Static Operations Helper

The status-change methods on `DigitalID` (`suspend()`, `reactivate()`, `revoke()`) are package-private. Code outside the `model` package cannot call them directly. `DigitalIDOperations` acts as a public static bridge, exposing these mutators and the attribute update methods so the service layer can apply them without the model leaking its internals to arbitrary callers. It also provides a generic `applyUpdate(DigitalID, Consumer<DigitalID>)` method for flexible mutation.

### Status Transition Rules on the Enum

The business rules for status transitions (e.g. a REVOKED identity cannot be suspended) live directly on the `DigitalIDStatus` enum as boolean methods: `canBeSuspended()`, `canBeRevoked()`, `canBeReactivated()`, `canBeUpdated()`, and `isUsableByConsumers()`. This means the transition logic is co-located with the status values themselves, making it impossible for a new status to be added without the transition rules being explicitly considered.

The service layer simply calls `if (!digitalID.getStatus().canBeSuspended())` before acting. This keeps the service clean and places responsibility where it belongs.

### Immutable Core Attributes

`DigitalID` uses Java's `final` keyword to enforce immutability on `id`, `nationalIdNumber`, and `dateOfBirth`. These fields can never be changed after creation. Mutable attributes (firstName, lastName, address, nationality) expose update methods, but only the service layer calls them and only after checking the identity's current status.

### Portal Layer as Organisational Boundary

Each organisation accesses the platform through its own portal class, which implements either `ManagementPortal` or `VerificationPortal`. The portal controls what information is returned to the calling organisation. For example:

- `BankPortal` strips the reason from a verification result, returning only `"Identity verified"` or `"Identity could not be verified"`. This ensures it receives no more information than it needs.
- `EmployerPortal` returns the citizen's full name on a successful verification but gives a generic failure message, keeping the response minimal without exposing status details.
- `AirportServicesPortal` returns a specific reason for each failure type (suspended vs. revoked vs. not found vs. nationality not on the permitted list), because airport staff need to act differently depending on the cause. It accepts a `Set<String>` of permitted nationalities at construction, enabling route-level filtering.
- `DrivingLicenceAuthorityPortal` checks the identity is active and then verifies the applicant's age against a minimum driving age of 17, rejecting underage applicants with a descriptive reason.
- `TaxAuthorityPortal` uses `verifyActiveForPeriod()` rather than `verifyIsActive()`, checking the audit log for suspension events within a specified reporting window. The reporting period start and end dates are configured at construction.

### Audit Logging

Every significant action (creation, updates, status changes, successful and failed verifications) is recorded through `LogService`. The log records the action type, the Digital ID it relates to, the actor that performed it, a description, and a timestamp. Rejected operations are also logged before the exception is thrown, meaning the audit trail is complete even for failed requests.

### Deterministic Rejection

All requests that conflict with the current state of a Digital ID are rejected consistently via named exceptions (`InvalidOperationException`, `DuplicateIdentityException`, `DigitalIdNotFoundException`). The `guardAgainstNonUpdatable` method in the management service centralises this rejection logic, logging the attempted operation before throwing therefore making rejections both auditable and consistent.

---

## Testing

Unit tests are written with JUnit 5 and cover:

| Test Class | What it covers |
|---|---|
| `DigitalIdStatusTest` | Enum transition rules in isolation |
| `InMemoryDigitalIdRepositoryTest` | Save, find-by-ID, find-by-national-ID, duplicate handling |
| `IdentityManagementImplTest` | Identity creation, attribute validation, all status transitions, rejection of invalid transitions, updates on revoked IDs, not-found handling |
| `IdentityConsumptionServiceImplTest` | Active/suspended/revoked/non-existent verification, period-based checks, failure reasons |
| `InMemoryLogServiceTest` | Log recording, retrieval, filtering |
| `CentralAuthorityPortalTest` | Portal-level management operations |
| `TaxAuthorityPortalTest` | Period-based verification through the portal |
| `DrivingLicenceAuthorityTest` | Active-status check and minimum-age rejection |
| `BankPortalTest` | Generic valid/invalid response stripping |
| `EmployerPortalTest` | Name-returning success and generic failure |
| `AirportServicesPortalTest` | Full status checks, nationality filtering, descriptive failure reasons |

Tests use real service and repository instances (no mocking framework), which validates the full component interaction rather than just isolated units.

**Continuous integration** is configured via `.github/workflows/ci.yml`. On every push or pull request to `main`, GitHub Actions automatically compiles the project with Maven and runs the full test suite on Java 17.

---

## Development Approach Reflection

Task tracking for this project was finalised retroactively by mapping completed milestones from the commit history to GitHub Issues. While commits reflect genuine incremental development (starting from the `DigitalID` model and building upward through the service and portal layers) the GitHub Issues and Project Board were created after implementation to make that progression explicit and traceable.

In future projects, I would establish issues and a project board at the start of each development cycle, updating them as work progresses rather than after the fact. The commit history demonstrates the actual development order clearly, but real-time task tracking would have made the intent behind each phase more visible throughout development rather than only in retrospect.
