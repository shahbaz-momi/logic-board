import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D

/**
 * Created by Asdev on 02/04/17. All rights reserved.
 * Unauthorized copying via any medium is stricitly
 * prohibited.
 *
 * Authored by Shahbaz Momi as part of LogicBoard
 * under the package
 */

val BORDER_COLOR = Color(200, 200, 200)
val FOREGROUND = Color(24, 24, 24)

val STATE_TRUE_COLOR = Color(0, 200, 0, 100)
val STATE_FALSE_COLOR = Color(200, 0, 0, 100)

val STROKE_THICKNESS = 12

val BASIC_STROKE = BasicStroke()
val THICK_STROKE = BasicStroke(STROKE_THICKNESS.toFloat())

/**
 * A class that draws a visible tile and also holds what type of tile this is.
 */
class Tile(val x: Int, val y: Int, var type: TileType = TileType.TYPE_BLANK, val isUiTile: Boolean = false) {

    /**
     * The state of this tile, whether it is true or false.
     */
    var state = false

    /**
     * An optional connection parameter.
     */
    var connectionX = -1
    var connectionY = -1

    private var lastUpdate = -1L

    /**
     * Updates this tile and its state.
     */
    fun update(callerX: Int, callerY: Int, id: Long) {
        if(id == lastUpdate) {
            return
        }

        lastUpdate = id

        // check neighbours and update state accordingly
        if(type == TileType.TYPE_TRIGGER) {
            // only respond to a click event, which has the coords -1, -1
            if(callerX != -1 && callerY != -1) {
                return
            }
            // toggle this state
            state = !state

            // find thing above
            TileContainer.TILES[x][y - 1].update(x, y, id)
            // do a double update to do a stateless update
            // TileContainer.TILES[x][y - 1].update(x, y, id)
        } else if(type == TileType.TYPE_WIRE) {
            state = TileContainer.TILES[callerX][callerY].state
            // get junctions and update them
            TileContainer.TILES[x][y - 1].update(x, y, id)
            TileContainer.TILES[x][y + 1].update(x, y, id)
            TileContainer.TILES[x + 1][y].update(x, y, id)
            TileContainer.TILES[x - 1][y].update(x, y, id)
        } else if(type == TileType.TYPE_INVERTER) {
            if(callerX == x && callerY == y + 1)
                state = !TileContainer.TILES[callerX][callerY].state
            // update the one above
            if(!(callerX == x && callerY == y - 1))
                TileContainer.TILES[x][y - 1].update(x, y, id)
        } else if(type == TileType.TYPE_OR) {
            val leftState = TileContainer.TILES[x - 1][y].state
            val rightState = TileContainer.TILES[x + 1][y].state
            // perform an or operation of the two states and update
            state = leftState || rightState
            if(!(callerX == x && callerY == y - 1))
                TileContainer.TILES[x][y - 1].update(x, y, id)
        } else if(type == TileType.TYPE_AND) {
            val leftState = TileContainer.TILES[x - 1][y].state
            val rightState = TileContainer.TILES[x + 1][y].state
            // perform an and operation of the two states and update
            state = leftState && rightState
            if(!(callerX == x && callerY == y - 1))
                TileContainer.TILES[x][y - 1].update(x, y, id)
        } else if(type == TileType.TYPE_NOR) {
            val leftState = TileContainer.TILES[x - 1][y].state
            val rightState = TileContainer.TILES[x + 1][y].state
            // perform a nor operation of the two states and update
            state = !(leftState || rightState)
            if(!(callerX == x && callerY == y - 1))
                TileContainer.TILES[x][y - 1].update(x, y, id)
        } else if(type == TileType.TYPE_XOR) {
            val leftState = TileContainer.TILES[x - 1][y].state
            val rightState = TileContainer.TILES[x + 1][y].state
            // perform a xor operation of the two states and update
            state = leftState == rightState
            if(!(callerX == x && callerY == y - 1))
                TileContainer.TILES[x][y - 1].update(x, y, id)
        } else if(type == TileType.TYPE_LONG_WIRE_START || type == TileType.TYPE_LONG_WIRE_END) {
            state = TileContainer.TILES[callerX][callerY].state
            // get junctions and update them
            TileContainer.TILES[x][y - 1].update(x, y, id)
            TileContainer.TILES[x][y + 1].update(x, y, id)
            TileContainer.TILES[x + 1][y].update(x, y, id)
            TileContainer.TILES[x - 1][y].update(x, y, id)
            // update the connection
            if(connectionX != -1)
                TileContainer.TILES[connectionX][connectionY].update(x, y, id)
        }
    }

