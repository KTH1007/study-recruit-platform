package com.study.platform.support

import org.mockito.Mockito

@Suppress("UNCHECKED_CAST")
fun <T : Any> anyNonNull(): T = Mockito.any<T>() as T
