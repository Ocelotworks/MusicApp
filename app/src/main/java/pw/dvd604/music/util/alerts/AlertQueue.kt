package pw.dvd604.music.util.alerts

class AlertQueue {
    companion object {
        private val queue = ArrayList<AlertItem>(0)
        private val callbacks = ArrayList<(() -> Unit)>(0)

        fun getAlert(): AlertItem = queue[0]
        fun notifyListeners() = callbacks.forEach { cb -> cb() }
        fun addListener(listener: () -> Unit) = callbacks.add(listener)
        fun addAlert(item: AlertItem) = queue.add(item)
        fun handled() = queue.removeAt(0)
    }
}