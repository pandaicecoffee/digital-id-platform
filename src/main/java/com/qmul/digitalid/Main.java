package com.qmul.digitalid;

import com.qmul.digitalid.exception.InvalidOperationException;
import com.qmul.digitalid.model.DigitalID;
import com.qmul.digitalid.portal.VerificationPortal;
import com.qmul.digitalid.portal.VerificationResult;
import com.qmul.digitalid.portal.implementation.CentralAuthorityPortal;
import com.qmul.digitalid.portal.implementation.DrivingLicenceAuthorityPortal;
import com.qmul.digitalid.portal.implementation.EmployerPortal;
import com.qmul.digitalid.portal.implementation.TaxAuthorityPortal;
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


        section("SCENARIO 2 — Multi-portal Verification (ACTIVE)");

        verify(taxPortal, aleena.getId());
        verify(drivingLicencePortal, aleena.getId());
        verify(employerPortal, aleena.getId());


        section("SCENARIO 3 — Suspend Identity");

        authority.suspendIdentity(aleena.getId());

        System.out.println(
                "Status after suspend: "
                        + authority.lookupIdentity(aleena.getId()).getStatus()
        );

        verify(taxPortal, aleena.getId());
        verify(employerPortal, aleena.getId());


        section("SCENARIO 4 — Reactivate Identity");

        authority.reactivateIdentity(aleena.getId());

        System.out.println(
                "Status after reactivate: "
                        + authority.lookupIdentity(aleena.getId()).getStatus()
        );

        verify(drivingLicencePortal, aleena.getId());


        section("SCENARIO 5 — Update Address");

        authority.updateAddress(
                aleena.getId(),
                "456 New Road, Manchester"
        );

        System.out.println(
                "Updated: "
                        + authority.lookupIdentity(aleena.getId())
        );


        section("SCENARIO 6 — Revoke Identity");

        authority.revokeIdentity(aleena.getId());

        System.out.println(
                "Status after revoke: "
                        + authority.lookupIdentity(aleena.getId()).getStatus()
        );

        verify(taxPortal, aleena.getId());
        verify(employerPortal, aleena.getId());


        section("SCENARIO 7 — Attempt Operation on Revoked Identity");

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

        // =========================================================
        // SCENARIO 8 — Attempt suspend on revoked identity
        // =========================================================
        section("SCENARIO 8 — Attempt to Suspend Revoked Identity");

        try {
            authority.suspendIdentity(aleena.getId());
        } catch (InvalidOperationException e) {
            System.out.println(
                    "Correctly rejected: " + e.getMessage()
            );
        }


        section("SCENARIO 9 — Verify Non-existent Identity");

        verify(taxPortal, "non-existent-id-999");

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
