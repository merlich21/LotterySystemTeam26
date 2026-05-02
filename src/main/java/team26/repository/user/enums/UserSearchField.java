package team26.repository.user.enums;

public enum UserSearchField {
    ID("id"),
    LOGIN("login"),
    EMAIL("email"),
    ROLE("role");

    private final String columnName;

    UserSearchField(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
    }
}
