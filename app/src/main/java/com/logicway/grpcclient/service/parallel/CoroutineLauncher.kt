package com.logicway.grpcclient.service.parallel

import android.util.Log
import com.google.protobuf.Empty
import com.logicway.grpcclient.filedownload.FileDownloadGrpc
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicInteger

@Obfuscate
class CoroutineLauncher(private val blockingStub: FileDownloadGrpc.FileDownloadBlockingStub,
                        private val coroutinesNumber: Int) {

    private val tag: String? = CoroutineLauncher::class.qualifiedName

    private final val threadPoolName: String = "Simple pool"

    private val sum: AtomicInteger = AtomicInteger(0)

    fun runCoroutines(): Int {
        runBlocking {
            val jobs: List<Job> = (1..coroutinesNumber).map {
                launch(newFixedThreadPoolContext(coroutinesNumber, threadPoolName)) {
                    performCalculating()
                }
            }
            jobs.joinAll()
        }
        return sum.toInt();
    }

    private fun performCalculating() {
        var coroutineSum = 0
        var value: Int
        do {
            value = blockingStub.getCollectionElement(Empty.getDefaultInstance()).value
            coroutineSum += value
        } while (value != 0)
        Log.d(tag, "Thread name: " + Thread.currentThread().name
                + ", calculated sum: " + coroutineSum)
        sum.addAndGet(coroutineSum);
    }
}

