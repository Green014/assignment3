// src/main/scala/GameModel.scala
import javafx.collections.FXCollections
import scalafx.beans.property.{IntegerProperty, ObjectProperty}
import scala.collection.JavaConverters.*
import scala.collection.mutable.ListBuffer
import scala.util.Random

// Farm tile states
sealed trait TileState
case object Empty extends TileState
case object Hoeing extends TileState
case object Planted extends TileState
case object Active extends TileState
case object Grown extends TileState
case object Dead extends TileState
case object RequireWater extends TileState

// Crop growth stages
sealed trait CropState
case object Seedling extends CropState
case object Growing extends CropState
case object Mature extends CropState

// Crop data model
case class Crop(name: String, growthStage: CropState, daysToGrow: Int, totalGrowthDays: Int, var hasBeenCheckedForWater: Boolean = false)

// FarmTile data model
class FarmTile(initialState: TileState) {
  var state = ObjectProperty[TileState](initialState)
  val crop = ObjectProperty[Option[Crop]](None)
  var daysWithoutWater: Int = 0
}

class GameModel {
  val day = IntegerProperty(1)
  val money = IntegerProperty(500)

  val initialInventory = Map[String, Int](
    "carrot_seed" -> 5,
    "carrot" -> 0
  )
  val inventory = FXCollections.observableHashMap[String, Int]()
  initialInventory.foreach { case (key, value) => inventory.put(key, value) }

  val messageHistory = ListBuffer[String]()

  val mapWidth = 8
  val mapHeight = 8
  val farmMap = Array.ofDim[ObjectProperty[FarmTile]](mapHeight, mapWidth)

  for (y <- 0 until mapHeight; x <- 0 until mapWidth) {
    farmMap(y)(x) = ObjectProperty(new FarmTile(Empty))
  }

  val cropProperties = Map(
    "carrot_seed" -> Map("name" -> "Carrot", "growth_days" -> 3, "sell_price" -> 10, "harvest_name" -> "carrot")
  )

  val gameEndDay = 20
  val objective = Map[String, Int](
    "carrot" -> 20
  )

  def performBuy(itemName: String, quantity: Int): Boolean = {
    val seedData = cropProperties(itemName)
    val price = seedData("sell_price").asInstanceOf[Int]
    val cost = price * quantity

    if (money.value >= cost) {
      money.value -= cost
      val currentCount = Option(inventory.get(itemName)).map(_.intValue()).getOrElse(0)
      inventory.put(itemName, currentCount + quantity)
      true
    } else {
      false
    }
  }

  def performSell(itemName: String, quantity: Int): Boolean = {
    val seedData = cropProperties.values.find(v => v.get("harvest_name").contains(itemName)).get
    val price = seedData.get("sell_price").get.asInstanceOf[Int]
    val earnings = price * quantity

    val currentCount = Option(inventory.get(itemName)).map(_.intValue()).getOrElse(0)
    if (currentCount >= quantity) {
      money.value += earnings
      inventory.put(itemName, currentCount - quantity)
      true
    } else {
      false
    }
  }

