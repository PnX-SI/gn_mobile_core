package fr.geonature.sync.ui.home

import android.content.Context
import android.text.format.DateFormat
import android.util.AttributeSet
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.work.WorkInfo
import fr.geonature.sync.R
import fr.geonature.sync.sync.DataSyncManager
import java.util.Date

/**
 * Custom [View] about data synchronization status.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class DataSyncView : ConstraintLayout {

    private lateinit var iconStatus: TextView
    private lateinit var textViewMessage: TextView
    private lateinit var textViewLastSynchronizedDateTitle: TextView
    private lateinit var textViewLastSynchronizedDate: TextView

    private val stateAnimation = AlphaAnimation(
        0.0f,
        1.0f
    ).apply {
        duration = 250
        startOffset = 10
        repeatMode = Animation.REVERSE
        repeatCount = Animation.INFINITE
    }

    constructor(context: Context) : super(context) {
        init(
            null,
            0
        )
    }

    constructor(
        context: Context,
        attrs: AttributeSet
    ) : super(
        context,
        attrs
    ) {
        init(
            attrs,
            0
        )
    }

    constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int
    ) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(
            attrs,
            defStyleAttr
        )
    }

    fun setState(state: WorkInfo.State) {
        when (state) {
            WorkInfo.State.RUNNING -> {
                iconStatus.setTextColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.status_pending,
                        context?.theme
                    )
                )

                if (iconStatus.animation?.hasStarted() != true) {
                    iconStatus.startAnimation(stateAnimation)
                }
            }
            WorkInfo.State.FAILED -> {
                iconStatus.setTextColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.status_ko,
                        context?.theme
                    )
                )
                iconStatus.clearAnimation()
            }
            WorkInfo.State.SUCCEEDED -> {
                iconStatus.setTextColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.status_ok,
                        context?.theme
                    )
                )
                iconStatus.clearAnimation()
            }
            else -> {
                iconStatus.setTextColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.status_unknown,
                        context?.theme
                    )
                )
                iconStatus.clearAnimation()
            }
        }
    }

    fun setMessage(text: String?) {
        textViewMessage.text = text
    }

    fun setLastSynchronizedDate(lastSynchronized: Pair<DataSyncManager.SyncState, Date?>) {
        val formatLastSynchronizedDate = if (lastSynchronized.second == null) context.getString(R.string.sync_last_synchronization_never)
        else DateFormat.format(
            context.getString(R.string.sync_last_synchronization_date),
            lastSynchronized.second
        )

        textViewLastSynchronizedDateTitle.text = context.getText(if (lastSynchronized.first == DataSyncManager.SyncState.FULL) R.string.sync_last_synchronization_full else R.string.sync_last_synchronization)
        textViewLastSynchronizedDate.text = formatLastSynchronizedDate
    }

    private fun init(
        attrs: AttributeSet?,
        defStyle: Int
    ) {
        View.inflate(
            context,
            R.layout.view_sync_data,
            this
        )

        iconStatus = findViewById(android.R.id.icon)
        textViewMessage = findViewById(android.R.id.message)
        textViewLastSynchronizedDateTitle = findViewById(android.R.id.text1)
        textViewLastSynchronizedDate = findViewById(android.R.id.text2)
    }
}
