package coffeeshop.simulation.model;

public record StaffViewData(
        String staffId, ServiceStaffStatus status, String currentOrderId, String currentActionText) {
    public StaffViewData {
        if (staffId == null || staffId.isBlank()) {
            throw new IllegalArgumentException("staffId must not be null or blank.");
        }
        if (status == null) {
            throw new IllegalArgumentException("status must not be null.");
        }
        if (currentOrderId == null || currentOrderId.isBlank()) {
            throw new IllegalArgumentException("currentOrderId must not be null or blank.");
        }
        if (currentActionText == null || currentActionText.isBlank()) {
            throw new IllegalArgumentException("currentActionText must not be null or blank.");
        }
    }
}
