// src/main/scala/InventoryController.scala
import javafx.fxml.FXML
import javafx.scene.control.{Button, Label}
import javafx.scene.image.{Image, ImageView}
import javafx.scene.layout.{GridPane, StackPane, VBox}
import javafx.scene.input.MouseEvent
import javafx.stage.Stage
import javafx.scene.Parent
import javafx.collections.FXCollections
import javafx.geometry.Pos
import scala.collection.JavaConverters._
import scalafx.Includes._
import scalafx.scene.control.{Button as SfxButton, Label as SfxLabel}
import scalafx.scene.image.ImageView as SfxImageView
import scalafx.scene.layout.{GridPane as SfxGridPane, StackPane as SfxStackPane}

class InventoryController() {

  @FXML var inventoryGrid: GridPane = _
  @FXML var closeButton: Button = _
  @FXML var seedsCategoryButton: Button = _
  @FXML var cropsCategoryButton: Button = _

  var dialogStage: Stage = _
  private var gameModel: GameModel = _
  def setGameModel(model: GameModel): Unit = {
    this.gameModel = model
  }
  private var imageManager: ImageManager = _
  def setImageManager(manager: ImageManager): Unit = {
    this.imageManager = manager
  }
  var parentController: GameController = _

  var currentCategory: String = "all"

  // Image resources are now loaded by ImageManager

  @FXML
  def initialize(): Unit = {
    new SfxButton(closeButton).onAction = _ => dialogStage.close()

    new SfxButton(seedsCategoryButton).onAction = _ => {
      currentCategory = "seeds"
      renderInventory()
    }
    new SfxButton(cropsCategoryButton).onAction = _ => {
      currentCategory = "crops"
      renderInventory()
    }
  }

  // === FIX: initData now handles all post-injection logic ===
  def initData(): Unit = {
    renderInventory()
  }

  def renderInventory(): Unit = {
    new SfxGridPane(inventoryGrid).children.clear()

    val allItems = gameModel.inventory.asScala.toSeq

    val itemsToShow = allItems.filter { case (key, value) =>
      currentCategory match {
        case "seeds" => key.contains("_seed") && value > 0
        case "crops" => !key.contains("_seed") && value > 0
        case _ => value > 0
      }
    }

    val gridWidth = 4
    var index = 0

    itemsToShow.foreach { case (itemName, count) =>
      val row = index / gridWidth
      val col = index % gridWidth

      val itemStackPane = createInventorySlot(itemName, count)

      new SfxStackPane(itemStackPane).onMouseClicked = (event: MouseEvent) => {
        if (itemName.contains("_seed")) {
          // === FIX: This part is now safe as parentController is injected
          parentController.selectedSeed = itemName
          parentController.currentMode = "Seeding"
          dialogStage.close()
        } else {
          // TODO: Add selling logic here
          println(s"Selected crop for selling: $itemName")
        }
      }

      new SfxGridPane(inventoryGrid).add(itemStackPane, col, row)
      index += 1
    }

    updateCategoryButtonStyles()
  }

  private def updateCategoryButtonStyles(): Unit = {
    val defaultStyle = "-fx-background-color: #d1b18e; -fx-border-color: #a48c6b; -fx-border-width: 2;"
    val selectedStyle = "-fx-background-color: #a48c6b; -fx-border-color: black; -fx-border-width: 3;"

    new SfxButton(seedsCategoryButton).style = defaultStyle
    new SfxButton(cropsCategoryButton).style = defaultStyle

    currentCategory match {
      case "seeds" => new SfxButton(seedsCategoryButton).style = selectedStyle
      case "crops" => new SfxButton(cropsCategoryButton).style = selectedStyle
      case _ =>
    }
  }

  private def createInventorySlot(itemName: String, count: Int): StackPane = {
    val itemImage = itemName match {
      case "carrot_seed" => Some(new ImageView(imageManager.carrotSeedBagImage))
      case "carrot" => Some(new ImageView(imageManager.carrotCropImage))
      case _ => None
    }

    itemImage.foreach(imgView => {
      imgView.setFitWidth(60)
      imgView.setFitHeight(60)
    })

    val countLabel = new SfxLabel(count.toString)
    countLabel.style = "-fx-font-size: 14px; -fx-text-fill: white; -fx-background-color: rgba(0, 0, 0, 0.5); -fx-padding: 2px 5px; -fx-background-radius: 5;"

    val slotPane = new StackPane()
    itemImage.foreach(slotPane.getChildren.add)
    slotPane.getChildren.add(countLabel)

    new SfxStackPane(slotPane).prefWidth = 90
    new SfxStackPane(slotPane).prefHeight = 90
    new SfxStackPane(slotPane).style = "-fx-background-color: #e0d8c0; -fx-border-color: #a48c6b; -fx-border-width: 1;"

    StackPane.setAlignment(countLabel, Pos.BOTTOM_RIGHT)

    slotPane
  }
}
