package kyung.kung_android.ui.store_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kyung.kung_android.data.network.ApiException
import kyung.kung_android.data.store.dto.StoreProductResponse
import kyung.kung_android.domain.booking.BookingRepository
import kyung.kung_android.domain.store.StoreRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

enum class SlotState { AVAILABLE, RESERVED, PAST, CHECKING }

data class StoreDetailUiState(
    val storeProductId: Long = 0L,
    val product: StoreProductResponse? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val dateWindowStart: LocalDate = LocalDate.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedTime: String? = null,
    val slotStates: Map<String, SlotState> = emptyMap(),
    val isPreparingBooking: Boolean = false,
    val reserveError: String? = null,
) {
    val canReserve: Boolean
        get() = product != null && selectedTime != null && !isPreparingBooking &&
            slotStates[selectedTime] == SlotState.AVAILABLE
}

sealed interface StoreDetailEffect {
    data class NavigateToCheckout(val bookingId: Long) : StoreDetailEffect
}

@HiltViewModel
class StoreDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val storeRepository: StoreRepository,
    private val bookingRepository: BookingRepository,
) : ViewModel() {

    private val storeProductId: Long = savedStateHandle.get<Long>("storeProductId") ?: 0L

    private val _state = MutableStateFlow(StoreDetailUiState(storeProductId = storeProductId))
    val state: StateFlow<StoreDetailUiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<StoreDetailEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<StoreDetailEffect> = _effects.asSharedFlow()

    private var slotCheckJob: Job? = null

    companion object {
        val TIME_OPTIONS = listOf(
            "오전 9:00", "오전 10:00", "오전 11:00",
            "오후 12:00", "오후 1:00", "오후 2:00", "오후 3:00", "오후 4:00",
            "오후 5:00", "오후 6:00", "오후 7:00", "오후 8:00",
            "오후 9:00", "오후 10:00", "오후 11:00",
        )
    }

    fun loadProduct() {
        if (_state.value.product != null) return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val product = storeRepository.getStoreProduct(storeProductId)
                _state.update { it.copy(product = product, isLoading = false) }
                refreshSlots()
            } catch (e: ApiException) {
                if (e.isAuthError) {
                    _state.update { it.copy(isLoading = false, error = null) }
                } else {
                    _state.update { it.copy(isLoading = false, error = "정보를 불러오지 못했어요.") }
                }
            } catch (t: Throwable) {
                _state.update { it.copy(isLoading = false, error = "정보를 불러오지 못했어요.") }
            }
        }
    }

    fun moveDateWindow(days: Long) {
        val today = LocalDate.now()
        _state.update {
            val next = it.dateWindowStart.plusDays(days)
            it.copy(dateWindowStart = if (next.isBefore(today)) today else next)
        }
    }

    fun onDateSelected(date: LocalDate) {
        if (date == _state.value.selectedDate) return
        _state.update { it.copy(selectedDate = date, selectedTime = null, reserveError = null) }
        refreshSlots()
    }

    fun onTimeSelected(time: String) {
        if (_state.value.slotStates[time] != SlotState.AVAILABLE) return
        _state.update { it.copy(selectedTime = time, reserveError = null) }
    }

    private fun refreshSlots() {
        val product = _state.value.product ?: return
        val date = _state.value.selectedDate
        val now = LocalDateTime.now()

        val initial = TIME_OPTIONS.associateWith { time ->
            val (start, _) = parseSlot(date, time)
            if (!start.isAfter(now)) SlotState.PAST else SlotState.CHECKING
        }
        _state.update { it.copy(slotStates = initial) }

        slotCheckJob?.cancel()
        slotCheckJob = viewModelScope.launch {
            val locationId = resolveLocationId(product)
            val results = coroutineScope {
                TIME_OPTIONS.filter { initial[it] == SlotState.CHECKING }.map { time ->
                    async {
                        val (start, end) = parseSlot(date, time)
                        val state = runCatching {
                            bookingRepository.checkAvailability(
                                storeProductId = product.storeProductId,
                                startAt = start,
                                endAt = end,
                                locationId = locationId,
                            ).available
                        }.getOrDefault(true)
                        time to if (state) SlotState.AVAILABLE else SlotState.RESERVED
                    }
                }.awaitAll()
            }
            _state.update { s ->
                if (s.selectedDate != date) return@update s
                s.copy(slotStates = s.slotStates + results.toMap())
            }
        }
    }

    fun reserve() {
        val current = _state.value
        val product = current.product ?: return
        val time = current.selectedTime ?: return
        if (current.isPreparingBooking) return

        val (start, end) = parseSlot(current.selectedDate, time)
        if (!start.isAfter(LocalDateTime.now())) {
            _state.update { it.copy(reserveError = "이미 지난 시간입니다. 다른 시간을 선택해주세요.") }
            return
        }

        _state.update { it.copy(isPreparingBooking = true, reserveError = null) }
        viewModelScope.launch {
            try {
                val booking = bookingRepository.prepareBooking(
                    storeProductId = product.storeProductId,
                    startAt = start,
                    endAt = end,
                    locationId = resolveLocationId(product),
                )
                _state.update { it.copy(isPreparingBooking = false) }
                _effects.emit(StoreDetailEffect.NavigateToCheckout(booking.bookingId))
            } catch (e: ApiException) {
                val msg = if (e.errorCode == "BOOKING_409_1") {
                    "이미 예약된 시간입니다. 다른 시간대를 선택해주세요."
                } else {
                    "예약 생성에 실패했어요. 다시 시도해주세요."
                }
                _state.update { it.copy(isPreparingBooking = false, reserveError = msg, selectedTime = null) }
                refreshSlots()
            } catch (t: Throwable) {
                _state.update {
                    it.copy(isPreparingBooking = false, reserveError = "예약 생성에 실패했어요. 다시 시도해주세요.")
                }
            }
        }
    }

    private fun resolveLocationId(product: StoreProductResponse): Long? =
        if (product.serviceType?.uppercase() == "ONLINE") null else product.locationId

    private fun parseSlot(date: LocalDate, timeLabel: String): Pair<LocalDateTime, LocalDateTime> {
        val isPm = timeLabel.contains("오후")
        val raw = timeLabel.replace("오전 ", "").replace("오후 ", "")
        val parts = raw.split(":")
        var hour = parts[0].toInt()
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        if (isPm && hour != 12) hour += 12
        if (!isPm && hour == 12) hour = 0
        val start = LocalDateTime.of(date, LocalTime.of(hour, minute))
        return start to start.plusHours(1)
    }
}
