//package ru.vassuv.plugin.createfromtemplate.model
//
//
//import kotlinx.coroutines.*
//import kotlinx.coroutines.internal.*
//import java.awt.event.*
//import javax.swing.*
//import kotlin.coroutines.*
//import kotlin.time.Duration
//
//val Dispatchers.Swing : SwingDispatcher
//    get() = Swing
//
//sealed class SwingDispatcher : MainCoroutineDispatcher(), Delay {
//    /** @suppress */
//    override fun dispatch(context: CoroutineContext, block: Runnable): Unit = SwingUtilities.invokeLater(block)
//
//    /** @suppress */
//    override fun scheduleResumeAfterDelay(timeMillis: Long, continuation: CancellableContinuation<Unit>) {
//        val timer = schedule(timeMillis) {
//            with(continuation) { resumeUndispatched(Unit) }
//        }
//        continuation.invokeOnCancellation { timer.stop() }
//    }
//
//    /** @suppress */
//    override fun invokeOnTimeout(timeMillis: Long, block: Runnable, context: CoroutineContext): DisposableHandle {
//        val timer = schedule(timeMillis) {
//            block.run()
//        }
//        return DisposableHandle { timer.stop() }
//    }
//
//    private fun schedule(timeMillis: Long, action: ActionListener): Timer =
//        Timer(timeMillis.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(), action).apply {
//            isRepeats = false
//            start()
//        }
//}
//
//internal class SwingDispatcherFactory : MainDispatcherFactory {
//    override val loadPriority: Int
//        get() = 0
//
//    override fun createDispatcher(allFactories: List<MainDispatcherFactory>): MainCoroutineDispatcher = Swing
//}
//
//private object ImmediateSwingDispatcher : SwingDispatcher() {
//    override val immediate: MainCoroutineDispatcher
//        get() = this
//
//    override fun isDispatchNeeded(context: CoroutineContext): Boolean = !SwingUtilities.isEventDispatchThread()
//
//    override fun toString() = toStringInternalImpl() ?: "Swing.immediate"
//}
//
//
//object Swing : SwingDispatcher() {
//    init {
//        Timer(1) { }.apply {
//            isRepeats = false
//            start()
//        }
//    }
//
//    override val immediate: MainCoroutineDispatcher
//        get() = ImmediateSwingDispatcher
//
//    override fun toString() = toStringInternalImpl() ?: "Swing"
//}
//
//interface Delay {
//
//    suspend fun delay(time: Long) {
//        if (time <= 0) return // don't delay
//        return suspendCancellableCoroutine { scheduleResumeAfterDelay(time, it) }
//    }
//
//    fun scheduleResumeAfterDelay(timeMillis: Long, continuation: CancellableContinuation<Unit>)
//
//    fun invokeOnTimeout(timeMillis: Long, block: Runnable, context: CoroutineContext): DisposableHandle =
//        DefaultDelay.invokeOnTimeout(timeMillis, block, context)
//}
//
//val DefaultDelay: Delay = initializeDefaultDelay()
//
//fun initializeDefaultDelay(): Delay {
//    val main = Dispatchers.Main
//    return if (main.isMissing() || main !is Delay) DefaultExecutor else main
//}
//
//suspend fun awaitCancellation(): Nothing = suspendCancellableCoroutine {}
//
//suspend fun delay(timeMillis: Long) {
//    if (timeMillis <= 0) return // don't delay
//    return suspendCancellableCoroutine sc@ { cont: CancellableContinuation<Unit> ->
//        // if timeMillis == Long.MAX_VALUE then just wait forever like awaitCancellation, don't schedule.
//        if (timeMillis < Long.MAX_VALUE) {
//            cont.context.delay.scheduleResumeAfterDelay(timeMillis, cont)
//        }
//    }
//}
//
//suspend fun delay(duration: Duration): Unit = delay(duration.toDelayMillis())
//
//internal val CoroutineContext.delay: Delay
//    get() = get(ContinuationInterceptor) as? Delay ?: DefaultDelay
//
//internal fun Duration.toDelayMillis(): Long =
//    if (this > Duration.ZERO) inWholeMilliseconds.coerceAtLeast(1) else 0
//
//abstract class MainCoroutineDispatcher : CoroutineDispatcher() {
//
//    abstract val immediate: kotlinx.coroutines.MainCoroutineDispatcher
//    override fun toString(): String = toStringInternalImpl() ?: "$classSimpleName@$hexAddress"
//    override fun limitedParallelism(parallelism: Int): CoroutineDispatcher {
//        parallelism.checkParallelism()
//        return this
//    }
//
//    protected fun toStringInternalImpl(): String? {
//        val main = Dispatchers.Main
//        if (this === main) return "Dispatchers.Main"
//        val immediate =
//            try { main.immediate }
//            catch (e: UnsupportedOperationException) { null }
//        if (this === immediate) return "Dispatchers.Main.immediate"
//        return null
//    }
//}
//
//internal val Any.hexAddress: String
//    get() = Integer.toHexString(System.identityHashCode(this))
//
//
//internal val Any.classSimpleName: String get() = this::class.java.simpleName
//
//// Save a few bytecode ops
//internal fun Int.checkParallelism() = require(this >= 1) { "Expected positive parallelism level, but got $this" }
//
//interface MainDispatcherFactory {
//    val loadPriority: Int // higher priority wins
//
//    fun createDispatcher(allFactories: List<MainDispatcherFactory>): MainCoroutineDispatcher
//
//    fun hintOnError(): String? = null
//}
//