  def growCrops(): Unit = {
    val random = new Random()
    for (y <- 0 until mapHeight; x <- 0 until mapWidth) {
      val farmTile = farmMap(y)(x).value

      farmTile.crop.value match {
        case Some(crop) =>
          if (farmTile.state.value == Dead || farmTile.state.value == Grown) {
          } else if (farmTile.state.value == RequireWater) {
            farmTile.daysWithoutWater += 1
            if (farmTile.daysWithoutWater > 2) {
              farmTile.state.value = Dead
            }
          } else {
            val newDaysToGrow = crop.daysToGrow - 1

            if (!crop.hasBeenCheckedForWater) {
              if (random.nextInt(100) < 15) {
                farmTile.state.value = RequireWater
                crop.hasBeenCheckedForWater = true
                farmTile.daysWithoutWater = 0
              } else {
                crop.hasBeenCheckedForWater = true
                if (newDaysToGrow <= 0) {
                  farmTile.state.value = Grown
                  farmTile.crop.value = Some(crop.copy(growthStage = Mature, daysToGrow = newDaysToGrow))
                } else {
                  farmTile.state.value = Active
                  val newStage = Growing
                  farmTile.crop.value = Some(crop.copy(growthStage = newStage, daysToGrow = newDaysToGrow))
                }
              }
            } else {
              if (newDaysToGrow <= 0) {
                farmTile.state.value = Grown
                farmTile.crop.value = Some(crop.copy(growthStage = Mature, daysToGrow = newDaysToGrow))
              } else {
                farmTile.state.value = Active
                val newStage = Growing
                farmTile.crop.value = Some(crop.copy(growthStage = newStage, daysToGrow = newDaysToGrow))
              }
            }
          }
        case None =>
      }
    }
  }

  def checkVictoryCondition(): (Boolean, Int) = {
    var win = true
    var extraScore = 0

    objective.foreach { case (item, targetCount) =>
      val currentCount = Option(inventory.get(item)).map(_.intValue()).getOrElse(0)
      if (currentCount < targetCount) {
        win = false
      } else {
        val extra = currentCount - targetCount
        val sellPrice = cropProperties.values.find(v => v.get("harvest_name").contains(item)).flatMap(_.get("sell_price")).map(_.asInstanceOf[Int]).getOrElse(0)
        extraScore += extra * sellPrice
      }
    }
    (win, extraScore)
  }

  def performHoe(x: Int, y: Int): Boolean = {
    val farmTile = farmMap(y)(x).value
    if (farmTile.state.value == Empty || farmTile.state.value == Dead) {
      farmTile.state.value = Hoeing
      farmTile.crop.value = None
      farmTile.daysWithoutWater = 0
      true
    } else {
      false
    }
  }

  def performSeeding(x: Int, y: Int, selectedSeed: String): Boolean = {
    val farmTile = farmMap(y)(x).value
    val seedCount = Option(inventory.get(selectedSeed)).map(_.intValue()).getOrElse(0)
    if (seedCount > 0) {
      val cropData = cropProperties(selectedSeed)
      val newCrop = Crop(
        name = cropData("name").asInstanceOf[String],
        growthStage = Seedling,
        daysToGrow = cropData("growth_days").asInstanceOf[Int],
        totalGrowthDays = cropData("growth_days").asInstanceOf[Int]
      )
      farmTile.state.value = Planted
      farmTile.crop.value = Some(newCrop)
      inventory.put(selectedSeed, seedCount - 1)
      true
    } else {
      false
    }
  }

  def performWatering(x: Int, y: Int): Boolean = {
    val farmTile = farmMap(y)(x).value
    if (farmTile.state.value == RequireWater) {
      farmTile.state.value = Active
      farmTile.crop.value.foreach(_.hasBeenCheckedForWater = true)
      farmTile.daysWithoutWater = 0
      farmTile.crop.value.foreach(c => farmTile.crop.value = Some(c.copy(growthStage = Growing)))
      true
    } else {
      false
    }
  }

  def performHarvest(x: Int, y: Int): Boolean = {
    val farmTile = farmMap(y)(x).value
    if (farmTile.state.value == Grown) {
      val harvestedCropName = farmTile.crop.value.get.name
      val harvestItemName = cropProperties.values.find(v => v.get("name").contains(harvestedCropName)).flatMap(_.get("harvest_name")).getOrElse("unknown_crop").asInstanceOf[String]

      val currentCount = Option(inventory.get(harvestItemName)).map(_.intValue()).getOrElse(0)

      farmTile.state.value = Hoeing
      farmTile.crop.value = None

      inventory.put(harvestItemName, currentCount + 1)
      true
    } else {
      false
    }
  }
}
