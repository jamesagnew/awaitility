/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.awaitility.kotlin

import org.awaitility.Awaitility.await
import org.awaitility.classes.Asynch
import org.awaitility.classes.FakeRepository
import org.awaitility.classes.FakeRepositoryImpl
import org.awaitility.core.ConditionFactory
import org.awaitility.core.ConditionTimeoutException
import org.hamcrest.Matchers.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import java.util.concurrent.TimeUnit.SECONDS
import kotlin.test.assertEquals

class KotlinTest {

    @Rule
    @JvmField
    val exception = ExpectedException.none()

    lateinit var asynch: Asynch
    lateinit var fakeRepository: FakeRepository

    @Before
    fun setup() {
        fakeRepository = FakeRepositoryImpl()
        asynch = Asynch(fakeRepository)
    }

    @Test
    fun booleanCondition() {
        Asynch(fakeRepository).perform()
        await().until { fakeRepository.value == 1 }
    }

    @Test
    fun assertionCondition() {
        Asynch(fakeRepository).perform()
        await().untilAsserted { assertEquals(1, fakeRepository.value) }
    }

    @Test
    fun assertionConditionFailsWithANiceErrorMessage() {
        exception.expect(ConditionTimeoutException::class.java)
        exception.expectMessage(allOf(startsWith("Assertion condition defined in"), endsWith("Expected <2>, actual <1> within 1 seconds.")))

        Asynch(fakeRepository).perform()
        await().atMost(1, SECONDS).untilAsserted { assertEquals(2, fakeRepository.value) }
    }

    @Test
    fun booleanConditionFailsWithANiceErrorMessage() {
        exception.expect(ConditionTimeoutException::class.java)
        exception.expectMessage(allOf(startsWith("Condition"), endsWith("was not fulfilled within 1 seconds.")))

        Asynch(fakeRepository).perform()
        await().atMost(1, SECONDS).until { fakeRepository.value == 2 }
    }

    @Test
    fun extensionFn() {
        Asynch(fakeRepository).perform()

        await().untilFun { fakeRepository.value } matches { it == 1 }
    }
}

data class AwaitilityKotlinCondition<T>(val cond: ConditionFactory, val fn: () -> T?)

fun <T> ConditionFactory.untilFun(fn: () -> T?) = AwaitilityKotlinCondition(this, fn)

infix fun <T> AwaitilityKotlinCondition<T>.matches(pred: (T?) -> Boolean) = cond.until(fn, pred)