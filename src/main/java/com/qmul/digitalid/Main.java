package com.qmul.digitalid;

import com.qmul.digitalid.exception.InvalidOperationException;
import com.qmul.digitalid.model.DigitalID;
import com.qmul.digitalid.portal.VerificationPortal;
import com.qmul.digitalid.portal.VerificationResult;
import com.qmul.digitalid.portal.implementation.*;
import com.qmul.digitalid.repository.InMemoryDigitalIdRepository;
import com.qmul.digitalid.service.*;

import java.time.LocalDate;

public class Main {

    public static void main(String[] args) {

        InMemoryDigitalIdRepository repository = new InMemoryDigitalIdRepository();

        LogService logService = new InMemoryLogService();

        IdentityManagementService managementService =
                new IdentityManagementServiceImpl(repository, logService);

        IdentityConsumptionService consumptionService =
                new IdentityConsumptionServiceImpl(repository, logService);

        CentralAuthorityPortal authority =
                new CentralAuthorityPortal(managementService);

        TaxAuthorityPortal taxPortal =
                new TaxAuthorityPortal(
                        consumptionService,
                        LocalDate.of(2024, 1, 1),
                        LocalDate.of(2024, 12, 31)
                );

        DrivingLicenceAuthorityPortal drivingLicencePortal =
                new DrivingLicenceAuthorityPortal(consumptionService);

        EmployerPortal employerPortal =
                new EmployerPortal(consumptionService);

        BankPortal bankPortal = new BankPortal(consumptionService);
        AirportServicesPortal airportPortal = new AirportServicesPortal(consumptionService);


        section("SCENARIO 1 — Identity Creation");

        DigitalID aleena = authority.createIdentity(
                "NIN-001",
                "Aleena",
                "Joseph",
                LocalDate.of(1990, 3, 15),
                "123 High Street, London",
                "British"
        );

        System.out.println("Created: " + aleena);


        section("SCENARIO 2 — Update Name");

        authority.updateFirstName(aleena.getId(), "Aleena");
        authority.updateLastName(aleena.getId(), "Joseph");
        System.out.println("Updated name: " + authority.lookupIdentity(aleena.getId()));


        section("SCENARIO 3 — Multi-portal Verification (ACTIVE)");

        verify(taxPortal, aleena.getId());
        verify(drivingLicencePortal, aleena.getId());
        verify(employerPortal, aleena.getId());
        verify(bankPortal, aleena.getId());
        verify(airportPortal, aleena.getId());


        section("SCENARIO 4 — Suspend Identity");

        authority.suspendIdentity(aleena.getId());

        System.out.println(
                "Status after suspend: "
                        + authority.lookupIdentity(aleena.getId()).getStatus()
        );

        verify(taxPortal, aleena.getId());
        verify(employerPortal, aleena.getId());
        verify(bankPortal, aleena.getId());
        verify(airportPortal, aleena.getId());


        section("SCENARIO 5 — Reactivate Identity");

        authority.reactivateIdentity(aleena.getId());

        System.out.println(
                "Status after reactivate: "
                        + authority.lookupIdentity(aleena.getId()).getStatus()
        );

        verify(drivingLicencePortal, aleena.getId());


        section("SCENARIO 6 — Update Address");

        authority.updateAddress(
                aleena.getId(),
                "456 New Road, Manchester"
        );

        System.out.println(
                "Updated: "
                        + authority.lookupIdentity(aleena.getId())
        );

        section("SCENARIO 7 — Tax Period Check (was suspended in period)");

        // aleena was suspended during 2024, so this should fail the period check
        verify(taxPortal, aleena.getId());

        section("SCENARIO 8 — Revoke Identity");

        authority.revokeIdentity(aleena.getId());

        System.out.println(
                "Status after revoke: "
                        + authority.lookupIdentity(aleena.getId()).getStatus()
        );

        verify(taxPortal, aleena.getId());
        verify(employerPortal, aleena.getId());
        verify(bankPortal, aleena.getId());
        verify(airportPortal, aleena.getId());


        section("SCENARIO 9 — Attempt Operation on Revoked Identity");

        try {
            authority.updateAddress(
                    aleena.getId(),
                    "Should fail"
            );
        } catch (InvalidOperationException e) {
            System.out.println(
                    "Correctly rejected: " + e.getMessage()
            );
        }

        section("SCENARIO 10 — Attempt to Suspend Revoked Identity");

        try {
            authority.suspendIdentity(aleena.getId());
        } catch (InvalidOperationException e) {
            System.out.println(
                    "Correctly rejected: " + e.getMessage()
            );
        }


        section("SCENARIO 11 — Verify Non-existent Identity");

        verify(taxPortal, "non-existent-id-999");
        verify(airportPortal, "non-existent-id-999");

        section("LOG");

        logService.printAll();
    }

    private static void section(String title) {
        System.out.println("\n========================================");
        System.out.println(" " + title);
        System.out.println("========================================");
    }

    private static void verify(VerificationPortal portal, String id) {

        VerificationResult result = portal.verify(id);

        System.out.printf(
                "[%s] verify(%s) → valid=%b, reason='%s'%n",
                portal.getOrganisationName(),
                id,
                result.valid(),
                result.reason()
        );
    }
}
