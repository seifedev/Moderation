package tech.seife.moderation.enums;

public enum ReplaceType {
    PLAYER_NAME("%player%"),
    DATE("%date%"),
    REASON("%reason%"),
    Channel("%channel%");

    private final String value;

    ReplaceType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
