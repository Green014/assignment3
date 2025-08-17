// src/main/scala/ImageManager.scala
import javafx.scene.image.Image
import java.io.InputStream

class ImageManager {

  private def loadImage(path: String): Image = {
    val stream: InputStream = getClass.getResourceAsStream(s"/images/$path")
    if (stream != null) {
      new Image(stream)
    } else {
      println(s"Warning: Could not load image resource: /images/$path")
      new Image(getClass.getResourceAsStream("/images/placeholder.png"))
    }
  }

  val grassImage: Image = loadImage("grass_tile.png")
  val hoeingImage: Image = loadImage("hoeing_tile.png")
  val carrotSeedBagImage: Image = loadImage("carrot_seed_bag.png")
  val carrotSeedlingImage: Image = loadImage("carrot_seedling_tile.jpeg")
  val carrotGrowingImage: Image = loadImage("carrot_growing_tile.png")
  val carrotMatureImage: Image = loadImage("carrot_mature_tile.jpg")
  val carrotCropImage: Image = loadImage("carrot.png")
  val carrotRequireWaterImage: Image = loadImage("carrot_require_water.jpg")
  val deadImage: Image = loadImage("dead.png")
  val mainBackgroundImage: Image = loadImage("main_background.jpg")
  val shopBackgroundImage: Image = loadImage("shop_background.png")
  val shopkeeperImage: Image = loadImage("shopkeeper.jpg")
  val emptySlotImage: Image = loadImage("empty_slot.png")

}
