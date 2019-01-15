import java.awt.*
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import java.util.*
import javax.swing.JFrame
import javax.swing.JPanel

/**
 * Created by Asdev on 02/04/17. All rights reserved.
 * Unauthorized copying via any medium is stricitly
 * prohibited.
 *
 * Authored by Shahbaz Momi as part of LogicBoard
 * under the package
 */

const val TILE_WIDTH = 100
const val TILE_HEIGHT = 100

const val NUM_TILES_W = 11
const val NUM_TILES_H = 11

const val NUM_UI_TILES_W = 2
const val NUM_UI_TILES_H = 5

val HOVER_COLOR = Color(255, 242, 0, 100)

fun main(args: Array<String>) {
    BoardUI()
}

class BoardUI: JPanel(), MouseMotionListener, MouseListener {

    var hoverX = -1
    var hoverY = -1
    var hoverTile = Tile(0, 0, TileType.TYPE_BLANK, true)

    // for dragging the tiles
    var isDragging = false
    var dragX = -1
    var dragY = -1
    var dragOffsetX = 0
    var dragOffsetY = 0

    var currentConnX = -1
    var currentConnY = -1

    // contains the UI elements
    /// NOTE: this array is in order of Y, X
    private val UI_TILES = arrayOf(
            arrayOf(
                    Tile(0, 0, TileType.TYPE_BLANK, true),
                    Tile(1, 0, TileType.TYPE_AND, true)
            ),
            arrayOf(
                    Tile(0, 1, TileType.TYPE_OR, true),
                    Tile(1, 1, TileType.TYPE_NOR, true)
            ),
            arrayOf(
                    Tile(0, 2, TileType.TYPE_INVERTER, true),
                    Tile(1, 2, TileType.TYPE_XOR, true)
            ),
            arrayOf(
                    Tile(0, 3, TileType.TYPE_WIRE, true),
                    Tile(1, 3, TileType.TYPE_TRIGGER, true)
            ),
            arrayOf(
                    Tile(0, 4, TileType.TYPE_LONG_WIRE_START, true),
                    Tile(1, 4, TileType.TYPE_LONG_WIRE_END, true)
            )
    )

    private var frame = JFrame("Logic Board")

    init {

        minimumSize = Dimension(TILE_WIDTH * (NUM_TILES_W + NUM_UI_TILES_W), TILE_HEIGHT * NUM_TILES_H) // add 2 tiles for the ui
        preferredSize = minimumSize
        addMouseMotionListener(this)
        addMouseListener(this)
        frame.add(this)
        frame.pack()
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.isVisible = true
    }


    override fun mouseMoved(e: MouseEvent?) {
        // find the hovered tile
        if(e == null)
            return

        hoverX = Math.floorDiv(e.x, TILE_WIDTH)
        hoverY = Math.floorDiv(e.y, TILE_HEIGHT)

        dragOffsetX = e.x % TILE_WIDTH
        dragOffsetY = e.y % TILE_HEIGHT

        repaint()
    }

    // use for changing tile types
    override fun mouseDragged(e: MouseEvent?) {
        // check if the x is of the ui tiles
        if (hoverX >= NUM_TILES_W && hoverY < NUM_UI_TILES_H) {
            hoverTile = UI_TILES[hoverY][hoverX - NUM_TILES_W]
            isDragging = true
        } else if (hoverX < NUM_TILES_W){
            // not a hovering ui tile, a standard tile
            hoverTile = getTile(hoverX, hoverY)
            isDragging = true
        }

        dragX = e!!.x
        dragY = e.y

        repaint()
    }

    override fun mouseEntered(e: MouseEvent?) {
    }

    override fun mouseClicked(e: MouseEvent?) {
    }

    val random = Random()

    override fun mouseReleased(e: MouseEvent?) {
        if(e == null)
            return

        isDragging = false

        // use the hover x and y to change the tile
        val newX = Math.floorDiv(e.x, TILE_WIDTH)
        val newY = Math.floorDiv(e.y, TILE_HEIGHT)

        // checking if it was dragging a ui tile
        if(hoverX >= NUM_TILES_W && hoverY < NUM_UI_TILES_H) {
            TileContainer.TILES[newX][newY].type = hoverTile.type
            // check if it is a start wire or end wire
            if(hoverTile.type == TileType.TYPE_LONG_WIRE_END) {
                TileContainer.TILES[newX][newY].connectionX = currentConnX
                TileContainer.TILES[newX][newY].connectionY = currentConnY

                TileContainer.TILES[currentConnX][currentConnY].connectionX = newX
                TileContainer.TILES[currentConnX][currentConnY].connectionY = newY
            } else if(hoverTile.type == TileType.TYPE_LONG_WIRE_START) {
                currentConnX = newX
                currentConnY = newY
            }
            repaint()
        } else {
            // trigger the tile
            val tile = TileContainer.TILES[newX][newY]
            if(tile.type == TileType.TYPE_TRIGGER) {
                tile.update(-1, -1, random.nextLong())
                repaint()
            } else {
                // get the hover tile
                tile.type = hoverTile.type

                tile.connectionX = hoverTile.connectionX
                tile.connectionY = hoverTile.connectionY
                if(tile.connectionX != -1) {
                    getTile(tile.connectionX, tile.connectionY).connectionX = tile.x
                    getTile(tile.connectionX, tile.connectionY).connectionY = tile.y
                }

                getTile(hoverX, hoverY).type = TileType.TYPE_BLANK

                getTile(hoverX, hoverY).connectionX = -1
                getTile(hoverX, hoverY).connectionY = -1
                repaint()
            }
        }
    }

    override fun mouseExited(e: MouseEvent?) {
    }

    override fun mousePressed(e: MouseEvent?) {
    }

    /**
     * Paints this board
     */
    override fun paintComponent(g: Graphics?) {
        val g2d = g!! as Graphics2D
        // g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2d.color = Color.WHITE
        g2d.fillRect(0, 0, frame.width, frame.height)

        for(x in 0 until NUM_TILES_W) {
            for(y in 0 until NUM_TILES_H) {
                val tile = getTile(x, y)
                // translate the canvas to the area
                g2d.translate(x * TILE_WIDTH, y * TILE_HEIGHT)
                tile.draw(g2d)
                // translate the canvas back
                g2d.translate(-x * TILE_WIDTH, -y * TILE_HEIGHT)
            }
        }

        // draw the ui tiles
        for(x in 0 until NUM_UI_TILES_W) {
            for(y in 0 until NUM_UI_TILES_H) {
                val tile = UI_TILES[y][x]
                // translate the canvas to the area
                g2d.translate((x + NUM_TILES_W) * TILE_WIDTH, y * TILE_HEIGHT)
                tile.draw(g2d)
                // translate the canvas back
                g2d.translate(-(x + NUM_TILES_W) * TILE_WIDTH, -y * TILE_HEIGHT)
            }
        }

        // draw the dragging tile otherwise the hovering tile
        if(!isDragging) {
            g2d.color = HOVER_COLOR
            // highlight the hovered tile
            g2d.fillRect(hoverX * TILE_WIDTH, hoverY * TILE_HEIGHT, TILE_WIDTH, TILE_HEIGHT)
        } else {
            g2d.translate(dragX - dragOffsetX, dragY - dragOffsetY)
            hoverTile.draw(g2d)
            g2d.translate(-dragX + dragOffsetX, -dragY + dragOffsetY)
        }
    }

}