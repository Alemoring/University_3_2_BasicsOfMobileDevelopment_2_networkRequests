import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import java.util.LinkedList
import kotlin.math.abs
import kotlin.math.max

abstract class SwipeHelper(
    private val recyclerView: RecyclerView
) : ItemTouchHelper.SimpleCallback(
    ItemTouchHelper.ACTION_STATE_IDLE,
    ItemTouchHelper.DOWN
) {
    private var swipedPosition = -1
    private val buttonsBuffer: MutableMap<Int, List<UnderlayButton>> = mutableMapOf()
    private var swipeProgressMap = mutableMapOf<Int, Float>()
    private val recoverQueue = object : LinkedList<Int>() {
        override fun add(element: Int): Boolean {
            if (contains(element)) return false
            return super.add(element)
        }
    }
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        if (swipedPosition != position) recoverQueue.add(swipedPosition)
        swipedPosition = position
        recoverSwipedItem()
    }
    @SuppressLint("ClickableViewAccessibility")
    private val touchListener = View.OnTouchListener { _, event ->
        if (swipedPosition < 0) return@OnTouchListener false
        buttonsBuffer[swipedPosition]?.forEach { it.handle(event) }
        recoverQueue.add(swipedPosition)
        swipedPosition = -1
        recoverSwipedItem()
        true
    }

    init {
        recyclerView.setOnTouchListener(touchListener)
    }
    private fun recoverSwipedItem() {
        while (!recoverQueue.isEmpty()) {
            val position = recoverQueue.poll() ?: return
            recyclerView.adapter?.notifyItemChanged(position)
        }
    }
    private fun drawButtons(
        canvas: Canvas,
        buttons: List<UnderlayButton>,
        itemView: View,
        dY: Float
    ) {
        var up = itemView.top
        buttons.forEach { button ->
            val height = button.intrinsicHeight / buttons.intrinsicHeight() * abs(dY)
            val down = height + up
            button.onDraw(
                canvas,
                RectF(itemView.left.toFloat(), up.toFloat(), itemView.right.toFloat(), down.toFloat())
            )

            up = down.toInt()
        }
    }


    /** использовал 1.5f как множитель для ширины пространства*/
    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val position = viewHolder.adapterPosition
        var maxDY = dY
        val itemView = viewHolder.itemView


        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            swipeProgressMap[position] = abs(dY) / viewHolder.itemView.height.toFloat()
            if (dY > 0) {
                if (!buttonsBuffer.containsKey(position)) {
                    buttonsBuffer[position] = instantiateUnderlayButton(position)
                }

                val buttons = buttonsBuffer[position] ?: return
                if (buttons.isEmpty()) return
                //
                maxDY = max(-buttons.intrinsicHeight() * 1.5f, dY)*0.5f
                drawButtons(c, buttons, itemView, maxDY)
            }

        }

        super.onChildDraw(
            c,
            recyclerView,
            viewHolder,
            dX,
            maxDY,
            actionState,
            isCurrentlyActive
        )
    }

    abstract fun instantiateUnderlayButton(position: Int): List<UnderlayButton>
    interface UnderlayButtonClickListener {
        fun onClick()
    }

    class UnderlayButton(
        private val context: Context,
        private val title: String,
        textSize: Float,
        @ColorRes private val colorRes: Int,
        @DrawableRes private val iconRes: Int? = null,
        iconSize: Int? = null,
        private val clickListener: UnderlayButtonClickListener
    ) {
        private var clickableRegion: RectF? = null
        private val textSizeInPixel: Float = textSize * context.resources.displayMetrics.density // dp to px
        private val iconSizeInPixel: Float = iconSize?.let { it * context.resources.displayMetrics.density } ?: 100f
        private val verticalPadding = 50.0f
        private val horizontalPadding = verticalPadding
        val intrinsicHeight: Float

        init {
            val paint = Paint()
            paint.textSize = textSizeInPixel
            paint.typeface = Typeface.DEFAULT_BOLD
            paint.textAlign = Paint.Align.LEFT
            val titleBounds = Rect()
            paint.getTextBounds(title, 0, title.length, titleBounds)
            intrinsicHeight = (titleBounds.height() * verticalPadding)
        }

        fun onDraw(canvas: Canvas, rect: RectF) {

            val paint = Paint()

            // Фон
            paint.color = ContextCompat.getColor(context, colorRes)
            canvas.drawRect(rect, paint)


            // Расчет размера иконки
            val iconLeft = rect.left + (rect.width() - iconSizeInPixel) / 2
            val iconTop = rect.top + (rect.height() * 0.8f - iconSizeInPixel) / 2

            // Отрисовка иконки
            iconRes?.let {
                val drawable = ContextCompat.getDrawable(context, it)
                drawable?.let { icon ->
                    icon.setBounds(
                        iconLeft.toInt(),
                        iconTop.toInt(),
                        (iconLeft + iconSizeInPixel).toInt(),
                        (iconTop + iconSizeInPixel).toInt()
                    )
                    icon.draw(canvas)
                }
            }

            paint.color = ContextCompat.getColor(context, android.R.color.white)
            paint.textSize = textSizeInPixel
            paint.typeface = Typeface.DEFAULT
            paint.textAlign = Paint.Align.CENTER

            val titleBounds = Rect()
            paint.getTextBounds(title, 0, title.length, titleBounds)

            // Отрисовка текста под иконкой с одинаковым отступом от верхней и нижней границы кнопки
            val textTop = iconTop + iconSizeInPixel + (verticalPadding / 2 ) // Расстояние от верха кнопки до верха текста
            val textY = textTop + titleBounds.height()
            canvas.drawText(title, rect.centerX(), textY, paint)


            clickableRegion = rect

        }

        fun handle(event: MotionEvent) {
            clickableRegion?.let {
                if (it.contains(event.x, event.y)) {
                    clickListener.onClick()
                }
            }
        }
    }
}

private fun List<SwipeHelper.UnderlayButton>.intrinsicHeight(): Float {
    if (isEmpty()) return 0.0f
    return map { it.intrinsicHeight }.reduce { acc, fl -> acc + fl }
}
