package com.study.platform.global.exception

class CustomException(val errorCode: ErrorCode) : RuntimeException(errorCode.message)
