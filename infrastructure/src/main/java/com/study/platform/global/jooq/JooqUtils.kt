package com.study.platform.global.jooq

import org.jooq.Condition
import org.jooq.Field
import org.jooq.impl.DSL
import java.util.UUID

object JooqUtils {

    fun uuidEq(fieldExpr: String, uuid: UUID): Condition =
        DSL.condition("$fieldExpr = UUID_TO_BIN(?)", uuid.toString())

    fun binToUuid(fieldExpr: String): Field<String> =
        DSL.field("BIN_TO_UUID($fieldExpr)", String::class.java)
}
