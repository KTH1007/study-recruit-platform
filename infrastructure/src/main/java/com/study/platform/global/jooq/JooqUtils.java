package com.study.platform.global.jooq;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;

import java.util.UUID;

public class JooqUtils {

    private JooqUtils() {}

    public static Condition uuidEq(String fieldExpr, UUID uuid) {
        return DSL.condition(fieldExpr + " = UUID_TO_BIN(?)", uuid.toString());
    }

    public static Field<String> binToUuid(String fieldExpr) {
        return DSL.field("BIN_TO_UUID(" + fieldExpr + ")", String.class);
    }
}
