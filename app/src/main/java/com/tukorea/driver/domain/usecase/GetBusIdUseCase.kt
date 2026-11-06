package com.tukorea.driver.domain.usecase

import com.tukorea.driver.domain.repository.BusNumberRepository
import javax.inject.Inject

/**
 * 서버에 보내는 버스 id. 기사가 정한 호차 번호로 만든다(3호차 → "bus-3").
 * 학생 앱은 이 형식에서 호차 번호를 읽어 보여 준다. 번호를 정하기 전이면 null.
 */
class GetBusIdUseCase @Inject constructor(
    private val busNumbers: BusNumberRepository,
) {
    operator fun invoke(): String? = busNumbers.number.value?.let(::busIdOf)

    companion object {
        fun busIdOf(number: Int): String = "bus-$number"
    }
}
