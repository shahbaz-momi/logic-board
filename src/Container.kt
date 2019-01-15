import java.util.*

/**
 * Created by Asdev on 02/04/17. All rights reserved.
 * Unauthorized copying via any medium is stricitly
 * prohibited.
 *
 * Authored by Shahbaz Momi as part of LogicBoard
 * under the package
 */

/**
 * A singleton which contains all of the tiles.
 */
object TileContainer {

    val TILES: Array<Array<Tile>>

    init {
        // create a list of arrays first
        val list = ArrayList<Array<Tile>>(NUM_TILES_H)
        // add the sub arrays into the main array
        for(i in 0 until NUM_TILES_H) {
            list.add(
                    Array(NUM_TILES_W) { Tile(i, it) }
            )
        }

        // set the main data array to the list
        TILES = list.toTypedArray()
    }
}

/**
 * Returns the tile at the specified index.
 */
fun getTile(x: Int, y: Int) = TileContainer.TILES[x][y]