package com.study.platform.global.ratelimit

import com.study.platform.global.constant.RateLimitConstants

object RateLimitKeyBuilder {

    fun build(scope: String, identifier: String, action: String): String =
        "${RateLimitConstants.RATE_LIMIT_PREFIX}$scope:$identifier:$action"
}
