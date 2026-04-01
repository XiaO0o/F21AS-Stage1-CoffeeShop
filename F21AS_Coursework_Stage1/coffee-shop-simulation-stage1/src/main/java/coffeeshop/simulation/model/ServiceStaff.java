package coffeeshop.simulation.model;

public class ServiceStaff {

    private final String staffId;
    private ServiceStaffStatus status = ServiceStaffStatus.IDLE;
    private String currentOrderId = "-";
    private String currentActionText = "Ready";

    public ServiceStaff(String staffId) {
        if (staffId == null || staffId.isBlank()) {
            throw new IllegalArgumentException("staffId must not be null or blank.");
        }
        this.staffId = staffId;
    }

    public String getStaffId() {
        return staffId;
    }

    public synchronized ServiceStaffStatus getStatus() {
        return status;
    }

    public synchronized String getCurrentOrderId() {
        return currentOrderId;
    }

    public synchronized String getCurrentActionText() {
        return currentActionText;
    }

    public synchronized void setState(
            ServiceStaffStatus status, String currentOrderId, String currentActionText) {
        if (status == null) {
            throw new IllegalArgumentException("status must not be null.");
        }
        this.status = status;
        this.currentOrderId = normalizeText(currentOrderId, "-");
        this.currentActionText = normalizeText(currentActionText, "-");
    }

    private static String normalizeText(String value, String defaultText) {
        if (value == null || value.isBlank()) {
            return defaultText;
        }
        return value;
    }
}
