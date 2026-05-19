package com.qmul.digitalid;

import com.qmul.digitalid.exception.InvalidOperationException;
import com.qmul.digitalid.model.DigitalID;
import com.qmul.digitalid.model.LogEvent;
import com.qmul.digitalid.portal.VerificationPortal;
import com.qmul.digitalid.model.VerificationResult;
import com.qmul.digitalid.portal.implementation.*;
import com.qmul.digitalid.repository.InMemoryDigitalIdRepository;
import com.qmul.digitalid.service.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public class Main {

    public static void main(String[] args) {

        InMemoryDigitalIdRepository repository = new InMemoryDigitalIdRepository();

        LogService logService = new InMemoryLogService();

        IdentityManagementService managementService =
                new IdentityManagementServiceImpl(repository, logService);

        IdentityConsumptionService consumptionService =
                new IdentityConsumptionServiceImpl(repository, logService);

        //create portals
        CentralAuthorityPortal authority =
                new CentralAuthorityPortal(managementService);

        TaxAuthorityPortal taxPortal =
                new TaxAuthorityPortal(
                        consumptionService,
                        LocalDate.of(2025, 4, 6),   // UK tax year 2025-26
                        LocalDate.of(2026, 4, 5)
                );

        DrivingLicenceAuthorityPortal drivingLicencePortal =
                new DrivingLicenceAuthorityPortal(consumptionService);

        EmployerPortal employerPortal = new EmployerPortal(consumptionService);
        BankPortal bankPortal = new BankPortal(consumptionService);

        //Airport with nationality restrictions (e.g. domestic terminal)
        AirportServicesPortal airportPortal = new AirportServicesPortal(
                consumptionService,
                Set.of("British", "Irish")
        );


        section("SCENARIO 1 — Identity Creation");
        // A citizen registers for a Digital ID


        DigitalID aleena = authority.createIdentity(
                "NIN-001",
                "Aleena",
                "Joseph",
                LocalDate.of(1990, 3, 15),
                "123 High Street, London",
                "British"
        );
        System.out.println("Created: " + aleena);

        //Create a second identity who is a minor (for driving licence age check)
        DigitalID minor = authority.createIdentity(
                "NIN-002",
                "James",
                "Taylor",
                LocalDate.of(2012, 7, 20),    //13 years old which is under driving age
                "45 Oak Lane, Manchester",
                "British"
        );
        System.out.println("Created (minor): " + minor);

        //Create a third identity for airport check
        DigitalID foreign = authority.createIdentity(
                "NIN-003",
                "Sofia",
                "Martinez",
                LocalDate.of(1985, 11, 2),
                "78 Elm Road, Birmingham",
                "Spanish"
        );
        System.out.println("Created (foreign national): " + foreign);


        section("SCENARIO 2 — Update Mutable Attributes");
        //Aleena changes her name after marriage and moves house

        authority.updateLastName(aleena.getId(), "Joseph-Williams");
        authority.updateAddress(aleena.getId(), "456 New Road, Manchester");
        System.out.println("After updates: " + authority.lookupIdentity(aleena.getId()));


        section("SCENARIO 3 — Update Nationality (before any status changes)");
        //Sofia becomes a British Citizen

        authority.updateNationality(foreign.getId(), "British");
        System.out.println("Nationality updated: " + authority.lookupIdentity(foreign.getId()));


        section("SCENARIO 4 — Multi-portal Verification (all ACTIVE)");
        //All portals verify Aleena and each returns a different response

        verify(taxPortal, aleena.getId());
        verify(drivingLicencePortal, aleena.getId());
        verify(employerPortal, aleena.getId());
        verify(bankPortal, aleena.getId());
        verify(airportPortal, aleena.getId());


        section("SCENARIO 5 — Driving Licence: Minor Rejected on Age");
        //James (aged 13) applies for a driving licence, this should fail

        verify(drivingLicencePortal, minor.getId());


        section("SCENARIO 6 — Airport: Nationality Not on Permitted List");
        //Before nationality update, Sofia was Spanish, she is now British.
        //Create a new non-CTA identity to demonstrate the rejection.

        DigitalID traveller = authority.createIdentity(
                "NIN-004",
                "Yuki",
                "Tanaka",
                LocalDate.of(1992, 5, 18),
                "9 Sakura Street, London",
                "Japanese"
        );
        verify(airportPortal, traveller.getId());  // should fail as Japanese not in CTA
        verify(airportPortal, foreign.getId());     // should pass as Sofia is now British



        section("SCENARIO 7 — Suspend Identity");
        //Central authority suspends Aleena's ID - investigation for e.g. fraud

        authority.suspendIdentity(aleena.getId());
        System.out.println("Status: " + authority.lookupIdentity(aleena.getId()).getStatus());

        //All portals should now reject Aleena
        verify(taxPortal, aleena.getId());
        verify(employerPortal, aleena.getId());
        verify(bankPortal, aleena.getId());
        verify(airportPortal, aleena.getId());



        section("SCENARIO 8 — Reactivate Identity");
        //Investigation cleared — Aleena's ID is restored

        authority.reactivateIdentity(aleena.getId());
        System.out.println("Status: " + authority.lookupIdentity(aleena.getId()).getStatus());
        verify(drivingLicencePortal, aleena.getId());


        section("SCENARIO 9 — Tax Period Check (suspension in period)");
        //Aleena was suspended during the 2025-26 tax year, so the tax authority should reject the return

        verify(taxPortal, aleena.getId());


        section("SCENARIO 10 — Revoke Identity");
        //Permanent revocation (e.g. confirmed identity fraud)

        authority.revokeIdentity(aleena.getId());
        System.out.println("Status: " + authority.lookupIdentity(aleena.getId()).getStatus());
        verify(taxPortal, aleena.getId());
        verify(airportPortal, aleena.getId());


        section("SCENARIO 11 — Reject Update on Revoked Identity");
        //Any modification attempt on a revoked ID must fail

        try {
            authority.updateAddress(aleena.getId(), "Should fail");
        } catch (InvalidOperationException e) {
            System.out.println("Correctly rejected: " + e.getMessage());
        }

        try {
            authority.suspendIdentity(aleena.getId());
        } catch (InvalidOperationException e) {
            System.out.println("Correctly rejected: " + e.getMessage());
        }


        section("SCENARIO 12 — Verify Non-existent Identity");
        // All portals should handle a missing ID gracefully

        verify(taxPortal, "non-existent-id-999");
        verify(bankPortal, "non-existent-id-999");
        verify(airportPortal, "non-existent-id-999");


        section("SCENARIO 13 — Input Validation");
        // Demonstrate that blank/null fields are rejected at creation

        try {
            authority.createIdentity("NIN-BAD", "", "Smith",
                    LocalDate.of(2000, 1, 1), "London", "British");
        } catch (IllegalArgumentException e) {
            System.out.println("Correctly rejected blank name: " + e.getMessage());
        }

        try {
            authority.createIdentity("NIN-BAD", null, "Smith",
                    LocalDate.of(2000, 1, 1), "London", "British");
        } catch (NullPointerException e) {
            System.out.println("Correctly rejected null name: " + e.getMessage());
        }


        section("SCENARIO 14 — Duplicate National ID Rejection");

        try {
            authority.createIdentity("NIN-001", "Duplicate", "Person",
                    LocalDate.of(1995, 6, 1), "London", "British");
        } catch (Exception e) {
            System.out.println("Correctly rejected: " + e.getMessage());
        }


        section("SCENARIO 15 — Portal Types");

        System.out.println(authority.getOrganisationName() + " → " + authority.getPortalType());
        for (VerificationPortal p : List.of(taxPortal, drivingLicencePortal,
                employerPortal, bankPortal, airportPortal)) {
            System.out.println(p.getOrganisationName() + " → " + p.getPortalType());
        }


        section("SCENARIO 16 — Audit Log Summary");

        List<LogEvent> allLogs = logService.getAll();
        System.out.println("Total log entries: " + allLogs.size());
        System.out.println("\nFirst 5 entries:");
        allLogs.stream().limit(5).forEach(e -> System.out.println("  " + e));
        System.out.println("\nLast 5 entries:");
        allLogs.stream().skip(Math.max(0, allLogs.size() - 5)).forEach(e -> System.out.println("  " + e));
    }



    private static void section(String title) {
        System.out.println("\n========================================");
        System.out.println("  " + title);
        System.out.println("========================================");
    }

    private static void verify(VerificationPortal portal, String id) {
        VerificationResult result = portal.verify(id);
        System.out.printf("[%s] verify(%s) → valid=%b, reason='%s'%n",
                portal.getOrganisationName(),
                id.length() > 12 ? id.substring(0, 12) + "…" : id,
                result.valid(),
                result.reason()
        );
    }
}