    fun draw(g: Graphics2D) {
        // draw the border first
        g.color = BORDER_COLOR
        g.drawRect(0, 0, TILE_WIDTH, TILE_HEIGHT)

        if(type != TileType.TYPE_BLANK && !isUiTile) {
            if(state) {
                g.color = STATE_TRUE_COLOR
            } else {
                g.color = STATE_FALSE_COLOR
            }

            g.fillRect(0, 0, TILE_WIDTH, TILE_HEIGHT)
        }

        g.color = FOREGROUND
        if(type == TileType.TYPE_WIRE) {
            if(isUiTile) {
                // draw a 4 junction wire
                g.fillRect(0, TILE_HEIGHT / 2 - STROKE_THICKNESS / 2, TILE_WIDTH, STROKE_THICKNESS)
                g.fillRect(TILE_WIDTH / 2 - STROKE_THICKNESS / 2, 0, STROKE_THICKNESS, TILE_HEIGHT)
            } else {
                // draw the center junction square
                g.fillRect(TILE_WIDTH / 2 - STROKE_THICKNESS / 2, TILE_HEIGHT / 2 - STROKE_THICKNESS / 2, STROKE_THICKNESS, STROKE_THICKNESS)
                // get junctions and their directions
                // check left neighbour
                if(x > 0 && (getTile(x - 1, y).type != TileType.TYPE_BLANK && getTile(x - 1, y).type != TileType.TYPE_INVERTER && getTile(x - 1, y).type != TileType.TYPE_TRIGGER)) {
                    g.fillRect(0, TILE_HEIGHT / 2 - STROKE_THICKNESS / 2, TILE_WIDTH / 2, STROKE_THICKNESS)
                }
                // check right neighbour
                if(getTile(x + 1, y).type != TileType.TYPE_BLANK && getTile(x + 1, y).type != TileType.TYPE_INVERTER && getTile(x + 1, y).type != TileType.TYPE_TRIGGER) {
                    g.fillRect(TILE_WIDTH / 2, TILE_HEIGHT / 2 - STROKE_THICKNESS / 2, TILE_WIDTH / 2, STROKE_THICKNESS)
                }
                // check top neighbour
                if(y > 0 && (getTile(x, y - 1).type == TileType.TYPE_WIRE || getTile(x, y - 1).type == TileType.TYPE_INVERTER || getTile(x, y - 1).type == TileType.TYPE_LONG_WIRE_START || getTile(x, y - 1).type == TileType.TYPE_LONG_WIRE_END)) {
                    g.fillRect(TILE_WIDTH / 2 - STROKE_THICKNESS / 2, 0, STROKE_THICKNESS, TILE_HEIGHT / 2)
                }
                // check bottom neighbour
                if(getTile(x, y + 1).type != TileType.TYPE_BLANK) {
                    g.fillRect(TILE_WIDTH / 2 - STROKE_THICKNESS / 2, TILE_HEIGHT / 2, STROKE_THICKNESS, TILE_HEIGHT / 2)
                }
            }
        } else if(type == TileType.TYPE_AND) {
            g.drawImage(TileRes.AND_GATE, 0, 0, TILE_WIDTH, TILE_HEIGHT, null)
        } else if(type == TileType.TYPE_OR) {
            g.drawImage(TileRes.OR_GATE, 0, 0, TILE_WIDTH, TILE_HEIGHT, null)
        } else if(type == TileType.TYPE_XOR) {
            g.drawImage(TileRes.XOR_GATE, 0, 0, TILE_WIDTH, TILE_HEIGHT, null)
        } else if(type == TileType.TYPE_NOR) {
            g.drawImage(TileRes.NOR_GATE, 0, 0, TILE_WIDTH, TILE_HEIGHT, null)
        } else if(type == TileType.TYPE_INVERTER) {
            g.drawImage(TileRes.INV_GATE, 0, 0, TILE_WIDTH, TILE_HEIGHT, null)
        } else if(type == TileType.TYPE_TRIGGER) {
            g.drawImage(TileRes.TRIGGER, 0, 0, TILE_WIDTH, TILE_HEIGHT, null)
        } else if(type == TileType.TYPE_LONG_WIRE_START) {
            if(isUiTile) {
                g.fillOval(TILE_WIDTH / 2 - STROKE_THICKNESS / 2, TILE_HEIGHT / 2 - STROKE_THICKNESS / 2, STROKE_THICKNESS, STROKE_THICKNESS)
            } else {
                g.fillOval(TILE_WIDTH / 2 - STROKE_THICKNESS / 2, TILE_HEIGHT / 2 - STROKE_THICKNESS / 2, STROKE_THICKNESS, STROKE_THICKNESS)

                // get junctions and their directions
                // check left neighbour
                if(x > 0 && (getTile(x - 1, y).type != TileType.TYPE_BLANK && getTile(x - 1, y).type != TileType.TYPE_INVERTER && getTile(x - 1, y).type != TileType.TYPE_TRIGGER)) {
                    g.fillRect(0, TILE_HEIGHT / 2 - STROKE_THICKNESS / 2, TILE_WIDTH / 2, STROKE_THICKNESS)
                }
                // check right neighbour
                if(getTile(x + 1, y).type != TileType.TYPE_BLANK && getTile(x + 1, y).type != TileType.TYPE_INVERTER && getTile(x + 1, y).type != TileType.TYPE_TRIGGER) {
                    g.fillRect(TILE_WIDTH / 2, TILE_HEIGHT / 2 - STROKE_THICKNESS / 2, TILE_WIDTH / 2, STROKE_THICKNESS)
                }
                // check top neighbour
                if(y > 0 && (getTile(x, y - 1).type == TileType.TYPE_WIRE || getTile(x, y - 1).type == TileType.TYPE_INVERTER || getTile(x, y - 1).type == TileType.TYPE_LONG_WIRE_START || getTile(x, y - 1).type == TileType.TYPE_LONG_WIRE_END)) {
                    g.fillRect(TILE_WIDTH / 2 - STROKE_THICKNESS / 2, 0, STROKE_THICKNESS, TILE_HEIGHT / 2)
                }
                // check bottom neighbour
                if(getTile(x, y + 1).type != TileType.TYPE_BLANK) {
                    g.fillRect(TILE_WIDTH / 2 - STROKE_THICKNESS / 2, TILE_HEIGHT / 2, STROKE_THICKNESS, TILE_HEIGHT / 2)
                }
            }
        } else if(type == TileType.TYPE_LONG_WIRE_END) {
            if(isUiTile) {
                g.drawOval(TILE_WIDTH / 2 - STROKE_THICKNESS / 2, TILE_HEIGHT / 2 - STROKE_THICKNESS / 2, STROKE_THICKNESS, STROKE_THICKNESS)
            } else {
                g.drawOval(TILE_WIDTH / 2 - STROKE_THICKNESS / 2, TILE_HEIGHT / 2 - STROKE_THICKNESS / 2, STROKE_THICKNESS, STROKE_THICKNESS)

                if(connectionX != -1 && connectionY != -1) {
                    g.stroke = THICK_STROKE
                    g.drawLine(TILE_WIDTH / 2, TILE_HEIGHT / 2, (connectionX - x) * TILE_WIDTH + TILE_WIDTH / 2, (connectionY - y) * TILE_HEIGHT + TILE_HEIGHT / 2)
                    g.stroke = BASIC_STROKE
                }

                // get junctions and their directions
                // check left neighbour
                if(x > 0 && (getTile(x - 1, y).type != TileType.TYPE_BLANK && getTile(x - 1, y).type != TileType.TYPE_INVERTER && getTile(x - 1, y).type != TileType.TYPE_TRIGGER)) {
                    g.fillRect(0, TILE_HEIGHT / 2 - STROKE_THICKNESS / 2, TILE_WIDTH / 2, STROKE_THICKNESS)
                }
                // check right neighbour
                if(getTile(x + 1, y).type != TileType.TYPE_BLANK && getTile(x + 1, y).type != TileType.TYPE_INVERTER && getTile(x + 1, y).type != TileType.TYPE_TRIGGER) {
                    g.fillRect(TILE_WIDTH / 2, TILE_HEIGHT / 2 - STROKE_THICKNESS / 2, TILE_WIDTH / 2, STROKE_THICKNESS)
                }
                // check top neighbour
                if(y > 0 && (getTile(x, y - 1).type == TileType.TYPE_WIRE || getTile(x, y - 1).type == TileType.TYPE_INVERTER || getTile(x, y - 1).type == TileType.TYPE_LONG_WIRE_START || getTile(x, y - 1).type == TileType.TYPE_LONG_WIRE_END)) {
                    g.fillRect(TILE_WIDTH / 2 - STROKE_THICKNESS / 2, 0, STROKE_THICKNESS, TILE_HEIGHT / 2)
                }
                // check bottom neighbour
                if(getTile(x, y + 1).type != TileType.TYPE_BLANK) {
                    g.fillRect(TILE_WIDTH / 2 - STROKE_THICKNESS / 2, TILE_HEIGHT / 2, STROKE_THICKNESS, TILE_HEIGHT / 2)
                }
            }
        }
    }

}

/**
 * Different types of tiles available.
 */
enum class TileType {

    TYPE_BLANK,

    TYPE_WIRE,

    TYPE_AND,

    TYPE_OR,

    TYPE_XOR,

    TYPE_NOR,

    TYPE_INVERTER,

    TYPE_TRIGGER,

    TYPE_LONG_WIRE_START,

    TYPE_LONG_WIRE_END;

}