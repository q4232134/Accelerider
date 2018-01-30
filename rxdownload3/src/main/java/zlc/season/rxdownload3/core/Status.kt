package zlc.season.rxdownload3.core

import zlc.season.rxdownload3.helper.formatSize
import java.text.NumberFormat.getPercentInstance
import kotlin.properties.Delegates


open class Status(var totalSize: Long = 0L,
                  var chunkFlag: Boolean = false) {
    constructor(downloadSize: Long, totalSize: Long, chunkFlag: Boolean) : this(totalSize, chunkFlag) {
        this.downloadSize = downloadSize
    }

    constructor(status: Status) : this(status.downloadSize, status.totalSize, status.chunkFlag)

    var downloadSize by Delegates.observable(0L) { _, old, new ->
        val currentTime = System.currentTimeMillis()
        val duration = (currentTime - recordTime) / 1000.0
        recordTime = currentTime
        speed = ((new - old) / duration).toLong()
    }

    var recordTime = System.currentTimeMillis()
    var speed = 0L

    val formatSpeed get() = "${formatSize(speed)}/s"

    fun formatTotalSize(): String {
        return formatSize(totalSize)
    }

    fun formatDownloadSize(): String {
        return formatSize(downloadSize)
    }

    fun formatString(): String {
        return formatDownloadSize() + "/" + formatTotalSize()
    }

    fun percent(): String {
        val percent: String
        val result = if (totalSize == 0L) {
            0.0
        } else {
            downloadSize * 1.0 / totalSize
        }
        val nf = getPercentInstance()
        nf.minimumFractionDigits = 2
        percent = nf.format(result)
        return percent
    }
}

class Normal(status: Status) : Status(status)

class Suspend(status: Status) : Status(status)

class Waiting(status: Status) : Status(status)

class Downloading(status: Status) : Status(status)

class Failed(status: Status, val throwable: Throwable) : Status(status)

class Succeed(status: Status) : Status(status)

class Deleted(status: Status) : Status(status)



