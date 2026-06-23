package com.study.platform.global.jooq

import org.jooq.Condition
import org.jooq.Field
import org.jooq.impl.DSL
import java.util.UUID

object JooqUtils {

    fun uuidEq(field: Field<ByteArray?>, uuid: UUID): Condition =
        field.eq(DSL.function("UUID_TO_BIN", ByteArray::class.java, DSL.`val`(uuid.toString())))

    fun binToUuid(field: Field<ByteArray?>): Field<String> =
        DSL.field("BIN_TO_UUID({0})", String::class.java, field)
}
