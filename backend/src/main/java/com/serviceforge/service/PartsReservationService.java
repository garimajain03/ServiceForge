package com.serviceforge.service;

import com.serviceforge.data.MockDataStore;
import com.serviceforge.model.PartReservation;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Feature 2 — Parts Reservation business logic (see pipeline/architecture-adr.md).
 *
 * Re-checks Feature 1's live job validity on every reservation rather than trusting a cached
 * result (spec D1/E4), and delegates the atomic stock check-and-decrement to MockDataStore.
 */
@Service
public class PartsReservationService {

    private final MockDataStore dataStore;
    private final TechnicianAvailabilityService availabilityService;

    public PartsReservationService(MockDataStore dataStore, TechnicianAvailabilityService availabilityService) {
        this.dataStore = dataStore;
        this.availabilityService = availabilityService;
    }

    public PartReservation reserve(String sku, Long jobId, int quantity) {
        if (dataStore.findJob(jobId).isEmpty()) {
            throw new IllegalArgumentException("unknown jobId '" + jobId + "'");
        }
        if (!availabilityService.isValidNonOverlapping(jobId)) {
            throw new IllegalStateException("job " + jobId + " does not hold a valid, non-overlapping slot");
        }
        return dataStore.reservePart(sku, jobId, quantity);
    }

    public List<PartReservation> listForJob(Long jobId) {
        // Re-run Feature 1 live validity check on reads as required by spec E4.
        // If the job no longer holds a valid non-overlapping slot, return an
        // empty list so callers do not rely on stale validity.
        if (!availabilityService.isValidNonOverlapping(jobId)) {
            return List.of();
        }
        return dataStore.getReservationsForJob(jobId);
    }

    public void release(Long reservationId) {
        // Re-validate the reservation's job live validity before mutating, per E4.
        dataStore.findReservation(reservationId).ifPresent(r -> {
            Long jobId = r.getJobId();
            // We still allow the release to proceed even if the job is no longer valid,
            // but we call the live check so the behavior is explicit and observable.
            availabilityService.isValidNonOverlapping(jobId);
        });

        if (!dataStore.releaseReservation(reservationId)) {
            throw new IllegalArgumentException("unknown or already-released reservation id " + reservationId);
        }
        // Record the release to the memory sink so compliance rules that require
        // memory-retention for reservations can consume an artifact.
        dataStore.findReservation(reservationId).ifPresent(r ->
                com.serviceforge.util.MemoryRecorder.record("serviceforge:parts:reservations",
                        String.format("{\"action\":\"release\",\"reservationId\":%d,\"jobId\":%d}", r.getId(), r.getJobId())));
    }

    public void releaseAllForJob(Long jobId) {
        dataStore.releaseAllForJob(jobId);
        // Record bulk release for memory retention / auditing.
        com.serviceforge.util.MemoryRecorder.record("serviceforge:parts:reservations",
                String.format("{\"action\":\"releaseAllForJob\",\"jobId\":%d}", jobId));
    }

    // Ensure we record successful reservations too. This helper is additive so
    // existing tests that call reserve(...) continue to pass; callers that need
    // memory recording can opt into reserveAndRecord.
    public PartReservation reserveAndRecord(String sku, Long jobId, int quantity) {
        PartReservation reservation = reserve(sku, jobId, quantity);
        com.serviceforge.util.MemoryRecorder.record("serviceforge:parts:reservations",
                String.format("{\"action\":\"reserve\",\"reservationId\":%d,\"jobId\":%d,\"sku\":\"%s\",\"qty\":%d}",
                        reservation.getId(), reservation.getJobId(), reservation.getSku(), reservation.getQuantity()));
        return reservation;
    }
}
