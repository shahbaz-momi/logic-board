import java.io.File
import javax.imageio.ImageIO

/**
 * Created by Asdev on 02/04/17. All rights reserved.
 * Unauthorized copying via any medium is stricitly
 * prohibited.
 *
 * Authored by Shahbaz Momi as part of LogicBoard
 * under the package
 */

/**
 * Contains the images of the gates.
 */
object TileRes {

    val AND_GATE = ImageIO.read(File("res/and_gate.png"))
    val OR_GATE = ImageIO.read(File("res/or_gate.png"))
    val XOR_GATE = ImageIO.read(File("res/xor_gate.png"))
    val NOR_GATE = ImageIO.read(File("res/nor_gate.png"))
    val INV_GATE = ImageIO.read(File("res/inv_gate.png"))
    val TRIGGER = ImageIO.read(File("res/trigger.png"))

}