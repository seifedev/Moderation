package tech.seife.moderation.datamanager.tickets;

import java.time.LocalDateTime;
import java.util.UUID;

public class Ticket {

    private final long id;

    private final UUID reporterUuid;

    private final String reporterUsername;
    private final String smallDescription;
    private final String description;

    private final LocalDateTime creationDate;


    public Ticket(long id, UUID reporterUuid, String reporterUsername, String smallDescription, String description, LocalDateTime creationDate) {
        this.id = id;
        this.reporterUuid = reporterUuid;
        this.reporterUsername = reporterUsername;
        this.smallDescription = smallDescription;
        this.description = description;
        this.creationDate = creationDate;
   }

    public long getId() {
        return id;
    }

    public UUID getReporterUuid() {
        return reporterUuid;
    }

    public String getReporterUsername() {
        return reporterUsername;
    }

    public String getSmallDescription() {
        return smallDescription;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }
}
