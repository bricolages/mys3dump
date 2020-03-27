package org.bricolages.mys3dump;

class ResultSetColumn {
    public final String name;
    public final int type;
    public final String typeName;

    public ResultSetColumn(String name, int type, String typeName) {
        this.name = name;
        this.type = type;
        this.typeName = typeName;
    }

    String sqlExpression() {
        String e;
        switch (typeName) {
            case "GEOMETRY":
                e = String.format("ST_AsText(%s) as %s", name, name);
                break;
            default:
                e = name;
        }
        return e;
    }
}
