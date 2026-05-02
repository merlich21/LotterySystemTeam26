package team26.repository.user.enums;

public enum UserExistsField {
    EMAIL("email"),
    LOGIN("login"),
    PHONE("phone");

    private String columnName;

    UserExistsField(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
    }
}
